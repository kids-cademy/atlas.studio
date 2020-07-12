package com.kidscademy.atlas.studio.model;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import js.util.Strings;

@Entity
@Table(name = "\"RELEASE\"")
public class Release implements GraphicObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Transient
    private final String title = "Release";

    private Date timestamp;
    private Date contentTimestamp;
    private String name;
    private String display;
    private String brief;
    private String definition;
    private String graphicsBackground;
    private String publisher;
    private String edition;
    private String version;
    private String license;
    private String readme;
    private String privacy;

    @Transient
    private MediaSRC iconSrc;

    @Transient
    private Map<String, MediaSRC> images;

    @PostLoad
    public void postLoad() throws IOException {
	iconSrc = new MediaSRC(Strings.concat("/media/release/", name, "/icon.png"));

	images = new HashMap<>();
	images.put("icon", new MediaSRC(Strings.concat("/media/release/", name, "/icon.png")));
	images.put("feature", new MediaSRC(Strings.concat("/media/release/", name, "/feature.png")));
	images.put("cover", new MediaSRC(Strings.concat("/media/release/", name, "/cover.png")));
    }

    @Override
    public int getId() {
	return id;
    }

    public boolean isPersisted() {
	return id != 0;
    }

    public void setTimestamp(Date timestamp) {
	this.timestamp = timestamp;
    }

    public Date getTimestamp() {
	return timestamp;
    }

    public Date getContentTimestamp() {
        return contentTimestamp;
    }

    public void setContentTimestamp(Date contentTimestamp) {
        this.contentTimestamp = contentTimestamp;
    }

    @Override
    public String getTitle() {
	return title;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public String getDisplay() {
	return display;
    }

    public String getBrief() {
	return brief;
    }

    @Override
    public String getDefinition() {
	return definition;
    }

    public void setGraphicsBackground(String graphicsBackground) {
	this.graphicsBackground = graphicsBackground;
    }

    public String getGraphicsBackground() {
	return graphicsBackground;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }

    public String getReadme() {
	return readme;
    }

    public String getPublisher() {
	return publisher;
    }

    public String getEdition() {
	return edition;
    }

    public String getVersion() {
	return version;
    }

    public String getLicense() {
	return license;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getPrivacy() {
	return privacy;
    }

    @Override
    public MediaSRC getIconSrc() {
	return iconSrc;
    }

    public Map<String, MediaSRC> getImages() {
	return images;
    }

    public static Release create() {
	Release release = new Release();
	release.graphicsBackground = "0000D3";
	release.publisher = "kids (a)cademy";
	release.edition = "community edition";
	release.version = "Release 1.0";
	release.license = "Apache License 2.0";
	return release;
    }
}
