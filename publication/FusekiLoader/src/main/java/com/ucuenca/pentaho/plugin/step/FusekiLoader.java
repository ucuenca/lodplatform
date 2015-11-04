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


package com.ucuenca.pentaho.plugin.step;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

//-----------------------
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.FusekiVocab;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.query.Dataset;

//----------------------------

/** .
 * @author Fabian Peñaloza Marin
 * @version 1
 */
/**
 * This class is part of the demo step plug-in implementation. It demonstrates
 * the basics of developing a plug-in step for PDI.
 * 
 * The demo step adds a new string field to the row stream and sets its value to
 * "Hello World!". The user may select the name of the new field.
 * 
 * This class is the implementation of StepInterface. Classes implementing this
 * interface need to:
 * 
 * - initialize the step - execute the row processing logic - dispose of the
 * step
 * 
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data
 * interface instead.
 * 
 */

public class FusekiLoader extends BaseStep implements StepInterface {

	private Model fModel = ModelFactory.createDefaultModel();

	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s
	 *            step description
	 * @param stepDataInterface
	 *            step data class
	 * @param c
	 *            step copy
	 * @param t
	 *            transformation description
	 * @param dis
	 *            transformation executing
	 */
	public FusekiLoader(StepMeta s, StepDataInterface stepDataInterface, int c,
			TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	/**
	 * This method is called by PDI during transformation startup.
	 * 
	 * It should initialize required for step execution.
	 * 
	 * The meta and data implementations passed in can safely be cast to the
	 * step's respective implementations.
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a
	 * database, as wall as obtaining resources, like file handles.
	 * 
	 * @param smi
	 *            step meta interface implementation, containing the step
	 *            settings
	 * @param sdi
	 *            step data interface implementation, used to store runtime
	 *            information
	 * 
	 * @return true if initialization completed successfully, false if there was
	 *         an error preventing the step from working.
	 * 
	 */
	/*
	 * public boolean init(StepMetaInterface smi, StepDataInterface sdi) { //
	 * Casting to step-specific implementation classes is safe FusekiLoaderMeta
	 * meta = (FusekiLoaderMeta) smi; FusekiLoaderData data = (FusekiLoaderData)
	 * sdi;
	 * 
	 * return super.init(meta, data); }
	 */
	/**
	 * Once the transformation starts executing, the processRow() method is
	 * called repeatedly by PDI for as long as it returns true. To indicate that
	 * a step has finished processing rows this method must call setOutputDone()
	 * and return false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a
	 * single row from the input stream, change or add row content, call
	 * putRow() to pass the changed row on and return true. If getRow() returns
	 * null, no more rows are expected to come in, and the processRow()
	 * implementation calls setOutputDone() and returns false to indicate that
	 * it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a
	 * call to RowDataUtil.allocateRowData(numberOfFields), add row content, and
	 * call putRow() to pass the new row on. Above process may happen in a loop
	 * to generate multiple rows, at the end of which processRow() would call
	 * setOutputDone() and return false;
	 * 
	 * @param smi
	 *            the step meta interface containing the step settings
	 * @param sdi
	 *            the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false
	 *         if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		
		
		// safely cast the step settings (meta) and runtime info (data) to
		// specific implementations
		FusekiLoaderMeta meta = (FusekiLoaderMeta) smi;
		FusekiLoaderData data = (FusekiLoaderData) sdi;
		
		System.out.println(meta.getFuGraph());
		System.out.println(meta.getFuQuery());
		getRow();
		String status = "ERROR";
		if (meta.getValidate().equals("true")) {
			// the "first" flag is inherited from the base step implementation
			// it is used to guard some processing tasks, like figuring out
			// field
			// indexes
			// in the row structure that only need to be done once
			if (first) {
				first = false;

				super.init(meta, data);
				data.outputRowMeta = getInputRowMeta() != null ? getInputRowMeta().clone(): new RowMeta();
				//data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
				try {
					data.model.read(meta.getOutputField().trim());

				} catch (Exception eox) {
					logBasic(" ERROR " + eox + " Unload model "
							+ meta.getOutputField());
				}
				//eliminar extension y escribir con el mismo pero diferente extension 
				String name = meta.getInputName().replaceFirst("[.][^.]+$", ""); 
				String FileName = name+".ttl";
				try {
		
					
					FileWriter out = new FileWriter(
							"plugins/steps/FusekiLoader/fuseki/Data/"
									+ FileName);
					data.model.write(out, "TTL");
					logBasic("mapping from " + meta.getOutputField()
							+ " is Ok in "
							+ "plugins/steps/FusekiLoader/fuseki/Data/"
							+ meta.getInputName());
				} catch (FileNotFoundException e) {
					System.out.println(e);
				} catch (IOException e) {

					e.printStackTrace();
					logBasic(" ERROR " + e);
				}

				// create mapping.ttl-------------------------------------
				createmapping(meta,FileName);

				logBasic(" config.ttl is ok ");

				try {
					/**
					 * File source=new
					 * File("plugins/steps/FusekiLoader/fuseki.war"); File
					 * destination=new File(meta.getDirectory()+"/fuseki.war");
					 */

					// create destination
					File dir = new File(meta.getDirectory() + "/fuseki");
					dir.mkdir();
					//
					File source = new File("plugins/steps/FusekiLoader/fuseki");
					File destination = new File(meta.getDirectory() + "/fuseki");

					recursiveCopy(source, destination);
					// copyFile(source,destination);

					logBasic("The file was build succesfully in "
							+ meta.getDirectory() + "/" + "fuseki");

				} catch (Exception e1) {

					logBasic(" ERROR " + e1 + "The File was not created. in "
							+ meta.getDirectory() + "/" + "fuseki");
				}

			}

			// safely add the string "Hello World!" at the end of the output row
			// the row array will be resized if necessary

			// getinputrowmeta (directorio , ok);
			// setoutputdown
			status = "OK";

			// put the row to the output row stream
			// putRow(meta.get, outputRow);
			//Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta
			//		.size());
			
			Object[] outputRow = new Object[2];

			outputRow[0] = meta.getDirectory() + "/fuseki";
			outputRow[1] = status;
			putRow(data.outputRowMeta, outputRow);

			// log progress if it is time to to so
			if (checkFeedback(getLinesRead())) {
				logBasic("Linenr " + getLinesRead()); // Some basic logging
			}
		} else {

			logBasic(" ERROR Tranformation dont started because parameters es empty");

			status = "ERROR";
		}
		// indicate that processRow() should be called again
		return false;
	}

	private void createmapping(FusekiLoaderMeta meta, String nombre) {
		
		
		
		Resource resource3 = fModel.createResource("file:Data/"
				+ nombre);
		
		Resource resourceModel = fModel.createResource("#model");
				resourceModel.addProperty(RDF.type,ja.MemoryModel)
							.addProperty(ja.content, fModel.createResource()
													.addProperty(ja.externalContent, resource3)
									)	
				;
		
		Resource resourceModel0 = fModel.createResource("#model0");
				resourceModel0.addProperty(RDF.type,ja.MemoryModel)
							.addProperty(ja.content, fModel.createResource()
													.addProperty(ja.externalContent, resource3)
									)	
				;
				
		//configuracion Dataset
		Resource resource2 = fModel.createResource("#" + meta.getFuDataset());
		
		//base Uri RESOURCE
		Resource resourceBaseUri = fModel.createResource(meta.getFubaseURI());
		
		try {
		resource2
				.addProperty(RDF.type, ja.RDFDataset)
				//.addProperty(RDFS.label, meta.getFuDataset())
				.addProperty(
						ja.defaultGraph,
						resourceModel0)
				.addProperty(ja.namedGraph, fModel.createResource()
											.addProperty(ja.graphName, resourceBaseUri)
											.addProperty(ja.graph, resourceModel)
						)		
						;
		}catch(InvalidPropertyURIException e){
			logBasic(" ERROR Invalida Base URI "+ meta.getFubaseURI());
		}
		/* 
		 *
		 * .addProperty(
						ja.defaultGraph,
						fModel.createResource()
								.addProperty(RDFS.label, meta.getInputName())
								.addProperty(RDF.type, ja.MemoryModel)
								.addProperty(
										ja.content,
										fModel.createResource().addProperty(
												ja.externalContent, resource3)));*
		 */
		 

		Resource resource1 = fModel.createResource("#service1");

		resource1
				.addProperty(RDF.type, fuseki.Service)
				.addProperty(FusekiVocab.pServiceName, meta.getServiceName())
			//	.addProperty(FusekiVocab.pServiceQueryEP, meta.getFuQuery())
			//	.addProperty(FusekiVocab.pServiceReadgraphStoreEP,
			//			meta.getFuGraph())
				.addProperty(fuseki.dataset, resource2);
		/*
		for (int i= 0; i <){
			
		}*/
		List<String> listaPropiedades;
		listaPropiedades = cleanspaces(meta.getListaPropiedades());
		List<String> listaValores;
		listaValores = cleanspaces(meta.getListaValores());
		String datasetlabel= "";
                //JO adding fulltext support
                List<String> indexProperties=null;
                //JO*
		for (int i= 0; i < listaPropiedades.size(); i++){ //obtener valores del combo
			if (listaPropiedades.get(i).compareTo("fuseki:dataset")==0){
				datasetlabel =  listaValores.get(i);
			}
			if (listaPropiedades.get(i).compareTo("fuseki:serviceQuery")==0){
				resource1.addProperty(FusekiVocab.pServiceQueryEP, listaValores.get(i));
			}
			if (listaPropiedades.get(i).compareTo("fuseki:serviceReadWriteGraphStore")==0){
				resource1.addProperty(FusekiVocab.pServiceReadWriteGraphStoreEP, listaValores.get(i));
			}
			if (listaPropiedades.get(i).compareTo("fuseki:serviceUpload")==0){
				resource1.addProperty(FusekiVocab.pServiceUploadEP, listaValores.get(i));
			}
			if (listaPropiedades.get(i).compareTo("fuseki:serviceUpdate")==0){
				resource1.addProperty(FusekiVocab.pServiceUpdateEP, listaValores.get(i));
			}
			if (listaPropiedades.get(i).compareTo("fuseki:serviceReadGraphStore")==0){
				resource1.addProperty(FusekiVocab.pServiceReadgraphStoreEP, listaValores.get(i));
			}
		//JO adding fulltext support
                        if (listaPropiedades.get(i).compareTo("lucene:fulltext")==0){
				//resource1.addProperty(FusekiVocab.pServiceReadgraphStoreEP, listaValores.get(i));
                            String properties[] = listaValores.get(i).split(";");
                            indexProperties=Arrays.asList(properties);
			}
			//JO*
		}
		resource2.addProperty(RDFS.label, datasetlabel);
		
		FileWriter out;
		try {
			out = new FileWriter("plugins/steps/FusekiLoader/fuseki/"
					+ "config.ttl");
			fModel.write(out, "TTL");

		} catch (IOException e) {

			logBasic(e.toString());

		}
//JO adding fulltexy support
                String fulltext="";
                boolean applyFullText = false;
                if (indexProperties!=null && !indexProperties.isEmpty()){
                    applyFullText=true;
                    int countIndexProperties=0;
                    for (String pr: indexProperties){
                        countIndexProperties++;
                        if (countIndexProperties ==1 ){
                            fulltext += "[ text:field \"text\" ; text:predicate <"+pr+"> ]\n";
                        }else{
                            fulltext += "[ text:field \"text_"+countIndexProperties+"\" ; text:predicate <"+pr+"> ]\n";
                        }
                        
                    }
                }
                
                String confiAdd="## Initialize text query\n" +
"[] ja:loadClass       \"org.apache.jena.query.text.TextQuery\" .\n" +
"# A TextDataset is a regular dataset with a text index.\n" +
"text:TextDataset      rdfs:subClassOf   ja:RDFDataset .\n" +
"# Lucene index\n" +
"text:TextIndexLucene  rdfs:subClassOf   text:TextIndex .\n" +
"# Solr index\n" +
"text:TextIndexSolr    rdfs:subClassOf   text:TextIndex .\n" +
"\n" +
":text_dataset rdf:type     text:TextDataset ;\n" +
"    text:dataset   <#myds> ;\n" +
"    text:index     <#indexLucene> ;\n" +
"    .\n" +
"\n" +
"<#indexLucene> a text:TextIndexLucene ;\n" +
"    text:directory <file:Lucene> ;\n" +
"    ##text:directory \"mem\" ;\n" +
"    text:entityMap <#entMap> ;\n" +
"    .\n" +
"# Mapping in the index\n" +
"# URI stored in field \"uri\"\n" +
"# rdfs:label is mapped to field \"text\"\n" +
"<#entMap> a text:EntityMap ;\n" +
"    text:entityField      \"uri\" ;\n" +
"    text:defaultField     \"text\" ;\n" +
"    text:map (\n" +fulltext+

                        
"         ) .";
                
                if(!applyFullText){
                    confiAdd="";
                }
                
                //JO*
		try {
			File file = new File(
					"plugins/steps/FusekiLoader/fuseki/configO.ttl");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "", oldtext = "";
			while ((line = reader.readLine()) != null) {
				oldtext += line + "\r\n";
			}
			reader.close();

			file = new File("plugins/steps/FusekiLoader/fuseki/config.ttl");
			BufferedReader reader2 = new BufferedReader(new FileReader(file));
			String line2 = "", oldtext2 = "";
			while ((line2 = reader2.readLine()) != null) {
				oldtext2 += line2 + "\r\n";
			}
			reader2.close();

			String newtext = oldtext +"\n"+ confiAdd+"\n" + oldtext2;

                        
                        if(applyFullText){
                            newtext = newtext.replaceAll("                <#myds> ;", "                :text_dataset ;");
                        }
                        
			FileWriter writer = new FileWriter(
					"plugins/steps/FusekiLoader/fuseki/config.ttl");
			writer.write(newtext);
			writer.close();

		} catch (IOException ioe) {
			logBasic(" ERROR " + ioe + "The File config.ttl was not created. ");
			ioe.printStackTrace();

		}

	}

	/**
	 * This method is called by PDI once the step is done processing.
	 * 
	 * The dispose() method is the counterpart to init() and should release any
	 * resources acquired for step execution like file handles or database
	 * connections.
	 * 
	 * The meta and data implementations passed in can safely be cast to the
	 * step's respective implementations.
	 * 
	 * It is mandatory that super.dispose() is called to ensure correct
	 * behavior.
	 * 
	 * @param smi
	 *            step meta interface implementation, containing the step
	 *            settings
	 * @param sdi
	 *            step data interface implementation, used to store runtime
	 *            information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		FusekiLoaderMeta meta = (FusekiLoaderMeta) smi;
		FusekiLoaderData data = (FusekiLoaderData) sdi;

		super.dispose(meta, data);
	}

	private void recursiveCopy(File fSource, File fDest) {
		try {
			if (fSource.isDirectory()) {
				// A simple validation, if the destination is not exist then
				// create it
				if (!fDest.exists()) {
					fDest.mkdirs();
				}

				// Create list of files and directories on the current source
				// Note: with the recursion 'fSource' changed accordingly
				String[] fList = fSource.list();

				for (int index = 0; index < fList.length; index++) {
					File dest = new File(fDest, fList[index]);
					File source = new File(fSource, fList[index]);

					// Recursion call take place here
					recursiveCopy(source, dest);
				}
			} else {
				// Found a file. Copy it into the destination, which is already
				// created in 'if' condition above
				fSource.setExecutable(true); // code to set permises

				// Open a file for read and write (copy)
				FileInputStream fInStream = new FileInputStream(fSource);
				FileOutputStream fOutStream = new FileOutputStream(fDest);

				// Read 2K at a time from the file
				byte[] buffer = new byte[2048];
				int iBytesReads;

				// In each successful read, write back to the source
				while ((iBytesReads = fInStream.read(buffer)) >= 0) {
					fOutStream.write(buffer, 0, iBytesReads);

				}

				// Safe exit
				if (fInStream != null) {
					fInStream.close();
				}

				if (fOutStream != null) {
					fOutStream.close();
				}

				fDest.setExecutable(true); // code to set permises
			}
		} catch (Exception ex) {
			// Please handle all the relevant exceptions here
		}
	}
	
    /**
     * Metodo para limpiar y pasarle a una lista de string
     * @param limpiar
     * @return
     */
	public List<String> cleanspaces(String limpiar) {

		// logError("nameontology "+meta.getNameOntology());
		String replace = limpiar.replace("[", "");

		String replace1 = replace.replace("]", "");

		String replace2 = replace1.replaceAll("\\s+", "");

		List<String> myList = new ArrayList<String>(Arrays.asList(replace2
				.split(",")));

		return myList;
	}

}
