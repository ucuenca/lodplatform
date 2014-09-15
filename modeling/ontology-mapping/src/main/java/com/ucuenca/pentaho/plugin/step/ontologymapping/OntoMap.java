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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.constant.ConstantData;
import org.pentaho.di.trans.steps.constant.ConstantMeta;

import com.ucuenca.pentaho.plugin.step.ontologymapping.rdf.Entity;
import com.ucuenca.pentaho.plugin.step.ontologymapping.rdf.RDFModel;

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

public class OntoMap extends BaseStep implements StepInterface {
	
	private static Class<?> PKG = OntoMapMeta.class; // for i18n purposes, needed by Translator2!!
	
	private OntoMapMeta meta;
	private OntoMapData data;

	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public OntoMap(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
		
		meta = (OntoMapMeta) getStepMeta().getStepMetaInterface();
		data = (OntoMapData) stepDataInterface;
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
		meta = (OntoMapMeta) smi;
	    data = (OntoMapData) sdi;

	    data.firstRow = true;

	    if ( super.init( smi, sdi ) ) {
	      // Create a row (constants) with all the values in it...
	      List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>(); // stores the errors...
	      data.constants = buildRow( meta, data, remarks );
	      if ( remarks.isEmpty() ) {
	        return true;
	      } else {
	        for ( int i = 0; i < remarks.size(); i++ ) {
	          CheckResultInterface cr = remarks.get( i );
	          logError( cr.getText() );
	        }
	      }
	    }
	    return false;
	}
	
	/**
	 * Implementation
	 * @param meta
	 * @param data
	 * @param remarks
	 * @return
	 */
	public static final RowMetaAndData buildRow( OntoMapMeta meta, OntoMapData data,
	    List<CheckResultInterface> remarks ) {
	    RowMetaInterface rowMeta = new RowMeta();
	    Object[] rowData = new Object[meta.getFieldName().length];

	    for ( int i = 0; i < meta.getFieldName().length; i++ ) {
	      int valtype = ValueMeta.getType( meta.getFieldType()[i] );
	      if ( meta.getFieldName()[i] != null ) {
	        ValueMetaInterface value = null;
	        try {
	          value = ValueMetaFactory.createValueMeta( meta.getFieldName()[i], valtype );
	        } catch ( Exception exception ) {
	          remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, exception.getMessage(), null ) );
	          continue;
	        }
	        value.setLength( meta.getFieldLength()[i] );
	        value.setPrecision( meta.getFieldPrecision()[i] );

	        if ( meta.isSetEmptyString()[i] ) {
	          // Just set empty string
	          rowData[i] = StringUtil.EMPTY_STRING;
	        } else {

	          String stringValue = meta.getValue()[i];

	          // If the value is empty: consider it to be NULL.
	          if ( stringValue == null || stringValue.length() == 0 ) {
	            rowData[i] = null;

	            if ( value.getType() == ValueMetaInterface.TYPE_NONE ) {
	              String message =
	                BaseMessages.getString(
	                  PKG, "Constant.CheckResult.SpecifyTypeError", value.getName(), stringValue );
	              remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
	            }
	          } else {
	            switch ( value.getType() ) {
	              case ValueMetaInterface.TYPE_NUMBER:
	                try {
	                  if ( meta.getFieldFormat()[i] != null
	                    || meta.getDecimal()[i] != null || meta.getGroup()[i] != null
	                    || meta.getCurrency()[i] != null ) {
	                    if ( meta.getFieldFormat()[i] != null && meta.getFieldFormat()[i].length() >= 1 ) {
	                      data.df.applyPattern( meta.getFieldFormat()[i] );
	                    }
	                    if ( meta.getDecimal()[i] != null && meta.getDecimal()[i].length() >= 1 ) {
	                      data.dfs.setDecimalSeparator( meta.getDecimal()[i].charAt( 0 ) );
	                    }
	                    if ( meta.getGroup()[i] != null && meta.getGroup()[i].length() >= 1 ) {
	                      data.dfs.setGroupingSeparator( meta.getGroup()[i].charAt( 0 ) );
	                    }
	                    if ( meta.getCurrency()[i] != null && meta.getCurrency()[i].length() >= 1 ) {
	                      data.dfs.setCurrencySymbol( meta.getCurrency()[i] );
	                    }

	                    data.df.setDecimalFormatSymbols( data.dfs );
	                  }

	                  rowData[i] = new Double( data.nf.parse( stringValue ).doubleValue() );
	                } catch ( Exception e ) {
	                  String message =
	                    BaseMessages.getString(
	                      PKG, "Constant.BuildRow.Error.Parsing.Number", value.getName(), stringValue, e
	                        .toString() );
	                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
	                }
	                break;

	              case ValueMetaInterface.TYPE_STRING:
	                rowData[i] = stringValue;
	                break;

	              case ValueMetaInterface.TYPE_DATE:
	                try {
	                  if ( meta.getFieldFormat()[i] != null ) {
	                    data.daf.applyPattern( meta.getFieldFormat()[i] );
	                    data.daf.setDateFormatSymbols( data.dafs );
	                  }

	                  rowData[i] = data.daf.parse( stringValue );
	                } catch ( Exception e ) {
	                  String message =
	                    BaseMessages.getString(
	                      PKG, "Constant.BuildRow.Error.Parsing.Date", value.getName(), stringValue, e.toString() );
	                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
	                }
	                break;

	              case ValueMetaInterface.TYPE_INTEGER:
	                try {
	                  rowData[i] = new Long( Long.parseLong( stringValue ) );
	                } catch ( Exception e ) {
	                  String message =
	                    BaseMessages.getString(
	                      PKG, "Constant.BuildRow.Error.Parsing.Integer", value.getName(), stringValue, e
	                        .toString() );
	                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
	                }
	                break;

	              case ValueMetaInterface.TYPE_BIGNUMBER:
	                try {
	                  rowData[i] = new BigDecimal( stringValue );
	                } catch ( Exception e ) {
	                  String message =
	                    BaseMessages.getString(
	                      PKG, "Constant.BuildRow.Error.Parsing.BigNumber", value.getName(), stringValue, e
	                        .toString() );
	                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
	                }
	                break;

	              case ValueMetaInterface.TYPE_BOOLEAN:
	                rowData[i] =
	                  Boolean
	                    .valueOf( "Y".equalsIgnoreCase( stringValue ) || "TRUE".equalsIgnoreCase( stringValue ) );
	                break;

	              case ValueMetaInterface.TYPE_BINARY:
	                rowData[i] = stringValue.getBytes();
	                break;

	              case ValueMetaInterface.TYPE_TIMESTAMP:
	                try {
	                  rowData[i] = Timestamp.valueOf( stringValue );
	                } catch ( Exception e ) {
	                  String message =
	                    BaseMessages.getString(
	                      PKG, "Constant.BuildRow.Error.Parsing.Timestamp", value.getName(), stringValue, e
	                        .toString() );
	                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
	                }
	                break;

	              default:
	                String message =
	                  BaseMessages.getString(
	                    PKG, "Constant.CheckResult.SpecifyTypeError", value.getName(), stringValue );
	                remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
	            }
	          }
	        }
	        // Now add value to the row!
	        // This is in fact a copy from the fields row, but now with data.
	        rowMeta.addValueMeta( value );

	      } // end if
	    } // end for

	    return new RowMetaAndData( rowMeta, rowData );
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

		Object[] r = null;
	    r = getRow();

	    if ( r == null ) { // no more rows to be expected from the previous step(s)
	      setOutputDone();
	      return false;
	    }
	    String rowKey = (String)r[0];
	    if ( data.firstRow ) {
	      // The output meta is the original input meta + the
	      // additional constant fields.

	      data.firstRow = false;
	      data.entity = new Entity<String, String>(rowKey);
	      data.outputRowMeta = super.getInputRowMeta().clone();

	      RowMetaInterface constants = data.constants.getRowMeta();
	      data.outputRowMeta.mergeRowMeta( constants );
	    }
	    if(data.entity.getKey() != null && data.entity.getKey().equals(rowKey)) {
	    	data.entity.addEntityRow(r);
	    } else {
	    	new RDFModel("http://biblioteca.ucuenca.edu.ec/resource/", super.getInputRowMeta())
	    	.process(data.entity);
	    	
	    	data.entity = new Entity<String, String>(rowKey);
	    }
	    

	    // Add the constant data to the end of the row.
	    r = RowDataUtil.addRowData( r, getInputRowMeta().size(), data.constants.getData() );

	    putRow( data.outputRowMeta, r );

	    if ( log.isRowLevel() ) {
	      logRowlevel( BaseMessages.getString(
	        PKG, "Constant.Log.Wrote.Row", Long.toString( getLinesWritten() ), getInputRowMeta().getString( r ) ) );
	    }

	    if ( checkFeedback( getLinesWritten() ) ) {
	      if ( log.isBasic() ) {
	        logBasic( BaseMessages.getString( PKG, "Constant.Log.LineNr", Long.toString( getLinesWritten() ) ) );
	      }
	    }

	    return true;
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
		OntoMapMeta meta = (OntoMapMeta) smi;
		OntoMapData data = (OntoMapData) sdi;
		
		super.dispose(meta, data);
	}

}
