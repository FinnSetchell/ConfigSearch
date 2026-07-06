package dev.finndog.configsearch.api;

import java.util.List;

public interface GlobalOptionProvider {
	List<ConfigOptionEntry> scanAll();
}
