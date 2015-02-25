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
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ucuenca.misctools.StepDataLoader;
import com.ucuenca.misctools.DatabaseLoader;
import com.ucuenca.pentaho.plugin.oai.ListRecords;
import com.ucuenca.pentaho.plugin.oai.ListSets;
import com.ucuenca.pentaho.plugin.oai.Schema;

/**
 * This class is part of the demo step plug-in implementation. It demonstrates
 * the basics of developing a plug-in step for PDI.
 * 
 * The demo step adds a new string field to the row stream and sets its value to
 * "Hello World!". The user may select the name of the new field.
 * 
 * This class is the implementation of StepDataInterface.
 * 
 * Implementing classes inherit from BaseStepData, which implements the entire
 * interface completely.
 * 
 * In addition classes implementing this interface usually keep track of
 * per-thread resources during step execution. Typical examples are: result
 * sets, temporary data, caching indexes, etc.
 * 
 * The implementation for the demo step stores the output row structure in the
 * data class.
 * 
 */
public class OAILoaderData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;
	public ListRecords listRecords;
	public ListSets listSet;
	public String resumptionToken;
	public String initialResumptionToken;
	public String fromDate, untilDate, set;
	public Schema schema;
	int total;

	private ArrayList<String> datos;
	private ArrayList<String> nameFields;
	private String numRegistro;
	Hashtable<String, String> Sets = new Hashtable<String, String>();

	// must be included for DataBase Data Loading
	public static final String DBTABLE = "OAIPMHDATA";
	private final StepDataLoader dataLoader = new StepDataLoader(DBTABLE);

	// End Database Data Loading attributes

	public OAILoaderData() {
		super();
		initialResumptionToken = null;
		fromDate = null;
		untilDate = null;
		set = null;
		total = 0;
	}

	// must be included for DataBase Data Loading
	public StepDataLoader getDataLoader() {
		return this.dataLoader;
	}

	/**
	 * OAI Harvester initialization
	 * 
	 * @param meta
	 *            Step Metadata class
	 * @param data
	 *            Step Data class
	 */
	public void initOAIHarvester(OAILoaderMeta meta, OAILoaderData data) {
		data.schema = new Schema();
		data.schema.setNamespace(meta.getNamespace());
		data.schema.setPrefix(meta.getPrefix());
		data.schema.setSchema(meta.getSchema());

		data.initialResumptionToken = meta.getInitialResumptionToken();
		try {
			if ((data.initialResumptionToken != null)
					&& data.schema.prefix.equals(meta.getPrefix())) {
				data.resumptionToken = data.initialResumptionToken;
				dataLoader.logBasic("Resuming harvesting from "
						+ data.resumptionToken); // Some basic logging
				data.listRecords = new ListRecords(meta.getInputURI(),
						data.resumptionToken, data.schema);
				data.listSet = new ListSets(meta.getInputURI(),
						data.resumptionToken);
			} else {
				data.resumptionToken = "";
				data.listRecords = new ListRecords(meta.getInputURI(),
						data.fromDate, data.untilDate, data.set,
						meta.getPrefix(), data.schema);
				data.listSet = new ListSets(meta.getInputURI());
			}
		} catch (Exception e) {
			try {
				dataLoader.logBasic(e.getMessage());
			} catch (KettleException ke) {
				ke.printStackTrace();
			}
		}

	}

	// must be included for DataBase Data Loading
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
	public Boolean getData(StepMetaInterface smi, StepDataInterface sdi,
			Boolean databaseLoad) throws KettleException {
		OAILoaderMeta meta = (OAILoaderMeta) smi;
		OAILoaderData data = (OAILoaderData) sdi;
		// must be included for DataBase Data Loading
		data.outputRowMeta = data.outputRowMeta == null ? dataLoader
				.getMetaFieldsDef(smi) : data.outputRowMeta;
		Object[] outputRow = new Object[data.outputRowMeta.size() + 3];

		try {
			NodeList header = null;
		
			NodeList records = null;
			
           //Process for load the set 
			NodeList recordsSets = null;
			if (Sets.isEmpty()) {
				while (listSet != null) {

					recordsSets = data.listSet
							.getNodeList("oai20:OAI-PMH/oai20:ListSets/oai20:set");
					int batch = recordsSets.getLength();
					total += batch;
					dataLoader.logBasic("Harvested Records: batch "
							+ recordsSets.getLength() + ", total " + total);
                    String key=null;
                    String name=null;
    				
    				for (int temp1 = 0; temp1 < recordsSets.getLength(); temp1++) {
    					Node nSet = recordsSets.item(temp1);
    					
    					if (nSet.getNodeType() == Node.ELEMENT_NODE) {
    						Element eElement1 = (Element) nSet;
    						NodeList recordSet= eElement1.getChildNodes();
    						for (int temp2 = 0; temp2 < recordSet.getLength(); temp2++) {
            					Node nSet2 = recordSet.item(temp2);
            					if (nSet2.getNodeType() == Node.ELEMENT_NODE) {
            						Element eElement2 = (Element) nSet2;
            						if(nSet2.getNodeName().equals("setSpec")){
            						  key = nSet2.getTextContent();
            						}else if (nSet2.getNodeName().equals("setName")) {
            							name = nSet2.getTextContent();                    							
            						}                    						
            					}            					
    						}//end second for
    						Sets.put(key, name);    					
    					}                    
    				}					
					
					 resumptionToken = listSet.getResumptionToken();
                     if (resumptionToken == null || resumptionToken.length() == 0) {
                    	 dataLoader.logBasic("No more resumption token found, end was reached.");
                         //listRecords = null;
                         listSet = null;
                     } else {
                    	 dataLoader.logBasic("Resuming harvesting from " + resumptionToken);
                         try {
                         	//listRecords = new ListRecords(url_str, resumptionToken);
                         	listSet = new ListSets(meta.getInputURI(),
            						data.resumptionToken);
                         } catch (IOException e) {
                        	 dataLoader.logBasic("IOException while trying to resume from " + resumptionToken + ", trying again.");
                         	//listRecords = new ListRecords(url_str, resumptionToken);
                         	listSet = new ListSets(meta.getInputURI(),
            						data.resumptionToken);
                         } catch (SAXException e) {
                        	 dataLoader.logBasic("SAXException while trying to resume from " + resumptionToken + ", trying again.");
                         	//listRecords = new ListRecords(url_str, resumptionToken);
                        	 listSet = new ListSets(meta.getInputURI(),
             						data.resumptionToken);
                         }
                     }
					
				}
			}//end of load of set
			
			
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

			dataLoader.logBasic("Harvested Records: batch "
					+ records.getLength() + ", total " + data.total);

			// must be included for DataBase Data Loading
			if (databaseLoad)
				DatabaseLoader.getConnection();

			for (int temp1 = 0; temp1 < records.getLength(); temp1++) {
				Node nNode1 = records.item(temp1);
				Node nNodeHeader = header.item(temp1);

				datos = new ArrayList<String>();
				nameFields = new ArrayList<String>();

				if (nNode1.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement1 = (Element) nNode1;
					Element eElementHeader = (Element) nNodeHeader;

					getHeader(eElementHeader);

					StringTokenizer strobj = new StringTokenizer(
							meta.getXpath(), "/");

					// methods to get data
					if (data.schema.prefix.equals("xoai")) {
						getDataXOAI(eElement1, strobj.countTokens(), 0, 0);
					} else if (data.schema.prefix.equals("uketd_dc")) {
						getDataUKETDDC(eElement1);
					} else if (data.schema.prefix.equals("qdc")) {
						getDataQDC(eElement1);
					} else if (data.schema.prefix.equals("ore")) {
						getDataUKETDDC(eElement1);
					} else if (data.schema.prefix.equals("oai_dc")) {
						getDataUKETDDC(eElement1);
					} else if (data.schema.prefix.equals("didl")) {
						getDataUKETDDC(eElement1);
					}

					// must be included for DataBase Data Loading
					for (int i = 0; i < datos.size(); i++) {
						dataLoader.sequence++;
						int dataIndex = databaseLoad ? 3 : 0;
						outputRow[dataIndex] = numRegistro;
						outputRow[dataIndex + 1] = nameFields.get(i);
						outputRow[dataIndex + 2] = datos.get(i);
						if (databaseLoad) {
							outputRow[0] = meta.getTransMeta().getName()
									.toUpperCase();
							outputRow[1] = meta.getStepName().toUpperCase();
							outputRow[2] = Integer.valueOf(dataLoader.sequence);
							dataLoader.insertTableRow(smi, outputRow);
						} else {
							dataLoader.getBaseStep().putRow(data.outputRowMeta,
									outputRow);
						}
					}

				}

			}// end two for
			if (databaseLoad)
				DatabaseLoader.closeConnection();

			data.resumptionToken = data.listRecords.getResumptionToken();

			if (data.resumptionToken == null
					|| data.resumptionToken.length() == 0) {

				data.listRecords = null;
				dataLoader
						.logBasic("No more resumption token found, end was reached.");
				return false;

			} else {
				dataLoader.logBasic("Resuming harvesting from "
						+ data.resumptionToken);
				try {
					data.listRecords = new ListRecords(meta.getInputURI(),
							data.resumptionToken);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					dataLoader
							.logBasic("IOException while trying to resume from "
									+ data.resumptionToken + ", trying again.");

					data.listRecords = new ListRecords(meta.getInputURI(),
							data.resumptionToken);

				} catch (SAXException e) {
					dataLoader
							.logBasic("SAXException while trying to resume from "
									+ data.resumptionToken + ", trying again.");

					data.listRecords = new ListRecords(meta.getInputURI(),
							data.resumptionToken);

				}
			}

			// }// end while

		} catch (Exception e) {
			dataLoader.logBasic("Error: " + e.toString());
		}
		return true;

	}

	String campo = "";
	String tag = "";

	private void getDataXOAI(Element objElement, int numtoken, int numNodes,
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

					getDataXOAI(eElement2, numtoken, numNode, aux + 1);
				}

			}

		}
	}

	private void getDataUKETDDC(Element prueba) {

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
				// String nameNode = nNode.getNodeName();
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement1 = (Element) nNode;

					getDataUKETDDC(eElement1);

				}
			}
		}
	}

	private void getDataQDC(Element prueba) {

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
				// String nameNode = nNode.getNodeName();
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement1 = (Element) nNode;
					getDataQDC(eElement1);

				}
			}
		}
	}

	// for extraction of the header

	private void getHeader(Element prueba) {

		String tag = null;

		if (prueba.getChildNodes().getLength() == 1) {
			
			String value = prueba.getNodeName().equals("setSpec") ? 
					(prueba.getTextContent() + " => " + Sets.get(prueba.getTextContent()))
					:prueba.getTextContent();
			datos.add(value);
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
					getHeader(eElement1);

				}
			}
		}
	}

}
