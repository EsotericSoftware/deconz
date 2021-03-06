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

import static com.esotericsoftware.minlog.Log.*;

import com.esotericsoftware.deconz.Sensor.SensorState;
import com.esotericsoftware.jsonbeans.JsonValue;
import com.esotericsoftware.minlog.Log;

/** @author Nathan Sweet */
public interface WebsocketListener {
	public void connected ();

	public void event (JsonValue event);

	public void groupChanged (String groupID, boolean anyOn);

	public void sceneCalled (String groupID, String sceneID);

	public void lightChanged (String lightID, boolean on);

	public void sensorChanged (String sensorID, SensorState state);

	public void disconnected (int code, String reason, boolean remote);

	public void error (Exception ex);

	static public class WebsocketAdapter implements WebsocketListener {
		public void connected () {
		}

		public void event (JsonValue event) {
		}

		public void groupChanged (String groupID, boolean anyOn) {
		}

		public void sceneCalled (String groupID, String sceneID) {
		}

		public void lightChanged (String lightID, boolean on) {
		}

		public void sensorChanged (String sensorID, SensorState state) {
		}

		public void disconnected (int code, String reason, boolean remote) {
		}

		public void error (Exception ex) {
		}
	}
}
