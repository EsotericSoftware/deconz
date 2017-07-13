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

import java.util.ArrayList;

/** @author Nathan Sweet */
public class Scene {
	public String name;
	public ArrayList<String> lights;

	static public class SceneAttributes {
		public String name;
		public ArrayList<SceneLightState> lights;
	}

	static public class SceneLightState {
		public String id;
		public boolean on;
		public int bri;
		public int hue;
		public int sat;
		public int ct;
		public float x;
		public float y;
		public int transitiontime;
	}

	static public class SceneAttributeChange extends Change<SceneAttributeChange> {
		public SceneAttributeChange name (String name) {
			return append("name", name);
		}
	}

	static public class SceneLightChange extends Change<SceneLightChange> {
		public SceneLightChange on () {
			return comma("\"on\":true");
		}

		public SceneLightChange on (boolean on) {
			return append("on", on);
		}

		public SceneLightChange off () {
			return comma("\"on\":false");
		}

		/** @param brightness 0 to 1 */
		public SceneLightChange brightness (float brightness) {
			return append("bri", (int)clamp(brightness * 255, 0, 255));
		}

		/** @param x 0 to 1
		 * @param y 0 to 1 */
		public SceneLightChange cie (float x, float y) {
			append("xy");
			buffer.append('[');
			buffer.append(clamp(x, 0, 1));
			buffer.append(',');
			buffer.append(clamp(y, 0, 1));
			buffer.append(']');
			return this;
		}

		public SceneLightChange transition (float seconds) {
			return append("transitiontime", (int)(seconds * 10));
		}
	}
}
