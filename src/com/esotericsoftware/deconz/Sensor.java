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
public class Sensor {
	public String id;
	public SensorConfig config;
	public int ep;
	public String etag;
	public String manufacturername;
	public String modelid;
	public int mode;
	public String name;
	public SensorState state;
	public String swversion;
	public SensorType type;
	public String uniqueid;

	static public class SensorConfig {
		public boolean on;
		public boolean reachable;
		public int battery;
		public int duration;
	}

	static public class SensorState {
		public String lastupdated;
		public String buttonevent; // ZHASwitch
		public int lux, lightlevel; // ZHALight
		public boolean presence; // ZHAPresence
		public int temperature; // ZHATemperature
	}

	public enum SensorType {
		ZHASwitch, ZHALight, ZHAPresence, ZHATemperature, ZHALightLevel, //
		CLIPSwitch, CLIPOpenClose, CLIPPresence, CLIPTemperature, CLIPGenericFlag, CLIPGenericStatus, CLIPHumidity, //
		Daylight
	}

	public enum ZHASensorState {
		buttonevent, lux, presence
	}

	public enum CLIPSensorState {
		buttonevent, open, presence, temperature, flag, status, humidity
	}

	static public class SensorAttributeChange extends Change<SensorAttributeChange> {
		public SensorAttributeChange name (String name) {
			return append("name", name);
		}

		public SensorAttributeChange mode (int mode) {
			return append("mode", mode);
		}
	}

	static public class SensorConfigChange extends Change<SensorConfigChange> {
		public SensorConfigChange on (boolean on) {
			return append("on", on);
		}

		public SensorConfigChange reachable (boolean reachable) {
			return append("reachable", reachable);
		}

		/** @param battery 0 to 100 */
		public SensorConfigChange battery (int battery) {
			return append("battery", clamp(battery, 0, 100));
		}
	}
}
