package com.kidscademy.atlas.studio.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.kidscademy.atlas.studio.export.ExportItem;
import com.kidscademy.atlas.studio.model.AtlasCollection;
import com.kidscademy.atlas.studio.model.AtlasItem;
import com.kidscademy.atlas.studio.model.AtlasObject;
import com.kidscademy.atlas.studio.model.Image;
import com.kidscademy.atlas.studio.model.Link;
import com.kidscademy.atlas.studio.model.Taxon;

import js.transaction.Immutable;
import js.transaction.Mutable;
import js.transaction.Transactional;

@Transactional
@Immutable
public class AtlasDaoImpl implements AtlasDao {
    private final EntityManager em;

    public AtlasDaoImpl(EntityManager em) {
	this.em = em;
    }

    @Override
    public AtlasCollection getCollectionById(int collectionId) {
	return em.find(AtlasCollection.class, 1);
    }

    @Override
    public List<AtlasCollection> getCollections() {
	return em.createQuery("select c from AtlasCollection c", AtlasCollection.class).getResultList();
    }

    @Override
    public List<AtlasItem> getCollectionItems(Map<String, String> filter, int collectionId) {
	StringBuilder queryBuilder = new StringBuilder();
	queryBuilder.append("select i from AtlasItem i where i.collection.id=?1 ");
	if (!filter.get("state").isEmpty()) {
	    queryBuilder.append("and i.state=?2 ");
	}
	queryBuilder.append("order by i.display");

	TypedQuery<AtlasItem> query = em.createQuery(queryBuilder.toString(), AtlasItem.class).setParameter(1,
		collectionId);
	if (!filter.get("state").isEmpty()) {
	    query.setParameter(2, AtlasObject.State.valueOf(filter.get("state")));
	}
	return query.getResultList();
    }

    @Override
    public List<AtlasItem> getCollectionItemsByTaxon(int collectionId, Taxon taxon) {
	List<Integer> ids = em.createQuery(
		"select o.id from AtlasObject o join o.taxonomy t where o.collection.id=:collectionId and t.name=:taxonName and t.value=:taxonValue",
		Integer.class).setParameter("collectionId", collectionId).setParameter("taxonName", taxon.getName())
		.setParameter("taxonValue", taxon.getValue()).getResultList();
	if (ids.isEmpty()) {
	    return Collections.emptyList();
	}
	return em.createQuery("select i from AtlasItem i where i.id in :ids", AtlasItem.class).setParameter("ids", ids)
		.getResultList();
    }

    @Override
    public List<ExportItem> getCollectionExportItems(int collectionId) {
	return em
		.createQuery("select i from ExportItem i where i.collection.id=?1 and i.state=?2 order by i.name",
			ExportItem.class)
		.setParameter(1, collectionId).setParameter(2, AtlasObject.State.PUBLISHED).getResultList();
    }

    @Override
    public List<ExportItem> getAllExportItems() {
	return em.createQuery("select i from ExportItem i where i.state=?1 order by i.name", ExportItem.class)
		.setParameter(1, AtlasObject.State.PUBLISHED).getResultList();
    }

    @Override
    @Mutable
    public void saveAtlasObject(AtlasObject object) {
	object.setLastUpdated(new Timestamp(System.currentTimeMillis()));
	if (object.getId() == 0) {
	    em.persist(object);
	} else {
	    em.merge(object).postMerge(object);
	}
    }

    @Override
    @Mutable
    public void removeAtlasObject(int objectId) {
	AtlasObject object = em.find(AtlasObject.class, objectId);
	if (object != null) {
	    em.remove(object);
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
	return em
		.createQuery("select o from AtlasObject o where o.collection.name=:collectionName and o.name=:name",
			AtlasObject.class)
		.setParameter("collectionName", collectionName).setParameter("name", name).getSingleResult();
    }

    @Override
    public String getAtlasObjectName(int objectId) {
	return em.createQuery("select o.name from AtlasObject o where o.id=:id", String.class)
		.setParameter("id", objectId).getSingleResult();
    }

    @Override
    public List<Link> getObjectLinks(AtlasItem object) {
	return em
		.createQuery(
			"select o.links from AtlasObject o where o.collection.name=:collectionName and o.name=:name",
			Link.class)
		.setParameter("collectionName", object.getRepositoryName()).setParameter("name", object.getName())
		.getResultList();
    }

    @Override
    public List<AtlasItem> getRelatedAtlasObjects(int collectionId, List<String> relatedNames) {
	List<AtlasItem> items = new ArrayList<>();
	String jpql = "select i from AtlasItem i where i.collection.id=?1 and i.name=?2";
	for (String relatedName : relatedNames) {
	    List<AtlasItem> result = em.createQuery(jpql, AtlasItem.class).setParameter(1, collectionId)
		    .setParameter(2, relatedName).getResultList();
	    if (!result.isEmpty()) {
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
	AtlasObject object = (AtlasObject) em.createQuery("select o from AtlasObject o where o.id=:id")
		.setParameter("id", objectId).getSingleResult();
	// object entity is managed in this scope and altering it will be synchronized
	// on database
	object.getImages().remove(image.getImageKey());
    }

    @Override
    @Mutable
    public void addObjectImage(int objectId, Image image) {
	AtlasObject object = (AtlasObject) em.createQuery("select o from AtlasObject o where o.id=:id")
		.setParameter("id", objectId).getSingleResult();
	// object entity is managed in this scope and altering it will be synchronized
	// on database
	object.getImages().put(image.getImageKey(), image);
    }

    @Override
    public Image getImageByKey(int objectId, String imageKey) {
	String jpql = "select i from AtlasObject o join o.images i where o.id=:id and KEY(i)=:key";
	List<Image> images = em.createQuery(jpql, Image.class).setParameter("id", objectId)
		.setParameter("key", imageKey).getResultList();
	return images.isEmpty() ? null : images.get(0);
    }

    @Override
    public AtlasObject getNextAtlasObject(int objectId) {
	String jpql = "select o from AtlasObject o where o.id>?1 order by o.id asc";
	List<AtlasObject> objects = em.createQuery(jpql, AtlasObject.class).setParameter(1, objectId).setMaxResults(1)
		.getResultList();
	return objects.isEmpty() ? null : objects.get(0);
    }

    @Override
    public List<AtlasItem> getAtlasItems(List<Integer> objectIds) {
	return em.createQuery("select i from AtlasItem i where i.id in :ids", AtlasItem.class)
		.setParameter("ids", objectIds).getResultList();
    }
}
