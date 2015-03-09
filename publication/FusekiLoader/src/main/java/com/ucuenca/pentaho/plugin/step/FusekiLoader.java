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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
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
import org.apache.jena.fuseki.*;
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
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.query.Dataset;

//----------------------------
/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepInterface.
 * Classes implementing this interface need to:
 * 
 * - initialize the step
 * - execute the row processing logic
 * - dispose of the step 
 * 
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data interface
 * instead.  
 * 
 */

public class FusekiLoader extends BaseStep implements StepInterface {

	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public FusekiLoader(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}
	
	/**
	 * This method is called by PDI during transformation startup. 
	 * 
	 * It should initialize required for step execution. 
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a database,
	 * as wall as obtaining resources, like file handles.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 * 
	 * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
	 *  
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		FusekiLoaderMeta meta = (FusekiLoaderMeta) smi;
		FusekiLoaderData data = (FusekiLoaderData) sdi;

		return super.init(meta, data);
	}	

	/**
	 * Once the transformation starts executing, the processRow() method is called repeatedly
	 * by PDI for as long as it returns true. To indicate that a step has finished processing rows
	 * this method must call setOutputDone() and return false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a single row from the
	 * input stream, change or add row content, call putRow() to pass the changed row on 
	 * and return true. If getRow() returns null, no more rows are expected to come in, 
	 * and the processRow() implementation calls setOutputDone() and returns false to
	 * indicate that it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a call to
	 * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
	 * pass the new row on. Above process may happen in a loop to generate multiple rows,
	 * at the end of which processRow() would call setOutputDone() and return false;
	 * 
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		// safely cast the step settings (meta) and runtime info (data) to specific implementations 
		FusekiLoaderMeta meta = (FusekiLoaderMeta) smi;
		FusekiLoaderData data = (FusekiLoaderData) sdi;


		// the "first" flag is inherited from the base step implementation
		// it is used to guard some processing tasks, like figuring out field indexes
		// in the row structure that only need to be done once
		if (first) {
			first = false;
			try{
			data.model.read(meta.getOutputField().trim());
			
			
		
		} catch (Exception eox) {
		logBasic(" ERROR " + eox +" Unload model " +meta.getOutputField() );
		}

			
			
			
			try{
				
			FileWriter out = new FileWriter("plugins/steps/FusekiLoader/fuseki/Data/myrdf.rdf" );
			data.model.write(out, "RDF/XML");
			logBasic("mapping RDf is Ok" );
			}
        	catch (FileNotFoundException e) { System.out.println(e); } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logBasic(" ERROR " + e );
			}
			
			compile(meta.getDirectory());
			
			 File oldFile = new File("plugins/steps/FusekiLoader/fuseki.war");
			 	 
			            if (oldFile.renameTo(new File(meta.getDirectory()+"/"+ oldFile.getName()))) {
			             //   System.out.println("The file was build succesfully in "+meta.getDirectory()+"/"+ oldFile.getName());
			                logBasic("The file was build succesfully in "+meta.getDirectory()+"/"+ oldFile.getName());
			            } else {
			            	logBasic("The File was not created.");
			                // System.out.println("The File was not created.");
			             }
			
		}

		// safely add the string "Hello World!" at the end of the output row
		// the row array will be resized if necessary 
		//Object[] outputRow = RowDataUtil.addValueData(r, data.outputRowMeta.size() - 1, "Hello World!");

		// put the row to the output row stream
		//putRow(data.outputRowMeta, outputRow); 

		// log progress if it is time to to so
		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}

		// indicate that processRow() should be called again
		return false;
	}

	/**
	 * This method is called by PDI once the step is done processing. 
	 * 
	 * The dispose() method is the counterpart to init() and should release any resources
	 * acquired for step execution like file handles or database connections.
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.dispose() is called to ensure correct behavior.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		FusekiLoaderMeta meta = (FusekiLoaderMeta) smi;
		FusekiLoaderData data = (FusekiLoaderData) sdi;
		
		super.dispose(meta, data);
	}
	private void compile(String direc)
	   {
		
	        
	       File buildFile = new File("\\plugins\\steps\\FusekiLoader\\axis_bujava.xml");
	       //File buildFile = new File("/plugins/steps/FusekiLoader/axis_bujava.xml");
		     
	       Project antProject = new Project();
	       
	       DefaultLogger consoleLogger = new DefaultLogger();
	       consoleLogger.setErrorPrintStream(System.err);
	       consoleLogger.setOutputPrintStream(System.out);
	       consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
	       antProject.addBuildListener(consoleLogger);
	      try { 
	    	  antProject.fireBuildStarted();
	    	  
	       antProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
	       antProject.init();
	       ProjectHelper helper = ProjectHelper.getProjectHelper();
	       antProject.addReference("ant.ProjectHelper", helper);
	       helper.parse(antProject, buildFile);
	       String target = "build-war";
	       antProject.setUserProperty("build.dir", direc);
	       antProject.executeTarget(target);
	     
	     
	       antProject.fireBuildFinished(null);
	   } catch (BuildException e) {
		   antProject.fireBuildFinished(e);
		   logBasic(" ERROR " + e );
		  
	    }
	        
	       }

}
