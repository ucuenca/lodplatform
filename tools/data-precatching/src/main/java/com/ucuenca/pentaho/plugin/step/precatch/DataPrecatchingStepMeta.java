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

package com.ucuenca.pentaho.plugin.step.precatch;

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
public class DataPrecatchingStepMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = DataPrecatchingStepMeta.class; // for i18n purposes

	private RowMetaInterface rowMeta;
	
	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public DataPrecatchingStepMeta() {
		super(); 
	}
	
	public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
	    readData( stepnode );
	  }

	  public Object clone() {
	    Object retval = super.clone();
	    return retval;
	  }

	  private void readData( Node stepnode ) {
	  }

	  public void setDefault() {
	  }

	  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
	  }

	  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
	  }

	  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
	    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
	    // Default: nothing changes to rowMeta
		  if(rowMeta != null) {
			  if(rowMeta.size() > 0) {
				  this.rowMeta = rowMeta;
			  } else { //Inherit rowMetaInterface from previous Step
				  if(this.rowMeta != null) {
					  for(ValueMetaInterface valueMeta:this.rowMeta.getValueMetaList()) {
						  rowMeta.addValueMeta(valueMeta);
					  }
				  }
			  }
		  }
	  }

	  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
	    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
	    Repository repository, IMetaStore metaStore ) {
	    CheckResult cr;
	    if ( prev == null || prev.size() == 0 ) {
	      cr =
	        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
	          PKG, "DummyTransMeta.CheckResult.NotReceivingFields" ), stepMeta );
	      remarks.add( cr );
	    } else {
	      cr =
	        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
	          PKG, "DummyTransMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
	      remarks.add( cr );
	    }

	    // See if we have input streams leading to this step!
	    if ( input.length > 0 ) {
	      cr =
	        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
	          PKG, "DummyTransMeta.CheckResult.StepRecevingData2" ), stepMeta );
	      remarks.add( cr );
	    } else {
	      cr =
	        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
	          PKG, "DummyTransMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
	      remarks.add( cr );
	    }
	  }

	  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
	    Trans trans ) {
	    return new DataPrecatchingStep( stepMeta, stepDataInterface, cnr, tr, trans );
	  }

	  public StepDataInterface getStepData() {
	    return new DataPrecatchingStepData();
	  }


}
