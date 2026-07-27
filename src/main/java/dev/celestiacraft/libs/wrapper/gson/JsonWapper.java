package dev.celestiacraft.libs.wrapper.gson;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;

import java.util.Map;
import java.util.Set;

public class JsonWapper extends JsonElement {
	private final LinkedTreeMap<String, JsonElement> members = new LinkedTreeMap<>(false);

	@SuppressWarnings("deprecation")
	public JsonWapper() {
	}

	@Override
	public JsonObject deepCopy() {
		JsonObject result = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
			result.add(entry.getKey(), entry.getValue().deepCopy());
		}
		return result;
	}

	public void add(String property, JsonElement value) {
		members.put(property, value == null ? JsonNull.INSTANCE : value);
	}

	public JsonElement remove(String property) {
		return members.remove(property);
	}

	public void addStringProperty(String property, String value) {
		add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
	}

	public void addNumberProperty(String property, Number value) {
		add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
	}

	public void addBooleanProperty(String property, Boolean value) {
		add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
	}

	public void addCharacterProperty(String property, Character value) {
		add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
	}

	public Set<Map.Entry<String, JsonElement>> entrySet() {
		return members.entrySet();
	}

	public Set<String> keySet() {
		return members.keySet();
	}

	public int size() {
		return members.size();
	}

	public boolean has(String memberName) {
		return members.containsKey(memberName);
	}

	public JsonElement get(String memberName) {
		return members.get(memberName);
	}

	public JsonPrimitive getAsJsonPrimitive(String memberName) {
		return (JsonPrimitive) members.get(memberName);
	}

	public JsonArray getAsJsonArray(String memberName) {
		return (JsonArray) members.get(memberName);
	}

	public JsonObject getAsJsonObject(String memberName) {
		return (JsonObject) members.get(memberName);
	}

	public Map<String, JsonElement> asMap() {
		return members;
	}

	@Override
	public boolean equals(Object object) {
		return (object == this)
				|| (object instanceof JsonWapper
				&& ((JsonWapper) object).members.equals(members));
	}

	@Override
	public int hashCode() {
		return members.hashCode();
	}
}