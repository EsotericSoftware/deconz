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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.esotericsoftware.deconz.DeCONZException.ErrorCode;
import com.esotericsoftware.deconz.Group.GroupAttributeChange;
import com.esotericsoftware.deconz.Group.GroupAttributes;
import com.esotericsoftware.deconz.Light.LightAttributeChange;
import com.esotericsoftware.deconz.Light.LightStateChange;
import com.esotericsoftware.deconz.Rule.RuleAction;
import com.esotericsoftware.deconz.Rule.RuleAttributeChange;
import com.esotericsoftware.deconz.Rule.RuleCondition;
import com.esotericsoftware.deconz.Rule.RuleStatus;
import com.esotericsoftware.deconz.Scan.Device;
import com.esotericsoftware.deconz.Scan.ScanStatus;
import com.esotericsoftware.deconz.Scene.SceneAttributeChange;
import com.esotericsoftware.deconz.Scene.SceneAttributes;
import com.esotericsoftware.deconz.Scene.SceneLightChange;
import com.esotericsoftware.deconz.Schedule.ScheduleAttributeChange;
import com.esotericsoftware.deconz.Schedule.ScheduleStatus;
import com.esotericsoftware.deconz.Sensor.CLIPSensorState;
import com.esotericsoftware.deconz.Sensor.SensorAttributeChange;
import com.esotericsoftware.deconz.Sensor.SensorConfig;
import com.esotericsoftware.deconz.Sensor.SensorConfigChange;
import com.esotericsoftware.deconz.Sensor.SensorState;
import com.esotericsoftware.deconz.Sensor.SensorType;
import com.esotericsoftware.deconz.Sensor.ZHASensorState;
import com.esotericsoftware.deconz.WebsocketListener.WebsocketAdapter;
import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonReader;
import com.esotericsoftware.jsonbeans.JsonValue;

import shaded.org.apache.http.HttpEntity;
import shaded.org.apache.http.client.methods.CloseableHttpResponse;
import shaded.org.apache.http.client.methods.HttpDelete;
import shaded.org.apache.http.client.methods.HttpGet;
import shaded.org.apache.http.client.methods.HttpPost;
import shaded.org.apache.http.client.methods.HttpPut;
import shaded.org.apache.http.client.methods.HttpUriRequest;
import shaded.org.apache.http.entity.StringEntity;
import shaded.org.apache.http.impl.client.CloseableHttpClient;
import shaded.org.apache.http.impl.client.HttpClients;
import shaded.org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import shaded.org.apache.http.util.EntityUtils;

/** @author Nathan Sweet */
public class DeCONZ {
	String apiKey;
	final String host;
	private int restPort;
	private CloseableHttpClient http;
	char[] buffer = new char[2048];
	int bufferSize = 0;
	final JsonReader jsonReader = new JsonReader();

	final Json json = new Json();
	{
		json.setIgnoreUnknownFields(true);
	}

	private final Gateway gateway = new Gateway();
	private final Groups groups = new Groups();
	private final Lights lights = new Lights();
	private final Rules rules = new Rules();
	private final Scenes scenes = new Scenes();
	private final Schedules schedules = new Schedules();
	private final Sensors sensors = new Sensors();
	private final Touchlink touchlink = new Touchlink();
	private final Websocket websocket = new Websocket();

	/** @param apiKey May be null.
	 * @param restPort The default REST API port is 80.
	 * @param connectionPoolSize The number of threads that can make HTTP requests concurrently. */
	public DeCONZ (String apiKey, String host, int restPort, int connectionPoolSize) {
		if (apiKey == null) throw new IllegalArgumentException("apiKey cannot be null.");
		if (host == null) throw new IllegalArgumentException("host cannot be null.");
		this.apiKey = apiKey;
		this.host = host;
		this.restPort = restPort;

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(connectionPoolSize);
		connectionManager.setDefaultMaxPerRoute(connectionPoolSize);
		http = HttpClients.custom().setConnectionManager(connectionManager).build();

		if (DEBUG) debug("deconz", "API key: " + apiKey);
	}

	/** @param apiKey May be null.
	 * @param restPort The default REST API port is 80. */
	public DeCONZ (String apiKey, String host, int restPort, CloseableHttpClient http) {
		if (apiKey == null) throw new IllegalArgumentException("apiKey cannot be null.");
		if (host == null) throw new IllegalArgumentException("host cannot be null.");
		if (http == null) throw new IllegalArgumentException("http cannot be null.");

		this.apiKey = apiKey;
		this.host = host;
		this.restPort = restPort;
		this.http = http;
		if (DEBUG) debug("deconz", "API key: " + apiKey);
	}

	private String url (String path) throws DeCONZException {
		if (path == null) throw new IllegalArgumentException("path cannot be null.");
		String url = "http://" + host + ':' + restPort + "/api/";
		if (!path.equals("")) {
			if (apiKey == null) throw new DeCONZException("API user has not been registered.");
			url += apiKey + '/';
			if (path.equals("/")) return url;
		}
		return url + path;
	}

	JsonValue httpGet (String path, Object... args) throws DeCONZException {
		try {
			int n = args.length;
			if (n > 0) {
				StringBuilder buffer = new StringBuilder(path);
				buffer.append('?');
				for (int i = 0; i < n; i += 2) {
					if (i > 0) buffer.append('&');
					buffer.append(args[i]);
					buffer.append(URLEncoder.encode(args[i].toString(), "UTF-8"));
				}
				path += buffer;
			}
		} catch (UnsupportedEncodingException ex) {
			throw new DeCONZException(ex);
		}
		String url = url(path);
		try {
			if (DEBUG) debug("deconz", "Get: " + url);
			return httpRequest(new HttpGet(url));
		} catch (Exception ex) {
			throw new DeCONZException("Get: " + url, ex);
		}
	}

	JsonValue httpPost (String path, String body) throws DeCONZException {
		String url = url(path);
		try {
			if (DEBUG)
				debug("deconz", "Post: " + url);
			else if (TRACE) //
				trace("deconz", "Post: " + url + " " + body);
			HttpPost post = new HttpPost(url);
			post.setEntity(new StringEntity(body));
			return httpRequest(post);
		} catch (Exception ex) {
			throw new DeCONZException("Post: " + url + " " + body, ex);
		}
	}

	JsonValue httpPut (String path, String body) throws DeCONZException {
		String url = url(path);
		try {
			if (DEBUG)
				trace("deconz", "Put: " + url);
			else if (TRACE) //
				trace("deconz", "Put: " + url + " " + body);
			HttpPut put = new HttpPut(url);
			put.setEntity(new StringEntity(body));
			return httpRequest(put);
		} catch (Exception ex) {
			throw new DeCONZException("Put: " + url + " " + body, ex);
		}
	}

	JsonValue httpDelete (String path) throws DeCONZException {
		String url = url(path);
		try {
			if (TRACE) trace("deconz", "Delete: " + url);
			return httpRequest(new HttpDelete(url));
		} catch (Exception ex) {
			throw new DeCONZException("Delete: " + url, ex);
		}
	}

	private JsonValue httpRequest (HttpUriRequest request) throws Exception {
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		try {
			response = http.execute(request);
			entity = response.getEntity();
			InputStream input = entity.getContent();
			if (input == null) throw new RuntimeException("Empty response.");
			int statusCode = response.getStatusLine().getStatusCode();
			synchronized (jsonReader) {
				buffer(new InputStreamReader(input, "UTF-8"));
				JsonValue json = null;
				try {
					json = jsonReader.parse(buffer, 0, bufferSize);
				} catch (Exception ex) {
					if (statusCode < 400)
						throw new DeCONZException("Error parsing REST response: " + new String(buffer, 0, bufferSize), ex);
				}
				if (statusCode >= 400) {
					ErrorCode errorCode = findErrorCode(json);
					DeCONZException ex = new DeCONZException(
						"REST request failed, " + statusCode + " " + errorCode + ": " + new String(buffer, 0, bufferSize));
					ex.errorCode = errorCode;
					throw ex;
				}
				if (bufferSize == 0) throw new DeCONZException("Empty REST response.");
				if (json == null) throw new DeCONZException("Null REST response: " + new String(buffer, 0, bufferSize));
				if (TRACE) trace("deconz", "REST response: " + json);
				return json;
			}
		} finally {
			if (entity != null) EntityUtils.consumeQuietly(entity);
			if (response != null) {
				try {
					response.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	/** @param json May be null. */
	private ErrorCode findErrorCode (JsonValue json) throws DeCONZException {
		if (json == null) return ErrorCode.unknown;
		JsonValue error = null;
		if (json.isArray()) {
			for (JsonValue entry = json.child; entry != null; entry = entry.next) {
				error = entry.get("error");
				if (error != null) break;
			}
		} else
			error = json.get("error");
		if (error == null) return ErrorCode.unknown;
		try {
			int type = error.getInt("type", 0);
			if (type < 1 || type >= ErrorCode.values.length) return ErrorCode.unknown;
			return ErrorCode.values[type];
		} catch (Exception ex) {
			return ErrorCode.unknown;
		}
	}

	private void buffer (Reader reader) throws DeCONZException {
		try {
			bufferSize = 0;
			while (true) {
				int length = reader.read(buffer, bufferSize, buffer.length - bufferSize);
				if (length == -1) break;
				if (length == 0) {
					char[] newBuffer = new char[buffer.length * 2];
					System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
					buffer = newBuffer;
				} else
					bufferSize += length;
			}
		} catch (IOException ex) {
			throw new DeCONZException("Error reading response.", ex);
		} finally {
			try {
				reader.close();
			} catch (IOException ignored) {
			}
		}
	}

	JsonValue success (JsonValue json) throws DeCONZException {
		if (!json.isArray()) {
			if (json.has("error")) {
				ErrorCode errorCode = findErrorCode(json);
				DeCONZException ex = new DeCONZException("Response error, " + errorCode + ": " + json);
				ex.errorCode = errorCode;
				ex.errorResponse = json.get("error");
				throw ex;
			}
			if (!json.has("success")) throw new DeCONZException("Response missing success: " + json);
		} else {
			for (JsonValue entry = json.child; entry != null; entry = entry.next) {
				if (entry.has("error")) {
					ErrorCode errorCode = findErrorCode(json);
					DeCONZException ex = new DeCONZException("Response error, " + errorCode + ": " + json);
					ex.errorCode = errorCode;
					ex.errorResponse = entry.get("error");
					throw ex;
				}
				if (!entry.has("success")) throw new DeCONZException("Response missing success: " + json);
			}
		}
		return json;
	}

	/** @param jsonValue May be null. */
	<T> ArrayList<T> toList (JsonValue jsonValue, Class<T> type, String nameField) throws DeCONZException {
		if (jsonValue == null) return new ArrayList(0);
		try {
			Field field = type.getField(nameField);
			ArrayList list = new ArrayList(jsonValue.size);
			for (JsonValue entry = jsonValue.child; entry != null; entry = entry.next) {
				T object = json.readValue(type, entry);
				field.set(object, entry.name);
				list.add(object);
			}
			return list;
		} catch (Exception ex) {
			throw new DeCONZException(ex);
		}
	}

	/** @param jsonValue May be null.
	 * @return May be null. */
	<T> T toObject (JsonValue jsonValue, Class<T> type) throws DeCONZException {
		if (jsonValue == null) return null;
		try {
			return json.readValue(type, jsonValue);
		} catch (Exception ex) {
			throw new DeCONZException("Error deserializing JSON: " + jsonValue, ex);
		}
	}

	/** @param jsonValue May be null.
	 * @return May be null. */
	<T> T toObject (JsonValue jsonValue, Class<T> type, String nameField) throws DeCONZException {
		if (jsonValue == null) return null;
		try {
			T object = json.readValue(type, jsonValue);
			type.getField(nameField).set(object, jsonValue.name);
			return object;
		} catch (Exception ex) {
			throw new DeCONZException("Error deserializing JSON: " + jsonValue, ex);
		}
	}

	public Gateway getGateway () {
		return gateway;
	}

	public Groups getGroups () {
		return groups;
	}

	public Lights getLights () {
		return lights;
	}

	public Rules getRules () {
		return rules;
	}

	public Scenes getScenes () {
		return scenes;
	}

	public Schedules getSchedules () {
		return schedules;
	}

	public Sensors getSensors () {
		return sensors;
	}

	public Websocket getWebsocket () {
		return websocket;
	}

	// ---

	public class Gateway {
		/** @param userName May be null. */
		public String register (String appName, String userName, boolean reset) throws DeCONZException {
			if (reset) apiKey = null;
			if (apiKey != null) return apiKey;
			String request = "{\"devicetype\":\"" + appName + "\"";
			if (userName != null) request += ",\"username\":\"" + userName + "\"";
			apiKey = success(httpPost("", request + "}")).get(0).require("success").getString("username");
			if (INFO) info("deconz", "API key registered: " + apiKey);
			return apiKey;
		}

		public void deregister (String apiKey) throws DeCONZException {
			success(httpDelete("config/whitelist/" + apiKey));
			if (INFO) info("deconz", "API key deregistered: " + apiKey);
		}

		public Config getConfig () throws DeCONZException {
			return toObject(httpGet("config"), Config.class);
		}

		public String updateSoftware () throws DeCONZException {
			if (INFO) info("deconz", "Performing software update.");
			return success(httpPost("config/update", "")).get("success").child.asString();
		}

		public String updateFirmware () throws DeCONZException {
			if (INFO) info("deconz", "Performing firmware update.");
			return success(httpPost("config/updatefirmware", "")).get("success").child.asString();
		}

		public String resetGateway (boolean resetNetworkSettings, boolean deleteDatabase) throws DeCONZException {
			if (INFO) info("deconz", "Reseting the gateway.");
			return success(
				httpPost("config/reset", "{\"resetGW\":" + resetNetworkSettings + "\",\"deleteDB\":" + deleteDatabase + "\"}"))
					.get("success").child.asString();
		}

		public void changePassword (String userName, String oldHash, String newHash) throws DeCONZException {
			success(httpPut("config/password",
				"{\"username\":\"" + userName + "\",\"oldhash\":\"" + oldHash + "\",\"newhash\":\"" + newHash + "\"}"));
			if (INFO) info("deconz", "Password changed: " + userName);
		}

		public void resetPassword () throws DeCONZException {
			success(httpDelete("config/password"));
			if (INFO) info("deconz", "Password reset.");
		}

		public FullState getFullState () throws DeCONZException {
			JsonValue jsonValue = httpGet("/");
			FullState state = new FullState();
			state.config = toObject(jsonValue, Config.class);
			state.groups = toList(jsonValue.get("groups"), GroupAttributes.class, "id");
			state.lights = toList(jsonValue.get("lights"), Light.class, "id");
			state.rules = toList(jsonValue.get("rules"), Rule.class, "id");
			state.schedules = toList(jsonValue.get("schedules"), Schedule.class, "id");
			state.sensors = toList(jsonValue.get("sensors"), Sensor.class, "id");
			return state;
		}
	}

	public class Groups {
		public String create (String name) throws DeCONZException {
			return success(httpPost("groups", "{\"name\":\"" + name + "\"}")).get(0).getString("id");
		}

		public ArrayList<Group> getAll () throws DeCONZException {
			return toList(httpGet("groups"), Group.class, "id");
		}

		public GroupAttributes get (String groupID) throws DeCONZException {
			return toObject(httpGet("groups/" + groupID), GroupAttributes.class);
		}

		public void apply (String groupID, GroupAttributeChange change) throws DeCONZException {
			success(httpPut("groups/" + groupID, "{" + change.buffer + "}"));
		}

		public void apply (String groupID, LightStateChange change) throws DeCONZException {
			success(httpPut("groups/" + groupID + "/action", "{" + change.buffer + "}"));
		}

		public void delete (String groupID) throws DeCONZException {
			success(httpDelete("groups/" + groupID));
		}
	}

	public class Lights {
		public ArrayList<Light> getAll () throws DeCONZException {
			return toList(httpGet("lights"), Light.class, "id");
		}

		public Light get (String lightID) throws DeCONZException {
			return toObject(httpGet("lights/" + lightID), Light.class);
		}

		public void apply (String lightID, LightAttributeChange change) throws DeCONZException {
			success(httpPut("lights/" + lightID, "{" + change.buffer + "}"));
		}

		public void apply (String lightID, LightStateChange change) throws DeCONZException {
			success(httpPut("lights/" + lightID + "/state", "{" + change.buffer + "}"));
		}

		public void delete (String lightID) throws DeCONZException {
			success(httpDelete("lights/" + lightID));
		}

		public void removeGroups (String lightID) throws DeCONZException {
			success(httpDelete("lights/" + lightID + "/groups"));
		}

		public void removeScenes (String lightID) throws DeCONZException {
			success(httpDelete("lights/" + lightID + "/scenes"));
		}
	}

	public class Rules {
		public String create (String name, ArrayList<RuleAction> actions, ArrayList<RuleCondition> conditions, int periodic,
			RuleStatus status) throws DeCONZException {
			StringBuilder request = new StringBuilder(
				"{\"name\":\"" + name + "\",\"periodic\":" + periodic + ",\"status\":\"" + status + "\",\"actions\":[");
			for (int i = 0, n = actions.size(); i < n; i++) {
				RuleAction action = actions.get(i);
				if (i != 0) request.append(',');
				request.append(
					"{\"address\":\"" + action.address + "\",\"body\":\"" + action.body + "\",\"method\":\"" + action.method + "\"}");
			}
			request.append("],\"conditions\":[");
			for (int i = 0, n = actions.size(); i < n; i++) {
				RuleCondition condition = conditions.get(i);
				if (i != 0) request.append(',');
				request.append("{\"address\":\"" + condition.address + "\",\"operator\":\"" + condition.operator + "\",\"value\":\""
					+ condition.value + "\"}");
			}
			request.append("]}");
			return success(httpPost("rules", request.toString())).get(0).getString("id");
		}

		public ArrayList<Rule> getAll () throws DeCONZException {
			return toList(httpGet("rules"), Rule.class, "id");
		}

		public Rule get (String ruleID) throws DeCONZException {
			return toObject(httpGet("rules/" + ruleID), Rule.class);
		}

		public void apply (String ruleID, RuleAttributeChange change) throws DeCONZException {
			success(httpPut("rules/" + ruleID, "{" + change.buffer + "}"));
		}

		public void delete (String ruleID) throws DeCONZException {
			success(httpDelete("rules/" + ruleID));
		}
	}

	public class Scenes {
		public String create (String name) throws DeCONZException {
			return success(httpPost("scenes", "{\"name\":\"" + name + "\"}")).get(0).getString("id");
		}

		public ArrayList<Scene> getAll (String groupID) throws DeCONZException {
			return toList(httpGet("group/" + groupID + "/scenes"), Scene.class, "id");
		}

		public SceneAttributes get (String groupID, String sceneID) throws DeCONZException {
			return toObject(httpGet("groups/" + groupID + "/scenes/" + sceneID), SceneAttributes.class);
		}

		public void apply (String groupID, String sceneID, SceneAttributeChange change) throws DeCONZException {
			success(httpPut("groups/" + groupID + "/scenes/" + sceneID, "{" + change.buffer + "}"));
		}

		public void apply (String groupID, String sceneID, String lightID, SceneLightChange change) throws DeCONZException {
			success(
				httpPut("groups/" + groupID + "/scenes/" + sceneID + "/lights/" + lightID + "/state", "{" + change.buffer + "}"));
		}

		public void store (String groupID, String sceneID) throws DeCONZException {
			success(httpPut("groups/" + groupID + "/scenes/" + sceneID + "/store", ""));
		}

		public void recall (String groupID, String sceneID) throws DeCONZException {
			success(httpPut("groups/" + groupID + "/scenes/" + sceneID + "/recall", ""));
		}

		public void delete (String groupID, String sceneID) throws DeCONZException {
			success(httpDelete("groups/" + groupID + "/scenes/" + sceneID));
		}
	}

	public class Schedules {
		/** @param name May be null.
		 * @param description May be null. */
		public String create (String name, String description, String commandAddress, String commandBody, ScheduleStatus status,
			boolean autodelete, String time) throws DeCONZException {
			String request = "{\"command\":{\"address\":\"" + commandAddress + "\",\"method\":\"PUT\",\"body\":\"" + commandBody
				+ "\"},\"status\":\"" + status + "\",\"autodelete\":" + autodelete + ",\"time\":\"" + time + "\"";
			if (name != null) request += ",\"name\":\"" + name + "\"";
			if (description != null) request += ",\"description\":\"" + description + "\"";
			return success(httpPost("schedules", request + "}")).get(0).getString("id");
		}

		public ArrayList<Schedule> getAll (String groupID) throws DeCONZException {
			return toList(httpGet("schedules"), Schedule.class, "id");
		}

		public Schedule get (String scheduleID) throws DeCONZException {
			return toObject(httpGet("schedules/" + scheduleID), Schedule.class);
		}

		public void apply (String scheduleID, ScheduleAttributeChange change) throws DeCONZException {
			success(httpPut("schedules/" + scheduleID, "{" + change.buffer + "}"));
		}

		public void delete (String scheduleID) throws DeCONZException {
			success(httpDelete("schedules/" + scheduleID));
		}
	}

	public class Sensors {
		/** @param zhaState May be null.
		 * @param config May be null. */
		public String create (String name, String modelID, String swVersion, SensorType zhaType, String uniqueID,
			String manufacturerName, ZHASensorState zhaState, SensorConfig config) throws DeCONZException {
			String request = "{\"name\":\"" + name + "\",\"modelid\":\"" + modelID + "\",\"swversion\":\"" + swVersion
				+ "\",\"type\":\"" + zhaType + "\",\"uniqueid\":\"" + uniqueID + "\",\"manufacturername\":\"" + manufacturerName
				+ "\"";
			if (zhaState != null) request += ",\"state\":\"" + zhaState + '\"';
			if (config != null) request += ",\"config\":\"" + config + "\"";
			return success(httpPost("sensors", request + "}")).get(0).getString("id");
		}

		public ArrayList<Sensor> getAll () throws DeCONZException {
			return toList(httpGet("sensors"), Sensor.class, "id");
		}

		public Sensor get (String sensorID) throws DeCONZException {
			return toObject(httpGet("sensors/" + sensorID), Sensor.class);
		}

		public void apply (String sensorID, SensorAttributeChange change) throws DeCONZException {
			success(httpPut("sensors/" + sensorID, "{" + change.buffer + "}"));
		}

		public void apply (String sensorID, SensorConfigChange change) throws DeCONZException {
			success(httpPut("sensors/" + sensorID + "/config", "{" + change.buffer + "}"));
		}

		public void setState (String sensorID, CLIPSensorState clipState, int value) throws DeCONZException {
			success(httpPut("sensors/" + sensorID + "/state", "{\"" + clipState + "\":" + value + "}"));
		}

		public void setState (String sensorID, CLIPSensorState clipState, boolean value) throws DeCONZException {
			success(httpPut("sensors/" + sensorID + "/state", "{\"" + clipState + "\":" + value + "}"));
		}

		public void delete (String sensorID) throws DeCONZException {
			success(httpDelete("sensors/" + sensorID));
		}
	}

	public class Touchlink {
		public void scan () throws DeCONZException {
			httpPost("touchlink/scan", "");
		}

		public Scan getScanResult () throws DeCONZException {
			JsonValue jsonValue = httpGet("touchlink/scan");
			Scan scan = new Scan();
			String value = jsonValue.getString("lastscan", null);
			if (value != null) scan.scanstate = ScanStatus.valueOf(value);
			scan.lastscan = jsonValue.getString("lastscan");
			scan.result = toList(jsonValue.get("result"), Device.class, "id");
			return scan;
		}

		public void identify (String deviceID) throws DeCONZException {
			httpPost("touchlink/" + deviceID + "/identify", "");
		}

		public void reset (String deviceID) throws DeCONZException {
			httpPost("touchlink/" + deviceID + "/rest", "");
		}
	}

	public class Websocket {
		final CopyOnWriteArrayList<WebsocketListener> listeners = new CopyOnWriteArrayList();
		private volatile WebSocketClient socket;
		char[] buffer = new char[64];
		final JsonReader jsonReader = new JsonReader();

		public synchronized void open (int port) throws DeCONZException {
			close();
			final String url = "ws://" + host + ":" + port;
			try {
				if (TRACE) trace("deconz", "Connecting to websocket: " + url);
				socket = new WebSocketClient(new URI(url)) {
					public void onOpen (ServerHandshake handshake) {
						if (INFO) info("deconz", "Websocket connected: " + url);
						for (WebsocketListener listener : listeners)
							listener.connected();
					}

					public void onMessage (String message) {
						int count = message.length();
						if (buffer.length < count) buffer = new char[(int)(count * 1.2f)];
						message.getChars(0, count, buffer, 0);
						JsonValue json;
						try {
							json = jsonReader.parse(buffer, 0, count);
						} catch (Exception ex) {
							onError(new DeCONZException("Error parsing websocket response: " + new String(buffer, 0, count), ex));
							Websocket.this.close();
							return;
						}
						if (TRACE) trace("deconz", "Websocket received: " + json);

						for (WebsocketListener listener : listeners)
							listener.event(json);

						if (json.getString("t").equals("event")) {
							String resource = json.getString("r", "");
							String type = json.getString("e", "");

							JsonValue state = json.get("state");
							if (state != null && type.equals("changed")) {
								if (resource.equals("sensors")) {
									SensorState sensorState;
									try {
										sensorState = toObject(state, SensorState.class);
									} catch (DeCONZException ex) {
										if (ERROR) error("deconz", "Error parsing websocket sensor state change: " + json);
										return;
									}
									String id = json.getString("id");
									for (WebsocketListener listener : listeners)
										listener.sensorChanged(id, sensorState);
								} else if (resource.equals("lights")) {
									String id = json.getString("id");
									boolean on = state.getBoolean("on", false);
									for (WebsocketListener listener : listeners)
										listener.lightChanged(id, on);
								} else if (resource.equals("groups")) {
									String id = json.getString("id");
									boolean on = state.getBoolean("any_on", false);
									for (WebsocketListener listener : listeners)
										listener.groupChanged(id, on);
								}
							} else if (type.equals("scene-called") && resource.equals("scenes")) {
								String groupID = json.getString("gid");
								String sceneID = json.getString("scid");
								for (WebsocketListener listener : listeners)
									listener.sceneCalled(groupID, sceneID);
							}
						}
					}

					public void onClose (int code, String reason, boolean remote) {
						if (INFO)
							info("deconz", "Websocket disconnected: " + code + ", " + reason + ", " + (remote ? "remote" : "local"));
						for (WebsocketListener listener : listeners)
							listener.connected();
					}

					public void onError (Exception ex) {
						if (ERROR) error("deconz", "Websocket error.", ex);
					}
				};
				socket.connect();
			} catch (URISyntaxException ex) {
				throw new DeCONZException("Invalid URL.", ex);
			}
		}

		public synchronized void close () {
			if (socket != null) {
				if (TRACE) trace("deconz", "Closing websocket.");
				socket.close();
				socket = null;
			}
		}

		public boolean isConnected () {
			return socket != null;
		}

		public void addListener (WebsocketListener listener) {
			listeners.add(listener);
		}

		public void removeListener (WebsocketListener listener) {
			listeners.remove(listener);
		}

		public void clearListeners () {
			listeners.clear();
		}
	}

	// ---

	static int clamp (int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static float clamp (float value, float min, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public void main (String[] args) throws Exception {
		TRACE();
		DeCONZ deconz = new DeCONZ("9FE426D925", "192.168.0.90", 80, 1);
		deconz.getGateway().register("test app", null, false);

		System.out.println("Lights:");
		for (Light light : deconz.getLights().getAll())
			System.out.println(light.id + ": " + light.name);
		deconz.getLights().apply("3", new LightStateChange().on().brightness(1).transition(5));

		FullState state = deconz.getGateway().getFullState();
		System.out.println("Groups: " + state.groups.size());
		System.out.println("Lights: " + state.lights.size());

		deconz.getWebsocket().addListener(new WebsocketAdapter() {
			public void sensorChanged (String id) {
				System.out.println("Sensor changed: " + id);
			}
		});
		deconz.getWebsocket().open(deconz.getGateway().getConfig().websocketport);
	}
}
