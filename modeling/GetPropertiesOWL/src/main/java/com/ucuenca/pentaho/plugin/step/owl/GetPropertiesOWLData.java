/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.ucuenca.pentaho.plugin.step.owl;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


import com.ucuenca.misctools.StepDataLoader;
import com.ucuenca.misctools.DatabaseLoader;
import com.hp.hpl.jena.shared.*;
/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepDataInterface.
 *   
 * Implementing classes inherit from BaseStepData, which implements the entire
 * interface completely. 
 * 
 * In addition classes implementing this interface usually keep track of
 * per-thread resources during step execution. Typical examples are:
 * result sets, temporary data, caching indexes, etc.
 *   
 * The implementation for the demo step stores the output row structure in 
 * the data class. 
 *   
 */
public class GetPropertiesOWLData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;
	
	
	//public OntModel model;
	public OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	// must be included for DataBase Data Loading
		public static final String DBTABLE = "GetProperDATA";
		private static Class<?> PKG = GetPropertiesOWLMeta.class; 
		private final StepDataLoader dataLoader = new StepDataLoader(DBTABLE);
		// End Database Data Loading attributes
		
		protected LogChannelInterface log;
		public boolean first;
		int ii = 0;
		
	public StepDataLoader getDataLoader() {
		return this.dataLoader;
	}
	
    public GetPropertiesOWLData()
	{
		super();
	}
    public void logError(String message) {
		log.logError(message);
	}
    
	public boolean getData(StepMetaInterface smi, StepDataInterface sdi,
			Boolean databaseLoad) throws KettleException  {

		// safely cast the step settings (meta) and runtime info (data) to
		// specific implementations
		GetPropertiesOWLMeta meta = (GetPropertiesOWLMeta) smi;
		GetPropertiesOWLData data = (GetPropertiesOWLData) sdi;
		
		//validacion lleguen los datos 
		if(meta.getOutputField()==null||meta.getOutputField()=="[]"){
			dataLoader.logBasic(BaseMessages.getString(PKG,"GetPropertiesOWL.FieldName.Label"));
			
			return false;
		}else{
		dataLoader.logBasic("input are  "+ meta.getOutputField());
		}
		// must be included for DataBase Data Loading
		//data.outputRowMeta = data.outputRowMeta == null ? 
		//dataLoader.getMetaFieldsDef(smi):data.outputRowMeta;
		data.outputRowMeta = data.outputRowMeta == null ? 
		dataLoader.getMetaFieldsDef(smi):data.outputRowMeta;
		
		Object[] outputRow = new Object[data.outputRowMeta.size() + 3];
				
				
		if (databaseLoad){
			try {
				DatabaseLoader.getConnection();
			} catch (Exception e) {
				
				logError("Error in Database Loader "+e.getMessage());
			}
		}	
		
		boolean repetir = true;


		List<String> myList;

		myList = cleanspaces(meta.getOutputField());

		String mn = meta.getNameOntology();
		
		 String replace = mn.trim().replace("[", "");
		 String replace1 = replace.replace("]", "");
		ArrayList<String> myListNames = new ArrayList<String>(
				Arrays.asList(replace1.split(",")));
		
		String nameontology = myListNames.get(ii).toString();

		
		try {
			dataLoader.logBasic("work in model " +myList.get(ii));
			data.model.read(myList.get(ii));
			dataLoader.logBasic("the load model is ok");
		} catch (Exception eox) {
			dataLoader.logBasic(" ERROR " + eox +" Unload model " +myListNames.get(ii) +" from  [" + myList.get(ii));
			//Model model2 = FileManager.get().loadModel(myList.get(ii), myListNames.get(ii), "RDF/XML");
			//logError(" ERROR " + eox +" Unload model " +myListNames.get(ii) +" from URI [" + myList.get(ii));
			//data.model = (OntModel) FileManager.get().loadModel(myList.get(ii),"RDF/XML");
		}

		
		if (data.model.isEmpty()) { // par a ver si esta cargado el modelo
			// setOutputDone();
			return false;
			// the "first" flag is inherited from the base step implementation
		}


		if (first) { // all ok for the momento
			first = false;
		}

		
		if (!data.model.isEmpty()) {
			for (Iterator<OntClass> i = data.model.listClasses(); i.hasNext();) {
				OntClass cls = i.next();
				if(!i.hasNext()){dataLoader.logBasic("there are clases");}
				if (cls.getLocalName() != null) { // Para que no se recorran
													// clases vacias

					// outputRow[0]=cls.getURI()+" "+cls.getLocalName();//para
					// que salgan los nombres
					// System.out.println(cls.getNameSpace());
					// outputRow[0] = "Ontologia" + myList.get(ii);

					dataLoader.sequence++;
					int dataIndex = databaseLoad ? 3:0; 
					outputRow[dataIndex] = nameontology.trim();
					outputRow[dataIndex+1] = cls.getURI();
					outputRow[dataIndex+2] = "rdf:type";
					outputRow[dataIndex+3] = "rdfs:class";
/**
					outputRow[0] = nameontology;
					outputRow[1] = cls.getURI();
					outputRow[2] = "rdf:type";
					outputRow[3] = "rdfs:class";
*/
					if (databaseLoad) {
						if(!i.hasNext()){dataLoader.logBasic("to database is true");}
						
						outputRow[0] = meta.getTransMeta().getName().toUpperCase();
						outputRow[1] = meta.getStepName().toUpperCase();
						outputRow[2] = Integer.valueOf(dataLoader.sequence);
						
						try {
							dataLoader.insertTableRow(smi, outputRow);
						} catch (Exception e) {
							
							dataLoader.logBasic("Error in dataLoader.insertTableRow" + e.getMessage()+" in the model is  "+myListNames.get(ii)) ;
						}
					} else {
						
						dataLoader.getBaseStep().putRow(data.outputRowMeta,outputRow);
						// // put the row to the output row stream
						// putRow(data.outputRowMeta, outputRow);
					}

				}
			}// end for to search classes

			// to search properties
			for (Iterator<OntProperty> j = data.model.listAllOntProperties(); j
					.hasNext();) {
				if(!j.hasNext()){dataLoader.logBasic("there are properties");}
				OntProperty proper = j.next();
				if (proper.getLocalName() != null) {
					dataLoader.sequence++;
					int dataIndex = databaseLoad ? 3:0; 
					outputRow[dataIndex] = nameontology.trim();
					outputRow[dataIndex+1] = proper.getURI();
					outputRow[dataIndex+2] = "rdf:type";
					outputRow[dataIndex+3] = "rdfs:property";
					/**
					dataLoader.sequence++;
					outputRow[0] = nameontology;
					outputRow[1] = proper.getURI();
					outputRow[2] = "rdf:type";
					outputRow[3] = "rdfs:property";**/
					if (databaseLoad) {
						
						if(!j.hasNext()){dataLoader.logBasic("to database is true");}
						outputRow[0] = meta.getTransMeta().getName().toUpperCase();
						outputRow[1] = meta.getStepName().toUpperCase();
						outputRow[2] = Integer.valueOf(dataLoader.sequence);
						try {
							dataLoader.insertTableRow(smi, outputRow);
						} catch (Exception e) {

							dataLoader.logBasic("Error in dataLoader.inserTableRow"+e.getMessage()+" in the model is  "+myListNames.get(ii));
						}
					} else {
					
						
						dataLoader.getBaseStep().putRow(data.outputRowMeta,outputRow);
						
						
						// // put the row to the output row stream
						// putRow(data.outputRowMeta, outputRow);
					}
				}
			}
		}// fin comprobacion

		// }// fin del for
		ii++;
		if (ii >= myList.size()) {
			repetir = false;
			if(databaseLoad) // to close theconnection
				try {
					DatabaseLoader.closeConnection();
				} catch (Exception e) {
					dataLoader.logBasic("Error when DatabaseLoader.closeConnection() "+e.getMessage()+" in the model is  "+myList.get(ii));
					
				}
		}



		// indicate that processRow() should be called again
		return repetir;// to do it only one time

	}
	 public List<String> cleanspaces(String limpiar){
			
			//logError("nameontology "+meta.getNameOntology());
			String replace = limpiar.replace("[", "");
			
			String replace1 = replace.replace("]", "");

			String replace2 = replace1.replaceAll("\\s+", "");

			List<String> myList = new ArrayList<String>(Arrays.asList(replace2
					.split(",")));

			
			return myList;
	 }
	 
	 public String cleanSlash(String limpiar){
		 String nameontology = null;
		 StringTokenizer st2 = new StringTokenizer(limpiar.toString(),
					"/");
			int nutok = st2.countTokens();
			int cont = 0;

			while (st2.hasMoreTokens()) {

				nameontology = st2.nextToken();
				if (cont == nutok - 2) {
					nameontology = st2.nextToken();
					break;
				}
				;
				cont++;
			}
		 
		 return nameontology;
	 }
    
    
    
}
	
