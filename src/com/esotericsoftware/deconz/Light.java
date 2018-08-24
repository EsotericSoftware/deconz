/* Copyright (c) 2017, Esoteric Software
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.esotericsoftware.deconz;

import static com.esotericsoftware.deconz.DeCONZ.*;

/** @author Nathan Sweet */
public class Light {
	public String id;
	public String etag;
	public boolean hascolor;
	public String manufacturer;
	public String modelid;
	public String name;
	public LightState state;
	public String swversion;
	public String type;
	public String uniqueid;

	static public class LightState {
		public boolean on;
		public int bri;
		public int hue;
		public int sat;
		public int ct;
		public float[] xy;
		public LightAlert alert;
		public LightColorMode colormode;
		public LightEffect effect;
		public boolean reachable;
	}

	public enum LightAlert {
		none, select, lselect
	}

	public enum LightColorMode {
		hs, xy, ct
	}

	public enum LightEffect {
		none, colorloop
	}

	static public class LightAttributeChange extends Change<LightAttributeChange> {
		public LightAttributeChange name (String name) {
			return append("name", name);
		}
	}

	static public class LightStateChange extends Change<LightStateChange> {
		public LightStateChange on () {
			return comma("\"on\":true");
		}

		public LightStateChange on (boolean on) {
			return append("on", on);
		}

		public LightStateChange off () {
			return comma("\"on\":false");
		}

		/** @param brightness 0 to 1 */
		public LightStateChange brightness (float brightness) {
			return brightness((int)clamp(brightness * 255, 0, 255));
		}

		/** @param brightness 0 to 255 */
		public LightStateChange brightness (int brightness) {
			return append("bri", (int)clamp(brightness, 0, 255));
		}

		/** @param degrees 0 to 360 */
		public LightStateChange hue (float degrees) {
			return hue((int)clamp(degrees / 360 * 65535, 0, 65535));
		}

		/** @param hue 0 to 65535 */
		public LightStateChange hue (int hue) {
			return append("hue", clamp(hue, 0, 65535));
		}

		/** @param percent 0 to 1 */
		public LightStateChange saturation (float percent) {
			return saturation((int)clamp(percent, 0, 1) * 255);
		}

		/** @param saturation 0 to 255 */
		public LightStateChange saturation (int saturation) {
			return append("sat", saturation);
		}

		/** @param kelvin 2000 to 6500 */
		public LightStateChange kelvin (int kelvin) {
			return colorTemp(clamp(1000000 / kelvin, 153, 500));
		}

		/** @param temp 153 to 500 */
		public LightStateChange colorTemp (int temp) {
			return append("ct", clamp(temp, 153, 500));
		}

		/** @param x 0 to 1
		 * @param y 0 to 1 */
		public LightStateChange cie (float x, float y) {
			append("xy");
			buffer.append('[');
			buffer.append(clamp(x, 0, 1));
			buffer.append(',');
			buffer.append(clamp(y, 0, 1));
			buffer.append(']');
			return this;
		}

		public LightStateChange rgb (float r, float g, float b, String model) {
			float[] xy = ColorUtil.rgbToCie(r, g, b, model);
			return cie(xy[0], xy[1]);
		}

		public LightStateChange alert (LightAlert alert) {
			return append("alert", alert.name());
		}

		public LightStateChange effect (LightEffect effect) {
			return append("effect", effect.name());
		}

		/** @param speed 0 to 1, slowest to fastest. Default is 0.945f. Only valid with {@link LightEffect#colorloop}. */
		public LightStateChange colorLoopSpeed (float speed) {
			return append("colorloopspeed", (int)clamp((1 - speed) * 254 + 1, 1, 255));
		}

		public LightStateChange transition (float seconds) {
			return append("transitiontime", (int)(seconds * 10));
		}
	}
}
