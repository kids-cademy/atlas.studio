package com.kidscademy.atlas.studio.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

import com.kidscademy.atlas.studio.dao.StringsListConverter;

@Entity
public class AndroidApp implements GraphicObject
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private Date timestamp;
  @Transient
  private final String title = "Android App";
  @Transient
  private String name;
  @Transient
  private String display;
  @Transient
  private String definition;
  @Transient
  private MediaSRC iconSrc;

  @OneToOne
  private Release release;

  private Date buildTimestamp;
  private String packageName;
  private int versionCode;
  @Convert(converter = StringsListConverter.class)
  private List<String> languages;
  private URL gitRepository;
  private String gitUserName;
  private String gitPassword;

  public AndroidApp() {

  }

  @PostLoad
  public void postLoad() throws IOException {
    name = release.getName();
    display = release.getDisplay();
    definition = release.getDefinition();
    iconSrc = release.getIconSrc();
  }

  public int getId() {
    return id;
  }

  public Date getTimestamp() {
    return timestamp;
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

  @Override
  public String getDefinition() {
    return definition;
  }

  @Override
  public MediaSRC getIconSrc() {
    return iconSrc;
  }

  public Release getRelease() {
    return release;
  }

  public void setBuildTimestamp() {
    this.buildTimestamp = new Date();
  }

  public Date getBuildTimestamp() {
    return buildTimestamp;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getPackageName() {
    return packageName;
  }

  public int getVersionCode() {
    return versionCode;
  }

  public void setLanguages(List<String> languages) {
    this.languages = languages;
  }

  public void setLanguages(String... languages) {
    this.languages = Arrays.asList(languages);
  }

  public List<String> getLanguages() {
    return languages;
  }

  public void setGitRepository(URL gitRepository) {
    this.gitRepository = gitRepository;
  }

  public URL getGitRepository() {
    return gitRepository;
  }

  public void setGitUserName(String gitUserName) {
    this.gitUserName = gitUserName;
  }

  public String getGitUserName() {
    return gitUserName;
  }

  public void setGitPassword(String gitPassword) {
    this.gitPassword = gitPassword;
  }

  public String getGitPassword() {
    return gitPassword;
  }

  public File getDir() {
    return AndroidProject.appDir(name);
  }

  public AndroidProject getProject() {
    return new AndroidProject(name);
  }

  public static AndroidApp create(Release release) {
    AndroidApp app = new AndroidApp();
    app.timestamp = new Date();
    app.release = release;
    app.name = release.getName();
    app.packageName = "com.kidscademy.atlas." + app.name.replaceAll("-", "_");
    app.display = release.getDisplay();
    app.definition = release.getDefinition();
    app.iconSrc = release.getIconSrc();
    app.versionCode = 1;
    app.languages = Arrays.asList("EN");

    // although android application table has default value for build timestamp it is ignored
    // need to set it explicitly and also to take care to not be negative after DST processing
    // 86,400,000 = 24 * 3600 * 1000
    app.buildTimestamp = new Date(86400000L);

    return app;
  }
}
