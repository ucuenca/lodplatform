package com.ucuenca.pentaho.plugin.auxiliary;

import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ucuenca.pentaho.plugin.step.oai.OAILoaderMeta;

public class GetXPath {
	OAILoaderMeta meta;
	String prefix;

	// methods for get Xpath from the file XLM of the server OAI
	/*
	 * this method get the element metadata for send to the method GetXpath that
	 * recivies two parameters the firts parameter is a Element, it parameter
	 * element contains all the information about of data of the file xml
	 */

	public GetXPath(String prefix) {
		super();
		this.prefix = prefix;
		meta = new OAILoaderMeta();
	}

	public boolean getPath(Node record) {
		// meta.getListpath().add("/oai20:OAI-PMH");
		// meta.getListpath().add("/oai20:OAI-PMH/oai20:ListRecords");
		// meta.getListpath().add("/oai20:OAI-PMH/oai20:ListRecords/oai20:record");
		// meta.getListpath().add("/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata");

		if (record.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) record;
			NodeList datosnodo = eElement.getChildNodes();

			for (int temp1 = 0; temp1 < datosnodo.getLength(); temp1++) {
				Node nNode1 = datosnodo.item(temp1);

				String nameNode = nNode1.getNodeName();
				if (nNode1.getNodeType() == Node.ELEMENT_NODE
						&& nameNode.equalsIgnoreCase("metadata")) {
					Element eElement1 = (Element) nNode1;
					if (prefix.equals("xoai")) {
						GetXpath(eElement1, 0);
					} else if (prefix.equals("uketd_dc")) {
						GetXpathuketd_dc(nNode1);
					}
					else if (prefix.equals("qdc")) {
						GetXpathQDC(nNode1);
					}else if (prefix.equals("ore")) {
						GetXpathuketd_dc(nNode1);
					}
					else if (prefix.equals("oai_dc") || prefix.equals("marcxml")) {
						GetXpathuketd_dc(nNode1);
					}
					else if (prefix.equals("didl")) {
						GetXpathuketd_dc(nNode1);
					}
					
					
				 return true;	
				}
                               
			}// end two for

		}
                return false;
	}

	String develto = "/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata";

	public void GetXpath(Element objElement, int aux) {

		String tag = null;
		if (objElement.getChildNodes() == null
				|| objElement.getTagName().equals("field")) {
		} else {

			NodeList datosnodo1 = objElement.getChildNodes();

			for (int temp2 = 0; temp2 < datosnodo1.getLength(); temp2++) {
				Node nNode2 = datosnodo1.item(temp2);

				if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement2 = (Element) nNode2;
					tag = eElement2.getTagName();

					if (tag.contains("metadata")) {
						develto = develto + "/" + prefix + ":" + tag;
						meta.getListpath().add(develto);
						GetXpath(eElement2, aux);
					} else {

						// code for return to the nodes son of metadata
						if (aux == 0) {

							develto = "/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata/"
									+ prefix
									+ ":metadata/"
									+ prefix
									+ ":"
									+ eElement2.getTagName()
									+ "[@name='"
									+ eElement2.getAttribute("name") + "']";

							if (!meta.getListpath().contains(develto)) {
								meta.getListpath().add(develto);
							}

						} else if (aux == 1) {
							Element elementAux = (Element) eElement2
									.getParentNode();

							develto = "/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata/"
									+ prefix
									+ ":metadata/"
									+ prefix
									+ ":"
									+ elementAux.getTagName()
									+ "[@name='"
									+ elementAux.getAttribute("name") + "']";
							if (!tag.equals("field")) {
								develto = develto + "/" + prefix + ":"
										+ eElement2.getTagName() + "[@name='"
										+ eElement2.getAttribute("name") + "']";
								if (!meta.getListpath().contains(develto)) {
									meta.getListpath().add(develto);
								}

							}
						} else if (aux == 2) {
							Element elementAux = (Element) eElement2
									.getParentNode();
							Element elemetPanter = (Element) elementAux
									.getParentNode();
							develto = "/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata/"
									+ prefix
									+ ":metadata/"
									+ prefix
									+ ":"
									+ elemetPanter.getTagName()
									+ "[@name='"
									+ elemetPanter.getAttribute("name")
									+ "']/"
									+ prefix
									+ ":"
									+ elementAux.getTagName()
									+ "[@name='"
									+ elementAux.getAttribute("name") + "']";
							if (!tag.equals("field")) {
								develto = develto + "/" + prefix + ":"
										+ eElement2.getTagName() + "[@name='"
										+ eElement2.getAttribute("name") + "']";
								if (!meta.getListpath().contains(develto)) {
									meta.getListpath().add(develto);
								}
							}
						}

//						else if (!tag.equals("field")) {
//							develto = develto + "/" + prefix + ":"
//									+ eElement2.getTagName() + "[@name='"
//									+ eElement2.getAttribute("name") + "']";
//							if (!meta.getListpath().contains(develto)) {
//								meta.getListpath().add(develto);
//							}
//						}
						GetXpath(eElement2, aux + 1);
					}
				}
			}
		}// end tree for
	}

	public void GetXpathuketd_dc(Node prueba) {
		//meta.getListpath().add("/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata");
		String ruta = "/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata";
		
		if (prueba.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) prueba;
			NodeList datosnodo = eElement.getChildNodes();

			for (int temp = 0; temp < datosnodo.getLength(); temp++) {
				Node nNode = datosnodo.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement1 = (Element) nNode;

					
					if (!meta.getListpath().contains(ruta + "/"+ eElement1.getTagName())) {

						meta.getListpath().add(ruta + "/"+ eElement1.getTagName());
						ruta = ruta + "/"+ eElement1.getTagName();
					}

					NodeList datosnodo1 = eElement1.getChildNodes();

					for (int temp1 = 0; temp1 < datosnodo1.getLength(); temp1++) {
						Node nNode1 = datosnodo1.item(temp1);

						if (nNode1.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement2 = (Element) nNode1;
							System.out.println(eElement2.getTagName());

							if (!meta.getListpath().contains(ruta + "/"+eElement2.getTagName())) {

								meta.getListpath().add(ruta + "/"+ eElement2.getTagName());

							}
						}

					}

				}
			}// end two for

		}
		
	}


	public void GetXpathQDC(Node prueba) {		
		//meta.getListpath().add("/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata");
		String ruta = "/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata";
		
		
		if (prueba.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) prueba;
			NodeList datosnodo = eElement.getChildNodes();

			for (int temp = 0; temp < datosnodo.getLength(); temp++) {
				Node nNode = datosnodo.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement1 = (Element) nNode;
					
				
					if(!meta.getListpath().contains(ruta + "/"+ eElement1.getTagName() ))
					{						
						meta.getListpath().add(ruta + "/"+ eElement1.getTagName() );
					}	

				}
			}// end two for

		}
		
	}
	
	
	public OAILoaderMeta getMeta() {
		return meta;
	}

	public void setMeta(OAILoaderMeta meta) {
		this.meta = meta;
	}

}
