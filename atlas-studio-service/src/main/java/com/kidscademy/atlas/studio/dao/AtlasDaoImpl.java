package com.kidscademy.atlas.studio.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import com.kidscademy.atlas.studio.model.AtlasItem;
import com.kidscademy.atlas.studio.model.AtlasObject;
import com.kidscademy.atlas.studio.model.AtlasObject.State;
import com.kidscademy.atlas.studio.model.Image;
import com.kidscademy.atlas.studio.model.Link;

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
    @Mutable
    public void saveObject(AtlasObject object) {
	if (object.getId() == 0) {
	    em.persist(object);
	} else {
	    em.merge(object).postMerge(object);
	}
    }

    @Override
    public AtlasObject getObjectById(int objectId) {
	return em.createQuery("select o from AtlasObject o where o.id=:id", AtlasObject.class)
		.setParameter("id", objectId).getSingleResult();
    }

    @Override
    public AtlasObject getObjectByName(String collectionName, String name) {
	return em
		.createQuery("select o from AtlasObject o where o.collection.name=:collectionName and o.name=:name",
			AtlasObject.class)
		.setParameter("collectionName", collectionName).setParameter("name", name).getSingleResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AtlasItem> getAtlasObjects() {
	return em.createQuery("select o from UIObject o where o.dtype='AtlasObject'").getResultList();
    }

    @Override
    public List<Link> getObjectLinks(AtlasItem object) {
	return em
		.createQuery(
			"select o.links from AtlasObject o where o.collection.name=:collectionName and o.name=:name",
			Link.class)
		.setParameter("collectionName", object.getCollectionName()).setParameter("name", object.getName())
		.getResultList();
    }

    @Override
    public List<AtlasObject> findObjectsByCategory(String collectionName) {
	return em.createQuery("select o from AtlasObject o where o.collection.name=:collectionName", AtlasObject.class)
		.setParameter("collectionName", collectionName).getResultList();
    }

    @Override
    public List<AtlasObject> findPublishedObjects(String collectionName) {
	return em
		.createQuery("select o from AtlasObject o where o.collection.name=:collectionName and o.state=:state",
			AtlasObject.class)
		.setParameter("collectionName", collectionName).setParameter("state", State.PUBLISHED).getResultList();
    }

    @Override
    @Mutable
    public void removeObject(Object object) {
	if (!em.contains(object)) {
	    object = em.merge(object);
	}
	em.remove(object);
    }

    @Override
    public List<AtlasItem> findObjectsByNames(String category, List<String> names) {
	if (names.isEmpty()) {
	    return Collections.emptyList();
	}
	String jpql = "select o from UIObject o where o.category=:category and o.name in :names";
	return em.createQuery(jpql, AtlasItem.class).setParameter("category", category).setParameter("names", names)
		.getResultList();
    }
//
//    @Override
//    public List<AtlasItem> getObjectsByCategory(String category) {
//	String jpql = "select i.id from AtlasObject i where i.category=:category";
//	List<Integer> ids = em.createQuery(jpql, Integer.class).setParameter("category", category).getResultList();
//
//	jpql = "select o from UIObject o where o.id in :ids";
//	return em.createQuery(jpql, AtlasItem.class).setParameter("ids", ids).getResultList();
//    }

    @Override
    @Mutable
    public void resetObjectSample(int objectId) {
	String jpql = "update AtlasObject o set o.sampleTitle=null,o.sampleName=null,o.waveformName=null where o.id=:id";
	em.createQuery(jpql).setParameter("id", objectId).executeUpdate();
    }

    @Override
    @Mutable
    public void removeObjectPicture(int objectId, Image picture) {
	AtlasObject object = (AtlasObject) em.createQuery("select o from AtlasObject o where o.id=:id")
		.setParameter("id", objectId).getSingleResult();
	object.getImages().remove(picture);
    }

    @Override
    @Mutable
    public void addObjectPicture(int objectId, Image picture) {
	AtlasObject object = (AtlasObject) em.createQuery("select o from AtlasObject o where o.id=:id")
		.setParameter("id", objectId).getSingleResult();
	object.getImages().add(picture);
    }

    @Override
    public Image getPictureByName(int objectId, String name) {
	String jpql = "select i from AtlasObject o join o.images i where o.id=:id and i.name=:name";
	List<Image> pictures = em.createQuery(jpql, Image.class).setParameter("id", objectId).setParameter("name", name)
		.getResultList();
	return pictures.isEmpty() ? null : pictures.get(0);
    }
}