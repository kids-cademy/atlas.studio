package com.kidscademy.atlas.studio.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

import com.kidscademy.atlas.studio.util.Files;

/**
 * Lightweight {@link AtlasObject} usually used as item in a collection. It has
 * only enough fields to be displayed as an icon with a name.
 * 
 * @author Iulian Rotaru
 */
@Entity
public class AtlasItem implements CollectionObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String collectionName;
    private String name;
    private String display;
    
    /**
     * Media file name for object icon. Object icon has a small dimension and has
     * 1:1 ratio; usually is 96x96 pixels. This field is optional and can be null.
     */
    private String iconName;

    @Transient
    private MediaSRC iconSrc;

    public AtlasItem() {
    }

    public AtlasItem(String collectionName, String name) {
	this.collectionName = collectionName;
	this.name = name;
    }

    /**
     * Test constructor.
     * 
     * @param id
     * @param name
     */
    public AtlasItem(int id, String collectionName, String name) {
	this.id = id;
	this.collectionName = collectionName;
	this.name = name;
    }

    @PostLoad
    public void postLoad() {
	if (iconName != null) {
	    iconSrc = Files.mediaSrc(this, iconName, "96x96");
	}
    }

    public int getId() {
	return id;
    }

    @Override
    public String getCollectionName() {
	return collectionName;
    }

    @Override
    public String getName() {
	return name;
    }

    public String getDisplay() {
	return display;
    }

    public String getIconName() {
	return iconName;
    }

    public MediaSRC getIconSrc() {
	return iconSrc;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + id;
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	AtlasItem other = (AtlasItem) obj;
	if (id != other.id)
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return display;
    }
}