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
import org.pentaho.di.core.row.RowMeta;
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

/** .
 * @author Fabian Peñaloza Marin
 * @version 1
 */
/**
 * This class is part of the FusekiLoader step plug-in implementation.
 * It FusekiLoadernstrates the basics of developing a plug-in step for PDI. 
 * 
 * The FusekiLoader step adds a new string field to the row stream and sets its
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
public class FusekiLoaderMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = FusekiLoaderMeta.class; // for i18n purposes
	
	/**
	 * Stores the name of the field added to the row-stream. 
	 */
	private String outputField;
	private String directory;
	private String inputName;
	private String serviceName;
	private String PortName;
	private String fuQuery;
	private String fuGraph;
	private String fuDataset;
	private String Validate;
	private String Federada;
	public String getFederada() {
		return Federada;
	}

	public void setFederada(String federada) {
		Federada = federada;
	}

	public String getValidate() {
		return Validate;
	}

	public void setValidate(String validate) {
		Validate = validate;
	}

	public String getPortName() {
		return PortName;
	}

	public void setPortName(String portName) {
		PortName = portName;
	}

	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getFuQuery() {
		return fuQuery;
	}

	public void setFuQuery(String fuQuery) {
		this.fuQuery = fuQuery;
	}

	public String getFuGraph() {
		return fuGraph;
	}

	public void setFuGraph(String fuGraph) {
		this.fuGraph = fuGraph;
	}

	public String getFuDataset() {
		return fuDataset;
	}

	public void setFuDataset(String fuDataset) {
		this.fuDataset = fuDataset;
	}

	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public FusekiLoaderMeta() {
		super(); 
	}
	
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
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
		return new FusekiLoaderDialog(shell, meta, transMeta, name);
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
		return new FusekiLoader(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new FusekiLoaderData();
	}	

	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault() {
		outputField = " ";
		directory = " ";
		serviceName = "";
		inputName = " ";
		PortName= " ";
		Federada= " ";
		Validate = "false";
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
		StringBuffer retval = new StringBuffer( 400 );
		retval.append( "    " ).append( XMLHandler.addTagValue("outputfield", outputField));
		retval.append( "    " ).append( XMLHandler.addTagValue("directory", directory));
		retval.append( "    " ).append( XMLHandler.addTagValue("servicename", serviceName));
		retval.append( "    " ).append( XMLHandler.addTagValue("inputName", inputName));
		retval.append( "    " ).append( XMLHandler.addTagValue("fuDataset",fuDataset));
		retval.append( "    " ).append( XMLHandler.addTagValue("fuGraph",fuGraph));
		retval.append( "    " ).append( XMLHandler.addTagValue("fuQuery",fuQuery));
		retval.append( "    " ).append( XMLHandler.addTagValue("PortName",PortName));
		retval.append( "    " ).append( XMLHandler.addTagValue("Validate",Validate));
		retval.append( "    " ).append( XMLHandler.addTagValue("Federada",Federada));
		
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
			setDirectory(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "directory")));
			setServiceName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "servicename")));
			setInputName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "inputName")));
			setFuDataset(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "fuDataset")));
			setFuGraph(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "fuGraph")));
			setFuQuery(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "fuQuery")));
			setPortName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "PortName")));
			setValidate(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "Validate")));
			setFederada(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "Federada")));

		} catch (Exception e) {
			throw new KettleXMLException("FusekiLoader plugin unable to read step info from XML node", e);
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
			rep.saveStepAttribute(id_transformation, id_step, "outputfield", outputField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "directory", directory); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "servicename", serviceName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "fuDataset", fuDataset); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "fuGraph", fuGraph); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "fuQuery", fuQuery); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "PortName", PortName);
			rep.saveStepAttribute(id_transformation, id_step, "Validate", Validate);
			rep.saveStepAttribute(id_transformation, id_step, "Federada", Federada);
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
			directory= rep.getStepAttributeString(id_step, "directory");
			inputName= rep.getStepAttributeString(id_step, "inputName");
			serviceName= rep.getStepAttributeString(id_step, "servicename");
			
			fuDataset  = rep.getStepAttributeString(id_step, "fuDataset");
			
			
			fuGraph  = rep.getStepAttributeString(id_step, "fuGraph");
			fuQuery  = rep.getStepAttributeString(id_step, "fuQuery");
			PortName = rep.getStepAttributeString(id_step, "PortName");
			Validate = rep.getStepAttributeString(id_step, "Validate");
			Federada = rep.getStepAttributeString(id_step, "Federada");
	 
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

		r.clear();
		RowMeta rowMeta = new RowMeta();
		
		ValueMetaInterface file = new ValueMeta("File", ValueMetaInterface.TYPE_STRING);
		file.setOrigin(origin);
		file.setLength(100);
		rowMeta.addValueMeta(file);
		
		
		ValueMetaInterface status = new ValueMeta("Status", ValueMetaInterface.TYPE_STRING);
		status.setOrigin(origin);
		status.setLength(100);
		rowMeta.addValueMeta(status);
		
		r.addRowMeta(rowMeta);
		
		
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
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FusekiLoader.CheckResult.ReceivingRows.OK"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "FusekiLoader.CheckResult.ReceivingRows.ERROR"), stepMeta);
			remarks.add(cr);
		}	
	
	
		if (outputField==null   ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"FusekiLoader.input.empty"),
					stepMeta);
			remarks.add(cr);
		}
		if ( serviceName==null   ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"FusekiLoader.serviceName.empty"),
					stepMeta);
			remarks.add(cr);
		}
		if ( directory==null   ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"FusekiLoader.ouput.empty"),
					stepMeta);
			remarks.add(cr);
		}
		if (PortName==null   ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"FusekiLoader.port.empty"),
					stepMeta);
			remarks.add(cr);
		}
		if (fuQuery==null   ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.FileR2rml"),
					stepMeta);
			remarks.add(cr);
		}
		if (fuGraph==null   ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.FileR2rml"),
					stepMeta);
			remarks.add(cr);
		}
		if (fuDataset==null   ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.FileR2rml"),
					stepMeta);
			remarks.add(cr);
		}
		
    	
	}


}
