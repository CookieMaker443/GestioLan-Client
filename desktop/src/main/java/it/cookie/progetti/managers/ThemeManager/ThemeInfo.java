package it.cookie.progetti.managers.ThemeManager;

public record ThemeInfo(String id, String name, String author, String description,
                        String cssUrl, boolean builtin) {}