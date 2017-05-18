package com.ucuenca.pentaho.plugin.step.marc21loader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.xbaseinput.XBase;
import org.w3c.dom.Node;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepMetaInterface.
 * Classes implementing this interface need to:
 * 
 * - keep track of the step settings
 * - serialize step settings both to xml and a repository
 * - provide new instances of objects implementing StepDialogInterface, StepInterface and StepDataInterface
 * - report on how the step modifies the meta-data of the row-stream (row structure and field types)
 * - perform a sanity-check on the settings provided by the user 
 * 
 */
public class MARC21LoaderMeta extends BaseStepMeta implements StepMetaInterface {
	
	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = MARC21LoaderMeta.class; // for i18n purposes
	
    /** MARC file URL */
	private String  marcFilename;
	
	/** Read multiple mrc files */
	private Boolean batchMode;
	
	/** MARC Filelist for batchmode */
	private List<File> marcfiles;
	
	/** Generate MARCXML from input */
	private Boolean genMARCXML;
	
	/** MARCXML Output Filename **/ 
	private String marcxmlFilename = "marc21loader.xml";
	
	/** MARC fields to be extracted */
	private String  marcFields;
	
	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public MARC21LoaderMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step.
	 * A standard implementation passing the arguments to the constructor of the step dialog is recommended.
	 * 
	 * @param shell		an SWT Shell
	 * @param meta 		description of the step 
	 * @param transMeta	description of the the transformation 
	 * @param name		the name of the step
	 * @return 			new instance of a dialog for this step 
	 */
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new MARC21LoaderDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. 
	 * A standard implementation passing the arguments to the constructor of the step class is recommended.
	 * 
	 * @param stepMeta				description of the step
	 * @param stepDataInterface		instance of a step data class
	 * @param cnr					copy number
	 * @param transMeta				description of the transformation
	 * @param disp					runtime implementation of the transformation
	 * @return						the new instance of a step implementation 
	 */
	public StepInterface getStep(StepMeta si, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans disp)
	{
		return new MARC21Loader(si, stepDataInterface, cnr, tr, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData()
	{
		return new MARC21LoaderData();
	}
	
	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault()
	{
	    marcFilename = "";
	    marcFields = "";
	}

	/**
     * @return Returns the marcFilename.
     */
    public String getmarcFilename()
    {
        return marcFilename;
    }
    
    /**
     * @param marcFilename The marcFilename to set.
     */
    public void setmarcFilename(String marcFilename)
    {
        this.marcFilename = marcFilename;
    }
    
    /**
     * @return Returns the marcFields.
     */
    public String getmarcFields()
    {
        return marcFields;
    }
    
    /**
     * @param marcFields The marcFields to set.
     */
    public void setmarcFields(String marcFields)
    {
        this.marcFields = marcFields;
    }
    
    
    public Boolean getBatchMode() {
		return batchMode;
	}

	public void setBatchMode(Boolean batchMode) {
		this.batchMode = batchMode;
	}

	public List<File> getMarcfiles() {
		return marcfiles;
	}

	public void addMarcfile(File marcfile) {
		this.marcfiles.add(marcfile);
	}

	public Boolean getGenMARCXML() {
		return genMARCXML;
	}

	public void setGenMARCXML(Boolean genMARCXML) {
		this.genMARCXML = genMARCXML;
	}

	public String getMarcxmlFilename() {
		return marcxmlFilename;
	}

	public void setMarcxmlFilename(String marcxmlFilename) {
		this.marcxmlFilename = marcxmlFilename;
	}

	/**
	 * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this
	 * step meta object. Be sure to create proper deep copies if the step configuration is stored in
	 * modifiable objects.
	 * 
	 * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating
	 * a deep copy.
	 * 
	 * @return a deep copy of this
	 */
    public Object clone()
	{
		MARC21LoaderMeta retval = (MARC21LoaderMeta)super.clone();
				
		return retval;
	}
    
        
    /**
	 * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
	 * return value is an XML fragment consisting of one or more XML tags.  
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
	 * 
	 * @return a string containing the XML serialization of this step
	 */
    public String getXML()
	{
		String retval="";
		
		retval+="    "+XMLHandler.addTagValue("marcFilename", marcFilename);
		retval+="    "+XMLHandler.addTagValue("marcFields",   marcFields);
		retval+="    "+XMLHandler.addTagValue("batchMode",   batchMode);
		retval+="    "+XMLHandler.addTagValue("genMARCXML",   genMARCXML);
		
		return retval;
	}

    /**
	 * This method is called by PDI when a step needs to load its configuration from XML.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
	 * XML node passed in.
	 * 
	 * @param stepnode	the XML node containing the configuration
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}	

    /**
     * Lectura de metadatos para construccion de XML de Step
     * @param stepnode
     * @throws KettleXMLException
     */
	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{			
			marcFilename = XMLHandler.getTagValue(stepnode, "marcFilename");
			marcFields   = XMLHandler.getTagValue(stepnode, "marcFields");
			batchMode   = XMLHandler.getTagValue(stepnode, "batchMode").equals("Y");
			genMARCXML   = XMLHandler.getTagValue(stepnode, "genMARCXML").equals("Y");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to a repository.
	 * The repository implementation provides the necessary methods to save the step attributes.
	 *
	 * @param rep					the repository to save to
	 * @param id_transformation		the id to use for the transformation when saving
	 * @param id_step				the id to use for the step  when saving
	 */
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "marcFilename",     marcFilename);
			rep.saveStepAttribute(id_transformation, id_step, "marcFields",       marcFields);
			rep.saveStepAttribute(id_transformation, id_step, "batchMode",       batchMode);
			rep.saveStepAttribute(id_transformation, id_step, "genMARCXML",       genMARCXML);
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}
	
	/**
	 * This method is called by PDI when a step needs to read its configuration from a repository.
	 * The repository implementation provides the necessary methods to read the step attributes.
	 * 
	 * @param rep		the repository to read from
	 * @param id_step	the id of the step being read
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
		try
		{
			marcFilename =      rep.getStepAttributeString (id_step, "marcFilename"    );
			marcFields   =      rep.getStepAttributeString (id_step, "marcFields"      );
			batchMode   =      rep.getStepAttributeBoolean (id_step, "batchMode"      );
			genMARCXML   =      rep.getStepAttributeBoolean (id_step, "genMARCXML"      );
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
		
	}
	
	/**
	 * This method is called to determine the changes the step is making to the row-stream.
	 * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
	 * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
	 * row-stream.
	 * 
	 * @param r			the row structure coming in to the step
	 * @param origin	the name of the step making the changes
	 * @param info		row structures of any info steps coming in
	 * @param nextStep	the description of a step this step is passing rows to
	 * @param space		the variable space for resolving variables
	 */
	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {

		// Atributo MFN...
		ValueMetaInterface mfn = new ValueMeta("MFN", ValueMetaInterface.TYPE_STRING);
		mfn.setLength(50);
		mfn.setOrigin(name);
		row.addValueMeta(mfn);
                
                ValueMetaInterface secuencia = new ValueMeta("Secuencia", ValueMetaInterface.TYPE_STRING);
		secuencia.setLength(2);
		secuencia.setOrigin(name);
		row.addValueMeta(secuencia);
		
		// Atributo Campo
		ValueMetaInterface campo = new ValueMeta("Campo", ValueMetaInterface.TYPE_INTEGER);
		campo.setOrigin(name);
		row.addValueMeta(campo);
		
		// Atributo Indicadores
		ValueMetaInterface indicadores = new ValueMeta("Indicadores", ValueMetaInterface.TYPE_STRING);
		indicadores.setLength(2);
		indicadores.setOrigin(name);
		row.addValueMeta(indicadores);
		
		// Atributo Subcampo
		ValueMetaInterface subcampo = new ValueMeta("Subcampo", ValueMetaInterface.TYPE_STRING);
		subcampo.setLength(50);
		subcampo.setOrigin(name);
		row.addValueMeta(subcampo);
		
		//Atributo dato
		ValueMetaInterface dato = new ValueMeta("dato", ValueMetaInterface.TYPE_STRING);
		dato.setLength(5000);
		dato.setOrigin(name);
		row.addValueMeta( dato ); 
		
	/*	// The shape nr
		ValueMetaInterface shnr = new ValueMeta("shapenr", ValueMetaInterface.TYPE_INTEGER);
		shnr.setOrigin(name);
		row.addValueMeta( shnr ); 

		// The part nr
		ValueMetaInterface pnr = new ValueMeta("partnr", ValueMetaInterface.TYPE_INTEGER);
		pnr.setOrigin(name);
		row.addValueMeta( pnr ); 

		// The part nr
		ValueMetaInterface nrp = new ValueMeta("nrparts", ValueMetaInterface.TYPE_INTEGER);
		nrp.setOrigin(name);
		row.addValueMeta( nrp ); 

		// The point nr
		ValueMetaInterface ptnr = new ValueMeta("pointnr", ValueMetaInterface.TYPE_INTEGER);
		ptnr.setOrigin(name);
		row.addValueMeta( ptnr ); 

		// The nr of points
		ValueMetaInterface nrpt = new ValueMeta("nrpointS", ValueMetaInterface.TYPE_INTEGER);
		nrpt.setOrigin(name);
		row.addValueMeta( nrpt ); 

		// The X coordinate
		ValueMetaInterface x = new ValueMeta("x", ValueMetaInterface.TYPE_NUMBER);
		x.setOrigin(name);
		row.addValueMeta( x );

		// The Y coordinate
		ValueMetaInterface y = new ValueMeta("y", ValueMetaInterface.TYPE_NUMBER);
		y.setOrigin(name);
		row.addValueMeta( y );

		// The measure
		ValueMetaInterface m = new ValueMeta("measure", ValueMetaInterface.TYPE_NUMBER);
		m.setOrigin(name);
		row.addValueMeta( m );
		*/
		
		/*if (getmarcFields()!=null)
		{
            XBase xbase = new XBase(log, getmarcFields());
            try
			{
			    xbase.open();
			    RowMetaInterface fields = xbase.getFields();
			    for (int i=0;i<fields.size();i++)
			    {
			        fields.getValueMeta(i).setOrigin(name);
			        row.addValueMeta( fields.getValueMeta(i) );
			    }
			}
			catch(Throwable e)
			{
				throw new KettleStepException("Unable to read from DBF file", e);
			}
            finally
            {
                xbase.close();
            }
		}
		else
		{
			throw new KettleStepException("Unable to read from DBF file: no filename specfied");
		}*/
	}
	
	/**
	 * This method is called when the user selects the "Verify Transformation" option in Spoon. 
	 * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
	 * The method should perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include:
	 * - verify that all mandatory configuration is given
	 * - verify that the step receives any input, unless it's a row generating step
	 * - verify that the step does not receive any input if it does not take them into account
	 * - verify that the step finds fields it relies on in the row-stream
	 * 
	 *   @param remarks		the list of remarks to append to
	 *   @param transmeta	the description of the transformation
	 *   @param stepMeta	the description of the step
	 *   @param prev		the structure of the incoming row-stream
	 *   @param input		names of steps sending input to the step
	 *   @param output		names of steps this step is sending output to
	 *   @param info		fields coming in from info steps 
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) 
	{
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "This step is not expecting nor reading any input", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Not receiving any input from other steps.", stepinfo);
			remarks.add(cr);
		}
		
		if (marcFilename==null || marcFields==null || marcFilename.length()==0 || marcFields.length()==0 )
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No files founded to read.", stepinfo);
			remarks.add(cr);
		}
		else if(this.marcFilesValidator()) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "MARC File(s) founded.", stepinfo);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "MARC File(s) (.mrc extension) was not found.", stepinfo);
			remarks.add(cr);
		}
	}
	
	/**
	 * Validation of existing .mrc files on directory path
	 * @return
	 */
	public boolean marcFilesValidator(){
		File url = new File(this.marcFilename);
		this.marcfiles = new ArrayList<File>();
		boolean hasMarcFiles = false;
		if(url.isDirectory()) {
			File [] files = url.listFiles();
			Arrays.sort(files);
			for(File file: files) {
				if(file.getName().endsWith(".mrc")) {
					this.addMarcfile(file);
					hasMarcFiles = this.batchMode = true;
				}
			}
		} else {
			this.addMarcfile(url);
			 hasMarcFiles = true;
		}
		return hasMarcFiles;

	}

}
