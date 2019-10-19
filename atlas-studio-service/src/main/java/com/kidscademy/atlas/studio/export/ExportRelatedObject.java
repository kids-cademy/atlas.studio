package com.kidscademy.atlas.studio.export;

@SuppressWarnings("unused")
public class ExportRelatedObject {
    private final int index;
    private final String display;
    private final String definition;
    private final String iconPath;

    public ExportRelatedObject(ExportItem item) {
	this.index = item.getIndex();
	this.display = item.getDisplay();
	this.definition = item.getDefinition();
	this.iconPath = item.getIconPath();
    }

    public String getDisplay() {
        return display;
    }
}
