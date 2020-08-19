package com.kidscademy.atlas.studio.model;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostRemove;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import com.kidscademy.atlas.studio.dao.StringsListConverter;
import com.kidscademy.atlas.studio.tool.AudioProcessor;
import com.kidscademy.atlas.studio.tool.AudioSampleInfo;
import com.kidscademy.atlas.studio.util.Files;

import js.tiny.container.annotation.TestConstructor;
import js.util.Params;

@Entity
public class AtlasObject implements GraphicObject, RepositoryObject, HDateRange
{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  private AtlasCollection collection;

  @Enumerated(EnumType.STRING)
  private State state;

  @Transient
  private final String title = "Atlas Object";

  /** Last change timestamp. */
  private Date timestamp;

  /**
   * Object name unique per its parent collection. This value is used internally and is not meant to be displayed to user.
   */
  private String name;

  /**
   * Object name as displayed on user interface. It is subject to internationalization.
   */
  private String display;

  @Convert(converter = StringsListConverter.class)
  private List<String> aliases;

  private String definition;

  /**
   * Object description is rich text, that is, it can contains images, links and text formatting. It is stored as HTML.
   */
  private String description;

  /**
   * Pictures associated with this object. There are three kinds of pictures: object icon, one about object itself and one with object in its natural context.
   * <p>
   * Object icon has a small dimension and has 1:1 ratio; usually is 96x96 pixels.
   * <p>
   * Object picture focused on object only and has transparent background. For this reason it is of PNG type. It has fixed width - usually 560 pixels, but
   * variable height to accommodate picture content, hence aspect ratio is variable too.
   * <p>
   * Contextual picture present object in its context, e.g. for a musical instrument it contains a player using the instrument. It has 16:9 aspect ration and
   * usually is 920x560 pixels.
   */
  @ElementCollection
  @MapKeyColumn(name = "imageKey")
  private Map<String, Image> images;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "atlasobject_id")
  @OrderColumn
  private List<Taxon> taxonomy;

  /** Optional object spreading, empty list if not applicable. */
  @ElementCollection
  @OrderColumn
  private List<Region> spreading;

  @Embedded
  @AttributeOverrides(
  {
      @AttributeOverride(name = "value", column = @Column(name = "startDateValue")), @AttributeOverride(name = "mask", column = @Column(name = "startDateMask"))
  })
  private HDate startDate;

  @Embedded
  @AttributeOverrides(
  {
      @AttributeOverride(name = "value", column = @Column(name = "endDateValue")), @AttributeOverride(name = "mask", column = @Column(name = "endDateMask"))
  })
  private HDate endDate;

  /**
   * Progenitor is the entity that somehow originates this object. For example, if this atlas object is an airplaine, progenitor is the manufacturer. Null if
   * not used. It is usualy displayed on info box.
   */
  private String progenitor;

  /**
   * Optional conservation status for life forms. Null if not used. It is usualy displayed on info box.
   */
  private ConservationStatus conservation;

  /**
   * Audio sample title is the name of the audio work from which sample is extracted.
   */
  private String sampleTitle;

  /**
   * Media file name for audio sample. This audio sequence contains a sample from an audio work relevant for this object. It usually has around 30 seconds, but
   * not strictly. See {@link #pictureName} for details about media file name.
   */
  private String sampleName;

  /**
   * Media file name for audio sample waveform. Waveform is generated by audio processor and has transparent background - see
   * {@link AudioProcessor#generateWaveform(java.io.File, java.io.File)} for details about dimension and aspect ratio. See {@link #pictureName} for details
   * about media file name.
   */
  private String waveformName;

  @Transient
  private String samplePath;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "atlasobject_id")
  @OrderColumn
  private List<Fact> facts;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "atlasobject_id")
  @OrderColumn
  private List<Feature> features;

  @ElementCollection
  @OrderColumn
  private List<Link> links;

  @ElementCollection
  @OrderColumn
  private List<String> related;

  @Transient
  private AudioSampleInfo sampleInfo;

  /**
   * Root-relative media SRC for object audio sample. It contains root-relative path from repository context to media file and always starts with path
   * separator, e.g. <code>/media/atlas/instrument/accordion/sample.mp3</code>.
   * <p>
   * This field is used by user interface to actually render media file and is not persisted. It is initialized by {@link #postLoad()} immediately after this
   * atlas object instance loaded from persistence. When atlas object entity is saved to persistence this field is used to initialize related media file name,
   * see {@link #postMerge(AtlasObject)}.
   */
  @Transient
  private MediaSRC sampleSrc;

  /**
   * Root-relative media SRC for object audio waveform. See {@link #sampleSrc}.
   */
  @Transient
  private MediaSRC waveformSrc;

  @Transient
  private MediaSRC iconSrc;

  public AtlasObject() {
  }

  @TestConstructor
  public AtlasObject(AtlasCollection collection) {
    Params.notNull(collection, "Atlas collection");
    this.collection = collection;
    this.images = Collections.emptyMap();
    this.features = Collections.emptyList();
    this.links = Collections.emptyList();
  }

  /**
   * Hook executed after {@link EntityManager.merge()} to initialize media file names from related root-relative media SRC. This method is executed into a
   * persistent context while this atlas object instance is managed, aka attached to context. Source atlas object has media SRC properly initialized; it is not
   * attached to persistence context.
   * 
   * @param sourceObject unmanaged source atlas object.
   */
  public void postMerge(AtlasObject sourceObject) {
    if(images != null) {
      for(Map.Entry<String, Image> entry : images.entrySet()) {
        entry.getValue().postMerge(sourceObject.getImage(entry.getKey()));
      }
    }

    if(sampleName == null && sourceObject.sampleSrc != null) {
      sampleName = sourceObject.sampleSrc.fileName();
    }
    if(waveformName == null && sourceObject.waveformSrc != null) {
      waveformName = sourceObject.waveformSrc.fileName();
    }
  }

  /**
   * Persistence event listener executed before {@link EntityManager#persist(Object)}. It serves the same purpose as {@link #postMerge(AtlasObject)} but is
   * enacted when atlas object is created into database.
   * 
   * @see #postMerge(AtlasObject)
   */
  @PrePersist
  public void preSave() {
    // reuse merge logic but with both SRC and file name from not managed entity
    // instance is not managed yet since it is not yet persisted
    postMerge(this);
  }

  /**
   * Persistence event listener executed just after entity instance is loaded from database. It initializes root-relative media SRC used by web user interface.
   * Delegates {@link Files#mediaSrc(AtlasObject, String)} that knows about media repository context.
   */
  @PostLoad
  public void postLoad() {
    // database contains only media file names; convert to root-relative URLs, aka
    // SRC
    for(Image picture : images.values()) {
      picture.postLoad(this);
    }

    for(Feature feature : features) {
      feature.postLoad();
    }

    for(Link link : links) {
      link.postLoad();
    }

    Image icon = images.get(Image.KEY_ICON);
    iconSrc = icon != null ? icon.getSrc() : null;
    sampleSrc = Files.mediaSrc(this, sampleName);
    waveformSrc = Files.mediaSrc(this, waveformName);
  }

  @PostRemove
  public void postRemove() throws IOException {
    File mediaDir = Files.objectDir(this);
    if(mediaDir.exists()) {
      Files.removeFilesHierarchy(mediaDir).delete();
    }
  }

  /**
   * For testing
   * 
   * @param id
   */
  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public int getId() {
    return id;
  }

  public void setCollection(AtlasCollection collection) {
    Params.notNull(collection, "Atlas collection");
    this.collection = collection;
  }

  public AtlasCollection getCollection() {
    return collection;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void updateTimestamp() {
    this.timestamp = new Date();
  }

  @Override
  public String getRepositoryName() {
    return collection.getName();
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Image getImage(String imageKey) {
    // image key is a public constant, e.g. Image.KEY_ICON
    return images != null ? images.get(imageKey) : null;
  }

  public Map<String, Image> getImages() {
    return images;
  }

  public void setImages(Map<String, Image> pictures) {
    this.images = pictures;
  }

  @Override
  public MediaSRC getIconSrc() {
    return iconSrc;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public void setAliases(List<String> aliases) {
    this.aliases = aliases;
  }

  @Override
  public String getDisplay() {
    return display;
  }

  public void setDisplay(String display) {
    this.display = display;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Taxon> getTaxonomy() {
    return taxonomy;
  }

  public void setTaxonomy(List<Taxon> taxonomy) {
    this.taxonomy = taxonomy;
  }

  public List<Feature> getFeatures() {
    return features;
  }

  public void setFeatures(List<Feature> features) {
    this.features = features;
  }

  public List<Region> getSpreading() {
    return spreading;
  }

  public void setSpreading(List<Region> spreading) {
    this.spreading = spreading;
  }

  public List<Fact> getFacts() {
    return facts;
  }

  public void setFacts(List<Fact> facts) {
    this.facts = facts;
  }

  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }

  public List<String> getRelated() {
    return related;
  }

  public void setRelated(List<String> related) {
    this.related = related;
  }

  public boolean isPublished() {
    return state == State.PUBLISHED;
  }

  public String getSampleTitle() {
    return sampleTitle;
  }

  public void setSampleTitle(String sampleTitle) {
    this.sampleTitle = sampleTitle;
  }

  public String getSampleName() {
    return sampleName;
  }

  public void setSampleName(String sampleName) {
    this.sampleName = sampleName;
  }

  public String getWaveformName() {
    return waveformName;
  }

  public void setWaveformName(String waveformPath) {
    this.waveformName = waveformPath;
  }

  public MediaSRC getSampleSrc() {
    return sampleSrc;
  }

  public void setSampleSrc(MediaSRC sampleSrc) {
    this.sampleSrc = sampleSrc;
  }

  public MediaSRC getWaveformSrc() {
    return waveformSrc;
  }

  public void setWaveformSrc(MediaSRC waveformSrc) {
    this.waveformSrc = waveformSrc;
  }

  public String getSamplePath() {
    return samplePath;
  }

  public void setSamplePath(String samplePath) {
    this.samplePath = samplePath;
  }

  public void setSampleInfo(AudioSampleInfo sampleInfo) {
    this.sampleInfo = sampleInfo;
  }

  public AudioSampleInfo getSampleInfo() {
    return sampleInfo;
  }

  public HDate getStartDate() {
    return startDate;
  }

  public void setStartDate(HDate startDate) {
    this.startDate = startDate;
  }

  public HDate getEndDate() {
    return endDate;
  }

  public void setEndDate(HDate endDate) {
    this.endDate = endDate;
  }

  public String getProgenitor() {
    return progenitor;
  }

  public void setProgenitor(String progenitor) {
    this.progenitor = progenitor;
  }

  public ConservationStatus getConservation() {
    return conservation;
  }

  public void setConservation(ConservationStatus conservation) {
    this.conservation = conservation;
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
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    AtlasObject other = (AtlasObject)obj;
    if(id != other.id) return false;
    return true;
  }

  @Override
  public String toString() {
    return name;
  }

  public enum State
  {
    // ENUM('NONE', 'CREATED', 'DEVELOPMENT', 'RELEASED', 'PUBLISHED')
    NONE, CREATED, DEVELOPMENT, RELEASED, PUBLISHED
  }

  public static AtlasObject create(AtlasCollection collection) {
    AtlasObject object = new AtlasObject();
    object.collection = collection;
    object.state = AtlasObject.State.CREATED;

    StringBuilder description = new StringBuilder();
    description.append("<text>");
    for(DescriptionMeta descriptionMeta : collection.getDescriptionMeta()) {
      description.append("<section name='");
      description.append(descriptionMeta.getName());
      description.append("'></section>");
    }
    description.append("</text>");
    object.description = description.toString();

    object.aliases = Collections.emptyList();
    object.images = Collections.emptyMap();
    object.spreading = Collections.emptyList();
    object.facts = Collections.emptyList();
    object.links = Collections.emptyList();
    object.related = Collections.emptyList();
    return object;
  }
}
