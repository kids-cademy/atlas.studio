package com.kidscademy.atlas.studio.util;

import js.lang.Event;

public class ConsoleMessage implements Event {
    @SuppressWarnings("unused")
    private final String text;

    public ConsoleMessage(String text) {
	this.text = text;
    }
}
