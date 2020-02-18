package com.kidscademy.atlas.studio.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.kidscademy.atlas.studio.util.Files;

@Entity
@Table(name = "atlasitem")
public class AtlasRelated implements RepositoryObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private AtlasCollection collection;

    private String name;
    private String display;
    private String iconName;
    private AtlasObject.State state;
    private Date lastUpdated;

    @ElementCollection
    @OrderColumn
    @CollectionTable(name = "atlasobject_related", joinColumns = @JoinColumn(name = "atlasobject_id"))
    private List<String> related;

    @Transient
    private MediaSRC iconSrc;

    @Transient
    private List<AtlasItem> relatedItems;
    
    @PostLoad
    public void postLoad() {
	if (iconName != null) {
	    iconSrc = Files.mediaSrc(this, iconName, "96x96");
	}
	for (@SuppressWarnings("unused") String relatedName : related) {
	    
	}
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public String getRepositoryName() {
	return collection.getName();
    }

    public int getId() {
	return id;
    }

    public AtlasCollection getCollection() {
	return collection;
    }

    public String getDisplay() {
	return display;
    }

    public String getIconName() {
        return iconName;
    }

    public AtlasObject.State getState() {
        return state;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }
}