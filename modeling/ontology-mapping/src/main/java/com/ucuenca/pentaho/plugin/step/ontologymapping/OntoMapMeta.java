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

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
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
import org.pentaho.di.trans.steps.constant.Constant;
import org.pentaho.di.trans.steps.constant.ConstantData;
import org.pentaho.di.trans.steps.constant.ConstantMeta;
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
	
	private String[] currency;
	  private String[] decimal;
	  private String[] group;
	  private String[] value;
	
	  private String[] fieldName;
	  private String[] fieldType;
	  private String[] fieldFormat;
	
	  private int[] fieldLength;
	  private int[] fieldPrecision;
	  /** Flag : set empty string **/
	  private boolean[] setEmptyString;

	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public OntoMapMeta() {
		super(); 
	}
	
	/**
	   * @return Returns the currency.
	   */
	  public String[] getCurrency() {
	    return currency;
	  }

	  /**
	   * @param currency
	   *          The currency to set.
	   */
	  public void setCurrency( String[] currency ) {
	    this.currency = currency;
	  }

	  /**
	   * @return Returns the decimal.
	   */
	  public String[] getDecimal() {
	    return decimal;
	  }

	  /**
	   * @param decimal
	   *          The decimal to set.
	   */
	  public void setDecimal( String[] decimal ) {
	    this.decimal = decimal;
	  }

	  /**
	   * @return Returns the fieldFormat.
	   */
	  public String[] getFieldFormat() {
	    return fieldFormat;
	  }

	  /**
	   * @param fieldFormat
	   *          The fieldFormat to set.
	   */
	  public void setFieldFormat( String[] fieldFormat ) {
	    this.fieldFormat = fieldFormat;
	  }

	  /**
	   * @return Returns the fieldLength.
	   */
	  public int[] getFieldLength() {
	    return fieldLength;
	  }

	  /**
	   * @param fieldLength
	   *          The fieldLength to set.
	   */
	  public void setFieldLength( int[] fieldLength ) {
	    this.fieldLength = fieldLength;
	  }

	  /**
	   * @return Returns the fieldName.
	   */
	  public String[] getFieldName() {
	    return fieldName;
	  }

	  /**
	   * @param fieldName
	   *          The fieldName to set.
	   */
	  public void setFieldName( String[] fieldName ) {
	    this.fieldName = fieldName;
	  }

	  /**
	   * @return Returns the fieldPrecision.
	   */
	  public int[] getFieldPrecision() {
	    return fieldPrecision;
	  }

	  /**
	   * @param fieldPrecision
	   *          The fieldPrecision to set.
	   */
	  public void setFieldPrecision( int[] fieldPrecision ) {
	    this.fieldPrecision = fieldPrecision;
	  }

	  /**
	   * @return Returns the fieldType.
	   */
	  public String[] getFieldType() {
	    return fieldType;
	  }

	  /**
	   * @param fieldType
	   *          The fieldType to set.
	   */
	  public void setFieldType( String[] fieldType ) {
	    this.fieldType = fieldType;
	  }

	  /**
	   * @return the setEmptyString
	   */
	  public boolean[] isSetEmptyString() {
	    return setEmptyString;
	  }

	  /**
	   * @param setEmptyString
	   *          the setEmptyString to set
	   */
	  public void setEmptyString( boolean[] setEmptyString ) {
	    this.setEmptyString = setEmptyString;
	  }

	  /**
	   * @return Returns the group.
	   */
	  public String[] getGroup() {
	    return group;
	  }

	  /**
	   * @param group
	   *          The group to set.
	   */
	  public void setGroup( String[] group ) {
	    this.group = group;
	  }

	  /**
	   * @return Returns the value.
	   */
	  public String[] getValue() {
	    return value;
	  }

	  /**
	   * @param value
	   *          The value to set.
	   */
	  public void setValue( String[] value ) {
	    this.value = value;
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
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault() {
		int i, nrfields = 0;

	    this.allocate( nrfields );

	    DecimalFormat decimalFormat = new DecimalFormat();

	    for ( i = 0; i < nrfields; i++ ) {
	      fieldName[i] = "field" + i;
	      fieldType[i] = "Number";
	      fieldFormat[i] = "\u00A40,000,000.00;\u00A4-0,000,000.00";
	      fieldLength[i] = 9;
	      fieldPrecision[i] = 2;
	      currency[i] = decimalFormat.getDecimalFormatSymbols().getCurrencySymbol();
	      decimal[i] = new String( new char[] { decimalFormat.getDecimalFormatSymbols().getDecimalSeparator() } );
	      group[i] = new String( new char[] { decimalFormat.getDecimalFormatSymbols().getGroupingSeparator() } );
	      value[i] = "-";
	      setEmptyString[i] = false;
	    }
	}
	
	/**
	 * Implementation
	 * @param nrfields
	 */
	public void allocate( int nrfields ) {
		fieldName = new String[nrfields];
		fieldType = new String[nrfields];
		fieldFormat = new String[nrfields];
		fieldLength = new int[nrfields];
		fieldPrecision = new int[nrfields];
		currency = new String[nrfields];
		decimal = new String[nrfields];
		group = new String[nrfields];
		value = new String[nrfields];
		setEmptyString = new boolean[nrfields];
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

	    int nrfields = fieldName.length;

	    retval.allocate( nrfields );

	    for ( int i = 0; i < nrfields; i++ ) {
	      retval.fieldName[i] = fieldName[i];
	      retval.fieldType[i] = fieldType[i];
	      retval.fieldFormat[i] = fieldFormat[i];
	      retval.currency[i] = currency[i];
	      retval.decimal[i] = decimal[i];
	      retval.group[i] = group[i];
	      retval.value[i] = value[i];
	      retval.fieldLength[i] = fieldLength[i];
	      retval.fieldPrecision[i] = fieldPrecision[i];
	      retval.setEmptyString[i] = setEmptyString[i];
	    }

	    return retval;
	}
	
	/**
	 * Implementation
	 * @param stepnode
	 * @throws KettleXMLException
	 */
	private void readData( Node stepnode ) throws KettleXMLException {
	    try {
	      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
	      int nrfields = XMLHandler.countNodes( fields, "field" );

	      allocate( nrfields );

	      String slength, sprecision;

	      for ( int i = 0; i < nrfields; i++ ) {
	        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

	        fieldName[i] = XMLHandler.getTagValue( fnode, "name" );
	        fieldType[i] = XMLHandler.getTagValue( fnode, "type" );
	        fieldFormat[i] = XMLHandler.getTagValue( fnode, "format" );
	        currency[i] = XMLHandler.getTagValue( fnode, "currency" );
	        decimal[i] = XMLHandler.getTagValue( fnode, "decimal" );
	        group[i] = XMLHandler.getTagValue( fnode, "group" );
	        value[i] = XMLHandler.getTagValue( fnode, "nullif" );
	        slength = XMLHandler.getTagValue( fnode, "length" );
	        sprecision = XMLHandler.getTagValue( fnode, "precision" );

	        fieldLength[i] = Const.toInt( slength, -1 );
	        fieldPrecision[i] = Const.toInt( sprecision, -1 );
	        String emptyString = XMLHandler.getTagValue( fnode, "set_empty_string" );
	        setEmptyString[i] = !Const.isEmpty( emptyString ) && "Y".equalsIgnoreCase( emptyString );
	      }
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

	    retval.append( "    <fields>" ).append( Const.CR );
	    for ( int i = 0; i < fieldName.length; i++ ) {
	      if ( fieldName[i] != null && fieldName[i].length() != 0 ) {
	        retval.append( "      <field>" ).append( Const.CR );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "name", fieldName[i] ) );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "type", fieldType[i] ) );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "format", fieldFormat[i] ) );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "currency", currency[i] ) );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "decimal", decimal[i] ) );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "group", group[i] ) );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "nullif", value[i] ) );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "length", fieldLength[i] ) );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "precision", fieldPrecision[i] ) );
	        retval.append( "        " ).append( XMLHandler.addTagValue( "set_empty_string", setEmptyString[i] ) );
	        retval.append( "      </field>" ).append( Const.CR );
	      }
	    }
	    retval.append( "    </fields>" ).append( Const.CR );

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
	      for ( int i = 0; i < fieldName.length; i++ ) {
	        if ( fieldName[i] != null && fieldName[i].length() != 0 ) {
	          rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldName[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "field_type", fieldType[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "field_format", fieldFormat[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "field_currency", currency[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "field_decimal", decimal[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "field_group", group[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "field_nullif", value[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "field_length", fieldLength[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", fieldPrecision[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "set_empty_string", setEmptyString[i] );
	        }
	      }
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
	      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

	      allocate( nrfields );

	      for ( int i = 0; i < nrfields; i++ ) {
	        fieldName[i] = rep.getStepAttributeString( id_step, i, "field_name" );
	        fieldType[i] = rep.getStepAttributeString( id_step, i, "field_type" );

	        fieldFormat[i] = rep.getStepAttributeString( id_step, i, "field_format" );
	        currency[i] = rep.getStepAttributeString( id_step, i, "field_currency" );
	        decimal[i] = rep.getStepAttributeString( id_step, i, "field_decimal" );
	        group[i] = rep.getStepAttributeString( id_step, i, "field_group" );
	        value[i] = rep.getStepAttributeString( id_step, i, "field_nullif" );
	        fieldLength[i] = (int) rep.getStepAttributeInteger( id_step, i, "field_length" );
	        fieldPrecision[i] = (int) rep.getStepAttributeInteger( id_step, i, "field_precision" );
	        setEmptyString[i] = rep.getStepAttributeBoolean( id_step, i, "set_empty_string", false );
	      }
	    } catch ( Exception e ) {
	      throw new KettleException( "Unexpected error reading step information from the repository", e );
	    }
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
	public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
		    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
		  
		for ( int i = 0; i < fieldName.length; i++ ) {
	      if ( fieldName[i] != null && fieldName[i].length() != 0 ) {
	        int type = ValueMeta.getType( fieldType[i] );
	        if ( type == ValueMetaInterface.TYPE_NONE ) {
	          type = ValueMetaInterface.TYPE_STRING;
	        }
	        try {
	          ValueMetaInterface v = ValueMetaFactory.createValueMeta( fieldName[i], type );
	          v.setLength( fieldLength[i] );
	          v.setPrecision( fieldPrecision[i] );
	          v.setOrigin( origin );
	          rowMeta.addValueMeta( v );
	        } catch ( Exception e ) {
	          throw new KettleStepException( e );
	        }
	      }
	    }
		
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

	    // Check the constants...
	    ConstantData data = new ConstantData();
	    ConstantMeta meta = (ConstantMeta) stepMeta.getStepMetaInterface();
	    Constant.buildRow( meta, data, remarks );	
    	
	}


}
