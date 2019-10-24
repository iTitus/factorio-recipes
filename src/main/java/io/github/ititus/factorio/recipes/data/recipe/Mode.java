package io.github.ititus.factorio.recipes.data.recipe;

import java.util.Locale;

public enum Mode {

    NORMAL("normal"), EXPENSIVE("expensive");

    private static final String NORMAL_STRING = "normal";
    private static final String EXPENSIVE_STRING = "expensive";

    private final String name;

    Mode(String name) {
        this.name = name;
    }

    public static Mode get(String name) {
        switch (name.toLowerCase(Locale.ENGLISH)) {
            case NORMAL_STRING:
                return NORMAL;
            case EXPENSIVE_STRING:
                return EXPENSIVE;
            default:
                throw new IllegalArgumentException();
        }
    }

    public String getName() {
        return name;
    }
}
