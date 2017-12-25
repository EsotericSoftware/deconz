
package com.esotericsoftware.deconz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.esotericsoftware.deconz.Group.GroupAttributes;
import com.esotericsoftware.deconz.Group.GroupSceneAttributes;
import com.esotericsoftware.deconz.Scene.SceneLightChange;
import com.esotericsoftware.jsonbeans.JsonReader;
import com.esotericsoftware.jsonbeans.JsonValue;

/** Modifies the scenes in deCONZ to match the scene data from a deCONZ database file. This is useful if deCONZ forgets a light
 * and TouchLink must be used to get the light back. TouchLink resets the light, losing the scenes, but the scene data is still in
 * the deCONZ database. */
public class ScenesFromSQL {
	final Connection connection;
	final DeCONZ deconz;

	public ScenesFromSQL (String deconzHost, int deconzRestPort, String sqliteFile) throws Exception {
		connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);

		deconz = new DeCONZ("ScenesFromSQL", deconzHost, deconzRestPort, 1);
		String apiKey = deconz.getGateway().register("ScenesFromSQL", "ScenesFromSQL");
		deconz.setApiKey(apiKey);

		ArrayList<GroupAttributes> groups = deconz.getGateway().getFullState().groups;
		for (GroupAttributes group : groups) {
			System.out.print(group.name + ": ");

			String groupID;
			try (Statement statement = connection.createStatement()) {
				ResultSet set = statement.executeQuery("SELECT * FROM groups WHERE state='normal' AND name='" + group.name + "'");
				if (!set.next()) {
					System.out.println("not found");
					continue;
				}
				groupID = set.getString("gid");
			}
			String intGroupID = intID(groupID);
			System.out.println(groupID + " (" + intGroupID + ")");

			for (GroupSceneAttributes scene : group.scenes) {
				System.out.print("   " + scene.name + ": ");
				String lights;
				try (Statement statement = connection.createStatement()) {
					ResultSet set = statement
						.executeQuery("SELECT * FROM scenes WHERE gid='" + groupID + "' AND name='" + scene.name + "'");
					if (!set.next()) {
						System.out.println("not found");
						continue;
					}
					lights = set.getString("lights");
				}

				JsonValue root = new JsonReader().parse(lights);
				System.out.println(root.size + " lights");
				if (root.size == 0) continue;
				for (JsonValue entry = root.child; entry != null; entry = entry.next) {
					String lightID = entry.getString("lid");
					SceneLightChange change = new SceneLightChange();
					change.brightness(entry.getInt("bri"));
					change.on(entry.getBoolean("on"));
					change.transition(entry.getInt("tt") / 10f);

					String colorModel = entry.getString("cm");
					if (!colorModel.equals("none")) {
						change.cie(entry.getInt("x") / 65279f, entry.getInt("y") / 65279f);
						if (colorModel.equals("ct")) {
							change.colorTemp(entry.getInt("ct"));
						}
					}
					// System.out.println(deconz.getLights().get(lightID).name + " " + change.buffer);

					try {
						deconz.getScenes().apply(intGroupID, scene.id, lightID, change);
					} catch (Exception ex) {
						// Probably the scene doesn't contain the light.
						System.out.println("      Retry: " + deconz.getLights().get(lightID).name);
						deconz.getScenes().store(intGroupID, scene.id);
						Thread.sleep(750);
						deconz.getScenes().apply(intGroupID, scene.id, lightID, change);
					}
					// Thread.sleep(200);
				}
			}
		}
	}

	String intID (String id) {
		return Integer.parseInt(id.replace("0x", ""), 16) + "";
	}

	static public void main (String[] args) throws Exception {
		if (args.length == 0)
			System.out.println("Usage: [deconzHost] [deconzRestPort] [sqliteFile]");
		else
			new ScenesFromSQL(args[0], Integer.parseInt(args[1]), args[2]);
	}
}
