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
public class Change<T extends Change> {
	protected final StringBuilder buffer = new StringBuilder();

	public void reset () {
		buffer.setLength(0);
	}

	T comma (String text) {
		if (buffer.length() > 0) buffer.append(',');
		buffer.append(text);
		return (T)this;
	}

	T append (String name) {
		comma("\"");
		buffer.append(name);
		buffer.append("\":");
		return (T)this;
	}

	T append (String name, String value) {
		comma("\"");
		buffer.append(name);
		buffer.append("\":\"");
		buffer.append(value);
		buffer.append('\"');
		return (T)this;
	}

	T append (String name, boolean value) {
		comma("\"");
		buffer.append(name);
		buffer.append("\":");
		buffer.append(value);
		return (T)this;
	}

	T append (String name, int value) {
		comma("\"");
		buffer.append(name);
		buffer.append("\":");
		buffer.append(value);
		return (T)this;
	}

	T append (ArrayList<String> values) {
		comma("");
		boolean comma = false;
		for (String value : values) {
			if (!comma) {
				buffer.append('\"');
				comma = true;
			} else
				buffer.append(",\"");
			buffer.append(value);
			buffer.append('\"');
		}
		return (T)this;
	}

	public String toString () {
		return buffer.toString();
	}
}
