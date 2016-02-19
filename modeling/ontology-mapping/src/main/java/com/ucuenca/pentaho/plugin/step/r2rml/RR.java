package com.ucuenca.pentaho.plugin.step.r2rml;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * R2RML Schema definition
 * @author depcc
 *
 */
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

   public static final Property logicalTable = property( "logicalTable" );
   public static final Property sqlQuery = property("sqlQuery");
   public static final Property tableName = property( "tableName" );
   public static final Property subjectMap = property( "subjectMap" );
   public static final Property template = property( "template" );
   public static final Property constant = property( "constant" );
   public static final Property cclass = property( "class" );
   public static final Property predicateObjectMap = property( "predicateObjectMap" );
   public static final Property predicate = property( "predicate" );
   public static final Property objectMap = property( "objectMap" );
   public static final Property column = property( "column" );
   public static final Property datatype = property( "datatype" );
   public static final Property language = property( "language" );
   public static final Property termType = property( "termType" );
   public static final Property IRI = property( "IRI" );
   

   /**
       The same items of vocabulary, but at the Node level, parked inside a
       nested class so that there's a simple way to refer to them.
   */
   @SuppressWarnings("hiding") public static final class Nodes
       {
       public static final Node logicalTable = RR.logicalTable.asNode();
       public static final Node sqlQuery = RR.sqlQuery.asNode();
       public static final Node tableName = RR.tableName.asNode();
       public static final Node subjectMap = RR.subjectMap.asNode();
       public static final Node template = RR.template.asNode();
       public static final Node constant = RR.constant.asNode();
       public static final Node cclass = RR.cclass.asNode();
       public static final Node predicateObjectMap = RR.predicateObjectMap.asNode();
       public static final Node objectMap = RR.objectMap.asNode();
       public static final Node predicate = RR.predicate.asNode();
       public static final Node column = RR.column.asNode();
       public static final Node datatype = RR.datatype.asNode();
       public static final Node language = RR.language.asNode();
       public static final Node termType = RR.termType.asNode();
       public static final Node IRI = RR.IRI.asNode();
       
       }

}

