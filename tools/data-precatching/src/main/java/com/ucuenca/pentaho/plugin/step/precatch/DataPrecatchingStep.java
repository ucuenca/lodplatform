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

import org.pentaho.di.core.exception.KettleException;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.ucuenca.misctools.DatabaseLoader;
import com.ucuenca.misctools.StepDataLoader;

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

public class DataPrecatchingStep extends BaseStep implements StepInterface {
	
	private static Class<?> PKG = DataPrecatchingStepMeta.class;

	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public DataPrecatchingStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
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
	/*public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		DataPrecatchingStepMeta meta = (DataPrecatchingStepMeta) smi;
		DataPrecatchingStepData data = (DataPrecatchingStepData) sdi;
		
		return super.init(meta, data);*/
		/*try {
			if(ok) DatabaseLoader.getConnection();
		}catch(Exception e) {
			logError("Error: " + e.getMessage());
		}
		return ok;*/
	//}	

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
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		
		DataPrecatchingStepMeta meta = (DataPrecatchingStepMeta) smi;
		DataPrecatchingStepData data = (DataPrecatchingStepData) sdi;
		data.row = getRow(); // get row, set busy!
		if (first) {
			try {
				DatabaseLoader.getConnection();
			}catch(Exception e) {
				throw new KettleException("Error: " + e.getMessage());
			}
			first = false;
			data.outputRowMeta = getInputRowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, null, null, null);
			data.dataLoader = new StepDataLoader(meta.getDbTable());
			data.dataLoader.setBaseStep(this);
		}
	    // no more input to be expected...
	    if ( data.row == null ) {
	      setOutputDone();
	      return false;
	    }

	    this.saveDataRow(meta, data);
	    putRow( getInputRowMeta(), data.row ); // copy row to possible alternate rowset(s).

	    if ( checkFeedback( getLinesRead() ) ) {
	      if ( log.isBasic() ) {
	        logBasic( BaseMessages.getString( PKG, "DummyTrans.Log.LineNumber" ) + getLinesRead() );
	      }
	    }

	    return true;
	}
	
	/**
	 * Method to extract a row of Data. The method MUST BE implemented for Data
	 * precatching. The logic must be similar to the method processRow of the
	 * BaseStep interface.
	 * 
	 * @param smi
	 *            StepMetaInterface
	 * @param sdi
	 *            StepDataInterface
	 * @param databaseLoad
	 *            Boolean.TRUE if the step needs to precatch data to a bundled
	 *            database
	 * @return Boolean.TRUE if there is more data to extract, otherwise
	 *         Boolean.FALSE
	 * @throws KettleException
	 */
	public void saveDataRow(DataPrecatchingStepMeta meta, DataPrecatchingStepData data) throws KettleException {
		data.dataLoader.sequence++;
		int dataIndex = 3;
		Object[] outputRow = new Object[data.outputRowMeta.size() + dataIndex];
		try {
			for(int i=0;i < data.row.length; i++) {
				if(data.row[i] != null) {
					outputRow[dataIndex + i] = data.row[i];
				} else {
					break;
				}
			}
			outputRow[0] = this.getTransMeta().getName().toUpperCase();
			outputRow[1] = meta.getDataStepName() != null ? meta.getDataStepName().toUpperCase():
				meta.getParentStepMeta().getName().toUpperCase();
			outputRow[2] = Integer.valueOf(data.dataLoader.sequence);
			data.dataLoader.insertTableRow(meta, outputRow);
		} catch (Exception e) {
			throw new KettleException(e);
		}
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
		DataPrecatchingStepMeta meta = (DataPrecatchingStepMeta) smi;
		DataPrecatchingStepData data = (DataPrecatchingStepData) sdi;
		try {
			DatabaseLoader.closeConnection();
		}catch(Exception e) {
			logError("Error: " + e.getMessage());
		}
		super.dispose(meta, data);
	}

}
