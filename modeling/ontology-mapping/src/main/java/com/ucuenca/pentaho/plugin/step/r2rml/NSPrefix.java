package com.ucuenca.pentaho.plugin.step.r2rml;

public enum NSPrefix {
	
	RR("rr", "http://www.w3.org/ns/r2rml#"),
	RDF("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
	RDFS("rdfs", "http://www.w3.org/2000/01/rdf-schema#"),
	XSD("xsd", "http://www.w3.org/2001/XMLSchema#");
	
	private String prefix;
	private String URI;
	
	private NSPrefix(String prefix, String URI) {
		this.prefix = prefix;
		this.URI = URI;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getURI() {
		return URI;
	}
	
	

}
