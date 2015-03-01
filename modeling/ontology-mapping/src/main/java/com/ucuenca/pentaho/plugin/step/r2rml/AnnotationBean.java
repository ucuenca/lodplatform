package com.ucuenca.pentaho.plugin.step.r2rml;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AnnotationBean extends BeanInterface{
	
	
	@Bean(fieldName = "ID")
	private String id;
	@Bean(fieldName = "ENTITY_CLASSID")
	private String entityclassid;
	@Bean(fieldName = "ONTOLOGY")
	private String ontology;
	@Bean(fieldName = "PROPERTY")
	private String property;
	@Bean(fieldName = "DATAFIELD")
	private String datafield;
	@Bean(fieldName = "EXTRACTIONFIELD")
	private String extractionfield;
	@Bean(fieldName = "DATAVALUE")
	private String datavalue;
	@Bean(fieldName = "DATATYPE")
	private String datatype;
	@Bean(fieldName = "LANGUAGE")
	private String language;
	
	public AnnotationBean(ResultSet rs) throws SQLException {
		this.setId(rs.getString(1));
		this.setEntityclassid(rs.getString(2));
		this.setOntology(rs.getString(3));
		this.setProperty(rs.getString(4));
		this.setExtractionfield(rs.getString(5));
		this.setDatafield(rs.getString(6));
		this.setDatavalue(rs.getString(7));
		this.setDatatype(rs.getString(8));
		this.setLanguage(rs.getString(9));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEntityclassid() {
		return entityclassid;
	}

	public void setEntityclassid(String entityclassid) {
		this.entityclassid = entityclassid;
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

	public String getExtractionfield() {
		return extractionfield;
	}

	public void setExtractionfield(String extractionfield) {
		this.extractionfield = extractionfield;
	}

	public String getDatafield() {
		return datafield;
	}

	public void setDatafield(String datafield) {
		this.datafield = datafield;
	}

	public String getDatavalue() {
		return datavalue;
	}

	public void setDatavalue(String datavalue) {
		this.datavalue = datavalue;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
}
