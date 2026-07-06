package dev.finndog.configsearch.index;

import dev.finndog.configsearch.api.ConfigOptionEntry;

public record SearchResult(ConfigOptionEntry entry, int score) {
}
