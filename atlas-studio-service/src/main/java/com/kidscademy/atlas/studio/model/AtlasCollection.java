package com.kidscademy.atlas.studio.model;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

import com.kidscademy.atlas.studio.util.Files;

/**
 * Atlas collection groups together {@link AtlasObject} that have something in
 * common. It is the primary mean to distribute atlas content. An atlas
 * application release contains a collection.
 * 
 * @author Iulian Rotaru
 */
@Entity
@Cacheable
public class AtlasCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String display;
    private String definition;
    private String iconName;

    @ElementCollection
    @OrderColumn
    private List<TaxonMeta> taxonomyMeta;

    private Flags flags;

    @Transient
    private MediaSRC iconSrc;

    public AtlasCollection() {
    }

    /**
     * Test constructor.
     * 
     * @param id
     * @param name
     */
    public AtlasCollection(int id, String name) {
	this.id = id;
	this.name = name;
    }

    @PostLoad
    public void postLoad() {
	iconSrc = Files.collectionSrc(iconName);
    }

    public int getId() {
	return id;
    }

    public String getName() {
	return name;
    }

    public String getDisplay() {
	return display;
    }

    public String getDefinition() {
	return definition;
    }

    public String getIconName() {
	return iconName;
    }

    public MediaSRC getIconSrc() {
	return iconSrc;
    }

    public List<TaxonMeta> getTaxonomyMeta() {
	return taxonomyMeta;
    }

    public Flags getFlags() {
	return flags;
    }
}
