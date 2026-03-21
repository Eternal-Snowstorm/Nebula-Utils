package dev.celestiacraft.libs.wrapper;

import lombok.Getter;

import java.util.Collection;
import java.util.List;

public class ReplaceRule {
	private final List<String> match;
	@Getter
	private final String result;

	public ReplaceRule(Object match, String result) {
		if (match instanceof String string) {
			this.match = List.of(string);
		} else if (match instanceof Collection<?> collection) {
			this.match = collection.stream().map(Object::toString).toList();
		} else {
			throw new IllegalArgumentException("Invalid match type");
		}
		this.result = result;
	}

	public boolean matches(String string) {
		return match.contains(string);
	}
}