package com.kidscademy.atlas.studio.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kidscademy.apiservice.client.Animalia;
import com.kidscademy.apiservice.client.Wikipedia;
import com.kidscademy.apiservice.model.PhysicalTrait;
import com.kidscademy.atlas.studio.ApiService;
import com.kidscademy.atlas.studio.dao.AtlasDao;
import com.kidscademy.atlas.studio.model.API;
import com.kidscademy.atlas.studio.model.ApiDescriptor;
import com.kidscademy.atlas.studio.model.AtlasCollection;
import com.kidscademy.atlas.studio.model.Feature;
import com.kidscademy.atlas.studio.model.FeatureMeta;
import com.kidscademy.atlas.studio.model.Link;
import com.kidscademy.atlas.studio.model.Taxon;
import com.kidscademy.atlas.studio.model.TaxonMeta;
import com.kidscademy.atlas.studio.util.Strings;
import com.kidscademy.atlas.studio.www.CambridgeDictionary;
import com.kidscademy.atlas.studio.www.MerriamWebster;
import com.kidscademy.atlas.studio.www.PlantVillage;
import com.kidscademy.atlas.studio.www.SoftSchools;
import com.kidscademy.atlas.studio.www.TheFreeDictionary;
import com.kidscademy.atlas.studio.www.WikipediaArticleText;

import js.tiny.container.core.AppContext;

public class ApiServiceImpl implements ApiService {
    private final List<ApiDescriptor> availableApis;

    private final AppContext context;
    private final AtlasDao atlasDao;

    private final SoftSchools softSchools;
    private final TheFreeDictionary freeDictionary;
    private final CambridgeDictionary cambridgeDictionary;
    private final MerriamWebster merriamWebster;

    private final Animalia animalia;
    private final Wikipedia wikipedia;

    public ApiServiceImpl(AppContext context) {
	this.availableApis = new ArrayList<>();
	for (Method method : getClass().getDeclaredMethods()) {
	    API api = method.getAnnotation(API.class);
	    if (api != null) {
		this.availableApis.add(new ApiDescriptor(api));
	    }
	}
	Collections.sort(this.availableApis);

	this.context = context;
	this.atlasDao = context.getInstance(AtlasDao.class);

	this.softSchools = context.getInstance(SoftSchools.class);
	this.freeDictionary = context.getInstance(TheFreeDictionary.class);
	this.cambridgeDictionary = context.getInstance(CambridgeDictionary.class);
	this.merriamWebster = context.getInstance(MerriamWebster.class);

	this.animalia = context.getInstance(Animalia.class);
	this.wikipedia = context.getInstance(Wikipedia.class);
    }

    @Override
    public List<ApiDescriptor> getAvailableApis() {
	return availableApis;
    }

    @Override
    public List<ApiDescriptor> getApiDescriptors(List<String> apiNames) {
	List<ApiDescriptor> apis = new ArrayList<>();
	for (ApiDescriptor api : availableApis) {
	    if (apiNames.contains(api.getName())) {
		apis.add(api);
	    }
	}
	return apis;
    }

    @Override
    @API(name = "definition", description = "Definition is a brief description, usually one statement.")
    public String getDefinition(Link link) {
	switch (link.getDomain()) {
	case "wikipedia.org":
	    return wikipedia.getDefinitions(link.getBasename()).get(0).getDefinition();

	case "thefreedictionary.com":
	    return freeDictionary.getDefinition(link.getBasename());

	case "cambridge.org":
	    return cambridgeDictionary.getDefinition(link.getBasename());

	case "merriam-webster.com":
	    return merriamWebster.getDefinition(link.getBasename());

	default:
	    return null;
	}
    }

    @Override
    @API(name = "description", description = "Description is organized on named sections with statements related by meaning.")
    public LinkedHashMap<String, String> getDescription(Link link) {
	LinkedHashMap<String, String> sections = new LinkedHashMap<>();
	switch (link.getDomain()) {
	case "softschools.com":
	    sections.put("softschools", Strings
		    .join(Strings.breakSentences(softSchools.getFacts(link.getPath()).getDescription()), "\r\n\r\n"));
	    break;

	case "wikipedia.org":
	    WikipediaArticleText article = new WikipediaArticleText(link.getUrl());
	    sections.put("wikipedia", article.getText());
	    break;

	case "psu.edu":
	    PlantVillage plantVillage = context.getInstance(PlantVillage.class);
	    plantVillage.getArticle(link.getPath()).getSections(sections);
	    break;

	default:
	    break;
	}
	return sections;
    }

    @Override
    @API(name = "facts", description = "A fact is a paragraph describing a piece of reality and has only one independent statement.")
    public Map<String, String> getFacts(Link link) {
	switch (link.getDomain()) {
	case "softschools.com":
	    Map<String, String> facts = new HashMap<>();
	    for (String fact : softSchools.getFacts(link.getPath()).getFacts()) {
		facts.put(Strings.excerpt(fact), fact);
	    }
	    return facts;

	default:
	    return null;
	}
    }

    @Override
    @API(name = "taxonomy", description = "Object classification. For now only life forms taxonomy is supported.")
    public List<Taxon> getTaxonomy(Link link) {
	LinkedHashMap<String, String> taxonomy = null;
	switch (link.getDomain()) {
	case "wikipedia.org":
	    taxonomy = wikipedia.getLifeTaxonomy(link.getBasename());
	    break;

	default:
	    return null;
	}

	AtlasCollection collection = atlasDao.getCollectionByLinkSource(link.getLinkSource().getId());
	List<Taxon> taxaList = new ArrayList<>();
	for (TaxonMeta taxonMeta : collection.getTaxonomyMeta()) {
	    for (Map.Entry<String, String> taxon : taxonomy.entrySet()) {
		if (!taxonMeta.getName().equals(taxon.getKey())) {
		    continue;
		}
		if (taxonMeta.getValues() != null) {
		    if (!Strings.split(taxonMeta.getValues(), ',').contains(taxon.getValue())) {
			continue;
		    }
		}
		taxaList.add(new Taxon(taxon.getKey(), taxon.getValue()));
		break;
	    }
	}
	return taxaList;
    }

    @Override
    @API(name = "features", description = "A feature is a named characteristic with a physical quantity.")
    public List<Feature> getFeatures(Link link) {
	AtlasCollection collection = atlasDao.getCollectionByLinkSource(link.getLinkSource().getId());
	List<Feature> features = new ArrayList<>();

	switch (link.getDomain()) {
	case "wikipedia.org":
	    Map<String, Double> nutrients = wikipedia.getEdibleNutrients(link.getBasename());
	    for (FeatureMeta meta : collection.getFeaturesMeta()) {
		// to avoid full scan we can create a name resolver with hash map
		// but at current sizes is not really helping
		for (String label : nutrients.keySet()) {
		    // all feature meta names related to nutrients have at least one dot
		    // if not used dot on name scanning there is confusion between 'saturated' and
		    // 'monounsaturated' because both ends with 'saturated'
		    if (meta.getName().endsWith("." + Strings.toDotCase(label))) {
			Feature feature = new Feature(meta, nutrients.get(label));
			feature.postLoad();
			features.add(feature);
			break;
		    }
		}
	    }
	    break;

	case "animalia.bio":
	    List<PhysicalTrait> traits = animalia.getPhysicalTraits(link.getBasename());
	    for (FeatureMeta meta : collection.getFeaturesMeta()) {
		for (PhysicalTrait trait : traits) {
		    if(meta.getName().equals(trait.getName())) {
			Feature feature = new Feature(meta, trait.getValue(), trait.getMaximum());
			feature.postLoad();
			features.add(feature);
			break;
		    }
		}		
	    }
	    break;

	default:
	    return null;
	}
	return features;
    }
}
