package com.ucuenca.pentaho.plugin.step;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

/** .
 * @author Fabian Peñaloza Marin
 * @version 1
 */
public class ja{

   protected static final String uri ="http://jena.hpl.hp.com/2005/11/Assembler#";

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

   public static final Property defaultGraph  = property( "defaultGraph" );
   public static final Property RDFDataset = property("RDFDataset");
   public static final Property MemoryModel = property( "MemoryModel" );
   public static final Property content = property( "content" );
   public static final Property externalContent = property( "externalContent" );
   
   public static final Property namedGraph = property("namedGraph");
   public static final Property graphName = property("graphName");
   public static final Property graph  = property("graph");
   /**
       The same items of vocabulary, but at the Node level, parked inside a
       nested class so that there's a simple way to refer to them.
   */
   @SuppressWarnings("hiding") public static final class Nodes
       {
       public static final Node defaultGraph  = ja.defaultGraph .asNode();
       public static final Node RDFDataset = ja.RDFDataset.asNode();
       public static final Node MemoryModel = ja.MemoryModel.asNode();
       public static final Node content = ja.content.asNode();
       public static final Node externalContent = ja.externalContent.asNode();
       
       public static final Node namedGraph = ja.namedGraph.asNode();
       public static final Node graphName = ja.graphName.asNode();
       public static final Node graph  = ja.graph.asNode();
       
       }

}

