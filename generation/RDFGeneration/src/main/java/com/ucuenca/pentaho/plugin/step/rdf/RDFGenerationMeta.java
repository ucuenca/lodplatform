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

package com.ucuenca.pentaho.plugin.step.rdf;

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


public class RDFGenerationMeta extends BaseStepMeta implements
		StepMetaInterface {

	/**
	 * The PKG member is used when looking up internationalized strings. The
	 * properties file with localized keys is expected to reside in {the package
	 * of the class specified}/messages/messages_{locale}.properties
	 */
	private static Class<?> PKG = RDFGenerationMeta.class; // for i18n purposes

	/**
	 * Stores the name of the field added to the row-stream.
	 */
	private String inputFieldr2rml;
	private String sqlvendor;
	private String databaseURL;
	private String databaseSchema;
	private String userName;
	private String password;
	private String baseUri;
	private String directorioOutputRDF;
	private String format;

	private String stepName;

	/**
	 * Constructor should call super() to make sure the base class has a chance
	 * to initialize properly.
	 */
	public RDFGenerationMeta() {
		super();

	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta,
			TransMeta transMeta, String name) {
		this.setStepName(name);
		return new RDFGenerationDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. A
	 * standard implementation passing the arguments to the constructor of the
	 * step class is recommended.
	 * 
	 * @param stepMeta
	 *            description of the step
	 * @param stepDataInterface
	 *            instance of a step data class
	 * @param cnr
	 *            copy number
	 * @param transMeta
	 *            description of the transformation
	 * @param disp
	 *            runtime implementation of the transformation
	 * @return the new instance of a step implementation
	 */
	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans disp) {
		return new RDFGeneration(stepMeta, stepDataInterface, cnr, transMeta,
				disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new RDFGenerationData();
	}

	/**
	 * This method is called every time a new step is created and should
	 * allocate/set the step configuration to sensible defaults. The values set
	 * here will be used by Spoon when a new step is created.
	 */
	public void setDefault() {
		inputFieldr2rml = "R2rml File";
		sqlvendor = "";
		databaseURL = "";
		databaseSchema = "";
		userName = "";
		password = "";
		baseUri = "";
		directorioOutputRDF = "";
		format = "";
	}

	/**
	 * This method is used when a step is duplicated in Spoon. It needs to
	 * return a deep copy of this step meta object. Be sure to create proper
	 * deep copies if the step configuration is stored in modifiable objects.
	 * 
	 * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for
	 * an example on creating a deep copy.
	 * 
	 * @return a deep copy of this
	 */
	public Object clone() {
		Object retval = super.clone();
		return retval;
	}

	/**
	 * This method is called by Spoon when a step needs to serialize its
	 * configuration to XML. The expected return value is an XML fragment
	 * consisting of one or more XML tags.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate
	 * the XML.
	 * 
	 * @return a string containing the XML serialization of this step
	 */
	public String getXML() throws KettleValueException {

		// only one field to serialize
		StringBuffer retval = new StringBuffer(1000);

		retval.append("    ").append(
				XMLHandler.addTagValue("nameStep", stepName));
		retval.append("    ").append(
				XMLHandler.addTagValue("Fieldr2rml", inputFieldr2rml));
		retval.append("    ").append(
				XMLHandler.addTagValue("sqldriver", sqlvendor));
		retval.append("    ").append(
				XMLHandler.addTagValue("baseURL", databaseURL));
		retval.append("    ").append(
				XMLHandler.addTagValue("baseSchema", databaseSchema));
		retval.append("    ").append(
				XMLHandler.addTagValue("DataSetUri", baseUri));
		retval.append("    ").append(XMLHandler.addTagValue("user", userName));
		retval.append("    ").append(XMLHandler.addTagValue("pass", password));
		retval.append("    ").append(
				XMLHandler.addTagValue("OutputRDF", directorioOutputRDF));
		retval.append("    ").append(XMLHandler.addTagValue("formats", format));

		return retval.toString();
	}

	/**
	 * This method is called by PDI when a step needs to load its configuration
	 * from XML.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from
	 * the XML node passed in.
	 * 
	 * @param stepnode
	 *            the XML node containing the configuration
	 * @param databases
	 *            the databases available in the transformation
	 * @param counters
	 *            the counters available in the transformation
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	private void readData(Node stepnode) throws KettleXMLException {

		stepName = XMLHandler.getTagValue(stepnode, "nameStep");
		inputFieldr2rml = XMLHandler.getTagValue(stepnode, "Fieldr2rml");
		sqlvendor = XMLHandler.getTagValue(stepnode, "sqldriver");
		databaseURL = XMLHandler.getTagValue(stepnode, "baseURL");
		databaseSchema = XMLHandler.getTagValue(stepnode, "baseSchema");
		baseUri = XMLHandler.getTagValue(stepnode, "DataSetUri");
		userName = XMLHandler.getTagValue(stepnode, "user");
		password = XMLHandler.getTagValue(stepnode, "pass");
		directorioOutputRDF = XMLHandler.getTagValue(stepnode, "OutputRDF");
		format = XMLHandler.getTagValue(stepnode, "formats");
	}

	/**
	 * This method is called by Spoon when a step needs to serialize its
	 * configuration to a repository. The repository implementation provides the
	 * necessary methods to save the step attributes.
	 * 
	 * @param rep
	 *            the repository to save to
	 * @param id_transformation
	 *            the id to use for the transformation when saving
	 * @param id_step
	 *            the id to use for the step when saving
	 */
	public void saveRep(Repository rep, ObjectId id_transformation,
			ObjectId id_step) throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step,
					"inputFieldr2rml", inputFieldr2rml); //$NON-NLS-1$

			//			rep.saveStepAttribute(id_transformation, id_step, "nameontology", nameOntology); //$NON-NLS-1$
			//			rep.saveStepAttribute(id_transformation, id_step, "stepname", stepName); //$NON-NLS-1$
			//			rep.saveStepAttribute(id_transformation, id_step, "outputfield", outputField); //$NON-NLS-1$
		} catch (Exception e) {
			throw new KettleException("Unable to save step into repository: "
					+ id_step, e);
		}
	}

	/**
	 * This method is called by PDI when a step needs to read its configuration
	 * from a repository. The repository implementation provides the necessary
	 * methods to read the step attributes.
	 * 
	 * @param rep
	 *            the repository to read from
	 * @param id_step
	 *            the id of the step being read
	 * @param databases
	 *            the databases available in the transformation
	 * @param counters
	 *            the counters available in the transformation
	 */
	public void readRep(Repository rep, ObjectId id_step,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		try {
			inputFieldr2rml = rep.getStepAttributeString(id_step,
					"inputFieldr2rml"); //$NON-NLS-1$

		} catch (Exception e) {
			throw new KettleException("Unable to load step from repository", e);
		}
	}

	/**
	 * This method is called to determine the changes the step is making to the
	 * row-stream. To that end a RowMetaInterface object is passed in,
	 * containing the row-stream structure as it is when entering the step. This
	 * method must apply any changes the step makes to the row stream. Usually a
	 * step adds fields to the row-stream.
	 * 
	 * @param r
	 *            the row structure coming in to the step
	 * @param origin
	 *            the name of the step making the changes
	 * @param info
	 *            row structures of any info steps coming in
	 * @param nextStep
	 *            the description of a step this step is passing rows to
	 * @param space
	 *            the variable space for resolving variables
	 */
	public void getFields(RowMetaInterface r, String origin,
			RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {

		/*
		 * This implementation appends the outputField to the row-stream
		 */

		// a value meta object contains the meta data for a field
		ValueMetaInterface v = new ValueMeta();

		// set the name of the new field
		v.setName(inputFieldr2rml);

		// type is going to be string
		v.setType(ValueMeta.TYPE_STRING);

		// setting trim type to "both"
		v.setTrimType(ValueMeta.TRIM_TYPE_BOTH);

		// the name of the step that adds this field
		v.setOrigin(origin);

		// modify the row structure and add the field this step generates
		r.addValueMeta(v);

	}

	/**
	 * This method is called when the user selects the "Verify Transformation"
	 * option in Spoon. A list of remarks is passed in that this method should
	 * add to. Each remark is a comment, warning, error, or ok. The method
	 * should perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include: - verify that all mandatory configuration is
	 * given - verify that the step receives any input, unless it's a row
	 * generating step - verify that the step does not receive any input if it
	 * does not take them into account - verify that the step finds fields it
	 * relies on in the row-stream
	 * 
	 * @param remarks
	 *            the list of remarks to append to
	 * @param transmeta
	 *            the description of the transformation
	 * @param stepMeta
	 *            the description of the step
	 * @param prev
	 *            the structure of the incoming row-stream
	 * @param input
	 *            names of steps sending input to the step
	 * @param output
	 *            names of steps this step is sending output to
	 * @param info
	 *            fields coming in from info steps
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transmeta,
			StepMeta stepMeta, RowMetaInterface prev, String input[],
			String output[], RowMetaInterface info) {

		CheckResult cr;

		// See if there are input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.ReceivingRows.OK"),
					stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.ReceivingRows.ERROR"),
					stepMeta);
			remarks.add(cr);
		}

		// validacion de campos

		if (inputFieldr2rml==null || inputFieldr2rml.equals("R2rml File")  ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.FileR2rml"),
					stepMeta);
			remarks.add(cr);
		}

		if ( sqlvendor==null ||  sqlvendor.equals("") ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.SQLVendor"),
					stepMeta);
			remarks.add(cr);
		}

		if ( databaseURL==null || databaseURL.equals("") ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.DataBaseUri"),
					stepMeta);
			remarks.add(cr);
		}

		if (databaseSchema==null || databaseSchema.equals("")  ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.dataBaseSchema"),
					stepMeta);
			remarks.add(cr);
		}

		if (userName==null || userName.equals("")  ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.userName"),
					stepMeta);
			remarks.add(cr);
		}

		if (password==null || password.equals("")) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.Password"),
					stepMeta);
			remarks.add(cr);
		}

		if (directorioOutputRDF==null || directorioOutputRDF.equals("") ) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.outputRDFfiel"),
					stepMeta);
			remarks.add(cr);
		}

		if (format==null || format.equals("")) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG,
							"RDFGeneration.CheckResult.Formats"),
					stepMeta);
			remarks.add(cr);
		}
		
	}

	public String getInputFieldr2rml() {
		return inputFieldr2rml;
	}

	public void setInputFieldr2rml(String inputFieldr2rml) {
		this.inputFieldr2rml = inputFieldr2rml;
	}

	public String getSqlvendor() {
		return sqlvendor;
	}

	public void setSqlvendor(String sqlvendor) {
		this.sqlvendor = sqlvendor;
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}

	public String getDatabaseSchema() {
		return databaseSchema;
	}

	public void setDatabaseSchema(String databaseSchema) {
		this.databaseSchema = databaseSchema;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public String getDirectorioOutputRDF() {
		return directorioOutputRDF;
	}

	public void setDirectorioOutputRDF(String directorioOutputRDF) {
		this.directorioOutputRDF = directorioOutputRDF;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

}
