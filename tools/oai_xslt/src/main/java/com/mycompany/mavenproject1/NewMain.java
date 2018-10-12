/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author cedia
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TransformerException, IOException, MalformedURLException, ParserConfigurationException, SAXException, XPathExpressionException {
        
        //"/home/cedia/tmp/MARCXML2OAI_DC.xslt"
        //"/home/cedia/tmp/c.xml"
        System.out.println(t(w(args[0]), w(args[1])));
    }

    public static String w(String a) throws FileNotFoundException, IOException {
        FileInputStream fileIS = new FileInputStream(new File(a));
        String theString = IOUtils.toString(fileIS);
        return theString;
    }

    public static String t(String a, String b) throws TransformerException, MalformedURLException, IOException, ParserConfigurationException, SAXException, XPathExpressionException {

        InputStream toInputStream1 = IOUtils.toInputStream(a);
        InputStream toInputStream2 = IOUtils.toInputStream(b);
        Source xslt = new StreamSource(toInputStream1);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(toInputStream2);
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "oai20":
                        return "http://www.openarchives.org/OAI/2.0/";
                    case "marc":
                        return "http://www.loc.gov/MARC21/slim";
                    case "dc":
                        return "http://purl.org/dc/elements/1.1/";
                    case "aoi_dc":
                        return "http://www.openarchives.org/OAI/2.0/oai_dc/";
                }
                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        });
        String expression = "/oai20:OAI-PMH/oai20:ListRecords/oai20:record/oai20:metadata/marc:record";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
                    TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
        Transformer transformer = transformerFactory.newTransformer(xslt);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);

            
            DOMSource source = new DOMSource(item);
            StringWriter xmlOutWriter = new StringWriter();
            transformer.transform(source, new StreamResult(xmlOutWriter));
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlOutWriter.toString()));
            Node firstChild = builder.parse(is).getFirstChild();
            Node importNode = xmlDocument.importNode(firstChild, true);
            item.getParentNode().replaceChild(importNode, item);
        }
        return nodeToXML(xmlDocument);
    }

    public static String nodeToXML(Node node) throws TransformerException {
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

}
