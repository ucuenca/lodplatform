/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucuenca.pentaho.plugin.step.link;

/**
 *
 * @author cedia
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
//import org.silkframework.Silk;


public class LinkGeneration extends BaseStep implements StepInterface
{
    private LinkGenerationData data;
	private LinkGenerationMeta meta;
     public   Process p;
      //  int DefaultThreads = max(8, Runtime.getRuntime.availableProcessors())
	
	public LinkGeneration(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis)
	{
		super(s,stepDataInterface,c,t,dis);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{  
	    meta = (LinkGenerationMeta)smi;
	    data = (LinkGenerationData)sdi;
	    
		Object[] r=getRow();    // get row, blocks when needed!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first = false;
            
            data.outputRowMeta = (RowMetaInterface)getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);            
        }
        
       // Object extraValue = meta.getValue().getValueData();
    //   File f = new File (meta.getFileinput());
    
    String filesls = "";
   /*  if (meta.getFileinput() == null){
    //logBasic ("In"+meta.getFileinput().length()+" -"+meta.getFileinput() )};
        logBasic ("Nulooooo");
     }*/
      if (meta.getFileinput() != null){
        filesls = meta.getFileinput();
        logBasic ("Ruta"+filesls);
      }else {
        logBasic (meta.getSparql1() +"-" +meta.getSparql2()+"- "+meta.getGraph1()+"-"+ meta.getGraph2()+"-"+meta.getUmbral1() +"-"+ meta.getUmbral2()+"-"+meta.getDiroutput());
        filesls = xmlgeneration (meta.getSparql1(), meta.getSparql2(),meta.getGraph1(), meta.getGraph2()  ,meta.getUmbral1() , meta.getUmbral2(),"Silk" , meta.getDiroutput());
        logBasic ("Ruta"+filesls);
      }
      
          int resultado = silk (filesls);
          Object status;
        if (  resultado == 0){
           status = "OK";
        } else {
           status = "Fail";
        }
   //   Silk.executeFile( filesls, null , 4 , true );
        Object extraValue = meta.getFileinput();
         Object [] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
         outputRow [0] = filesls;
         outputRow [1] =  status;
        //Object[] outputRow = RowDataUtil.addValueData(r, data.outputRowMeta.size()-1, extraValue);
        //Object[] outputRow2 = RowDataUtil.addValueData(outputRow, outputRow.length , status);	
 //Object [] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		
        putRow(data.outputRowMeta, outputRow);     // copy row to possible alternate rowset(s).

		if (checkFeedback(linesRead)) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.
			
		return true;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (LinkGenerationMeta)smi;
	    data = (LinkGenerationData)sdi;

	    return super.init(smi, sdi);
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (LinkGenerationMeta)smi;
	    data = (LinkGenerationData)sdi;

	    super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		logBasic("Starting to run...");
		try
		{
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error : "+e.toString());
            logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		}
		finally
		{
		    dispose(meta, data);
			logBasic("Finished, processing "+linesRead+" rows");
			markStop();
		}
	}
        public void mostrar(String v)
        {
         logBasic (v);
         
        }
        
        public InputStream process () {
            return p.getInputStream();
        }
        
        
        public int silk (String ruta) {
        try {
            String command = "java -DconfigFile="+ruta+" -jar  workbench-1.jar";
            
           // File dir = new File( "/home/cedia/EnlaceSILKPlugin/");
             File dir = new File( "plugins/steps/LinkingSource/lib");
             logBasic(dir.getAbsolutePath());
             p = Runtime.getRuntime().exec(command, null, dir);
            
           
            StreamGobbler outputGobbler = new
                 //       StreamGobbler(p.getInputStream(), "OUTPUT" , this);
                        StreamGobbler(p.getErrorStream(), "OUTPUT" , this);
           
          //   outputGobbler.start();
             
            
              int exitVal = p.waitFor();
              logBasic ("ExitValue: " + exitVal);
              return exitVal;
            // InputStream istream;
            // final   StringWriter writer;
            // final  BufferedReader reader;
            
            /*
            
            Thread msg  = new Thread (){ public void run (){
            try {
            Process p = null;
            try {
            p = Runtime.getRuntime().exec(command, null, dir);
            } catch (IOException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
            }
            InputStream istream = p.getInputStream();
            java.util.Scanner s = new java.util.Scanner(istream).useDelimiter("\\A");
            String val = "";
            if (s.hasNext()) {
            val = s.next();
            logBasic (val);
            }
            else {
            val = "";
            }
            p.waitFor();
            
            } catch (InterruptedException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            }}; */
            /* try {
            /* try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
            String line = reader.readLine();
            while (line != null) {
            logBasic (line);
            line = reader.readLine();
            }
            logBasic( "despues");
            reader.close();
            logBasic("End of logs");
            } catch (IOException e) {
            logBasic("The log reader died unexpectedly.");
            }*/
            // p.waitFor();
            /*    } catch (InterruptedException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            }};*/
            
            
            // writer = new StringWriter();
            //  char[] buffer = new char[1024];
            //  final char[] buffer = new char[1024];
            //  reader = new BufferedReader(new InputStreamReader( istream ));
            //  final  ArrayList  ls = new ArrayList () ;
            
            /* Thread msg  = new Thread (){ public void run (){
            try { int n;
            
            do {
            
            while ((n = reader.read(buffer)) != -1) {
            logBasic( "antes");
            // logBasic( buffer.length+"dimension");
            //  writer.write(buffer, 0, n);
            //  logBasic( "despues");
            Thread.sleep(100);
            //
            logBasic( buffer [0]+"salbuff"+buffer [10]);
            //   writer.flush();
            }
            logBasic( "despues");
            } while (true);
            
            
            } catch (IOException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
            logBasic( "error");
            } catch (InterruptedException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
            logBasic( "error");
            }
            logBasic( writer.toString());
            }};*/
            //       msg.start();
            /*
            logBasic("Entra");
            do {
            
            logBasic ("Bucle");     
            while ((n = reader.read(buffer)) != -1) {
            logBasic( "antes");
            writer.write(buffer, 0, n);
            logBasic( "despues");
            Thread.sleep(1000);
            logBasic( writer.toString());
            }
            
            logBasic( writer.toString());
            
            } while (p.exitValue() != 0); */
            
            //   ls.add("final");
           // logBasic( "Salio del while");
            //       p.waitFor();
            
            /*
            } catch (IOException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            */
        } catch (IOException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
        }
         return 1;
        }
        
         public String xmlgeneration2 ( String url1 , String url2 , String graph1 , String graph2 , String umbral1 ,String umbral2 , String name , String dir){
             String ruta = "";
            try {
           
            //String graph1 = "http://190.15.141.66:8899/uce/";
            //String graph2 = "http://190.15.141.66:8899/puce/";
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation implementation = builder.getDOMImplementation();
            Document document = implementation.createDocument(null, name, null);
            document.setXmlVersion("1.0");
            
             Element raiz = document.getDocumentElement();
             
             Element prefixes = document.createElement("Prefixes"); 
             
             String [][] prefix1 = {{"id","rdf"},{"namespace","http://www.w3.org/1999/02/22-rdf-syntax-ns#"}};
             prefixes.appendChild( getelement (document,"Prefix",prefix1 ,null));
             String [][] prefix2 = {{"id","rdfs"},{"namespace","http://www.w3.org/2000/01/rdf-schema#"}};
             prefixes.appendChild(getelement (document,"Prefix",prefix2 ,null));
             String [][] prefix3 = {{"id","owl"},{"namespace","http://www.w3.org/2002/07/owl#"}};
             prefixes.appendChild(getelement (document,"Prefix",prefix3 ,null));
             
             Element datasources = document.createElement("DataSources"); 
             
             String [][] dataset1 = {{"id","sparql1"},{"type","sparqlEndpoint"}};
             Element datasets1 = getelement (document,"Dataset",dataset1 ,null);
             
             String [][] dataset2 = {{"id","sparql2"},{"type","sparqlEndpoint"}};
             Element datasets2 = getelement (document,"Dataset",dataset2 ,null);
           
             String [][] param1 = {{"name","pageSize"} , {"value","1000"}};
             datasets1.appendChild(getelement (document,"Param",param1 ,null));
             datasets2.appendChild(getelement (document,"Param",param1 ,null));
             
              String [][] param2 = {{"name","pauseTime"} , {"value","0"}};
             datasets1.appendChild(getelement (document,"Param",param2 ,null));
             datasets2.appendChild(getelement (document,"Param",param2 ,null));
             
              String [][] param3 = {{"name","retryCount"} , {"value","3"}};
             datasets1.appendChild(getelement (document,"Param",param3 ,null));
             datasets2.appendChild(getelement (document,"Param",param3 ,null));
             
              String [][] param4 = {{"name","endpointURI"} , {"value",url1}};
              String [][] param4_2 = {{"name","endpointURI"} , {"value",url2}};
             datasets1.appendChild(getelement (document,"Param",param4 ,null));
             datasets2.appendChild(getelement (document,"Param",param4_2 ,null));
             
             String [][] param5 = {{"name","retryPause"} , {"value","1000"}};
             datasets1.appendChild(getelement (document,"Param",param5 ,null));
             datasets2.appendChild(getelement (document,"Param",param5 ,null));
             
             String [][] param6 = {{"name","graph"} , {"value", graph1}};
             String [][] param6_2 = {{"name","graph"} , {"value", graph2}};
             datasets1.appendChild(getelement (document,"Param",param6 ,null));
             datasets2.appendChild(getelement (document,"Param",param6_2 ,null));
             
             String [][] param7 = {{"name","queryParameters"} , {"value",""}};
             datasets1.appendChild(getelement (document,"Param",param7 ,null));
             datasets2.appendChild(getelement (document,"Param",param7 ,null));
             
             String [][] param8 = {{"name","login"} , {"value",""}};
             datasets1.appendChild(getelement (document,"Param",param8 ,null)) ;
             datasets2.appendChild(getelement (document,"Param",param8 ,null)) ;
                     
             String [][] param9 = {{"name","useOrderBy"} , {"value","true"}};
             datasets1.appendChild(getelement (document,"Param",param9 ,null)) ;
             datasets2.appendChild(getelement (document,"Param",param9 ,null)) ;
            
             String [][] param10 = {{"name","entityList"} , {"value",""}};
             datasets1.appendChild(getelement (document,"Param",param10 ,null));
             datasets2.appendChild(getelement (document,"Param",param10 ,null));
             
              String [][] param11 = {{"name","parallel"} , {"value","true"}};
             datasets1.appendChild(getelement (document,"Param",param11 ,null));
             datasets2.appendChild(getelement (document,"Param",param11 ,null));
             
             String [][] param12 = {{"name","password"} , {"value",""}};
             datasets1.appendChild(getelement (document,"Param",param12 ,null));
             datasets2.appendChild(getelement (document,"Param",param12 ,null));
             
             datasources.appendChild(datasets1);
             datasources.appendChild(datasets2);
             
             Element interlinks = document.createElement("Interlinks"); 
             Element interlink = getelement (document,"Interlink",new String [][]{{"id","Linking"}} ,null); 
           //  Element sourcedata = document.createElement("SourceDataset"); 
             String [][] sourcedata1 = {{"dataSource","sparql1"}, {"var","a"} , {"typeUri","http://xmlns.com/foaf/0.1/Person"}};
             Element  sourcedata = getelement (document,"SourceDataset",sourcedata1 ,null);
             Element restricto1 = document.createElement("RestrictTo");
             sourcedata.appendChild(restricto1);
             
             String [][] targetDataset2 = {{"dataSource","sparql2"}, {"var","b"} , {"typeUri","http://xmlns.com/foaf/0.1/Person"}};
             Element  TargetDataset = getelement (document,"TargetDataset",targetDataset2 ,null);
             Element restricto2 = document.createElement("RestrictTo");
             TargetDataset.appendChild(restricto2);
             
             interlink.appendChild(sourcedata);
             interlink.appendChild(TargetDataset);
             
             Element linkagerule = getelement (document,"LinkageRule",new String [][]{{"linkType","owl:sameAs"}} ,null);
             String [][] paramstransf = { {"id","jaroWinkler1"} , { "required","false"}, { "weight","1" }, { "metric","jaroWinkler"}, { "threshold","0.0"}, { "indexing","true"}};
             Element compare = getelement (document,"Compare", paramstransf ,null);
             
             Element TransformInput1 = getelement (document,"TransformInput",new String [][]{{"id","upperCase1"},{"function","upperCase"}} ,null);
             Element Input1 = getelement (document,"Input",new String [][]{{"id","sourcePath1"},{"path","/<http://xmlns.com/foaf/0.1/name>"}} ,null);
             TransformInput1.appendChild(Input1);
             compare.appendChild(TransformInput1);
             
             Element TransformInput2 = getelement (document,"TransformInput",new String [][]{{"id","upperCase2"},{"function","upperCase"}} ,null);
             Element Input2 = getelement (document,"Input",new String [][]{{"id","targetPath1"},{"path","/<http://xmlns.com/foaf/0.1/name>"}} ,null);
             TransformInput2.appendChild(Input2);
             compare.appendChild(TransformInput2);
             linkagerule.appendChild(compare);
             interlink.appendChild(linkagerule);
             Element outputs =  document.createElement("Outputs");
            outputs.appendChild( getelement (document,"Output",new String [][]{{"id","Salida"}} ,null));
             interlink.appendChild(outputs);
             interlinks.appendChild(interlink);
             Element transforms =  document.createElement("Transforms");
             
             Element outputs2 =  document.createElement("Outputs");
             Element datasetout =  getelement (document,"Dataset",new String [][]{{"id","Salida"},{"type","file"}} ,null);
             datasetout.appendChild(getelement (document,"Param",new String [][]{{"name","file"},{"value","SalidaLinkg"}} ,null));
             datasetout.appendChild(getelement (document,"Param",new String [][]{{"name","format"},{"value","N-Triples"}} ,null));
             datasetout.appendChild(getelement (document,"Param",new String [][]{{"name","graph"},{"value",""}} ,null));
             outputs2.appendChild(datasetout);
         
                //append itemNode to raiz*/
              raiz.appendChild(prefixes); //pegamos el elemento a la raiz "Documento"
              raiz.appendChild(datasources);
              raiz.appendChild(interlinks);
              raiz.appendChild(transforms);
              raiz.appendChild(outputs2);
             Source source = new DOMSource(document);
            //Indicamos donde lo queremos almacenar
             File  filexml = new File(dir+"/"+name+".xml");
            Result result = new StreamResult(filexml); //nombre del archivo
            logBasic (filexml.getAbsolutePath());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            StreamResult console = new StreamResult(System.out);
            logBasic (console+"");
            transformer.transform(source, result);
            ruta = dir+"/"+name+".xml";
            return ruta;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
        }
          return ruta;
        }
        
        public String xmlgeneration ( String url1 , String url2 , String graph1 , String graph2 , String umbral1 ,String umbral2 , String name , String dir){
             String ruta = "";
            try {
           
            //String graph1 = "http://190.15.141.66:8899/uce/";
            //String graph2 = "http://190.15.141.66:8899/puce/";
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation implementation = builder.getDOMImplementation();
            Document document = implementation.createDocument(null, name, null);
            document.setXmlVersion("1.0");
            
             Element raiz = document.getDocumentElement();
             
             Element prefixes = document.createElement("Prefixes"); 
             
             String [][] prefix1 = {{"id","rdf"},{"namespace","http://www.w3.org/1999/02/22-rdf-syntax-ns#"}};
             prefixes.appendChild( getelement (document,"Prefix",prefix1 ,null));
             String [][] prefix2 = {{"id","rdfs"},{"namespace","http://www.w3.org/2000/01/rdf-schema#"}};
             prefixes.appendChild(getelement (document,"Prefix",prefix2 ,null));
             String [][] prefix3 = {{"id","owl"},{"namespace","http://www.w3.org/2002/07/owl#"}};
             prefixes.appendChild(getelement (document,"Prefix",prefix3 ,null));
             
             Element datasources = document.createElement("DataSources"); 
             
             String [][] dataset1 = {{"id","sparql1"},{"type","sparqlEndpoint"}};
             Element datasets1 = getelement (document,"Dataset",dataset1 ,null);
             
             String [][] dataset2 = {{"id","sparql2"},{"type","sparqlEndpoint"}};
             Element datasets2 = getelement (document,"Dataset",dataset2 ,null);
           
             String [][] param1 = {{"name","pageSize"} , {"value","1000"}};
             datasets1.appendChild(getelement (document,"Param",param1 ,null));
             datasets2.appendChild(getelement (document,"Param",param1 ,null));
             
              String [][] param2 = {{"name","pauseTime"} , {"value","0"}};
             datasets1.appendChild(getelement (document,"Param",param2 ,null));
             datasets2.appendChild(getelement (document,"Param",param2 ,null));
             
              String [][] param3 = {{"name","retryCount"} , {"value","3"}};
             datasets1.appendChild(getelement (document,"Param",param3 ,null));
             datasets2.appendChild(getelement (document,"Param",param3 ,null));
             
              String [][] param4 = {{"name","endpointURI"} , {"value",url1}};
              String [][] param4_2 = {{"name","endpointURI"} , {"value",url2}};
             datasets1.appendChild(getelement (document,"Param",param4 ,null));
             datasets2.appendChild(getelement (document,"Param",param4_2 ,null));
             
             String [][] param5 = {{"name","retryPause"} , {"value","1000"}};
             datasets1.appendChild(getelement (document,"Param",param5 ,null));
             datasets2.appendChild(getelement (document,"Param",param5 ,null));
             
             String [][] param6 = {{"name","graph"} , {"value", graph1}};
             String [][] param6_2 = {{"name","graph"} , {"value", graph2}};
             datasets1.appendChild(getelement (document,"Param",param6 ,null));
             datasets2.appendChild(getelement (document,"Param",param6_2 ,null));
             
             String [][] param7 = {{"name","queryParameters"} , {"value",""}};
             datasets1.appendChild(getelement (document,"Param",param7 ,null));
             datasets2.appendChild(getelement (document,"Param",param7 ,null));
             
             String [][] param8 = {{"name","login"} , {"value",""}};
             datasets1.appendChild(getelement (document,"Param",param8 ,null)) ;
             datasets2.appendChild(getelement (document,"Param",param8 ,null)) ;
                     
             String [][] param9 = {{"name","useOrderBy"} , {"value","true"}};
             datasets1.appendChild(getelement (document,"Param",param9 ,null)) ;
             datasets2.appendChild(getelement (document,"Param",param9 ,null)) ;
            
             String [][] param10 = {{"name","entityList"} , {"value",""}};
             datasets1.appendChild(getelement (document,"Param",param10 ,null));
             datasets2.appendChild(getelement (document,"Param",param10 ,null));
             
              String [][] param11 = {{"name","parallel"} , {"value","true"}};
             datasets1.appendChild(getelement (document,"Param",param11 ,null));
             datasets2.appendChild(getelement (document,"Param",param11 ,null));
             
             String [][] param12 = {{"name","password"} , {"value",""}};
             datasets1.appendChild(getelement (document,"Param",param12 ,null));
             datasets2.appendChild(getelement (document,"Param",param12 ,null));
             
             datasources.appendChild(datasets1);
             datasources.appendChild(datasets2);
             
             Element interlinks = document.createElement("Interlinks"); 
             Element interlink = getelement (document,"Interlink",new String [][]{{"id","Linking"}} ,null); 
           //  Element sourcedata = document.createElement("SourceDataset"); 
             String [][] sourcedata1 = {{"dataSource","sparql1"}, {"var","a"} , {"typeUri",""}};
             Element  sourcedata = getelement (document,"SourceDataset",sourcedata1 ,null);
             
             Element restricto1 = document.createElement("RestrictTo");
             Text rest =  document.createTextNode ("{ ?a a <http://xmlns.com/foaf/0.1/Person> } .");
             restricto1.appendChild(rest);
             sourcedata.appendChild(restricto1);
             
             String [][] targetDataset2 = {{"dataSource","sparql2"}, {"var","b"} , {"typeUri","http://xmlns.com/foaf/0.1/Person"}};
             Element  TargetDataset = getelement (document,"TargetDataset",targetDataset2 ,null);
             Element restricto2 = document.createElement("RestrictTo");
              Text rest2 =  document.createTextNode ("{ ?a a <http://xmlns.com/foaf/0.1/Person> } .");
             restricto2.appendChild(rest2);
             TargetDataset.appendChild(restricto2);
             
             interlink.appendChild(sourcedata);
             interlink.appendChild(TargetDataset);
             
             Element linkagerule = getelement (document,"LinkageRule",new String [][]{{"linkType","owl:sameAs"}} ,null);
             String [][] paramstransf = { {"id","jaroWinkler1"} , { "required","true"}, { "weight","1" }, { "metric","jaccardwv"}, { "threshold",umbral1}, { "indexing","true"}};
             Element compare = getelement (document,"Compare", paramstransf ,null);
             
             Element TransformInput1 = getelement (document,"TransformInput",new String [][]{{"id","removespecial"},{"function","removeSpecialChars"}} ,null);
             Element TransformInput3 = getelement (document,"TransformInput",new String [][]{{"id","tokenize"},{"function","tokenize"}} ,null);
             Element TransformInput4 = getelement (document,"TransformInput",new String [][]{{"id","normalizeChars"},{"function","normalizeChars"}} ,null);
             Element TransformInput5 = getelement (document,"TransformInput",new String [][]{{"id","lowerCase"},{"function","lowerCase"}} ,null);
             
             Element Input1 = getelement (document,"Input",new String [][]{{"id","sourcePath1"},{"path","/<http://xmlns.com/foaf/0.1/name>"}} ,null);
             Element PARAM1 = getelement (document,"Param",new String [][]{{"name","regex"},{"value","\\s+"}} ,null);

             
             TransformInput5.appendChild(Input1);
             TransformInput4.appendChild(TransformInput5);
             TransformInput3.appendChild(TransformInput4);
             TransformInput3.appendChild(PARAM1);
             TransformInput1.appendChild(TransformInput3);
            
             compare.appendChild(TransformInput1);
             
             
             Element TransformInput2 = getelement (document,"TransformInput",new String [][]{{"id","removespecial2"},{"function","removeSpecialChars"}} ,null);
             Element TransformInput6 = getelement (document,"TransformInput",new String [][]{{"id","tokenize2"},{"function","tokenize"}} ,null);
             Element TransformInput7 = getelement (document,"TransformInput",new String [][]{{"id","normalizeChars2"},{"function","normalizeChars"}} ,null);
             Element TransformInput8 = getelement (document,"TransformInput",new String [][]{{"id","lowerCase2"},{"function","lowerCase"}} ,null);
                       
             
             Element Input2 = getelement (document,"Input",new String [][]{{"id","targetPath1"},{"path","/<http://xmlns.com/foaf/0.1/name>"}} ,null);
             Element PARAM2 = getelement (document,"Param",new String [][]{{"name","regex"},{"value","\\s+"}} ,null);

             TransformInput8.appendChild(Input2);
             TransformInput7.appendChild(TransformInput8);
             
             TransformInput6.appendChild(TransformInput7);
             TransformInput6.appendChild(PARAM2);
             
             TransformInput2.appendChild(TransformInput6);
             
             Element PARAMdist = getelement (document,"Param",new String [][]{{"name","ContextDistance"},{"value",umbral2}} ,null);
             
             compare.appendChild(TransformInput2);
             compare.appendChild(PARAMdist);
             
             Element filter = getelement (document,"Filter",new String [][]{{"limit","10"}} ,null);
             
           //  TransformInput2.appendChild(Input2);
            // compare.appendChild(TransformInput2);
             linkagerule.appendChild(compare);
             linkagerule.appendChild(filter);
             interlink.appendChild(linkagerule);
             Element outputs =  document.createElement("Outputs");
            outputs.appendChild( getelement (document,"Output",new String [][]{{"id","Salida"}} ,null));
             interlink.appendChild(outputs);
             interlinks.appendChild(interlink);
             Element transforms =  document.createElement("Transforms");
             
             Element outputs2 =  document.createElement("Outputs");
             Element datasetout =  getelement (document,"Dataset",new String [][]{{"id","Salida"},{"type","file"}} ,null);
             datasetout.appendChild(getelement (document,"Param",new String [][]{{"name","file"},{"value","SalidaLinkg"}} ,null));
             datasetout.appendChild(getelement (document,"Param",new String [][]{{"name","format"},{"value","N-Triples"}} ,null));
             datasetout.appendChild(getelement (document,"Param",new String [][]{{"name","graph"},{"value",""}} ,null));
             outputs2.appendChild(datasetout);
         
                //append itemNode to raiz*/
              raiz.appendChild(prefixes); //pegamos el elemento a la raiz "Documento"
              raiz.appendChild(datasources);
              raiz.appendChild(interlinks);
              raiz.appendChild(transforms);
              raiz.appendChild(outputs2);
             Source source = new DOMSource(document);
            //Indicamos donde lo queremos almacenar
             File  filexml = new File(dir+"/"+name+".xml");
            Result result = new StreamResult(filexml); //nombre del archivo
            logBasic (filexml.getAbsolutePath());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            StreamResult console = new StreamResult(System.out);
            logBasic (console+"");
            transformer.transform(source, result);
            ruta = dir+"/"+name+".xml";
            return ruta;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(LinkGeneration.class.getName()).log(Level.SEVERE, null, ex);
        }
          return ruta;
        }
        
        public Element getelement (Document document,String elementname , String [][] atributes , String [][] properties ){ 
           Element itemNode = document.createElement(elementname);
            if (atributes != null ){
             for (int i= 0 ; i  <  atributes.length ;i++){
              itemNode.setAttribute(atributes [i][0], atributes[i][1]);
             }
                
            }
            if (properties!= null){
            }
         return itemNode;
        }
       
        
}

 class StreamGobbler extends Thread 
{ 
    InputStream is;
    String type;
    LinkGeneration l;
    StreamGobbler(InputStream is, String type, Object b)
    {
        this.is = is;
        this.type = type;
        l = (LinkGeneration) b;
    }
    
    public void setinput ( InputStream is ){
        this.is = is;
    }
    
    @Override
    public void run()
    {       int val = 0;
            while (val < 100){
        try
        {
            try
            {
                is = l.process();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null; 
                while ( (line = br.readLine()) != null)
                    l.mostrar(line);
                //System.out.print ( "mostrar");
                // System.out.print  ( line+"");
                 l.mostrar ("ejecuta");
                
            } catch (IOException ioe)
            {
                ioe.printStackTrace();
                l.mostrar (ioe+"");
            }
            val++;
            
            Thread.sleep(1000);
            
        } catch (InterruptedException ex)
              {
                Logger.getLogger(StreamGobbler.class.getName()).log(Level.SEVERE, null, ex);  
                 l.mostrar (ex+"");
              }
        }
    }
}