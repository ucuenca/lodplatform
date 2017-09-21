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

import java.sql.SQLException;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.core.R2RMLProcessor;
import net.antidot.sql.model.core.DriverType;
import net.antidot.sql.model.core.SQLConnector;

import org.openrdf.rio.RDFFormat;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * This class is part of the demo step plug-in implementation. It demonstrates
 * the basics of developing a plug-in step for PDI.
 * 
 * The demo step adds a new string field to the row stream and sets its value to
 * "Hello World!". The user may select the name of the new field.
 * 
 * This class is the implementation of StepInterface. Classes implementing this
 * interface need to:
 * 
 * - initialize the step - execute the row processing logic - dispose of the
 * step
 * 
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data
 * interface instead.
 * 
 */

public class RDFGeneration extends BaseStep implements StepInterface {

	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s
	 *            step description
	 * @param stepDataInterface
	 *            step data class
	 * @param c
	 *            step copy
	 * @param t
	 *            transformation description
	 * @param dis
	 *            transformation executing
	 */

	private static Class<?> PKG = RDFGenerationMeta.class;

	public RDFGeneration(StepMeta s, StepDataInterface stepDataInterface,
			int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	/**
	 * This method is called by PDI during transformation startup.
	 * 
	 * It should initialize required for step execution.
	 * 
	 * The meta and data implementations passed in can safely be cast to the
	 * step's respective implementations.
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a
	 * database, as wall as obtaining resources, like file handles.
	 * 
	 * @param smi
	 *            step meta interface implementation, containing the step
	 *            settings
	 * @param sdi
	 *            step data interface implementation, used to store runtime
	 *            information
	 * 
	 * @return true if initialization completed successfully, false if there was
	 *         an error preventing the step from working.
	 * 
	 */
	// public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
	// // Casting to step-specific implementation classes is safe
	// RDFGenerationMeta meta = (RDFGenerationMeta) smi;
	// RDFGenerationData data = (RDFGenerationData) sdi;
	//
	// return super.init(meta, data);
	// }

	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {

		RDFGenerationMeta meta = (RDFGenerationMeta) smi;
		RDFGenerationData data = (RDFGenerationData) sdi;
		getRow();
		if (first) {
			first = false;
			super.init(meta, data);
			data.outputRowMeta = getInputRowMeta() != null ? getInputRowMeta().clone(): new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		}
		
		Object [] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		outputRow[0] = meta.getInputFieldr2rml(); 
		if (meta.getInputFieldr2rml() == null || meta.getSqlvendor() == null
				|| meta.getDatabaseURL() == null
				|| meta.getDatabaseSchema() == null
				|| meta.getUserName() == null || meta.getPassword() == null
				|| meta.getDirectorioOutputRDF() == null
				|| meta.getFormat() == null) {
			logError(BaseMessages.getString(PKG,
					"RDFGeneration.ERROR.MissingField"));
			outputRow[1] = "ERROR =>" + BaseMessages.getString(PKG,
					"RDFGeneration.ERROR.MissingField");

		} else {
			String status = "OK";
			if (meta.getBaseUri() == null) {
				meta.setBaseUri("");
			}
			try {

				if (meta.getFormat().equals("TURTLE")) {
                                       
					data.rdfFormat =  RDFFormat.TURTLE;
				} else if (meta.getFormat().equals("RDFXML")) {
					data.rdfFormat = RDFFormat.RDFXML;
				} else if (meta.getFormat().equals("NTRIPLES")) {
					data.rdfFormat = RDFFormat.NTRIPLES;
				} else if (meta.getFormat().equals("N3")) {
					data.rdfFormat = RDFFormat.N3;
				}

				if (meta.getSqlvendor().equals("H2")) {
					data.sqlDriver = new DriverType("org.h2.Driver");
				} else if (meta.getSqlvendor().equals("MySql")) {
					data.sqlDriver = new DriverType("com.mysql.jdbc.Driver");
				} else if (meta.getSqlvendor().equals("PostgreSql")) {
					data.sqlDriver = new DriverType("org.postgresql.Driver");
				}
				data.conn = SQLConnector.connect(meta.getUserName(),
						meta.getPassword(),
						meta.getDatabaseURL() + meta.getDatabaseSchema(),
						data.sqlDriver);

				SesameDataSet g = null;

				g = R2RMLProcessor.convertDatabase(data.conn,
						meta.getInputFieldr2rml(), meta.getBaseUri(),
						data.sqlDriver);
				outputRow[0] = meta.getDirectorioOutputRDF() + "/"
						+ meta.getFileoutput();
				g.dumpRDF(
						meta.getDirectorioOutputRDF() + "/"
								+ meta.getFileoutput(), data.rdfFormat);

			} catch (Exception e) {
				status = "ERROR => " + e.getMessage();
				logError(status, e);
			} finally {
				try {
					// Close db connection
                                    
                                    RDFFormat a = RDFFormat.TURTLE;
					data.conn.close();
				} catch (SQLException e) {
					logBasic("expection" + e.getMessage());
				}
				outputRow[1] = status;
			}
		}
		putRow(data.outputRowMeta, outputRow);

		if (checkFeedback(getLinesRead())) {
			logBasic(meta.getDirectorioOutputRDF()); // Some basic logging
		}

		// indicate that processRow() should be called again
		return false;
	}

	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		RDFGenerationMeta meta = (RDFGenerationMeta) smi;
		RDFGenerationData data = (RDFGenerationData) sdi;

		super.dispose(meta, data);
	}

}
