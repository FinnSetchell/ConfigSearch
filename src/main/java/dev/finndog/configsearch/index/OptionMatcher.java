package dev.finndog.configsearch.index;

import dev.finndog.configsearch.api.ConfigOptionEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;

public final class OptionMatcher {
	private static final int SCORE_NAME_PREFIX = 32;
	private static final int SCORE_NAME_WORD_START = 16;
	private static final int SCORE_NAME_CONTAINS = 8;
	private static final int SCORE_CATEGORY_CONTAINS = 4;
	private static final int SCORE_DESCRIPTION_CONTAINS = 2;
	private static final int SCORE_MOD_NAME_CONTAINS = 1;
	private static final char FORMATTING_MARKER = 167;

	private OptionMatcher() {
	}

	public static List<SearchResult> match(List<ConfigOptionEntry> entries, String query) {
		List<String> tokens = tokenize(query);
		if (tokens.isEmpty()) {
			return List.of();
		}
		List<SearchResult> results = new ArrayList<>();
		for (ConfigOptionEntry entry : entries) {
			int score = score(entry, tokens);
			if (score > 0) {
				results.add(new SearchResult(entry, score));
			}
		}
		results.sort(Comparator.comparingInt(SearchResult::score).reversed()
			.thenComparing(result -> normalize(result.entry().optionName().getString())));
		return List.copyOf(results);
	}

	private static List<String> tokenize(String query) {
		String normalized = normalize(query).strip();
		if (normalized.isEmpty()) {
			return List.of();
		}
		return List.of(normalized.split("\\s+"));
	}

	private static int score(ConfigOptionEntry entry, List<String> tokens) {
		String name = normalize(entry.optionName().getString());
		String category = normalize(entry.categoryPath().stream()
			.map(Component::getString)
			.collect(Collectors.joining(" ")));
		String description = entry.description() == null ? "" : normalize(entry.description().getString());
		String modName = normalize(entry.modName().getString());
		int total = 0;
		for (String token : tokens) {
			int tokenScore = scoreToken(token, name, category, description, modName);
			if (tokenScore == 0) {
				return 0;
			}
			total += tokenScore;
		}
		return total;
	}

	private static int scoreToken(String token, String name, String category, String description, String modName) {
		if (name.startsWith(token)) {
			return SCORE_NAME_PREFIX;
		}
		if (matchesWordStart(name, token)) {
			return SCORE_NAME_WORD_START;
		}
		if (name.contains(token)) {
			return SCORE_NAME_CONTAINS;
		}
		if (category.contains(token)) {
			return SCORE_CATEGORY_CONTAINS;
		}
		if (description.contains(token)) {
			return SCORE_DESCRIPTION_CONTAINS;
		}
		if (modName.contains(token)) {
			return SCORE_MOD_NAME_CONTAINS;
		}
		return 0;
	}

	private static boolean matchesWordStart(String text, String token) {
		int index = text.indexOf(token);
		while (index > 0) {
			if (!Character.isLetterOrDigit(text.charAt(index - 1))) {
				return true;
			}
			index = text.indexOf(token, index + 1);
		}
		return false;
	}

	private static String normalize(String text) {
		StringBuilder stripped = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == FORMATTING_MARKER) {
				i++;
				continue;
			}
			stripped.append(c);
		}
		return stripped.toString().toLowerCase(Locale.ROOT);
	}
}
