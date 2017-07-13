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

import com.esotericsoftware.deconz.Schedule.ScheduleStatus;

/** @author Nathan Sweet */
public class Rule {
	public String id;
	public ArrayList<RuleAction> actions;
	public ArrayList<RuleCondition> conditions;
	public String created;
	public String etag;
	public String lasttriggered;
	public String name;
	public String owner;
	public int periodic;
	public String status;
	public int timestriggered;

	static public class RuleAction {
		public String address;
		public String body;
		public RuleMethod method;
	}

	static public class RuleCondition {
		public String address;
		public RuleOperator operator;
		public String value;
	}

	public enum RuleMethod {
		PUT, POST, DELETE, BIND
	}

	public enum RuleOperator {
		eq, gt, lt, dx
	}

	public enum RuleStatus {
		enabled, disabled
	}

	static public class RuleAttributeChange extends Change<RuleAttributeChange> {
		public RuleAttributeChange name (String name) {
			return append("name", name);
		}

		public RuleAttributeChange periodic (int periodic) {
			return append("periodic", periodic);
		}

		public RuleAttributeChange status (ScheduleStatus status) {
			return append("status", status.name());
		}

		public RuleAttributeChange actions (ArrayList<RuleAction> actions) {
			comma("\"actions\":[");
			for (int i = 0, n = actions.size(); i < n; i++) {
				RuleAction action = actions.get(i);
				if (i != 0) buffer.append(',');
				buffer.append(
					"{\"address\":\"" + action.address + "\",\"body\":\"" + action.body + "\",\"method\":\"" + action.method + "\"}");
			}
			buffer.append(']');
			return this;
		}

		public RuleAttributeChange conditions (ArrayList<RuleCondition> conditions) {
			comma("\"conditions\":[");
			for (int i = 0, n = conditions.size(); i < n; i++) {
				RuleCondition condition = conditions.get(i);
				if (i != 0) buffer.append(',');
				buffer.append("{\"address\":\"" + condition.address + "\",\"operator\":\"" + condition.operator + "\",\"value\":\""
					+ condition.value + "\"}");
			}
			buffer.append(']');
			return this;
		}
	}
}
