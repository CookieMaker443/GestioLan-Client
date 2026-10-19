package it.cookie.progetti.managers.ThemeManager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ThemeManifest(String name, String author, String version, String description, String css) {}
