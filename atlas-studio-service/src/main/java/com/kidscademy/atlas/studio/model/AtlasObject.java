package com.kidscademy.atlas.studio.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import javax.sound.midi.Instrument;

import com.kidscademy.atlas.studio.tool.AudioProcessor;
import com.kidscademy.atlas.studio.tool.AudioSampleInfo;
import com.kidscademy.atlas.studio.util.Files;

@Entity
public class AtlasObject implements CollectionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    protected User user;

    @ManyToOne
    private AtlasCollection collection;

    /** Object state, for now only in development and published. */
    private State state;

    /** Last change timestamp. */
    private Date lastUpdated;

    private int rank;

    /**
     * Object name unique per its parent collection. This value is used internally
     * and is not meant to be displayed to user.
     */
    private String name;

    /**
     * Object name as displayed on user interface. It is subject to
     * internationalization.
     */
    private String display;

    private String definition;

    /**
     * Object description is rich text, that is, it can contains images, links and
     * text formatting. It is stored as HTML.
     */
    private String description;

    /**
     * Pictures associated with this object. There are three kinds of pictures:
     * object icon, one about object itself and one with object in its natural
     * context.
     * <p>
     * Object icon has a small dimension and has 1:1 ratio; usually is 96x96 pixels.
     * <p>
     * Object picture focused on object only and has transparent background. For
     * this reason it is of PNG type. It has fixed width - usually 560 pixels, but
     * variable height to accommodate picture content, hence aspect ratio is
     * variable too.
     * <p>
     * Contextual picture present object in its context, e.g. for a musical
     * instrument it contains a player using the instrument. It has 16:9 aspect
     * ration and usually is 920x560 pixels.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn
    @OrderColumn
    private List<Image> images;

    @ElementCollection
    private Map<String, String> classification;

    @ElementCollection
    private List<String> aliases;

    /** Optional object spreading, empty list if not applicable. */
    @ElementCollection
    private List<Region> spreading;

    @Embedded
    @AttributeOverrides({ @AttributeOverride(name = "value", column = @Column(name = "start_date_value")),
	    @AttributeOverride(name = "mask", column = @Column(name = "start_date_mask")) })
    private HDate startDate;

    @Embedded
    @AttributeOverrides({ @AttributeOverride(name = "value", column = @Column(name = "end_date_value")),
	    @AttributeOverride(name = "mask", column = @Column(name = "end_date_mask")) })
    private HDate endDate;

    /**
     * Audio sample title is the name of the instrumental work from which sample is
     * extracted.
     */
    private String sampleTitle;

    /**
     * Media file name for instrument audio sample. This audio sequence contains a
     * sample from an instrumental work relevant for this instrument. It usually has
     * around 30 seconds, but not strictly. See {@link #pictureName} for details
     * about media file name.
     */
    private String sampleName;

    /**
     * Media file name for audio sample waveform. Waveform is generated by audio
     * processor and has transparent background - see
     * {@link AudioProcessor#generateWaveform(java.io.File, java.io.File)} for
     * details about dimension and aspect ratio. See {@link #pictureName} for
     * details about media file name.
     */
    private String waveformName;

    @ElementCollection
    private Map<String, String> facts;

    @ElementCollection
    private Map<String, String> features;

    @ElementCollection
    private List<Link> links;

    @ElementCollection
    private List<String> related;

    @Transient
    private AudioSampleInfo sampleInfo;

    /**
     * Root-relative media SRC for object audio sample. It contains root-relative
     * path from repository context to media file and always starts with path
     * separator, e.g. <code>/media/atlas/instrument/accordion/sample.mp3</code>.
     * <p>
     * This field is used by user interface to actually render media file and is not
     * persisted. It is initialized by {@link #postLoad()} immediately after this
     * instrument instance loaded from persistence. When instrument entity is saved
     * to persistence this field is used to initialize related media file name, see
     * {@link #postMerge(Instrument)}.
     */
    @Transient
    private MediaSRC sampleSrc;

    /**
     * Root-relative media SRC for object audio waveform. See {@link #sampleSrc}.
     */
    @Transient
    private MediaSRC waveformSrc;

    public AtlasObject() {
    }

    /**
     * Test constructor.
     * 
     * @param collection
     */
    public AtlasObject(AtlasCollection collection) {
	this.collection = collection;
    }

    /**
     * Hook executed after {@link EntityManager.merge()} to initialize media file
     * names from related root-relative media SRC. This method is executed into a
     * persistent context while this instrument instance is managed, aka attached to
     * context. Source instrument has media SRC properly initialized; it is not
     * attached to persistence context.
     * 
     * @param source
     *            unmanaged source atlas object.
     * @see Instrument
     */
    public void postMerge(AtlasObject source) {
	if (images != null) {
	    for (int i = 0; i < images.size(); ++i) {
		images.get(i).postMerge(source.getImages().get(i));
	    }
	}

	if (links != null) {
	    for (int i = 0; i < links.size(); ++i) {
		links.get(i).postMerge(source.links.get(i));
	    }
	}

	if (sampleName == null && source.sampleSrc != null) {
	    sampleName = source.sampleSrc.fileName();
	}
	if (waveformName == null && source.waveformSrc != null) {
	    waveformName = source.waveformSrc.fileName();
	}
    }

    /**
     * Persistence event listener executed before
     * {@link EntityManager#persist(Object)}. It serves the same purpose as
     * {@link #postMerge(Instrument)} but is enacted when instrument is created into
     * database.
     * 
     * @see #postMerge(Instrument)
     */
    @PrePersist
    public void preSave() {
	// reuse merge logic but with both SRC and file name from not managed entity
	// instance is not managed yet since it is not yet persisted
	postMerge(this);
    }

    /**
     * Persistence event listener executed just after entity instance is loaded from
     * database. It initializes root-relative media SRC used by web user interface.
     * Delegates {@link Files#mediaSrc(AtlasObject, String)} that knows about media
     * repository context.
     */
    @PostLoad
    public void postLoad() {
	// database contains only media file names; convert to root-relative URLs, aka
	// SRC
	for (Image picture : images) {
	    picture.postLoad(this);
	}

	for (Link link : links) {
	    link.postLoad();
	}

	sampleSrc = Files.mediaSrc(this, sampleName);
	waveformSrc = Files.mediaSrc(this, waveformName);
    }

    /**
     * For testing
     * 
     * @param id
     */
    public void setId(Integer id) {
	this.id = id;
    }

    public Integer getId() {
	return id;
    }

    public void setCollection(AtlasCollection collection) {
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

    public Date getLastUpdated() {
	return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
	this.lastUpdated = lastUpdated;
    }

    public int getRank() {
	return rank;
    }

    public void setRank(int relevanceRank) {
	this.rank = relevanceRank;
    }

    public User getUser() {
	return user;
    }

    public void setUser(User user) {
	this.user = user;
    }

    @Override
    public String getCollectionName() {
	return collection.getName();
    }

    @Override
    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public List<Image> getImages() {
	return images;
    }

    public void setImages(List<Image> pictures) {
	this.images = pictures;
    }

    public List<String> getAliases() {
	return aliases;
    }

    public void setAliases(List<String> aliases) {
	this.aliases = aliases;
    }

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

    public Map<String, String> getClassification() {
	return classification;
    }

    public void setClassification(Map<String, String> classification) {
	this.classification = classification;
    }

    public Map<String, String> getFeatures() {
	return features;
    }

    public void setFeatures(Map<String, String> features) {
	this.features = features;
    }

    public List<Region> getSpreading() {
	return spreading;
    }

    public void setSpreading(List<Region> spreading) {
	this.spreading = spreading;
    }

    public Map<String, String> getFacts() {
	return facts;
    }

    public void setFacts(Map<String, String> facts) {
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
	AtlasObject other = (AtlasObject) obj;
	if (id != other.id)
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return name;
    }

    public enum State {
	// ENUM('DEVELOPMENT','PUBLISHED')
	DEVELOPMENT, PUBLISHED
    }

    public static AtlasObject create(User user, AtlasCollection collection) {
	AtlasObject object = new AtlasObject();
	object.user = user;
	object.collection = collection;
	object.state = AtlasObject.State.DEVELOPMENT;
	object.aliases = Collections.emptyList();
	object.spreading = Collections.emptyList();
	object.facts = Collections.emptyMap();
	object.links = Collections.emptyList();
	object.related = Collections.emptyList();
	return object;
    }
}
