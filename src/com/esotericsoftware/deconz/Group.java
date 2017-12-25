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

import java.util.ArrayList;

/** @author Nathan Sweet */
public class Group {
	public String id;
	public ArrayList<String> devicemembership;
	public String name;
	public String etag;
	public boolean hidden;

	static public class GroupAttributes {
		public String id;
		public GroupAction action;
		public ArrayList<String> devicemembership;
		public String etag;
		public boolean hidden;
		public ArrayList<String> lights;
		public ArrayList<String> lightsequence;
		public ArrayList<String> mulitdeviceids;
		public String name;
		public ArrayList<GroupSceneAttributes> scenes;
	}

	static public class GroupSceneAttributes {
		public String id;
		public int lightcount;
		public String name;
		public int transitiontime;
	}

	static public class GroupAction {
		public boolean on;
		public int bri;
		public int hue;
		public int sat;
		public int ct;
		public float[] xy;
		public GroupEffect effect;
	}

	public enum GroupEffect {
		none, colorloop
	}

	static public class GroupAttributeChange extends Change<GroupAttributeChange> {
		public GroupAttributeChange name (String name) {
			return append("name", name);
		}

		public GroupAttributeChange lights (ArrayList<String> lights) {
			return append(lights);
		}

		public GroupAttributeChange hidden (boolean hidden) {
			return append("hidden", hidden);
		}

		public GroupAttributeChange lightsequence (ArrayList<String> lights) {
			return append(lights);
		}

		public GroupAttributeChange mulitdeviceids (ArrayList<String> lights) {
			return append(lights);
		}
	}
}
