package com.kidscademy.atlas.studio.search;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kidscademy.atlas.studio.export.ExportObject;

import js.lang.GType;

public class SearchIndexProcessor {
    private final List<DirectIndex<Integer>> directIndices = new ArrayList<>();

    private final ObjectIndexer<ExportObject, Integer> objectIndexer;

    public SearchIndexProcessor() throws NoSuchMethodException {
	ObjectFields<ExportObject> fields = new ObjectFields<>(ExportObject.class);
	fields.addField("description");
	fields.addField("facts");
	fields.addField("definition");
	fields.addField("sampleTitle");
	fields.addField("spreading");
	fields.addField("conservation");
	fields.addField("taxonomy");
	fields.addField("related");
	fields.addField("aliases");
	fields.addField("display");

	this.objectIndexer = new ObjectIndexer<>(fields);
    }

    public DirectIndex<Integer> createDirectIndex(ExportObject object) throws IOException {
	// direct index is per atlas object
	// it stores all keywords and their relevance
	DirectIndex<Integer> directIndex = objectIndexer.scanObject(object, object.getIndex());
	directIndices.add(directIndex);
	return directIndex;
    }

    public Type getSearchIndexType() {
	return new GType(List.class, new GType(KeywordIndex.class, Integer.class));
    }

    public List<KeywordIndex<Integer>> updateSearchIndex() throws IOException {
	return createSearchIndex(directIndices);
    }

    public static List<KeywordIndex<Integer>> createSearchIndex(List<DirectIndex<Integer>> directIndices)
	    throws IOException {
	Map<String, KeywordIndex<Integer>> invertedIndexMap = new HashMap<>();

	for (DirectIndex<Integer> directIndex : directIndices) {
	    for (String word : directIndex) {
		KeywordIndex<Integer> searchIndex = invertedIndexMap.get(word);
		if (searchIndex == null) {
		    searchIndex = new KeywordIndex<Integer>(word);
		    invertedIndexMap.put(word, searchIndex);
		}
		searchIndex.setKeywordRelevance(directIndex.getRelevance(word));
		searchIndex.addObject(directIndex.getObjectKey(), directIndex.getRelevance(word));
	    }
	}

	List<KeywordIndex<Integer>> invertedIndex = new ArrayList<>(invertedIndexMap.values());
	Collections.sort(invertedIndex, new Comparator<KeywordIndex<Integer>>() {
	    @Override
	    public int compare(KeywordIndex<Integer> left, KeywordIndex<Integer> right) {
		return left.compareTo(right);
	    }
	});

	for (KeywordIndex<Integer> searchIndex : invertedIndex) {
	    searchIndex.update();
	}

	return invertedIndex;
    }
}
