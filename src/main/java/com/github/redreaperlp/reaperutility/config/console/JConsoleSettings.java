package com.github.redreaperlp.reaperutility.config.console;

public class JConsoleSettings {

    private boolean coloredConsole;

    public JConsoleSettings() {
        this.coloredConsole = false;
    }

    public boolean coloredConsole() {
        return coloredConsole;
    }

    public JConsoleSettings coloredConsole(boolean coloredConsole) {
        this.coloredConsole = coloredConsole;
        return this;
    }
}
