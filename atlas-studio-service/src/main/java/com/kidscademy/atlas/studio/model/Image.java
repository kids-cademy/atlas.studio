package com.kidscademy.atlas.studio.model;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.kidscademy.atlas.studio.util.Files;

@Embeddable
@Cacheable
public class Image
{
  public static final String KEY_ICON = "icon";
  public static final String KEY_COVER = "cover";
  public static final String KEY_FEATURED = "featured";
  public static final String KEY_TRIVIA = "trivia";
  public static final String KEY_CONTEXTUAL = "contextual";
  public static final String KEY_RELEASE = "release";

  /** Image key is unique per atlas object. */
  @Column(insertable = false, updatable = false)
  private String imageKey;

  private Date timestamp;
  private Date uploadDate;
  private String source = "";
  private String caption;

  /**
   * Picture file name.
   * <p>
   * This fields contains only file name, including extension, but does not contain any path components. This is to avoid keeping path structure into database.
   * Path components are added when object is loaded from persistence by a specialized hook, from every concrete object class. Media file name plus path
   * components are a root-relative URL and is named media SRC; remember that root-relative URL is the URL path after server that starts from context.
   */
  private String fileName;

  private int fileSize;
  private int width;
  private int height;

  @Transient
  private MediaSRC src;

  @Transient
  private String path;

  public Image() {
  }

  /**
   * Test constructor.
   * 
   * @param src
   */
  public Image(MediaSRC src) {
    this.src = src;
    this.imageKey = Files.basename(src.fileName());
    this.uploadDate = new Date();
  }

  public void postLoad(RepositoryObject object) {
    src = Files.mediaSrc(object, fileName);
  }

  public void postMerge(Image source) {
    if(fileName == null && source.src != null) {
      fileName = source.src.fileName();
    }
  }

  public String getImageKey() {
    return imageKey;
  }

  public void setImageKey(String imageKey) {
    this.imageKey = imageKey;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp() {
    this.timestamp = new Date();
  }

  public Date getUploadDate() {
    return uploadDate;
  }

  public void setUploadDate(Date uploadDate) {
    this.uploadDate = uploadDate;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public int getFileSize() {
    return fileSize;
  }

  public void setFileSize(int fileSize) {
    this.fileSize = fileSize;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setSrc(MediaSRC src) {
    this.src = src;
  }

  public MediaSRC getSrc() {
    return src;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((imageKey == null) ? 0 : imageKey.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    Image other = (Image)obj;
    if(imageKey == null) {
      if(other.imageKey != null) return false;
    }
    else if(!imageKey.equals(other.imageKey)) return false;
    return true;
  }

  @Override
  public String toString() {
    return fileName;
  }

  public enum Kind
  {
    COLLECTION, OBJECT, LINK, RELEASE
  }
}
