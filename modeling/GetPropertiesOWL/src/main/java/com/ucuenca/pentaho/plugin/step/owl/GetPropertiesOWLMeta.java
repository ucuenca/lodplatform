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

import java.util.LinkedList;
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

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepMetaInterface.
 * Classes implementing this interface need to:
 * 
 * - keep track of the step settings
 * - serialize step settings both to xml and a repository
 * - provide new instances of objects implementing StepDialogInterface, StepInterface and StepDataInterface
 * - report on how the step modifies the meta-data of the row-stream (row structure and field types)
 * - perform a sanity-check on the settings provided by the user 
 * 
 */
public class GetPropertiesOWLMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = GetPropertiesOWLMeta.class; // for i18n purposes
	
	/**
	 * Stores the name of the field added to the row-stream. 
	 */
	
	private String outputField;
	private LinkedList ListSourcetoProcess =  new LinkedList<String>();
	//must be included for DataBase Data Loading
	private String stepName;
	private TransMeta transMeta;
	private String nameOntology;

	public String getNameOntology() {
		return nameOntology;
	}

	public void setNameOntology(String nameOntology) {
		this.nameOntology = nameOntology;
	}

	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public GetPropertiesOWLMeta() {
		super(); 
		ListSourcetoProcess =  new LinkedList<String>();
		
	}
	
	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	public TransMeta getTransMeta() {
		return transMeta;
	}

	public void setTransMeta(TransMeta transMeta) {
		this.transMeta = transMeta;
	}
	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step.
	 * A standard implementation passing the arguments to the constructor of the step dialog is recommended.
	 * 
	 * @param shell		an SWT Shell
	 * @param meta 		description of the step 
	 * @param transMeta	description of the the transformation 
	 * @param name		the name of the step
	 * @return 			new instance of a dialog for this step 
	 */
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		//must be included for DataBase Data Loading
		this.setTransMeta(transMeta);
		this.setStepName(name);
		return new GetPropertiesOWLDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. 
	 * A standard implementation passing the arguments to the constructor of the step class is recommended.
	 * 
	 * @param stepMeta				description of the step
	 * @param stepDataInterface		instance of a step data class
	 * @param cnr					copy number
	 * @param transMeta				description of the transformation
	 * @param disp					runtime implementation of the transformation
	 * @return						the new instance of a step implementation 
	 */
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new GetPropertiesOWL(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new GetPropertiesOWLData();
	}	
	
	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault() {
		outputField = " ";
		nameOntology = " ";
	}
	
	/**
	 * Getter for the name of the field added by this step
	 * @return the name of the field added
	 */
	public String getOutputField() {
		return outputField;
	}

	/**
	 * Setter for the name of the field added by this step
	 * @param outputField the name of the field added
	 */
	public void setOutputField(String outputField) {
		this.outputField = outputField;
	}
	
	public LinkedList getListSourcetoProcess() {
		return ListSourcetoProcess;
	}

	public void setListSourcetoProcess(LinkedList listSourcetoProcess) {
		this.ListSourcetoProcess = listSourcetoProcess;
	}

	/**
	 * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this
	 * step meta object. Be sure to create proper deep copies if the step configuration is stored in
	 * modifiable objects.
	 * 
	 * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating
	 * a deep copy.
	 * 
	 * @return a deep copy of this
	 */
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
		/** 
		String xml = XMLHandler.addTagValue("outputfield", outputField);
		 XMLHandler.addTagValue("nameOntology", nameOntology);
		return xml;*/
		StringBuffer retval = new StringBuffer( 400 );

	    retval.append( "    " ).append( XMLHandler.addTagValue("outputfield", outputField));
	    retval.append( "    " ).append( XMLHandler.addTagValue( "nameontology", nameOntology));
	    retval.append( "    " ).append( XMLHandler.addTagValue( "stepname", stepName));
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
			setOutputField(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "outputfield")));
			setNameOntology(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "nameontology")));
			setStepName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "stepname")));
			
		} catch (Exception e) {
			throw new KettleXMLException("GetPropertiesOWL unable to read step info from XML node", e);
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
			rep.saveStepAttribute(id_transformation, id_step, "nameontology", nameOntology); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "stepname", stepName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "outputfield", outputField); //$NON-NLS-1$
			
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
			outputField  = rep.getStepAttributeString(id_step, "outputfield"); //$NON-NLS-1$
			nameOntology  = rep.getStepAttributeString(id_step, "nameontology"); //$NON-NLS-1$
			stepName  = rep.getStepAttributeString(id_step, "stepName");
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
	public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {

		/*
		 * This implementation appends the  to the row-stream
		 */		
		/*		
		// a value meta object contains the meta data for a field
		ValueMetaInterface v = new ValueMeta();

		// set the name of the new field 
		v.setName(outputField);
		
		// type is going to be string
		v.setType(ValueMeta.TYPE_STRING);
		
		// setting trim type to "both"
		v.setTrimType(ValueMeta.TRIM_TYPE_BOTH);

		// the name of the step that adds this field
		v.setOrigin(origin);
		
		// modify the row structure and add the field this step generates  
		row.addValueMeta(v);*/
		// The filename...
		ValueMetaInterface NombreOntologiaOWL = new ValueMeta(BaseMessages.getString(PKG,
				"GetPropertiesOWL.Result.col1"), ValueMetaInterface.TYPE_STRING);
		NombreOntologiaOWL.setOrigin(origin);
		NombreOntologiaOWL.setLength(30);
		row.addValueMeta(NombreOntologiaOWL);
		// The filename...
		ValueMetaInterface URInameOWL = new ValueMeta(BaseMessages.getString(PKG,
				"GetPropertiesOWL.Result.col2"), ValueMetaInterface.TYPE_STRING);
		URInameOWL.setOrigin(origin);
		URInameOWL.setLength(190);
		row.addValueMeta(URInameOWL);
		
		// The filename...
				ValueMetaInterface TypeOWL = new ValueMeta(BaseMessages.getString(PKG,
						"GetPropertiesOWL.Result.col3"), ValueMetaInterface.TYPE_STRING);
				TypeOWL.setOrigin(origin);
				TypeOWL.setLength(165);
				row.addValueMeta(TypeOWL);
				
				// The filename...
			ValueMetaInterface ObjectOWL = new ValueMeta(BaseMessages.getString(PKG,
					"GetPropertiesOWL.Result.col4"), ValueMetaInterface.TYPE_STRING);
				ObjectOWL.setOrigin(origin);
				ObjectOWL.setLength(110);
				row.addValueMeta(ObjectOWL);

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


}