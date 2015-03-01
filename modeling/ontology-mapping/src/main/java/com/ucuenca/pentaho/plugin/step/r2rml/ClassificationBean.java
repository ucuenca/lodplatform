package com.ucuenca.pentaho.plugin.step.r2rml;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClassificationBean extends BeanInterface{
	
	
	public static final String ID = "ID";
	public static final String ONTOLOGY = "ONTOLOGY";
	public static final String ENTITY = "ENTITY";
	public static final String RELATIVEURI = "RELATIVE_URI";
	public static final String URIFIELDID = "URI_FIELD_ID";
	public static final String DATAFIELD1 = "DATAFIELD_1";
	public static final String DATAVALUE1 = "DATAVALUE_1";
	public static final String DATAFIELD2 = "DATAFIELD_2";
	public static final String DATAVALUE2 = "DATAVALUE_2";
	public static final String DATAFIELD3 = "DATAFIELD_3";
	public static final String DATAVALUE3 = "DATAVALUE_3";
	
	@Bean(fieldName = ID)
	private String id;
	@Bean(fieldName = ONTOLOGY)
	private String ontology;
	@Bean(fieldName = ENTITY)
	private String entity;
	@Bean(fieldName = RELATIVEURI)
	private String relativeuri;
	@Bean(fieldName = URIFIELDID)
	private String uriFieldid; 
	@Bean(fieldName = DATAFIELD1)
	private String datafield1;
	@Bean(fieldName = DATAVALUE1)
	private String datavalue1;
	@Bean(fieldName = DATAFIELD2)
	private String datafield2; 
	@Bean(fieldName = DATAVALUE2)
	private String datavalue2; 
	@Bean(fieldName = DATAFIELD3)
	private String datafield3; 
	@Bean(fieldName = DATAVALUE3)
	private String datavalue3;
	
	public ClassificationBean(ResultSet rs) throws SQLException {
		this.setId(rs.getString(1));
		this.setOntology(rs.getString(2));
		this.setEntity(rs.getString(3));
		this.setRelativeUri(rs.getString(4));
		this.setUriFieldId(rs.getString(5));
		this.setDatafield1(rs.getString(6));
		this.setDatavalue1(rs.getString(7));
		this.setDatafield2(rs.getString(8));
		this.setDatavalue2(rs.getString(9));
		this.setDatafield3(rs.getString(10));
		this.setDatavalue3(rs.getString(11));
	}

	public String getRelativeUri() {
		return relativeuri;
	}

	public void setRelativeUri(String relativeUri) {
		this.relativeuri = relativeUri;
	}

	public String getUriFieldId() {
		return uriFieldid;
	}

	public void setUriFieldId(String uriFieldId) {
		this.uriFieldid = uriFieldId;
	}

	public String getDatafield1() {
		return datafield1;
	}

	public void setDatafield1(String datafield1) {
		this.datafield1 = datafield1;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getDatavalue1() {
		return datavalue1;
	}

	public void setDatavalue1(String datavalue1) {
		this.datavalue1 = datavalue1;
	}

	public String getDatafield2() {
		return datafield2;
	}

	public void setDatafield2(String datafield2) {
		this.datafield2 = datafield2;
	}

	public String getDatavalue2() {
		return datavalue2;
	}

	public void setDatavalue2(String datavalue2) {
		this.datavalue2 = datavalue2;
	}

	public String getDatafield3() {
		return datafield3;
	}

	public void setDatafield3(String datafield3) {
		this.datafield3 = datafield3;
	}

	public String getDatavalue3() {
		return datavalue3;
	}

	public void setDatavalue3(String datavalue3) {
		this.datavalue3 = datavalue3;
	}
	
	

}
