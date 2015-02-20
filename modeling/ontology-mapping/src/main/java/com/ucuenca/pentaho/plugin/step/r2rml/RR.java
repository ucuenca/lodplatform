package com.ucuenca.pentaho.plugin.step.r2rml;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;


public class RR{

   protected static final String uri ="http://www.w3.org/ns/r2rml#";

   /** returns the URI for this schema
       @return the URI for this schema
   */
   public static String getURI()
       { return uri; }

   protected static final Resource resource( String local )
       { return ResourceFactory.createResource( uri + local ); }

   protected static final Property property( String local )
       { return ResourceFactory.createProperty( uri, local ); }

   public static Property li( int i )
       { return property( "_" + i ); }

   /*
   public static final Resource Alt = resource( "Alt" );
   public static final Resource Bag = resource( "Bag" );
   public static final Resource Property = resource( "Property" );
   public static final Resource Seq = resource( "Seq" );
   public static final Resource Statement = resource( "Statement" );
   public static final Resource List = resource( "List" );
   public static final Resource nil = resource( "nil" );*/

   public static final Property logicalTable = property( "logicalTable" );
   public static final Property tableName = property( "tableName" );
   public static final Property subjectMap = property( "subjectMap" );
   public static final Property template = property( "template" );
   public static final Property cclass = property( "class" );
   public static final Property predicateObjectMap = property( "predicateObjectMap" );
   public static final Property predicate = property( "predicate" );
   public static final Property objectMap = property( "objectMap" );
   public static final Property column = property( "column" );
   

   /**
       The same items of vocabulary, but at the Node level, parked inside a
       nested class so that there's a simple way to refer to them.
   */
   @SuppressWarnings("hiding") public static final class Nodes
       {
	   /*
       public static final Node Alt = RR.Alt.asNode();
       public static final Node Bag = RR.Bag.asNode();
       public static final Node Property = RR.Property.asNode();
       public static final Node Seq = RR.Seq.asNode();
       public static final Node Statement = RR.Statement.asNode();
       public static final Node List = RR.List.asNode();
       public static final Node nil = RR.nil.asNode();*/
       
       public static final Node logicalTable = RR.logicalTable.asNode();
       public static final Node tableName = RR.tableName.asNode();
       public static final Node subjectMap = RR.subjectMap.asNode();
       public static final Node template = RR.template.asNode();
       public static final Node cclass = RR.cclass.asNode();
       public static final Node predicateObjectMap = RR.predicateObjectMap.asNode();
       public static final Node objectMap = RR.objectMap.asNode();
       public static final Node predicate = RR.predicate.asNode();
       public static final Node column = RR.column.asNode();
       
       }

}

