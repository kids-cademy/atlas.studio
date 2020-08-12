package com.kidscademy.atlas.studio.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import js.tiny.container.annotation.TestConstructor;
import js.util.Strings;

@Embeddable
public class TaxonMeta {
    private String name;
    private String display;
    
    /**
     * Comma separated values allowed for this taxon or null if input is not
     * constrained. User interface should use appropriate widgets: a select for list
     * of values or free text input if this allowed values list is null.
     */
    @Column(name = "\"VALUES\"")
    private String values;

    public TaxonMeta() {
    }

    @TestConstructor
    public TaxonMeta(String name) {
	this.name = name;
	this.display = Strings.toTitleCase(name);
    }

    @TestConstructor
    public TaxonMeta(String name, String values) {
	this.name = name;
	this.display = Strings.toTitleCase(name);
	this.values = values;
    }

    public String getName() {
	return name;
    }

    public String getDisplay() {
        return display;
    }

    public String getValues() {
	return values;
    }
}
