package dev.celestiacraft.libs.wrapper.gson;

import com.google.gson.*;

import java.util.Map;
import java.util.Set;

public class JsonWrapper extends JsonElement {
	private final JsonObject object;

	public JsonWrapper() {
		object = new JsonObject();
	}

	public JsonWrapper(JsonObject json) {
		object = json;
	}

	@Override
	public JsonObject deepCopy() {
		JsonObject result = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			result.add(entry.getKey(), entry.getValue().deepCopy());
		}
		return result;
	}

	public void add(String property, JsonElement value) {
		object.add(property, value == null ? JsonNull.INSTANCE : value);
	}

	public JsonElement remove(String property) {
		return object.remove(property);
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
		return object.entrySet();
	}

	public Set<String> keySet() {
		return object.keySet();
	}

	public int size() {
		return object.size();
	}

	public boolean has(String memberName) {
		return object.asMap().containsKey(memberName);
	}

	public JsonElement get(String memberName) {
		return object.get(memberName);
	}

	public JsonPrimitive getAsJsonPrimitive(String memberName) {
		return (JsonPrimitive) object.get(memberName);
	}

	public JsonArray getAsJsonArray(String memberName) {
		return (JsonArray) object.get(memberName);
	}

	public JsonObject getAsJsonObject(String memberName) {
		return (JsonObject) object.get(memberName);
	}

	@Override
	public JsonObject getAsJsonObject() {
		return object;
	}

	@Override
	public boolean isJsonObject() {
		return true;
	}

	public Map<String, JsonElement> asMap() {
		return object.asMap();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this)
				|| (obj instanceof JsonWrapper
				&& ((JsonWrapper) obj).object.equals(object));
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	public JsonObject toJson() {
		return object.deepCopy();
	}
}