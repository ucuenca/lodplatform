package com.ucuenca.pentaho.plugin.step.marc21loader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.marc4j.MarcReader;
import org.marc4j.MarcWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


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
public class MARC21LoaderData extends BaseStepData implements StepDataInterface 
{
	//public ShapeFile shapeFile;
	public List<MarcReader> marcfiles;
	public Iterator<MarcReader> marcfilesIterator;
	public MarcReader processingMarcfile;
	public MarcWriter marcXmlOutfile;
	// the size of the input rows
	public int inputSize;
	public int shapeNr;
	public RowMetaInterface outputRowMeta;

	/**
	 * 
	 */
	public MARC21LoaderData()
	{
		super();
		this.marcfiles = new ArrayList<MarcReader>();
	}
}
