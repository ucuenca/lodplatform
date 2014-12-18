package com.ucuenca.pentaho.plugin.oai;

public class Schema {
    
    public String prefix;
    public String schema;
    public String namespace;
    
    public Schema() {}

    public Schema(String p) {
        this.prefix = p;
    }
    
    public void setPrefix(String p) {
        this.prefix = p;
    }
    public void setSchema(String s) {
        this.schema = s;
    }
    public void setNamespace(String n) {
        this.namespace = n;
    }
}
