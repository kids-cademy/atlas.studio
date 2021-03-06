package com.kidscademy.atlas.studio.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.kidscademy.atlas.studio.export.ExportItem;
import com.kidscademy.atlas.studio.model.AndroidApp;
import com.kidscademy.atlas.studio.model.AtlasCollection;
import com.kidscademy.atlas.studio.model.AtlasCollectionKey;
import com.kidscademy.atlas.studio.model.AtlasImages;
import com.kidscademy.atlas.studio.model.AtlasItem;
import com.kidscademy.atlas.studio.model.AtlasLinks;
import com.kidscademy.atlas.studio.model.AtlasObject;
import com.kidscademy.atlas.studio.model.AtlasObjectKey;
import com.kidscademy.atlas.studio.model.AtlasRelated;
import com.kidscademy.atlas.studio.model.DescriptionMeta;
import com.kidscademy.atlas.studio.model.ExternalSource;
import com.kidscademy.atlas.studio.model.Feature;
import com.kidscademy.atlas.studio.model.FeatureMeta;
import com.kidscademy.atlas.studio.model.Image;
import com.kidscademy.atlas.studio.model.Link;
import com.kidscademy.atlas.studio.model.LinkSource;
import com.kidscademy.atlas.studio.model.Release;
import com.kidscademy.atlas.studio.model.ReleaseChild;
import com.kidscademy.atlas.studio.model.ReleaseItem;
import com.kidscademy.atlas.studio.model.ReleaseParent;
import com.kidscademy.atlas.studio.model.SearchFilter;
import com.kidscademy.atlas.studio.model.Taxon;
import com.kidscademy.atlas.studio.model.TaxonMeta;
import com.kidscademy.atlas.studio.model.TaxonUnit;
import com.kidscademy.atlas.studio.model.TranslationData;
import com.kidscademy.atlas.studio.model.TranslationKey;

import js.lang.BugError;
import js.transaction.Immutable;
import js.transaction.Mutable;
import js.transaction.Transactional;
import js.util.Strings;

@Transactional
@Immutable
public class AtlasDaoImpl implements AtlasDao
{
  private final EntityManager em;

  public AtlasDaoImpl(EntityManager em) {
    this.em = em;
  }

  @Override
  public <T> T getObjectById(Class<T> type, int id) {
    return em.find(type, id);
  }

  @Override
  @Mutable
  public void removeAtlasCollection(int collectionId) {
    AtlasCollection collection = em.find(AtlasCollection.class, collectionId);
    if(collection != null) {
      em.remove(collection);
    }
  }

  @Override
  public String getCollectionName(int collectionId) {
    return em.createQuery("select c.name from AtlasCollection c where c.id=:collectionId", String.class).setParameter("collectionId", collectionId).getSingleResult();
  }

  @Override
  public int getCollectionSize(int collectionId) {
    Number count = (Number)em.createQuery("select count(o) from AtlasObject o where o.collection.id=:id").setParameter("id", collectionId).getSingleResult();
    return count.intValue();
  }

  @Override
  public int getReleaseSize(int releaseId) {
    Number count = (Number)em.createQuery("select count(r.children) from ReleaseParent r where r.id=:id").setParameter("id", releaseId).getSingleResult();
    return count.intValue();
  }

  @Override
  public AtlasCollectionKey getCollectionKeyById(int collectionId) {
    return em.find(AtlasCollectionKey.class, collectionId);
  }

  @Override
  public AtlasCollection getCollectionById(int collectionId) {
    return em.find(AtlasCollection.class, collectionId);
  }

  @Override
  public AtlasCollection getCollectionByLinkSource(int linkSourceId) {
    return em.createQuery("select c from AtlasCollection c join c.linkSources l where l.id=:linkSourceId", AtlasCollection.class).setParameter("linkSourceId", linkSourceId).getSingleResult();
  }

  @Override
  public List<DescriptionMeta> getCollectionDescriptionsMeta(int collectionId) {
    // see getCollectionFeaturesMeta comment
    return em.find(AtlasCollection.class, collectionId).getDescriptionMeta();
  }

  @Override
  public boolean uniqueCollectionName(AtlasCollection collection) {
    List<AtlasCollection> result = em.createQuery("select c from AtlasCollection c where c.id!=:id and c.name=:name", AtlasCollection.class).setParameter("id", collection.getId()).setParameter("name", collection.getName()).getResultList();
    if(result.size() > 1) {
      throw new BugError("Database inconsistency. Multiple collections with the same name.");
    }
    return result.size() == 0;
  }

  @Override
  public boolean uniqueObjectName(AtlasObject object) {
    List<AtlasObject> result = em.createQuery("select o from AtlasObject o where o.id!=:id and o.name=:name", AtlasObject.class).setParameter("id", object.getId()).setParameter("name", object.getName()).getResultList();
    if(result.size() > 1) {
      throw new BugError("Database inconsistency. Multiple objects with the same name.");
    }
    return result.size() == 0;
  }

  @Override
  public boolean objectNameExists(String name) {
    return !em.createQuery("select o.name from AtlasObject o where o.name=:name", String.class).setParameter("name", name).getResultList().isEmpty();
  }

  @Override
  public List<AtlasCollection> getCollections() {
    return em.createQuery("select c from AtlasCollection c order by c.display", AtlasCollection.class).getResultList();
  }

  @Override
  public List<AtlasItem> getRecentUsedAtlasObjects() {
    return em.createQuery("select i from AtlasItem i order by i.timestamp desc", AtlasItem.class).setMaxResults(10).getResultList();
  }

  @Override
  public List<AtlasItem> getCollectionItems(SearchFilter filter, int collectionId) {
    return getCollectionItems(AtlasItem.class, filter, collectionId);
  }

  @Override
  public List<Integer> getCollectionObjectIds(int collectionId) {
    return em.createQuery("select o.id from AtlasObject o where o.collection.id=?1", Integer.class).setParameter(1, collectionId).getResultList();
  }

  @Override
  public List<AtlasImages> getCollectionImages(SearchFilter filter, int collectionId) {
    return getCollectionItems(AtlasImages.class, filter, collectionId);
  }

  @Override
  public List<AtlasRelated> getCollectionRelated(SearchFilter filter, int collectionId) {
    return getCollectionItems(AtlasRelated.class, filter, collectionId);
  }

  @Override
  public List<AtlasLinks> getCollectionLinks(SearchFilter filter, int collectionId) {
    return getCollectionItems(AtlasLinks.class, filter, collectionId);
  }

  private <T> List<T> getCollectionItems(Class<T> type, SearchFilter filter, int collectionId) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("select i from ");
    queryBuilder.append(type.getSimpleName());
    queryBuilder.append(" i where i.collection.id=?1 ");
    if(!filter.criterion("state").is(AtlasObject.State.NONE)) {
      if(filter.criterion("negate").is(true)) {
        queryBuilder.append("and i.state!=?2 ");
      }
      else {
        queryBuilder.append("and i.state=?2 ");
      }
    }
    queryBuilder.append("order by i.display");

    TypedQuery<T> query = em.createQuery(queryBuilder.toString(), type);
    query.setParameter(1, collectionId);
    if(!filter.criterion("state").is(AtlasObject.State.NONE)) {
      query.setParameter(2, filter.get("state", AtlasObject.State.class));
    }
    return query.getResultList();
  }

  @Override
  public List<AtlasItem> getAtlasCollectionItems(int collectionId) {
    return em.createQuery("select i from AtlasItem i where i.collection.id=:collectionId order by i.display", AtlasItem.class).setParameter("collectionId", collectionId).getResultList();
  }

  @Override
  public List<AtlasItem> getCollectionItemsByTaxon(int collectionId, Taxon taxon) {
    List<Integer> ids = em.createQuery("select o.id from AtlasObject o join o.taxonomy t join t.meta m where o.collection.id=:collectionId and m.unit.name=:taxonName and t.value=:taxonValue", Integer.class).setParameter("collectionId", collectionId).setParameter("taxonName", taxon.getName()).setParameter("taxonValue", taxon.getValue()).getResultList();
    return getAtlasItems(ids);
  }

  @Override
  public List<ExportItem> getCollectionExportItems(int collectionId) {
    return em.createQuery("select i from ExportItem i where i.collection.id=?1 order by i.display", ExportItem.class).setParameter(1, collectionId).getResultList();
  }

  @Override
  public List<ExportItem> getCollectionExportItemsByState(int collectionId, AtlasObject.State state) {
    return em.createQuery("select i from ExportItem i where i.collection.id=?1 and i.state=?2 order by i.display", ExportItem.class).setParameter(1, collectionId).setParameter(2, state).getResultList();
  }

  @Override
  public List<ExportItem> getAllExportItems() {
    return em.createQuery("select i from ExportItem i where i.state=?1 order by i.name", ExportItem.class).setParameter(1, AtlasObject.State.PUBLISHED).getResultList();
  }

  @Override
  @Mutable
  public void saveAtlasCollection(AtlasCollection collection) {
    if(collection.getId() == 0) {
      em.persist(collection);
    }
    else {
      em.merge(collection);
    }
  }

  @Override
  @Mutable
  public void saveAtlasObject(AtlasObject object) {
    object.updateTimestamp();
    if(object.getId() == 0) {
      em.persist(object);
      return;
    }

    em.merge(object).postMerge(object);

    // update content timestamp for all releases containing this atlas object
    List<ReleaseParent> releases = em.createQuery("select p from ReleaseParent p join p.children c where c.id=:objectId", ReleaseParent.class).setParameter("objectId", object.getId()).getResultList();
    for(ReleaseParent release : releases) {
      release.updateTimestamp();
    }
  }

  @Override
  @Mutable
  public void removeAtlasObject(int objectId) {
    AtlasObject object = em.find(AtlasObject.class, objectId);
    if(object != null) {
      em.remove(object);
    }
  }

  @Override
  @Mutable
  public void removeObjectFeatures(int objectId) {
    em.createQuery("delete from AtlasObjectFeature f where f.objectId = :objectId").setParameter("objectId", objectId).executeUpdate();
  }

  @Override
  @Mutable
  public void moveAtlasObject(int sourceObjectId, int targetCollectionId) {
    AtlasCollection targetCollection = em.find(AtlasCollection.class, targetCollectionId);
    if(targetCollection == null) {
      return;
    }

    for(Link link : getObjectLinks(sourceObjectId)) {
      LinkSource targetLinkSource = getLinkSourceByDomain(targetCollectionId, link.getDomain());
      if(targetLinkSource == null) {
        targetLinkSource = new LinkSource(link.getExternalSource());
        targetCollection.getLinkSources().add(targetLinkSource);
      }
      link.setLinkSource(targetLinkSource);
    }

    AtlasObjectKey object = em.find(AtlasObjectKey.class, sourceObjectId);
    if(object != null) {
      AtlasCollectionKey collectionKey = em.find(AtlasCollectionKey.class, targetCollectionId);
      object.setCollection(collectionKey);
    }
  }

  @Override
  public AtlasItem getAtlasItem(int objectId) {
    return em.find(AtlasItem.class, objectId);
  }

  @Override
  public AtlasObject getAtlasObject(int objectId) {
    return em.find(AtlasObject.class, objectId);
  }

  @Override
  public AtlasObject getObjectByName(String collectionName, String name) {
    return em.createQuery("select o from AtlasObject o where o.collection.name=:collectionName and o.name=:name", AtlasObject.class).setParameter("collectionName", collectionName).setParameter("name", name).getSingleResult();
  }

  @Override
  public String getAtlasObjectName(int objectId) {
    return em.createQuery("select o.name from AtlasObject o where o.id=:id", String.class).setParameter("id", objectId).getSingleResult();
  }

  @Override
  public List<Link> getObjectLinks(int objectId) {
    return em.find(AtlasObject.class, objectId).getLinks();
  }

  @Override
  public LinkSource getLinkSourceByDomain(int collectionId, String domain) {
    List<LinkSource> result = em.createQuery("select l from AtlasCollection c join c.linkSources l join l.externalSource e where c.id=:collectionId and e.domain=:domain", LinkSource.class).setParameter("collectionId", collectionId).setParameter("domain", domain).getResultList();
    if(result.size() > 1) {
      throw new BugError("Database inconsistency. Not unique colleciton link source domain.");
    }
    return result.size() == 1 ? result.get(0) : null;
  }

  @Override
  public List<ExternalSource> getExternalSources() {
    return em.createQuery("select s from ExternalSource s order by s.display", ExternalSource.class).getResultList();
  }

  @Override
  public List<ExternalSource> getExternalSourceCandidates(List<Integer> excludeIds) {
    if(excludeIds.isEmpty()) {
      return getExternalSources();
    }
    return em.createQuery("select s from ExternalSource s where s.id not in :excludeIds order by s.display", ExternalSource.class).setParameter("excludeIds", excludeIds).getResultList();
  }

  @Override
  public ExternalSource getExternalSourceById(int externalSourceId) {
    return em.find(ExternalSource.class, externalSourceId);
  }

  @Override
  public ExternalSource getExternalSourceByDomain(String domain) {
    List<ExternalSource> result = em.createQuery("select s from ExternalSource s where s.domain=:domain", ExternalSource.class).setParameter("domain", domain).getResultList();
    if(result.size() > 1) {
      throw new BugError("Database inconsistency. Not unique external source domain.");
    }
    return result.size() == 1 ? result.get(0) : null;
  }

  @Override
  @Mutable
  public void saveExternalSource(ExternalSource externalSource) {
    if(externalSource.getId() == 0) {
      em.persist(externalSource);
    }
    else {
      em.merge(externalSource);
    }
  }

  @Override
  @Mutable
  public void removeExternalSource(int externalSourceId) {
    ExternalSource externalSource = em.find(ExternalSource.class, externalSourceId);
    if(externalSource != null) {
      em.remove(externalSource);
    }
  }

  @Override
  public List<FeatureMeta> getFeaturesMeta() {
    return em.createQuery("select f from FeatureMeta f order by f.name", FeatureMeta.class).getResultList();
  }

  @Override
  public List<FeatureMeta> searchFeaturesMeta(String search, List<Integer> excludes) {
    if(excludes.isEmpty()) {
      return searchFeaturesMeta(search);
    }
    if(search == null) {
      return em.createQuery("select f from FeatureMeta f where f.id not in :excludes order by f.name", FeatureMeta.class).setParameter("excludes", excludes).getResultList();
    }
    search = Strings.concat('%', search, '%');
    return em.createQuery("select f from FeatureMeta f where f.name like :search and f.id not in :excludes order by f.name", FeatureMeta.class).setParameter("search", search).setParameter("excludes", excludes).getResultList();
  }

  @Override
  public List<FeatureMeta> searchFeaturesMeta(String search) {
    if(search == null) {
      return getFeaturesMeta();
    }
    search = Strings.concat('%', search, '%');
    return em.createQuery("select f from FeatureMeta f where f.name like :search order by f.name", FeatureMeta.class).setParameter("search", search).getResultList();
  }

  @Override
  public List<TaxonUnit> getTaxonUnits() {
    return em.createQuery("select t from TaxonUnit t order by t.name", TaxonUnit.class).getResultList();
  }

  @Override
  public List<TaxonUnit> searchTaxonUnits(String search, List<Integer> excludes) {
    if(excludes.isEmpty()) {
      return searchTaxonUnits(search);
    }
    if(search == null) {
      return em.createQuery("select t from TaxonUnit t where t.id not in :excludes order by t.name", TaxonUnit.class).setParameter("excludes", excludes).getResultList();
    }
    search = Strings.concat('%', search, '%');
    return em.createQuery("select t from TaxonUnit t where t.name like :search and t.id not in :excludes order by t.name", TaxonUnit.class).setParameter("search", search).setParameter("excludes", excludes).getResultList();
  }

  @Override
  public List<TaxonUnit> searchTaxonUnits(String search) {
    if(search == null) {
      return getTaxonUnits();
    }
    search = Strings.concat('%', search, '%');
    return em.createQuery("select t from TaxonUnit t where t.name like :search order by t.name", TaxonUnit.class).setParameter("search", search).getResultList();
  }

  @Override
  public List<TaxonMeta> getCollectionTaxonomyMeta(int collectionId) {
    return em.find(AtlasCollection.class, collectionId).getTaxonomyMeta();
  }

  @Override
  public List<LinkSource> getCollectionLinkSources(int collectionId) {
    return em.find(AtlasCollection.class, collectionId).getLinkSources();
  }

  @Override
  public List<FeatureMeta> getCollectionFeaturesMeta(int collectionId) {
    // JPA @OrderColumn is used to automatically handle order column - in our case featuresMeta_ORDER
    // JPA takes care of insert, update, reorder, delete, retrieve...

    // anyway, it is working only when select AtlasObject itself when try to use join - implicit or explicit order is not handled by JPA and retrieved list is
    // in database ID order

    // keep next commented query as reminder

    // return em.createQuery("select c.featuresMeta from AtlasCollection c where c.id=:collectionId", FeatureMeta.class).setParameter("collectionId",
    // collectionId).getResultList();

    return em.find(AtlasCollection.class, collectionId).getFeaturesMeta();
  }

  @Override
  public TaxonMeta getTaxonMetaById(int id) {
    return em.find(TaxonMeta.class, id);
  }

  @Override
  public TaxonMeta getTaxonMetaByName(int collectionId, String taxonName) throws NoResultException {
    return em.createQuery("select t from AtlasCollection c join c.taxonomyMeta t join t.unit u where c.id=:collectionId and u.name=:taxonName", TaxonMeta.class).setParameter("collectionId", collectionId).setParameter("taxonName", taxonName).getSingleResult();
  }

  @Override
  public FeatureMeta getFeatureMetaById(int featureMetaId) {
    return em.find(FeatureMeta.class, featureMetaId);
  }

  @Override
  public FeatureMeta getFeatureMetaByKey(int collectionId, String featureKey) {
    List<FeatureMeta> result = em.createQuery("select m from AtlasCollection c join c.featuresMeta m where c.id=:collectionId and m.name like :featureKey", FeatureMeta.class).setParameter("collectionId", collectionId).setParameter("featureKey", "%" + featureKey).getResultList();
    if(result.isEmpty()) {
      return null;
    }
    if(result.size() > 1) {
      throw new BugError("Ambiguous key |%s|.", featureKey);
    }
    return result.get(0);
  }

  @Override
  @Mutable
  public void saveFeatureMeta(FeatureMeta featureMeta) {
    if(featureMeta.getId() == 0) {
      em.persist(featureMeta);
    }
    else {
      em.merge(featureMeta);
    }
  }

  @Override
  @Mutable
  public void removeFeatureMeta(int featureMetaId) {
    FeatureMeta featureMeta = em.find(FeatureMeta.class, featureMetaId);
    if(featureMeta != null) {
      em.remove(featureMeta);
    }
  }

  @Override
  public List<AtlasItem> getRelatedAtlasObjects(int collectionId, List<String> relatedNames) {
    List<AtlasItem> items = new ArrayList<>();
    String jpql = "select i from AtlasItem i where i.collection.id=?1 and i.name=?2";
    for(String relatedName : relatedNames) {
      List<AtlasItem> result = em.createQuery(jpql, AtlasItem.class).setParameter(1, collectionId).setParameter(2, relatedName).getResultList();
      if(!result.isEmpty()) {
        items.add(result.get(0));
      }
    }
    return items;
  }

  @Override
  @Mutable
  public void resetObjectSample(int objectId) {
    String jpql = "update AtlasObject o set o.sampleTitle=null,o.sampleName=null,o.waveformName=null where o.id=:id";
    em.createQuery(jpql).setParameter("id", objectId).executeUpdate();
  }

  @Override
  @Mutable
  public void removeObjectImage(int objectId, Image image) {
    AtlasObject object = (AtlasObject)em.createQuery("select o from AtlasObject o where o.id=:id").setParameter("id", objectId).getSingleResult();
    // object entity is managed in this scope and altering it will be synchronized
    // on database
    object.getImages().remove(image.getImageKey());
  }

  @Override
  @Mutable
  public void putObjectImage(int objectId, Image image) {
    AtlasObject object = (AtlasObject)em.createQuery("select o from AtlasObject o where o.id=:id").setParameter("id", objectId).getSingleResult();
    object.getImages().put(image.getImageKey(), image);
  }

  @Override
  public Image getImageByKey(int objectId, String imageKey) {
    String jpql = "select i from AtlasObject o join o.images i where o.id=:id and KEY(i)=:key";
    List<Image> images = em.createQuery(jpql, Image.class).setParameter("id", objectId).setParameter("key", imageKey).getResultList();
    return images.isEmpty() ? null : images.get(0);
  }

  @Override
  public AtlasObject getNextAtlasObject(int objectId) {
    String jpql = "select o from AtlasObject o where o.id>?1 order by o.id asc";
    List<AtlasObject> objects = em.createQuery(jpql, AtlasObject.class).setParameter(1, objectId).setMaxResults(1).getResultList();
    return objects.isEmpty() ? null : objects.get(0);
  }

  @Override
  public List<AtlasItem> getAtlasItems(List<Integer> ids) {
    if(ids.isEmpty()) {
      return Collections.emptyList();
    }
    return em.createQuery("select i from AtlasItem i where i.id in :ids order by i.display", AtlasItem.class).setParameter("ids", ids).getResultList();
  }

  @Override
  public List<ReleaseItem> getReleases() {
    return em.createQuery("select r from ReleaseItem r order by r.display", ReleaseItem.class).getResultList();
  }

  @Override
  public Release getReleaseById(int releaseId) {
    return em.find(Release.class, releaseId);
  }

  @Override
  public Release getReleaseByName(String releaseName) {
    return em.createQuery("select r from Release r where r.name=:name", Release.class).setParameter("name", releaseName).getSingleResult();
  }

  @Override
  public ReleaseParent getReleaseParentById(int releaseId) {
    return em.find(ReleaseParent.class, releaseId);
  }

  @Override
  @Mutable
  public void saveRelease(Release release) throws IOException {
    if(release.getId() == 0) {
      em.persist(release);
    }
    else {
      em.merge(release);
    }
  }

  @Override
  @Mutable
  public void removeRelease(int releaseId) {
    Release release = em.find(Release.class, releaseId);
    if(release != null) {
      em.remove(release);
    }
  }

  @Override
  @Mutable
  public void addReleaseChild(int releaseId, int childId) {
    ReleaseParent release = em.find(ReleaseParent.class, releaseId);
    release.updateTimestamp();
    ReleaseChild child = em.find(ReleaseChild.class, childId);
    if(!release.getChildren().contains(child)) {
      release.getChildren().add(child);
    }
  }

  @Override
  @Mutable
  public void addReleaseChildren(int releaseId, List<Integer> childIds) {
    List<ReleaseChild> children = em.createQuery("select c from ReleaseChild c where c.id in :ids", ReleaseChild.class).setParameter("ids", childIds).getResultList();

    // to my real shame i did not find a way to retrieve only not already existing
    // children and i need to remove them manually
    // anyway, i do not expect performance impact
    ReleaseParent release = em.find(ReleaseParent.class, releaseId);
    release.updateTimestamp();
    Iterator<ReleaseChild> it = children.iterator();
    while(it.hasNext()) {
      if(release.getChildren().contains(it.next())) {
        it.remove();
      }
    }

    release.getChildren().addAll(children);
  }

  @Override
  @Mutable
  public void removeReleaseChildren(int releaseId) {
    ReleaseParent release = em.find(ReleaseParent.class, releaseId);
    release.updateTimestamp();
    release.getChildren().clear();
  }

  @Override
  @Mutable
  public void removeReleaseChild(int releaseId, int childId) {
    ReleaseParent release = em.find(ReleaseParent.class, releaseId);
    release.updateTimestamp();
    ReleaseChild child = em.find(ReleaseChild.class, childId);
    release.getChildren().remove(child);
  }

  @Override
  public List<AtlasItem> getReleaseItems(int releaseId) {
    return getAtlasItems(getReleaseItemIds(releaseId));
  }

  @Override
  public List<ExternalSource> getReleaseExternalSources(int releaseId) {
    List<Integer> ids = getReleaseItemIds(releaseId);
    if(ids.isEmpty()) {
      return Collections.emptyList();
    }
    return em.createQuery("select distinct e from AtlasObject o join o.links l join l.linkSource s join s.externalSource e where o.id in :ids order by e.display", ExternalSource.class).setParameter("ids", ids).getResultList();
  }

  private List<Integer> getReleaseItemIds(int releaseId) {
    ReleaseParent release = em.find(ReleaseParent.class, releaseId);
    if(release == null) {
      return Collections.emptyList();
    }
    List<Integer> ids = new ArrayList<>();
    for(ReleaseChild child : release.getChildren()) {
      ids.add(child.getId());
    }
    return ids;
  }

  @Override
  public AndroidApp getAndroidAppById(int appId) {
    return em.find(AndroidApp.class, appId);
  }

  @Override
  public AndroidApp getAndroidAppByName(String name) {
    List<AndroidApp> apps = em.createQuery("select a from AndroidApp a where a.release.name=:name", AndroidApp.class).setParameter("name", name).getResultList();
    return apps.isEmpty() ? null : apps.get(0);
  }

  @Override
  public AndroidApp getAndroidAppByRelease(int releaseId) {
    List<AndroidApp> apps = em.createQuery("select a from AndroidApp a where a.release.id=:id", AndroidApp.class).setParameter("id", releaseId).getResultList();
    return apps.isEmpty() ? null : apps.get(0);
  }

  @Override
  @Mutable
  public void saveAndroidApp(AndroidApp app) {
    if(app.getId() == 0) {
      em.persist(app);
    }
    else {
      em.merge(app);
    }
  }

  @Override
  @Mutable
  public void removeAndroidApp(int appId) {
    AndroidApp app = em.find(AndroidApp.class, appId);
    if(app != null) {
      em.remove(app);
    }
  }

  @Override
  public TranslationData getTranslationData(TranslationKey key) {
    return em.find(TranslationData.class, key);
  }

  @Override
  public String getTranslation(TranslationKey key) {
    TranslationData translation = em.find(TranslationData.class, key);
    return translation != null ? translation.getText() : null;
  }

  @Override
  public boolean hasTranslation(TranslationKey key) {
    return em.find(TranslationData.class, key) != null;
  }

  @Override
  @Mutable
  public void saveTranslation(TranslationData translation) {
    em.merge(translation);
  }

  @Override
  @Mutable
  public void removeTranslation(TranslationKey key) {
    TranslationData translation = em.find(TranslationData.class, key);
    if(translation != null) {
      em.remove(translation);
    }
  }

  @Override
  public List<Feature> getFeatures() {
    return em.createQuery("select f from Feature f", Feature.class).getResultList();
  }

  @Override
  @Mutable
  public void saveFeature(Feature feature) {
    em.merge(feature);
  }

  @Override
  public TaxonUnit getTaxonUnit(int id) {
    return em.find(TaxonUnit.class, id);
  }

  @Override
  @Mutable
  public void saveTaxonUnit(TaxonUnit taxonUnit) {
    em.merge(taxonUnit);
  }

  @Override
  @Mutable
  public void removeTaxonUnit(int id) {
    TaxonUnit taxonUnit = em.find(TaxonUnit.class, id);
    if(taxonUnit != null) {
      em.remove(taxonUnit);
    }
  }
}
