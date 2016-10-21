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

package com.ucuenca.pentaho.plugin.step.oai;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class OAILoader extends BaseStep implements StepInterface {

	ArrayList<String> datos;
	ArrayList<String> nameFields;
	String numRegistro;
	
	public OAILoader(StepMeta s, StepDataInterface stepDataInterface, int c,
			TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

        
        
        public void init_ResumptionToken (StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
            OAILoaderMeta meta = (OAILoaderMeta) smi;
            RowMetaInterface antRow = getInputRowMeta();
            
            
            
            
           // meta.setInitialResumptionToken(IRT);
        }
        
        
        
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		OAILoaderMeta meta = (OAILoaderMeta) smi;
		OAILoaderData data = (OAILoaderData) sdi;
		data.getDataLoader().setBaseStep(this);
		//
                
		return super.init(meta, data);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
            
            
            

		// safely cast the step settings (meta) and runtime info (data) to
		// specific implementations
		OAILoaderMeta meta = (OAILoaderMeta) smi;
		OAILoaderData data = (OAILoaderData) sdi;
		
		if (first) {
                    
                    String IRT = null;
                    
                    try{
                        Object[] row = getRow();
                        RowMetaInterface inputRowMeta = getInputRowMeta();
                        String[] fieldNames = inputRowMeta.getFieldNames();
                        String FilaTI = null;
                        int pFilaTI = -1;
                        for (int w = 0; w < fieldNames.length; w++) {
                            if (fieldNames[w].compareTo("ResponseT") == 0) {
                                FilaTI = fieldNames[w];
                                pFilaTI = w;
                                break;
                            }
                        }
                        IRT = ((String) row[pFilaTI]).trim();
                    }catch(Exception w){
                    
                    }
                    if (IRT !=null && IRT.compareTo("")==0){
                        IRT=null;
                    }
                    data.fromDate=IRT;
                    
                    
                    
                    data.initOAIHarvester(meta, data, false);
                    
                    if(data.listRecords == null) throw new KettleException("ERROR WHILE RETRIEVING OAI RECORDS");    
                    
			first = false;
			data.outputRowMeta = new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		}

		Boolean hasMoreData = data.getData(smi, sdi);
		if(!hasMoreData) setOutputDone();
		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
			
		}
		return hasMoreData;

	}

	// indicate that processRow() should be called again

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
	 * behavior. (index
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
		OAILoaderMeta meta = (OAILoaderMeta) smi;
		OAILoaderData data = (OAILoaderData) sdi;

		super.dispose(meta, data);
	}
}
