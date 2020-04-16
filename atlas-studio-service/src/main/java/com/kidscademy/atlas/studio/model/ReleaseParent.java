package com.kidscademy.atlas.studio.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "\"RELEASE\"")
public class ReleaseParent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToMany
    @JoinTable(name="release_atlasitem", joinColumns = @JoinColumn(name = "release_id"),inverseJoinColumns = @JoinColumn(name = "objects_id"))
    private List<ReleaseChild> children;

    public int getId() {
	return id;
    }

    public List<ReleaseChild> getChildren() {
	return children;
    }
}