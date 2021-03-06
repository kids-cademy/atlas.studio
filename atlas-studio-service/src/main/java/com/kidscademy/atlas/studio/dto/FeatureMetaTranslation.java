package com.kidscademy.atlas.studio.dto;

import com.kidscademy.atlas.studio.model.FeatureMeta;

public class FeatureMetaTranslation
{
  private int id;
  private String name;
  private String display;
  private String translation;

  public FeatureMetaTranslation() {
  }

  public FeatureMetaTranslation(FeatureMeta featureMeta, String translation) {
    this.id = featureMeta.getId();
    this.name = featureMeta.getName();
    this.display = featureMeta.getDisplay();
    this.translation = translation;
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

  public String getTranslation() {
    return translation;
  }
}
