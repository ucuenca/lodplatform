/**
 * Copyright 2006 OCLC, Online Computer Library Center
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ucuenca.pentaho.plugin.oai;

import com.ucuenca.pentaho.plugin.step.oai.OAILoaderDialog;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * This class represents an ListRecords response on either the server or on the
 * client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class ListRecords extends HarvesterVerb {

    /**
     * Mock object constructor (for unit testing purposes)
     */
    public boolean End = false;

    public ListRecords() {
        super();
    }

    /**
     * Client-side ListRecords verb constructor
     *
     * @param baseURL the baseURL of the server to be queried
     * @exception MalformedURLException the baseURL is bad
     * @exception SAXException the xml response is bad
     * @exception IOException an I/O error occurred
     */
    //sgonzalez parametro schemas
    public ListRecords(String baseURL, String from, String until,
            String set, String metadataPrefix, Schema... schemas)
            throws IOException, ParserConfigurationException, SAXException,
            TransformerException { 
        super(getRequestURL(baseURL, from, until, set, metadataPrefix), NeedTransform (metadataPrefix) , metadataPrefix, schemas);
    }
    
    private static boolean NeedTransform (String metadataPrefix) {
      if (metadataPrefix.compareTo(OAILoaderDialog.Format.MARCXML.getName()) == 0 || metadataPrefix.compareTo(OAILoaderDialog.Format.OAI_CERIF.getName())  == 0 )
      { return true;
      }
       return false;
  
    }

    /**
     * Client-side ListRecords verb constructor (resumptionToken version)
     *
     * @param baseURL
     * @param resumptionToken
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    //sgonzalez parametro schemas
    public ListRecords(String baseURL, String resumptionToken/*, String from*/, String metadataPrefix, Schema... schemas)
            throws IOException, ParserConfigurationException, SAXException,
            TransformerException {
        super(getRequestURL(baseURL, resumptionToken/*, from*/), NeedTransform (metadataPrefix), metadataPrefix , schemas);
    }

    /**
     * Get the oai:resumptionToken from the response
     *
     * @return the oai:resumptionToken value
     * @throws TransformerException
     * @throws NoSuchFieldException
     */
    public String getResumptionToken()
            throws TransformerException, NoSuchFieldException {
        if (End) {
            return "";
        }

        String schemaLocation = getSchemaLocation();
        if (schemaLocation.indexOf(SCHEMA_LOCATION_V2_0) != -1) {
            return getSingleString("/oai20:OAI-PMH/oai20:ListRecords/oai20:resumptionToken");
        } else if (schemaLocation.indexOf(SCHEMA_LOCATION_V1_1_LIST_RECORDS) != -1) {
            return getSingleString("/oai11_ListRecords:ListRecords/oai11_ListRecords:resumptionToken");
        } else {
            throw new NoSuchFieldException(schemaLocation);
        }
    }

    /**
     * Get the oai:ResponseDate from the response
     *
     * @return the oai:ResponseDate value
     * @throws TransformerException
     * @throws NoSuchFieldException
     */
    public String getResponeDate()
            throws TransformerException, NoSuchFieldException {
        String schemaLocation = getSchemaLocation();
        if (schemaLocation.indexOf(SCHEMA_LOCATION_V2_0) != -1) {
            return getSingleString("/oai20:OAI-PMH/oai20:responseDate");
        } else if (schemaLocation.indexOf(SCHEMA_LOCATION_V1_1_LIST_RECORDS) != -1) {
            return getSingleString("/oai11_ListRecords:ListRecords/oai11_ListRecords:responseDate");
        } else {
            throw new NoSuchFieldException(schemaLocation);
        }
    }

    public String getPrueba()
            throws TransformerException, NoSuchFieldException {
        String schemaLocation = getSchemaLocation();
        if (schemaLocation.indexOf(SCHEMA_LOCATION_V2_0) != -1) {
            return getSingleString("/oai20:OAI-PMH/oai20:ListRecords/oai20:field");
        } else if (schemaLocation.indexOf(SCHEMA_LOCATION_V1_1_LIST_RECORDS) != -1) {
            //getNodeList(xpath)
            return getSingleString("/oai11_ListRecords:ListRecords/oai11_ListRecords:field");
        } else {
            throw new NoSuchFieldException(schemaLocation);
        }
    }

    /**
     * Construct the query portion of the http request
     *
     * @return a String containing the query portion of the http request
     */
    private static String getRequestURL(String baseURL, String from,
            String until, String set,
            String metadataPrefix) {
        StringBuffer requestURL = new StringBuffer(baseURL);
        requestURL.append("?verb=ListRecords");
        if (from != null) {
            requestURL.append("&from=").append(from);
        }
        if (until != null) {
            requestURL.append("&until=").append(until);
        }
        if (set != null) {
            requestURL.append("&set=").append(set);
        }
        requestURL.append("&metadataPrefix=").append(metadataPrefix);
        return requestURL.toString();
    }

    /**
     * Construct the query portion of the http request (resumptionToken version)
     *
     * @param baseURL
     * @param resumptionToken
     * @return
     */
    private static String getRequestURL(String baseURL,
            String resumptionToken/*, String from*/) {
        StringBuffer requestURL = new StringBuffer(baseURL);
        requestURL.append("?verb=ListRecords");
        //if (from != null) requestURL.append("&from=").append(from);
        requestURL.append("&resumptionToken=").append(URLEncoder.encode(resumptionToken));
        return requestURL.toString();
    }
    
    
    public String getError()
            throws TransformerException, NoSuchFieldException {
        String schemaLocation = getSchemaLocation();
        if (schemaLocation.indexOf(SCHEMA_LOCATION_V2_0) != -1) {
            String date = getSingleString("/oai20:OAI-PMH/oai20:responseDate");
            String er = getSingleString("/oai20:OAI-PMH/oai20:error");
            return getSingleString("/oai20:OAI-PMH/oai20:error");               
        } else if (schemaLocation.indexOf(SCHEMA_LOCATION_V1_1_LIST_RECORDS) != -1) {
            return getSingleString("/oai11_ListRecords:ListRecords/oai11_ListRecords:responseDate");
        } else {
            throw new NoSuchFieldException(schemaLocation);
        }
    }
}
