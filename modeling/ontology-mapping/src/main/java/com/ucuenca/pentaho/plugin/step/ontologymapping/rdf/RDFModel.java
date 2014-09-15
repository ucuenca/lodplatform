package com.ucuenca.pentaho.plugin.step.ontologymapping.rdf;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.VCARD;

public class RDFModel {
	
	private String baseURI;
	
	private String[] fields;
	
	private RowMetaInterface fieldsMeta;

	
	private Model model;
	
	// some definitions
    static String tutorialURI  = "http://hostname/rdf/tutorial/";
    static String briansName   = "Brian McBride";
    static String briansEmail1 = "brian_mcbride@hp.com";
    static String briansEmail2 = "brian_mcbride@hpl.hp.com";
    static String title        = "An Introduction to RDF and the Jena API";
    static String date         = "23/01/2001";
    
    public RDFModel(String baseURI, String[] fields) {
    	this.baseURI = baseURI;
    	this.fields = fields;
    	this.model = ModelFactory.createDefaultModel(); 
    }
    
    public RDFModel(String baseURI, RowMetaInterface fieldsMeta) {
    	this.baseURI = baseURI;
    	this.fieldsMeta = fieldsMeta;
    	this.model = ModelFactory.createDefaultModel(); 
    }
    
    public void process(Entity<String, String> entity) {
    	List<Object[]> rows = entity.getEntityRows();
    	for(Object[]row :rows) {
    		switch(((Integer)row[1]).intValue()) {
    			case 5:
    		}
    	}
    	
    }
    
    private void processSubfields() {
    	
    }
    
    
    
    
    public static void main (String args[]) {
    
    	/*
        String personURI    = "http://somewhere/JohnSmith";
        String givenName    = "John";
        String familyName   = "Smith";
        String fullName     = givenName + " " + familyName;

        Model model = ModelFactory.createDefaultModel();

        Resource johnSmith 
          = model.createResource(personURI)
                 .addProperty(VCARD.FN, fullName)
                 .addProperty(VCARD.N, 
                              model.createResource()
                                   .addProperty(VCARD.Given, givenName)
                                   .addProperty(VCARD.Family, familyName));
        
        model.write(System.out);*/
    	// create an empty model
    	 Model model = ModelFactory.createDefaultModel();

    	 // use the FileManager to find the input file
    	 
    	 //InputStream in = FileManager.get().open( "http://www.w3.org/ns/r2rml#" );
    	/*if (in == null) {
    	    throw new IllegalArgumentException(
    	                                 "File: not found");
    	}*/

    	String ontology = "http://iflastandards.info/ns/fr/frbr/frbrer/";
    	//ontology = "http://www.w3.org/ns/r2rml#";
    	// read the RDF/XML file
    	 model.read(ontology);
    	//model.read(in, null);

    	// write it to standard out
    	model.write(System.out);
    }

}
