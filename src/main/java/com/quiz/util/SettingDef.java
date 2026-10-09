package com.quiz.util;

/** Describes one editable system setting (used to render the settings form). */
public class SettingDef {

    private final String key;
    private final String label;
    private final String help;
    private final String type;       // text | number | boolean
    private final String defaultValue;

    public SettingDef(String key, String label, String help, String type, String defaultValue) {
        this.key = key;
        this.label = label;
        this.help = help;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public String getHelp() { return help; }
    public String getType() { return type; }
    public String getDefaultValue() { return defaultValue; }
}
