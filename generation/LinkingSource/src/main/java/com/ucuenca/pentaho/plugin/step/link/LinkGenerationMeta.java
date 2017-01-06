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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
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
import org.w3c.dom.Node;

/*
 * Created on 02-jun-2003
 *
 */

public class LinkGenerationMeta extends BaseStepMeta implements StepMetaInterface
{

    public String getSparql1() {
        return sparql1;
    }

    public void setSparql1(String sparql1) {
        this.sparql1 = sparql1;
    }

    public String getSparql2() {
        return sparql2;
    }

    public void setSparql2(String sparql2) {
        this.sparql2 = sparql2;
    }

    public String getFileinput() {
        return fileinput;
    }

    public void setFileinput(String fileinput) {
        this.fileinput = fileinput;
    }
	
    private String sparql1;
    
    private String sparql2;
    
    private ValueMetaAndData value;
    
    private String fileinput;
    
    private String graph1, graph2;
    private String diroutput ;

    public String getGraph1() {
        return graph1;
    }

    public void setGraph1(String graph1) {
        this.graph1 = graph1;
    }

    public String getGraph2() {
        return graph2;
    }

    public void setGraph2(String graph2) {
        this.graph2 = graph2;
    }

    public String getDiroutput() {
        return diroutput;
    }

    public void setDiroutput(String diroutput) {
        this.diroutput = diroutput;
    }

    public String getUmbral1() {
        return umbral1;
    }

    public void setUmbral1(String umbral1) {
        this.umbral1 = umbral1;
    }

    public String getUmbral2() {
        return umbral2;
    }

    public void setUmbral2(String umbral2) {
        this.umbral2 = umbral2;
    }
    
    private   String  umbral1;
    private   String  umbral2;
	
	public LinkGenerationMeta()
	{
		super(); // allocate BaseStepInfo
	}

	/**
	 * @return Returns the value.
	 */
         public ValueMetaAndData getValue()
	{
		return value;
	}
	
	/**
	 * @param value The value to set.
	 */
	public void setValue(ValueMetaAndData value)
	{
		this.value = value;
	}
	
	/*public String getXML() throws KettleException
	{
		String retval = "";
		
		retval+="    <values>"+Const.CR;
		if (value!=null)
		{
			retval+=value.getXML();
		}
		retval+="      </values>"+Const.CR;

		return retval;
	}*/
        
        public String getXML() throws KettleValueException {

		// only one field to serialize
		StringBuffer retval = new StringBuffer(1000);

		retval.append("    ").append(
				XMLHandler.addTagValue("sparqluri1", sparql1));
                retval.append("    ").append(
				XMLHandler.addTagValue("sparqluri2", sparql2));
		retval.append("    ").append(
				XMLHandler.addTagValue("fileinput", fileinput));
                retval.append("    ").append(
				XMLHandler.addTagValue("umbral1", umbral1));
                retval.append("    ").append(
				XMLHandler.addTagValue("umbral2", umbral2));
                 retval.append("    ").append(
				XMLHandler.addTagValue("graph1", graph1));
                  retval.append("    ").append(
				XMLHandler.addTagValue("graph2", graph2));
                   retval.append("    ").append(
				XMLHandler.addTagValue("diroutput", diroutput));

		return retval.toString();
	}

	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
	{       
                 r.clear();
		  RowMeta rowMeta = new RowMeta();
            
           if (getDiroutput()  !=null)
		{  
			ValueMetaInterface v = new ValueMeta("File",ValueMetaInterface.TYPE_STRING);
			v.setOrigin(origin);
			rowMeta.addValueMeta(v);
			//r.addValueMeta( v );
                        
                      
		
		        ValueMetaInterface status = new ValueMeta("Status",ValueMetaInterface.TYPE_STRING);
			status.setOrigin(origin);
			rowMeta.addValueMeta(status);
			
                        r.addRowMeta(rowMeta);
		
		
	
		
		}
            
            
		/*if (value!=null)
		{
			ValueMetaInterface v = value.getValueMeta();
			v.setOrigin(origin);
			
			r.addValueMeta( v );
		}*/
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}

/*	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleXMLException
	{
		try
		{
			value = new ValueMetaAndData();
			
			Node valnode  = XMLHandler.getSubNode(stepnode, "values", "value");
			if (valnode!=null)
			{
				System.out.println("yreading value in "+valnode);
				value.loadXML(valnode);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read step info from XML node", e);
		}
	}
*/
        
        public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}
        
        private void readData(Node stepnode) throws KettleXMLException {
                sparql1= XMLHandler.getTagValue(stepnode, "sparqluri1");
    
                sparql2= XMLHandler.getTagValue(stepnode, "sparqluri2");
                
                fileinput = XMLHandler.getTagValue(stepnode, "fileinput");
                
                umbral1 = XMLHandler.getTagValue(stepnode, "umbral1");
                umbral2 = XMLHandler.getTagValue(stepnode, "umbral2");
                
                graph1 = XMLHandler.getTagValue(stepnode, "graph1");
                graph2 = XMLHandler.getTagValue(stepnode, "graph2");
                
                diroutput =  XMLHandler.getTagValue(stepnode, "diroutput");
		
	}
        
	public void setDefault()
	{    fileinput = "";    
            sparql1 = "";
                umbral1 =  "0.3";
                umbral2 =  "0.6";
                graph1 = "";
                graph2= "";
		//value = new ValueMetaAndData( new ValueMeta("valuename", ValueMetaInterface.TYPE_NUMBER), new Double(123.456) );
		//value.getValueMeta().setLength(12);
              //  value.getValueMeta().setPrecision(4);
                
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String,Counter> counters) throws KettleException
	{    
		try
		{   //    sparql1 = XMLHandler.getTagValue(id_step, "nameStep");
			String name      =      rep.getStepAttributeString (id_step, 0, "value_name");
			String typedesc  =      rep.getStepAttributeString (id_step, 0, "value_type");
			String text      =      rep.getStepAttributeString (id_step, 0, "value_text");
			boolean isnull   =      rep.getStepAttributeBoolean(id_step, 0, "value_null");
			int length       = (int)rep.getStepAttributeInteger(id_step, 0, "value_length");
			int precision    = (int)rep.getStepAttributeInteger(id_step, 0, "value_precision");
			
			int type = ValueMeta.getType(typedesc);
			value = new ValueMetaAndData(new ValueMeta(name, type), null);
			value.getValueMeta().setLength(length);
                        value.getValueMeta().setPrecision(precision);
			
			if (isnull) 
			{
				value.setValueData(null);
			}
			else
			{
                ValueMetaInterface stringMeta = new ValueMeta(name, ValueMetaInterface.TYPE_STRING);
				if (type!=ValueMetaInterface.TYPE_STRING) text=ValueDataUtil.trim(text);
				value.setValueData( value.getValueMeta().convertData(stringMeta, text));
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("error reading step with id_step="+id_step+" from the repository", dbe);
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step with id_step="+id_step+" from the repository", e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "value_name", value.getValueMeta().getName());
			rep.saveStepAttribute(id_transformation, id_step, 0, "value_type",      value.getValueMeta().getTypeDesc());
			rep.saveStepAttribute(id_transformation, id_step, 0, "value_text",      value.getValueMeta().getString(value.getValueData()));
			rep.saveStepAttribute(id_transformation, id_step, 0, "value_null",      value.getValueMeta().isNull(value.getValueData()));
			rep.saveStepAttribute(id_transformation, id_step, 0, "value_length",    value.getValueMeta().getLength());
			rep.saveStepAttribute(id_transformation, id_step, 0, "value_precision", value.getValueMeta().getPrecision());
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save step information to the repository, id_step="+id_step, dbe);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepMeta);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name)
	{
		return new LinkGenerationDialog(shell, meta, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
	{
		return new LinkGeneration(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	public StepDataInterface getStepData()
	{
		return new LinkGenerationData();
	}
        
        
       
}