package com.kidscademy.atlas.studio.export;

import java.util.SortedSet;
import java.util.TreeSet;

public class SearchIndex implements Comparable<SearchIndex> {
    private String keyword;
    private int keywordRelevance;
    private int[] objectIds;

    private transient SortedSet<ObjectId> objects;

    public SearchIndex(String keyword) {
	this.keyword = keyword;
	this.objects = new TreeSet<ObjectId>();
    }

    public void setKeywordRelevance(int keywordRelevance) {
	if (this.keywordRelevance < keywordRelevance) {
	    this.keywordRelevance = keywordRelevance;
	}
    }

    public String getKeyword() {
	return keyword;
    }

    public int getKeywordRelevance() {
	return keywordRelevance;
    }

    public void addObject(int objectId, int rank) {
	objects.add(new ObjectId(objectId, rank));
    }

    public void updateObjectIds() {
	objectIds = new int[objects.size()];
	int i = 0;
	for (ObjectId object : objects) {
	    objectIds[i++] = object.objectId;
	}
    }

    @Override
    public int compareTo(SearchIndex that) {
	if (this.keywordRelevance == that.keywordRelevance) {
	    return this.keyword.compareTo(that.keyword);
	}
	return this.keywordRelevance > that.keywordRelevance ? 1 : -1;
    }

    // --------------------------------------------------------------------------------------------

    private static class ObjectId implements Comparable<ObjectId> {
	private final int objectId;
	private final int rank;

	ObjectId(int objectId, int rank) {
	    super();
	    this.objectId = objectId;
	    this.rank = rank;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + objectId;
	    result = prime * result + rank;
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
	    ObjectId other = (ObjectId) obj;
	    if (objectId != other.objectId)
		return false;
	    if (rank != other.rank)
		return false;
	    return true;
	}

	@Override
	public int compareTo(ObjectId that) {
	    if (this.rank == that.rank) {
		return Integer.compare(that.objectId, this.objectId);
	    }
	    return Integer.compare(this.rank, that.rank);
	}
    }
}
