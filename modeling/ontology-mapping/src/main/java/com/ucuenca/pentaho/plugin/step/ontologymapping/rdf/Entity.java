package com.ucuenca.pentaho.plugin.step.ontologymapping.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Entity<K, V> {
	
	private String key;

	private Map<K, V> annotation;
	
	private Map<K, V>relation;
	
	private List<Object[]> entityRows; 
	
	public Entity(String key) {
		this.key = key;
		this.annotation = new HashMap<K, V>();
		this.relation = new HashMap<K, V>();
		this.entityRows = new ArrayList<Object[]>();
	}
	
	public List<Object[]> getEntityRows() {
		return entityRows;
	}
	
	public void addEntityRow(Object[] row) {
		entityRows.add(row);
	}
	
	
	public String getKey() {
		return key;
	}


	public void setKey(String pKey) {
		this.key = pKey;
	}


	public Map<K, V> getAnnotations()throws Exception {
		return this.annotation;
	}
	
	public void addAnnotation(K key, V value)throws Exception {
		annotation.put(key, value);
		
	}
	
	public Map<K, V> getRelations()throws Exception {
		return this.relation;
	}
	
	public void addRelatino(K key, V value)throws Exception {
		this.relation.put(key, value);
		
	}
}
