/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucuenca.pentaho.plugin.auxiliary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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
 * @author joseph6x
 */
public class XSLTUtils {

    private static final String TMP = "/tmp/";

    public InputStream Transform(InputStream in) throws SAXException, ParserConfigurationException, IOException, XPathExpressionException, TransformerException, Exception {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/exe.jar_");
        String theString = IOUtils.toString(in);
        String toString = IOUtils.toString(this.getClass().getResourceAsStream("/MARCXML2OAI_DC.xslt"));
        Files.copy(resourceAsStream, Paths.get(TMP + "exe.jar"), StandardCopyOption.REPLACE_EXISTING);
        wr(theString, "data.xml");
        wr(toString, "transformation.xslt");

        String tr = run("transformation.xslt", "data.xml");
        return new ByteArrayInputStream(tr.getBytes(StandardCharsets.UTF_8));
    }

    public void wr(String s, String f) throws IOException {
        File file = new File(TMP + f);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(s);
        fileWriter.flush();
        fileWriter.close();
    }

    public String run(String a, String b) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "exe.jar", TMP + a, TMP + b);
        pb.directory(new File(TMP));
        Process p = pb.start();
        InputStream in = p.getInputStream();
        String toString = IOUtils.toString(in);
        return toString;
    }

    public String ApplyXSLT(String xmlIn, String xsl) throws TransformerConfigurationException, TransformerException {
        StreamSource xslSource = new StreamSource(new StringReader(xsl));
        StreamSource xmlInSource = new StreamSource(new StringReader(xmlIn));
        Transformer tf = TransformerFactory.newInstance().newTransformer(xslSource);
        StringWriter xmlOutWriter = new StringWriter();
        tf.transform(xmlInSource, new StreamResult(xmlOutWriter));
        return xmlOutWriter.toString();
    }

    public Node Marcxml2Oai_dc(Node n) {
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("/MARCXML2OAI_DC.xslt");
            StreamSource style = new StreamSource(resourceAsStream);
            String nodeToString = nodeToXML(n);
            String nodeToString2 = StreamSourceToXML(style);
            String t = ApplyXSLT(nodeToString, nodeToString2);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(t));
            Document doc = db.parse(is);
            return doc.getFirstChild();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String StreamSourceToXML(StreamSource source) throws TransformerConfigurationException, TransformerException {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.transform(source, result);
        String strResult = writer.toString();
        return strResult;
    }

    public String nodeToXML(Node node) throws TransformerException {
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    public Document r2(String st) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = domFact.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(st));
        Document doc = builder.parse(is);
        return doc;
    }

    public String r(Document doc) throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = domFact.newDocumentBuilder();
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    public String tr(String a, String b) throws TransformerException, MalformedURLException, IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        InputStream toInputStream1 = IOUtils.toInputStream(a);
        InputStream toInputStream2 = IOUtils.toInputStream(b);
        Source xslt = new StreamSource(toInputStream1);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        //builderFactory.setNamespaceAware(true);
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
        String expression = "/OAI-PMH/ListRecords/record/metadata/record";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer(xslt);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            renameNamespaceRecursive(item, "http://www.loc.gov/MARC21/slim");
            String nodeToXML = nodeToXML(item);
            String ApplyXSLT = ApplyXSLT(nodeToXML, a);

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

    public static void renameNamespaceRecursive(Node node, String namespace) {
        Document document = node.getOwnerDocument();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            document.renameNode(node, namespace, node.getNodeName());
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); ++i) {
            renameNamespaceRecursive(list.item(i), namespace);
        }
    }

}
