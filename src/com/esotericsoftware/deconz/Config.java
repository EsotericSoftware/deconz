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

import java.util.HashMap;

import com.esotericsoftware.jsonbeans.JsonValue;

/** @author Nathan Sweet */
public class Config {
	public String apiversion;
	public boolean dhcp;
	public String gateway;
	public String ipaddress;
	public boolean linkbutton;
	public String localtime;
	public String mac;
	public String name;
	public String netmask;
	public int networkopenduration;
	public int panid;
	public boolean portalservices;
	public JsonValue softwareupdate;
	public String swversion;
	public String timeformat;
	public String timezone;
	public String utc;
	public String uuid;
	public HashMap<String, ApiKeyUsage> whitelist;
	public int zigbeechannel;
	public int websocketport;

	static public class ConfigChange extends Change<ConfigChange> {
		public ConfigChange name (String name) {
			return append("name", name);
		}

		public ConfigChange rfConnected (boolean connected) {
			return append("rfconnected", connected);
		}

		public ConfigChange updateChannel (String channel) {
			return append("updatechannel", channel);
		}

		public ConfigChange permitJoin (int seconds) {
			return append("permitjoin", seconds);
		}

		public ConfigChange groupDelay (int millis) {
			return append("groupdelay", millis);
		}

		public ConfigChange otauActive (boolean active) {
			return append("otauactive", active);
		}

		public ConfigChange discovery (boolean discovery) {
			return append("discovery", discovery);
		}

		public ConfigChange unlock (int seconds) {
			return append("unlock", seconds);
		}

		public ConfigChange zigBeeChannel (int channel) {
			return append("zigbeechannel", channel);
		}

		public ConfigChange timezone (String timezone) {
			return append("timezone", timezone);
		}

		public ConfigChange utc (String utc) {
			return append("utc", utc);
		}

		public ConfigChange timeFormat (boolean twentyFourHours) {
			return append("timeformat", twentyFourHours ? "24h" : "12h");
		}
	}

	static public class ApiKeyUsage {
		public String name;
		public String create_date;
		public String last_use_date;
	}
}