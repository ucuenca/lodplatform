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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


public class OAIExtraerMeta extends BaseStepMeta implements StepMetaInterface {

	
	private static Class<?> PKG = OAIExtraerMeta.class; // for i18n purposes
	
	/**
	 * Stores the name of the field added to the row-stream. 
	 */
	
	private String inputURI;
	private String prefix;
	private ArrayList<String> listpath;
	private String xpath;	
	private String namespace;
	private String schema;


	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public OAIExtraerMeta() {
		super(); 
		this.listpath = new ArrayList<String>();
		prefix=null;
		xpath=null;
		schema=null;
		namespace=null;
	}
	
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new OAIExtraerDialog(shell, meta, transMeta, name);
	}

	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new OAIExtraer(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new OAIExtraerData();
	}	

	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault() {
		inputURI = "Input URI";		
		xpath="";		
		prefix="";
		namespace="";
		schema="";
	}
	

	public Object clone() {
		Object retval = super.clone();
		return retval;
	}
	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
	 * return value is an XML fragment consisting of one or more XML tags.  
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
	 * 
	 * @return a string containing the XML serialization of this step
	 */
	public String getXML() throws KettleValueException {
		
		// only one field to serialize
		String xml = XMLHandler.addTagValue("outputfield", inputURI);
		
	    StringBuffer retval = new StringBuffer( 400 );

	    retval.append( "    " ).append( XMLHandler.addTagValue( "inputURI", inputURI ) );
	    retval.append( "    " ).append( XMLHandler.addTagValue( "prefix", prefix ) );
	    retval.append( "    " ).append( XMLHandler.addTagValue( "xpath", xpath ) );
	    retval.append( "    " ).append( XMLHandler.addTagValue( "namespace", namespace ) );
	    retval.append( "    " ).append( XMLHandler.addTagValue( "schema", schema ) );
	    
		return retval.toString();
	}

	/**
	 * This method is called by PDI when a step needs to load its configuration from XML.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
	 * XML node passed in.
	 * 
	 * @param stepnode	the XML node containing the configuration
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {

		try {
			setInputURI(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "inputURI")));
			setPrefix(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "prefix")));
			setXpath(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "xpath")));
			setNamespace(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "namespace")));
			setSchema(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "schema")));
		} catch (Exception e) {
			throw new KettleXMLException("Demo plugin unable to read step info from XML node", e);
		}

	}	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to a repository.
	 * The repository implementation provides the necessary methods to save the step attributes.
	 *
	 * @param rep					the repository to save to
	 * @param id_transformation		the id to use for the transformation when saving
	 * @param id_step				the id to use for the step  when saving
	 */
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try{
			//rep.saveStepAttribute(id_transformation, id_step, "outputfield", inputURI);
			//rep.saveStepAttribute(id_transformation, id_step, "outprefix", prefix);
			//$NON-NLS-1$
		}
		catch(Exception e){
			throw new KettleException("Unable to save step into repository: "+id_step, e); 
		}
	}		
	
	/**
	 * This method is called by PDI when a step needs to read its configuration from a repository.
	 * The repository implementation provides the necessary methods to read the step attributes.
	 * 
	 * @param rep		the repository to read from
	 * @param id_step	the id of the step being read
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try{
			inputURI  = rep.getStepAttributeString(id_step, "outputfield"); //$NON-NLS-1$
			prefix  = rep.getStepAttributeString(id_step, "outprefix"); //$NON-NLS-1$
		}
		catch(Exception e){
			throw new KettleException("Unable to load step from repository", e);
		}
	}

	/**
	 * This method is called to determine the changes the step is making to the row-stream.
	 * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
	 * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
	 * row-stream.
	 * 
	 * @param r			the row structure coming in to the step
	 * @param origin	the name of the step making the changes
	 * @param info		row structures of any info steps coming in
	 * @param nextStep	the description of a step this step is passing rows to
	 * @param space		the variable space for resolving variables
	 */
	
	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {

		/*
		 * This implementation appends the outputField to the row-stream
		 */

		ValueMetaInterface numRegistro = new ValueMeta("# Registro", ValueMetaInterface.TYPE_STRING);
		numRegistro.setOrigin(origin);
		numRegistro.setLength(5);
		r.addValueMeta(numRegistro);
		
		ValueMetaInterface Campo = new ValueMeta("Campo", ValueMetaInterface.TYPE_STRING);
		Campo.setOrigin(origin);
		Campo.setLength(255);
		r.addValueMeta(Campo);
		

		ValueMetaInterface Datos = new ValueMeta("Datos", ValueMetaInterface.TYPE_STRING);
		Datos.setOrigin(origin);
		Datos.setLength(255);
		r.addValueMeta(Datos);		
		
	}

	/**
	 * This method is called when the user selects the "Verify Transformation" option in Spoon. 
	 * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
	 * The method should perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include:
	 * - verify that all mandatory configuration is given
	 * - verify that the step receives any input, unless it's a row generating step
	 * - verify that the step does not receive any input if it does not take them into account
	 * - verify that the step finds fields it relies on in the row-stream
	 * 
	 *   @param remarks		the list of remarks to append to
	 *   @param transmeta	the description of the transformation
	 *   @param stepMeta	the description of the step
	 *   @param prev		the structure of the incoming row-stream
	 *   @param input		names of steps sending input to the step
	 *   @param output		names of steps this step is sending output to
	 *   @param info		fields coming in from info steps 
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
		
		CheckResult cr;

		// See if there are input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "Demo.CheckResult.ReceivingRows.OK"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "Demo.CheckResult.ReceivingRows.ERROR"), stepMeta);
			remarks.add(cr);
		}	
    	
	}

//get and set
	public String getInputURI() {
		return inputURI;
	}


	public void setInputURI(String inputURI) {
		this.inputURI = inputURI;
	}


	public String getPrefix() {
		return prefix;
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	public ArrayList<String> getListpath() {
		return listpath;
	}


	public void setListpath(ArrayList<String> listpath) {
		this.listpath = listpath;
	}
	
	public String getXpath() {
		return xpath;
	}


	public void setXpath(String xpath) {
		this.xpath = xpath;
	}


	public String getNamespace() {
		return namespace;
	}


	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}


	public String getSchema() {
		return schema;
	}


	public void setSchema(String schema) {
		this.schema = schema;
	}


	
	
	

}

