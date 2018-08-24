
package com.esotericsoftware.deconz;

public class ColorUtil {
	static private final float[] hueBulbsGamut = new float[] { //
		0.674f, 0.322f, // r
		0.408f, 0.517f, // g
		0.168f, 0.041f // b
	};
	static private final float[] livingColorsGamut = new float[] { //
		0.674f, 0.322f, // r
		0.408f, 0.517f, // g
		0.168f, 0.041f // b
	};
	static private final float[] defaultGamut = new float[] { //
		1, 0, // r
		0, 1, // g
		0, 0 // b
	};

	static public float[] rgbToCie (float red, float green, float blue, String model) {
		// Gamma correction.
		float r = red > 0.04045f ? (float)Math.pow((red + 0.055f) / (1.055f), 2.4f) : (red / 12.92f);
		float g = green > 0.04045f ? (float)Math.pow((green + 0.055f) / (1.055f), 2.4f) : (green / 12.92f);
		float b = blue > 0.04045f ? (float)Math.pow((blue + 0.055f) / (1.055f), 2.4f) : (blue / 12.92f);

		// Wide gamut conversion D65.
		float x = r * 0.664511f + g * 0.154324f + b * 0.162028f;
		float y = r * 0.283881f + g * 0.668433f + b * 0.047685f;
		float z = r * 0.000088f + g * 0.072310f + b * 0.986039f;
		float cx = x / (x + y + z);
		float cy = y / (x + y + z);
		if (Float.isNaN(cx)) cx = 0;
		if (Float.isNaN(cy)) cy = 0;

		// Check if the XY value is within the model's gamut.
		float[] gamut;
		if (model.equals("LCT001") || model.equals("LCT002") || model.equals("LCT003"))
			gamut = hueBulbsGamut;
		else if (model.equals("LLC001") || model.equals("LLC005") || model.equals("LLC006") || model.equals("LLC007")
			|| model.equals("LLC011") || model.equals("LLC012") || model.equals("LLC013") || model.equals("LST001")) {
			gamut = livingColorsGamut;
		} else
			gamut = defaultGamut;
		if (!isPointInGamut(cx, cy, gamut)) {
			// Find the closest point on each line in the triangle.
			float[] pAB = closestPointOnTriangle(gamut[0], gamut[1], gamut[2], gamut[3], cx, cy);
			float[] pAC = closestPointOnTriangle(gamut[4], gamut[5], gamut[0], gamut[1], cx, cy);
			float[] pBC = closestPointOnTriangle(gamut[2], gamut[3], gamut[4], gamut[5], cx, cy);

			// Return the point closest to our point.
			float lowest = getDistanceBetweenTwoPoints(cx, cy, pAB[0], pAB[1]);
			float[] closestPoint = pAB;

			float dAC = getDistanceBetweenTwoPoints(cx, cy, pAC[0], pAC[1]);
			if (dAC < lowest) {
				lowest = dAC;
				closestPoint = pAC;
			}

			return getDistanceBetweenTwoPoints(cx, cy, pBC[0], pBC[1]) < lowest ? pBC : closestPoint;
		}

		return new float[] {cx, cy};
	}

	static private float getDistanceBetweenTwoPoints (float x1, float y1, float x2, float y2) {
		float dx = x1 - x2, dy = y1 - y2;
		return (float)Math.sqrt(dx * dx + dy * dy);
	}

	static private float[] closestPointOnTriangle (float Ax, float Ay, float Bx, float By, float Px, float Py) {
		float APx = Px - Ax, APy = Py - Ay;
		float ABx = Bx - Ax, ABy = By - Ay;
		float ab2 = ABx * ABx + ABy * ABy;
		float ap_ab = APx * ABx + APy * ABy;
		float t = ap_ab / ab2;
		if (t < 0)
			t = 0;
		else if (t > 1) //
			t = 1;
		return new float[] {Ax + ABx * t, Ay + ABy * t};
	}

	static private boolean isPointInGamut (float x, float y, float[] gamut) {
		float rx = gamut[0], ry = gamut[1];
		float gx = gamut[2], gy = gamut[3];
		float bx = gamut[4], by = gamut[5];
		float v1x = gx - rx, v1y = gy - ry;
		float v2x = bx - rx, v2y = by - ry;
		float qx = x - rx, qy = y - ry;
		float s = crossProduct(qx, qy, v2x, v2y) / crossProduct(v1x, v1y, v2x, v2y);
		float t = crossProduct(v1x, v1y, qx, qy) / crossProduct(v1x, v1y, v2x, v2y);
		return s >= 0 && t >= 0 && s + t <= 1;
	}

	static private float crossProduct (float p1x, float p1y, float p2x, float p2y) {
		return p1x * p2y - p1y * p2x;
	}
}
