package com.ucuenca.pentaho.plugin.step.r2rml;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RelationBean extends BeanInterface{
	
	
	@Bean(fieldName = "ID")
	private String id;
	@Bean(fieldName = "ENTITY_CLASSID_1")
	private String entityclassid1;
	@Bean(fieldName = "ONTOLOGY")
	private String ontology;
	@Bean(fieldName = "PROPERTY")
	private String property;
	@Bean(fieldName = "ENTITY_CLASSID_2")
	private String entityclassid2;
	
	public RelationBean(ResultSet rs) throws SQLException {
		this.setId(rs.getString(1));
		this.setEntityclassid1(rs.getString(2));
		this.setOntology(rs.getString(3));
		this.setProperty(rs.getString(4));
		this.setEntityclassid2(rs.getString(5));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEntityclassid1() {
		return entityclassid1;
	}

	public void setEntityclassid1(String entityclassid) {
		this.entityclassid1 = entityclassid;
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
	public String getEntityclassid2() {
		return entityclassid2;
	}

	public void setEntityclassid2(String entityclassid) {
		this.entityclassid2 = entityclassid;
	}
	
}
