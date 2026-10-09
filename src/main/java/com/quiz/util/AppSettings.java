package com.quiz.util;

import com.quiz.dao.SettingsDAO;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of the system_settings table.
 * Reads are lock-free ({@link ConcurrentHashMap}); a reload is synchronized so two
 * threads never refresh at once. Falls back to defaults if the database is unreachable.
 */
public final class AppSettings {

    /** All known settings with their defaults, in display order. */
    private static final List<SettingDef> DEFINITIONS = List.of(
            new SettingDef("platform_name", "Platform name", "Shown in the header and on the login page.", "text", "JavaQuiz Arena"),
            new SettingDef("allow_registration", "Allow public sign-up", "Let new participants create their own account.", "boolean", "true"),
            new SettingDef("max_attempts", "Max attempts per quiz", "How many times one participant may take the same quiz.", "number", "3"),
            new SettingDef("default_duration", "Default quiz duration (min)", "Pre-filled when a creator makes a new quiz.", "number", "15"),
            new SettingDef("pass_percentage", "Pass mark (%)", "Attempts at or above this percentage are marked as passed.", "number", "40"),
            new SettingDef("announcement", "Announcement banner", "Optional message shown to everyone. Leave empty to hide.", "text", "")
    );

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    private static volatile boolean loaded;

    private AppSettings() {
    }

    public static List<SettingDef> definitions() {
        return DEFINITIONS;
    }

    /** Reloads the cache from the database (synchronized: one refresh at a time). */
    public static synchronized void reload() {
        try {
            Map<String, String> fresh = new SettingsDAO().findAll();
            CACHE.clear();
            CACHE.putAll(fresh);
            loaded = true;
        } catch (SQLException e) {
            // keep whatever we had; defaults will be used for missing keys
            loaded = true;
        }
    }

    public static String get(String key) {
        if (!loaded) {
            reload();
        }
        String value = CACHE.get(key);
        if (value != null) {
            return value;
        }
        for (SettingDef d : DEFINITIONS) {
            if (d.getKey().equals(key)) {
                return d.getDefaultValue();
            }
        }
        return "";
    }

    public static int getInt(String key) {
        try {
            return Integer.parseInt(get(key).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean getBoolean(String key) {
        return "true".equalsIgnoreCase(get(key).trim());
    }

    /** Snapshot for the settings page. */
    public static Map<String, String> snapshot() {
        if (!loaded) {
            reload();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (SettingDef d : DEFINITIONS) {
            map.put(d.getKey(), get(d.getKey()));
        }
        return Collections.unmodifiableMap(map);
    }
}
