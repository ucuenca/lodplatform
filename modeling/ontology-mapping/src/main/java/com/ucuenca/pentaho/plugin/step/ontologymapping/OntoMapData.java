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

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.ucuenca.pentaho.plugin.step.ontologymapping.rdf.Entity;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepDataInterface.
 *   
 * Implementing classes inherit from BaseStepData, which implements the entire
 * interface completely. 
 * 
 * In addition classes implementing this interface usually keep track of
 * per-thread resources during step execution. Typical examples are:
 * result sets, temporary data, caching indexes, etc.
 *   
 * The implementation for the demo step stores the output row structure in 
 * the data class. 
 *   
 */
public class OntoMapData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;
	
	public NumberFormat nf;
	  public DecimalFormat df;
	  public DecimalFormatSymbols dfs;
	  public SimpleDateFormat daf;
	  public DateFormatSymbols dafs;
	  public RowMetaAndData constants;
	  //RowMetaInterface outputMeta;
	  public boolean firstRow;
	  
	  public Entity<String, String> entity;
	
    public OntoMapData()
	{    	
		super();
		nf = NumberFormat.getInstance();
	    df = (DecimalFormat) nf;
	    dfs = new DecimalFormatSymbols();
	    daf = new SimpleDateFormat();
	    dafs = new DateFormatSymbols();
	}
}
	
