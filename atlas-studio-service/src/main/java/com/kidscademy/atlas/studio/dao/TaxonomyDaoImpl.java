package com.kidscademy.atlas.studio.dao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import js.transaction.Transactional;
import js.util.Strings;

@Transactional(schema = "itis")
public class TaxonomyDaoImpl implements TaxonomyDao {
    // base on integrated taxonomic information system - https://www.itis.gov/

    private final EntityManager em;

    public TaxonomyDaoImpl(EntityManager em) {
	this.em = em;
    }

    @Override
    public List<Integer> getObjectHierarchy(String binomialName) {
	String sql = "SELECT hierarchy FROM object_hierarchy WHERE binomialName=?1";
	String hierarchy = (String) em.createNativeQuery(sql).setParameter(1, binomialName).getSingleResult();
	return Strings.split(hierarchy, ",", Integer.class);
    }

    @Override
    public Map<String, String> getObjectTaxonomy(String binomialName) {
	String sql = "SELECT hierarchy FROM object_hierarchy WHERE binomialName=?1";
	@SuppressWarnings("unchecked")
	List<String> hierarchy = em.createNativeQuery(sql).setParameter(1, binomialName).getResultList();
	if (hierarchy.isEmpty()) {
	    return Collections.emptyMap();
	}

	sql = "SELECT DISTINCT " + //
		"t.rank_name AS taxonName," + //
		"u.complete_name AS taxonValue " + //
		"FROM itis.taxonomic_units u " + //
		"JOIN itis.taxon_unit_types t ON u.rank_id=t.rank_id " + //
		"WHERE u.tsn IN(%s) " + //
		"ORDER BY t.rank_id;";

	@SuppressWarnings("unchecked")
	List<Object[]> resultSet = em.createNativeQuery(String.format(sql, hierarchy.get(0))).getResultList();

	LinkedHashMap<String, String> taxonomy = new LinkedHashMap<>();
	for (Object[] resultRow : resultSet) {
	    taxonomy.put((String) resultRow[0], (String) resultRow[1]);
	}
	return taxonomy;
    }
}