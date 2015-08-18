package com.ucuenca.pentaho.plugin.step;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;


/** .
 * @author Fabian Peñaloza Marin
 * @version 1
 */

public class fuseki{

   protected static final String uri ="http://jena.apache.org/fuseki#";

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

   public static final Property Service = property( "Service" );
   public static final Property dataset = property("dataset");

   
   
   public static final Resource myds = property( "myds" );
   

   /**
       The same items of vocabulary, but at the Node level, parked inside a
       nested class so that there's a simple way to refer to them.
   */
   @SuppressWarnings("hiding") public static final class Nodes
       {
       public static final Node Service= fuseki.Service.asNode();
       public static final Node dataset = fuseki.dataset.asNode();
       public static final Node myds = fuseki.myds.asNode();
       
       }

}

