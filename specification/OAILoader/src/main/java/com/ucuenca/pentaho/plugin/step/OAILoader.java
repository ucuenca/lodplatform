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

package com.ucuenca.pentaho.plugin.step;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ucuenca.pentaho.plugin.oai.ListRecords;
import com.ucuenca.pentaho.plugin.oai.Schema;

public class OAILoader extends BaseStep implements StepInterface {

	ArrayList<String> datos;
	ArrayList<String> nameFields;
	String numRegistro;

	public OAILoader(StepMeta s, StepDataInterface stepDataInterface, int c,
			TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		OAILoaderMeta meta = (OAILoaderMeta) smi;
		OAILoaderData data = (OAILoaderData) sdi;

		return super.init(meta, data);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {

		// safely cast the step settings (meta) and runtime info (data) to
		// specific implementations
		OAILoaderMeta meta = (OAILoaderMeta) smi;
		OAILoaderData data = (OAILoaderData) sdi;

		Schema schema = new Schema();

		// get incoming row, getRow() potentially blocks waiting for more rows,
		// returns null if no more rows expected
		Object[] r = getRow();

		// if no more rows are expected, indicate step is finished and
		// processRow() should not be called again
		if (r == null) {
			setOutputDone();
			// return false;
		}

		if (first) {
			first = false;
			data.outputRowMeta = new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			schema.setNamespace(meta.getNamespace());
			schema.setPrefix(meta.getPrefix());
			schema.setSchema(meta.getSchema());
		}

		Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta
				.size());
		int outputIndex;

		if ((data.initialResumptionToken != null)
				&& schema.prefix.equals(meta.getPrefix())) {
			data.resumptionToken = data.initialResumptionToken;

			// sgonzalez parametro schema
			try {
				data.listRecords = new ListRecords(meta.getInputURI(),
						data.resumptionToken, schema);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			data.resumptionToken = "";

			// sgonzalez parametro schema
			try {
				data.listRecords = new ListRecords(meta.getInputURI(),
						data.fromDate, data.untilDate, data.set,
						meta.getPrefix(), schema);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// ********************************* Initial while
		// ***************************
		while (data.listRecords != null) {

			outputIndex = 0;

			NodeList records = null;
			NodeList header = null;

			try {
				records = data.listRecords.getNodeList(meta.getXpath());
				if (header == null) {
					header = data.listRecords
							.getNodeList("/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:header");
				}
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int batch = records.getLength();
			data.total += batch;

			for (int temp1 = 0; temp1 < records.getLength(); temp1++) {
				Node nNode1 = records.item(temp1);
				Node nNodeHeader = header.item(temp1);

				// inicializacion of arraylist
				datos = new ArrayList<String>();
				nameFields = new ArrayList<String>();

				if (nNode1.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement1 = (Element) nNode1;
					Element eElementHeader = (Element) nNodeHeader;

					GetHeader(eElementHeader);

					StringTokenizer strobj = new StringTokenizer(
							meta.getXpath(), "/");

					// called methods for get data
					if (schema.prefix.equals("xoai")) {
						GetDataXOAI(eElement1, strobj.countTokens(), 0, 0);
					} else if (schema.prefix.equals("uketd_dc")) {
						GetDataUketd_dc(eElement1);
					} else if (schema.prefix.equals("qdc")) {
						GetDataQDC(eElement1);
					} else if (schema.prefix.equals("ore")) {
						GetDataUketd_dc(eElement1);
					} else if (schema.prefix.equals("oai_dc")) {
						GetDataUketd_dc(eElement1);
					} else if (schema.prefix.equals("didl")) {
						GetDataUketd_dc(eElement1);
					}

					for (int i = 0; i < datos.size(); i++) {
						outputRow[0] = numRegistro;
						outputRow[1] = nameFields.get(i);
						outputRow[2] = datos.get(i);
						putRow(data.outputRowMeta, outputRow);
					}

				}

			}// end two for

			try {
				data.resumptionToken = data.listRecords.getResumptionToken();
				System.out.println(data.resumptionToken);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (data.resumptionToken == null
					|| data.resumptionToken.length() == 0) {

				data.listRecords = null;

				setOutputDone();
				return false;

			} else {

				try {
					data.listRecords = new ListRecords(meta.getInputURI(),
							data.resumptionToken);
					data.listRecords = new ListRecords(meta.getInputURI(),
							data.resumptionToken);

					data.listRecords = new ListRecords(meta.getInputURI(),
							data.resumptionToken);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}// end while

		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}
		return true;

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

	String campo = "";
	String tag = "";

	public void GetDataXOAI(Element objElement, int numtoken, int numNodes,
			int aux) {

		if (objElement.getChildNodes().getLength() == 1) {
			datos.add(objElement.getTextContent());
			nameFields.add(campo);
		} else {
			if (numtoken == 5 && (aux >= 1 && aux <= 4)) {

				if (aux == 2 && !tag.equals(objElement.getAttribute("name"))) {
					StringTokenizer strobj = new StringTokenizer(campo, "/");
					campo = strobj.nextToken();
				} else if (aux == 1
						&& !tag.equals(objElement.getAttribute("name"))) {
					campo = "";
				} else if (aux == 3
						&& !tag.equals(objElement.getAttribute("name"))) {
					StringTokenizer strobj = new StringTokenizer(campo, "/");
					campo = strobj.nextToken();
					campo = campo + "/" + strobj.nextToken();
				}

				if (campo.equals("")) {
					tag = objElement.getAttribute("name");
					campo = tag;

				} else {
					tag = objElement.getAttribute("name");
					campo = campo + "/" + tag;
				}

			}

			if (numtoken == 6 && (aux >= 0 && aux <= 3)) {

				if (aux == 1 && !tag.equals(objElement.getAttribute("name"))) {
					StringTokenizer strobj = new StringTokenizer(campo, "/");
					campo = strobj.nextToken();
				} else if (aux == 0
						&& !tag.equals(objElement.getAttribute("name"))) {
					campo = "";
				} else if (aux == 2
						&& !tag.equals(objElement.getAttribute("name"))) {
					StringTokenizer strobj = new StringTokenizer(campo, "/");
					campo = strobj.nextToken();
					campo = campo + "/" + strobj.nextToken();
				}

				if (campo.equals("")) {
					tag = objElement.getAttribute("name");
					campo = tag;

				} else {
					tag = objElement.getAttribute("name");
					campo = campo + "/" + tag;
				}

			}

			if (numtoken == 7 && (aux >= 0 && aux <= 2)) {

				if (aux == 1 && !tag.equals(objElement.getAttribute("name"))) {

					Element objpadre1 = (Element) objElement.getParentNode();
					Element objpadre = (Element) objpadre1.getParentNode();

					campo = objpadre.getAttribute("name") + "/"
							+ objpadre1.getAttribute("name");
				} else if (aux == 0
						&& !tag.equals(objElement.getAttribute("name"))) {
					Element objpadre1 = (Element) objElement.getParentNode();

					tag = objpadre1.getAttribute("name");
					campo = tag;
				}

				if (campo.equals("")) {

					tag = objElement.getAttribute("name");
					campo = tag;

				} else {
					tag = objElement.getAttribute("name");
					campo = campo + "/" + tag;
				}

			}

			if (numtoken == 8 && (aux >= 0 && aux <= 1)) {

				if (aux == 0 && !tag.equals(objElement.getAttribute("name"))) {
					Element objpadre1 = (Element) objElement.getParentNode();
					Element objpadre = (Element) objpadre1.getParentNode();
					campo = objpadre.getAttribute("name") + "/"
							+ objpadre1.getAttribute("name");
				}

				tag = objElement.getAttribute("name");
				campo = campo + "/" + tag;

			}

			NodeList datosnodo1 = objElement.getChildNodes();

			int numNode = datosnodo1.getLength();

			for (int temp2 = 0; temp2 < datosnodo1.getLength(); temp2++) {
				Node nNode2 = datosnodo1.item(temp2);

				if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement2 = (Element) nNode2;

					GetDataXOAI(eElement2, numtoken, numNode, aux + 1);
				}

			}

		}
	}

	public void GetDataUketd_dc(Element prueba) {

		String tag = null;

		if (prueba.getChildNodes().getLength() == 1) {
			datos.add(prueba.getTextContent());
			StringTokenizer strobj1 = new StringTokenizer(prueba.getTagName(),
					":");
			strobj1.nextToken();
			tag = strobj1.nextToken();
			nameFields.add(tag);

		} else {
			Element eElement = (Element) prueba;
			NodeList datosnodo = eElement.getChildNodes();

			for (int temp = 0; temp < datosnodo.getLength(); temp++) {
				Node nNode = datosnodo.item(temp);
				String nameNode = nNode.getNodeName();
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement1 = (Element) nNode;

					GetDataUketd_dc(eElement1);

				}
			}
		}
	}

	public void GetDataQDC(Element prueba) {

		String tag = null;

		if (prueba.getChildNodes().getLength() == 1) {
			datos.add(prueba.getTextContent());
			StringTokenizer strobj1 = new StringTokenizer(prueba.getTagName(),
					":");
			strobj1.nextToken();
			tag = strobj1.nextToken();
			nameFields.add(tag);

		} else {
			Element eElement = (Element) prueba;
			NodeList datosnodo = eElement.getChildNodes();

			for (int temp = 0; temp < datosnodo.getLength(); temp++) {
				Node nNode = datosnodo.item(temp);
				String nameNode = nNode.getNodeName();
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement1 = (Element) nNode;
					GetDataQDC(eElement1);

				}
			}
		}
	}

	// for extraction of the header

	public void GetHeader(Element prueba) {

		String tag = null;

		if (prueba.getChildNodes().getLength() == 1) {

			datos.add(prueba.getTextContent());
			tag = prueba.getTagName();
			nameFields.add(tag);
			if (prueba.getTagName().equals("identifier")) {
				numRegistro = prueba.getTextContent();
			}

		} else {
			Element eElement = (Element) prueba;
			NodeList datosnodo = eElement.getChildNodes();

			for (int temp = 0; temp < datosnodo.getLength(); temp++) {
				Node nNode = datosnodo.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement1 = (Element) nNode;
					GetHeader(eElement1);

				}
			}
		}
	}

}
