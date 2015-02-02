package com.ucuenca.pentaho.plugin.oai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.xml.sax.InputSource;

public class Transformers {

    private static final String PROPERTIES = "transformer.properties";
    private static final String TRANSFORMER = "transformer.xslt";
    private static final String SCHEMA_PROPERTY = "schema";
    private static final String NAMESPACE_PROPERTY = "namespace";
    
    public class Record {
        public String schema;
        public String namespace;
        public File file;
        public Record(String s, String n, File f) {
            this.schema = s;
            this.namespace = n;
            this.file = f;
        }
    }
    
    List registry = new ArrayList();
    
    SAXTransformerFactory factory;
    
    public Transformers(File folder) throws Exception {
        if (!folder.exists()) throw new IOException("Folder '" + folder.getAbsolutePath() + "' does not exist, could not load the transformers.");
        if (!folder.isDirectory()) throw new IOException("'" + folder.getAbsolutePath() + "' must be the folder containing the transformers.");
        if (!folder.canRead()) throw new IOException("You don't have permission to read the transformations folder '" + folder.getAbsolutePath() + "'.");
        
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                File p = new File(files[i],PROPERTIES);
                File t = new File(files[i],TRANSFORMER);
                if (p.exists() && t.exists()) {
                    Properties props = new Properties();
                    FileInputStream fis = new FileInputStream(p);
                    props.load(fis);
                    fis.close();
                    String schema = props.getProperty(SCHEMA_PROPERTY,"");
                    String namespace = props.getProperty(NAMESPACE_PROPERTY,"");
                    Record r = new Record(schema,namespace,t);
                    registry.add(r);
                }
            }
        }

        TransformerFactory transFactory = TransformerFactory.newInstance();
        if (!transFactory.getFeature(SAXTransformerFactory.FEATURE)) {
            throw new ClassNotFoundException("SAXTransformerFactory is not supported by the underlying JAXP implementation, so I can't continue.");
        }

        factory = (SAXTransformerFactory) transFactory;
    }
    
    public Transformer getTransformer(String namespace, String schema) throws Exception {
        Record found = null;
        Iterator i = registry.iterator();
        while (i.hasNext()) {
            Record r = (Record) i.next();
            if (r.namespace.equals(namespace) && r.schema.equals(schema)) {
                found = r;   
            }
        }
        if (found == null) {
            throw new FileNotFoundException("Could not find a transformer for schema '" + schema + "' and namespace '" + namespace + "'.");
        } else {
            FileInputStream fis = new FileInputStream(found.file);
            Transformer transformer = factory.newTransformer(new SAXSource(new InputSource(fis)));
            fis.close();
            return transformer;
        }
    }
    
    public Transformer getIdentityTransformer() throws Exception {
        return factory.newTransformer();
    }
}
