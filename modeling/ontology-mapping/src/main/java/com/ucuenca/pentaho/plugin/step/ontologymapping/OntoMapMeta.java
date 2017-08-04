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

package com.ucuenca.pentaho.plugin.step.ontologymapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
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
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.pentaho.metastore.api.IMetaStore;
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
public class OntoMapMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = OntoMapMeta.class; // for i18n purposes

	  private String ontologyStepName;
	  private String dataStepName;
	  private String ontologyDbTable;
	  private String dataDbTable;
	  private String mapBaseURI;
	  private String outputDir;
	  private List<String> sqlStack = new ArrayList<String>();
	  private String outFileName;
          
          private OntoMap Environment;

    public OntoMap getEnvironment() {
        return Environment;
    }

    public void setEnvironment(OntoMap Environment) {
        this.Environment = Environment;
    }

	  
	public String getOutFileName() {
		return outFileName;
	}

	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public List<String> getSqlStack() {
		return sqlStack;
	}

	public void setSqlStack(List<String> sqlStack) {
		this.sqlStack = sqlStack;
	}

	public String getMapBaseURI() {
		return mapBaseURI;
	}
        public String getEnvMapBaseURI() {
		return Environment.environmentSubstitute(mapBaseURI);
	}

        
	public void setMapBaseURI(String mapBaseURI) {
		this.mapBaseURI = mapBaseURI;
	}

	public String getOntologyStepName() {
		return ontologyStepName;
	}

	public void setOntologyStepName(String ontologyStepName) {
		this.ontologyStepName = ontologyStepName;
	}

	public String getDataStepName() {
		return dataStepName;
	}

	public void setDataStepName(String dataStepName) {
		this.dataStepName = dataStepName;
	}

	public String getOntologyDbTable() {
		return ontologyDbTable;
	}

	public void setOntologyDbTable(String ontologyDbTable) {
		this.ontologyDbTable = ontologyDbTable;
	}

	public String getDataDbTable() {
		return dataDbTable;
	}

	public void setDataDbTable(String dataDbTable) {
		this.dataDbTable = dataDbTable;
	}

	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public OntoMapMeta() {
		super(); 
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
		return new OntoMapDialog(shell, meta, transMeta, name);
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
		return new OntoMap(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new OntoMapData();
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
		OntoMapMeta retval = (OntoMapMeta) super.clone();
	    return retval;
	}
	
	/**
	 * Implementation
	 * @param stepnode
	 * @throws KettleXMLException
	 */
	private void readData( Node stepnode ) throws KettleXMLException {
	    try {
	    	
	      List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
	      infoStreams.get( 0 ).setSubject( XMLHandler.getTagValue( stepnode, "ontologiesStep" ) );
	      infoStreams.get( 1 ).setSubject( XMLHandler.getTagValue( stepnode, "dataStep" ) );
	      
			this.setOntologyStepName( XMLHandler.getTagValue( stepnode, "ontologiesStep" ));
			this.setOntologyDbTable( XMLHandler.getTagValue( stepnode, "ontologiesDBTable" ) );
			this.setDataStepName( XMLHandler.getTagValue( stepnode, "dataStep" ) );
			this.setDataDbTable( XMLHandler.getTagValue( stepnode, "dataDBTable" ) );
			this.setMapBaseURI( XMLHandler.getTagValue( stepnode, "mapBaseURI" ) );
			String sqlStack = XMLHandler.getTagValue( stepnode, "sqlStack" );
			sqlStack = sqlStack.substring(1, sqlStack.length()-1);
			this.setSqlStack( Arrays.asList( sqlStack.split(",\\s") ) );
			this.setOutputDir( XMLHandler.getTagValue( stepnode, "outputDir" ) );
			this.setOutFileName( XMLHandler.getTagValue( stepnode, "outFileName" ) );
	      
	    } catch ( Exception e ) {
	      throw new KettleXMLException( "Unable to load step info from XML", e );
	    }
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
		
		StringBuffer retval = new StringBuffer( 300 );

		retval.append( XMLHandler.addTagValue( "ontologiesStep", this.getOntologyStepName() ));
		retval.append( XMLHandler.addTagValue( "ontologiesDBTable", this.getOntologyDbTable() ));
		retval.append( XMLHandler.addTagValue( "dataStep", this.getDataStepName() ));
		retval.append( XMLHandler.addTagValue( "dataDBTable", this.getDataDbTable() ));
		retval.append( XMLHandler.addTagValue( "mapBaseURI", this.getMapBaseURI() ));
		retval.append( XMLHandler.addTagValue( "outputDir", this.getOutputDir() ));
		retval.append( XMLHandler.addTagValue( "outFileName", this.getOutFileName() ));
		retval.append( XMLHandler.addTagValue( "sqlStack", this.getSqlStack().toString()));

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
		readData( stepnode );
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
		try {
	      rep.saveStepAttribute( id_transformation, id_step, "ontologiesStep", this.getOntologyStepName() );
	      rep.saveStepAttribute( id_transformation, id_step, "ontologiesDBTable", this.getOntologyDbTable() );
	      rep.saveStepAttribute( id_transformation, id_step, "dataStep", this.getDataStepName() );
	      rep.saveStepAttribute( id_transformation, id_step, "dataDBTable", this.getDataDbTable() );
	      rep.saveStepAttribute( id_transformation, id_step, "mapBaseURI", this.getMapBaseURI() );
	      rep.saveStepAttribute( id_transformation, id_step, "outputDir", this.getOutputDir() );
	      rep.saveStepAttribute( id_transformation, id_step, "outFileName", this.getOutFileName() );
	      rep.saveStepAttribute( id_transformation, id_step, "sqlStack", this.getSqlStack().toString() );
	      
	    } catch ( Exception e ) {
	      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
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
		try {
			this.setOntologyStepName( rep.getStepAttributeString( id_step, "ontologiesStep" ));
			this.setOntologyDbTable( rep.getStepAttributeString( id_step, "ontologiesDBTable" ) );
			this.setDataStepName( rep.getStepAttributeString( id_step, "dataStep" ) );
			this.setDataDbTable( rep.getStepAttributeString( id_step, "dataDBTable" ) );
			this.setMapBaseURI( rep.getStepAttributeString( id_step, "mapBaseURI" ) );
			this.setOutputDir( rep.getStepAttributeString( id_step, "outputDir" ) );
			this.setOutFileName( rep.getStepAttributeString( id_step, "outFileName" ) );
			String sqlStack = rep.getStepAttributeString( id_step, "sqlStack" );
			sqlStack = sqlStack.substring(1, sqlStack.length()-1);
			this.setSqlStack( Arrays.asList( sqlStack.split(",\\s") ) );

	    } catch ( Exception e ) {
	      throw new KettleException( "Unexpected error reading step information from the repository", e );
	    }
	}
	
	/**
	   * Returns the Input/Output metadata for this step. The generator step only produces output, does not accept input!
	   */
	  public StepIOMetaInterface getStepIOMeta() {
	    if ( ioMeta == null ) {
	
	      ioMeta = new StepIOMeta( true, true, false, false, false, false );
	
	      ioMeta.addStream( new Stream( StreamType.INFO, null, BaseMessages.getString(
	        PKG, "OntologyMapping.InfoStream.FirstStream.Description" ), StreamIcon.INFO, null ) );
	      ioMeta.addStream( new Stream( StreamType.INFO, null, BaseMessages.getString(
	        PKG, "OntologyMapping.InfoStream.SecondStream.Description" ), StreamIcon.INFO, null ) );
	    }
	
	    return ioMeta;
	  }
	  
	  public void resetStepIoMeta() {
	    // Don't reset!
	  }
	  
	
	/**
	 * This method is called to determine the changes the step is making to the row-stream.
	 * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
	 * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
	 * row-stream.
	 * 
	 * @param rowMeta			the row structure coming in to the step
	 * @param origin	the name of the step making the changes
	 * @param info		row structures of any info steps coming in
	 * @param nextStep	the description of a step this step is passing rows to
	 * @param space		the variable space for resolving variables
	 */
	public void getFields( RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
		    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

	        ValueMetaInterface id = new ValueMeta(BaseMessages.getString(PKG, "OntologyMapping.Table.Field.Subject"), ValueMetaInterface.TYPE_STRING);
			id.setOrigin(origin);
			id.setLength(1000);
			r.addValueMeta(id);
			
			ValueMetaInterface field = new ValueMeta(BaseMessages.getString(PKG, "OntologyMapping.Table.Field.Predicate"), ValueMetaInterface.TYPE_STRING);
			field.setOrigin(origin);
			field.setLength(1000);
			r.addValueMeta(field);
			

			ValueMetaInterface data = new ValueMeta(BaseMessages.getString(PKG, "OntologyMapping.Table.Field.Object"), ValueMetaInterface.TYPE_STRING);
			data.setOrigin(origin);
			data.setLength(10000);
			r.addValueMeta(data);
		
	}
	
	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
		r.clear();
		RowMeta rowMeta = new RowMeta();
		this.getFields(rowMeta, origin, info, nextStep, space, null, null);
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
	    if ( prev != null && prev.size() > 0 ) {
	      cr =
	        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
	          PKG, "ConstantMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
	      remarks.add( cr );
	    } else {
	      cr =
	        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
	          PKG, "ConstantMeta.CheckResult.NoFields" ), stepMeta );
	      remarks.add( cr );
	    }

	}

	public void setDefault() {
		// TODO Auto-generated method stub
		
	}


}
