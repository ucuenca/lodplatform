package com.ucuenca.pentaho.plugin.step.marc21loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import com.ucuenca.pentaho.plugin.step.marc21loader.util.MARC21;
import java.util.Random;
import java.util.UUID;
import org.marc4j.marc.VariableField;

/**
 * This class is part of the demo step plug-in implementation. It demonstrates
 * the basics of developing a plug-in step for PDI.
 *
 * The demo step adds a new string field to the row stream and sets its value to
 * "Hello World!". The user may select the name of the new field.
 *
 * This class is the implementation of StepInterface. Classes implementing this
 * interface need to:
 *
 * - initialize the step - execute the row processing logic - dispose of the
 * step
 *
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data
 * interface instead.
 *
 */
public class MARC21Loader extends BaseStep implements StepInterface {

    private MARC21LoaderMeta meta;
    private MARC21LoaderData data;
    private String mfn;
    DOMResult source;
    Result result;

    //private MarcStreamWriter writer;
    /**
     * The constructor should simply pass on its arguments to the parent class.
     *
     * @param s step description
     * @param stepDataInterface	step data class
     * @param c	step copy
     * @param t	transformation description
     * @param dis	transformation executing
     */
    public MARC21Loader(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
    }

    /**
     * This method is called by PDI during transformation startup.
     *
     * It should initialize required for step execution.
     *
     * The meta and data implementations passed in can safely be cast to the
     * step's respective implementations.
     *
     * It is mandatory that super.init() is called to ensure correct behavior.
     *
     * Typical tasks executed here are establishing the connection to a
     * database, as wall as obtaining resources, like file handles.
     *
     * @param smi step meta interface implementation, containing the step
     * settings
     * @param sdi	step data interface implementation, used to store runtime
     * information
     *
     * @return true if initialization completed successfully, false if there was
     * an error preventing the step from working.
     *
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (MARC21LoaderMeta) smi;
        data = (MARC21LoaderData) sdi;

        if (super.init(meta, data)) {
            if (meta.getmarcFilename() == null
                    || meta.getmarcFields() == null
                    || meta.getmarcFilename().length() == 0
                    || meta.getmarcFields().length() == 0) {
                logError("We need both a path file and a fields to search.");
                return false;
            }
            //marcfiles validation and listing
            meta.marcFilesValidator();

            //Lectura del archivo MARC
            FileInputStream in = null;
            try {
                for (File file : meta.getMarcfiles()) {
                    logBasic("File to be processed: " + file.getAbsoluteFile());
                    in = new FileInputStream(file);
                    data.marcfiles.add(new MarcStreamReader(in));
                }
                if (meta.getGenMARCXML()) {
                    //String stylesheetUrl = "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl";
                    //Source stylesheet = new StreamSource(stylesheetUrl);
                    logBasic("Step will Generate MARCXML from input");
                    String out = meta.getmarcFilename().substring(0,
                            meta.getmarcFilename().lastIndexOf(System.getProperty("file.separator")) + 1);

                    data.marcXmlOutfile = new MarcXmlWriter(new FileOutputStream(out + meta.getMarcxmlFilename()), true);
                    result = new StreamResult(new FileOutputStream(out + meta.getMarcxmlFilename()));
                    //data.marcXmlOutfile = new MarcXmlWriter(result, stylesheet);
                    //source = new DOMResult();
                    //data.marcXmlOutfile = new MarcXmlWriter(source);
                    //AnselToUnicode converter = new AnselToUnicode();
                    //data.marcXmlOutfile.setConverter(converter);
                    //data.marcXmlOutfile.setUnicodeNormalization(true);

                    //this.writer = new MarcStreamWriter(new FileOutputStream(out + "marcReg.mrc"));
                }

            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                logError("FileNotFoundException " + e1);
                return false;
            }/*finally {
             try {
             in.close();

             } catch (IOException ex) {
             logError("IOException " + ex);
             //Logger.getLogger(PruebaMarc21.class.getName()).log(Level.SEVERE, null, ex);
             }
             }*/

            data.marcfilesIterator = data.marcfiles.iterator();
            data.processingMarcfile = data.marcfilesIterator.next();
        }
        return true;
    }

    /**
     * Once the transformation starts executing, the processRow() method is
     * called repeatedly by PDI for as long as it returns true. To indicate that
     * a step has finished processing rows this method must call setOutputDone()
     * and return false;
     *
     * Steps which process incoming rows typically call getRow() to read a
     * single row from the input stream, change or add row content, call
     * putRow() to pass the changed row on and return true. If getRow() returns
     * null, no more rows are expected to come in, and the processRow()
     * implementation calls setOutputDone() and returns false to indicate that
     * it is done too.
     *
     * Steps which generate rows typically construct a new row Object[] using a
     * call to RowDataUtil.allocateRowData(numberOfFields), add row content, and
     * call putRow() to pass the new row on. Above process may happen in a loop
     * to generate multiple rows, at the end of which processRow() would call
     * setOutputDone() and return false;
     *
     * @param smi the step meta interface containing the step settings
     * @param sdi the step data interface that should be used to store
     *
     * @return true to indicate that the function should be called again, false
     * if the step is done
     */
    public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleStepException {
        meta = (MARC21LoaderMeta) smi;
        data = (MARC21LoaderData) sdi;

        boolean retval = true;

        data.processingMarcfile = !data.processingMarcfile.hasNext()
                && data.marcfilesIterator.hasNext()
                ? data.marcfilesIterator.next() : data.processingMarcfile;

        if (!data.processingMarcfile.hasNext() && !data.marcfilesIterator.hasNext()) {
            if (meta.getGenMARCXML()) {
                logBasic("MARCXML file generated with name: " + meta.getMarcxmlFilename());
                //writer.close();
                data.marcXmlOutfile.close();
            }
            setOutputDone();
            return false;
        }

        if (first) {
            first = false;

            // the size of the incoming rows 
            //data.inputSize = getInputRowMeta().size();
            // determine output field structure
            data.outputRowMeta = new RowMeta();//(RowMetaInterface) getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

        }

        // building new row
        Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        Integer outputIndex = 0;

        Record record = data.processingMarcfile.next();
        if (meta.getGenMARCXML()) {
            //writer.write(record);
            data.marcXmlOutfile.write(record);
        }

        this.getBasicControlFields(record, outputIndex);
        if (mfn.equals("000002") && log.isDebug()) {
            if (meta.getGenMARCXML()) {
                logDebug("Sample MARCXML file generated with name: " + meta.getMarcxmlFilename());
                //writer.close();
                data.marcXmlOutfile.close();
            }
            setOutputDone();
            return false;
        }

        //MARC fields reading
        String leer_campos = meta.getmarcFields();
        String[] campos = leer_campos.split("@");
        List<MARC21> obj_marcAux = new ArrayList<MARC21>();
        String mfnInt = mfn;
        String currentField = "";
        String subfields = "";
        for (int i = 0; i < campos.length; i++) {
            if (campos[i].length() == 4) {
                subfields = !currentField.equals(campos[i].substring(0, 3)) ? "" : subfields;
                currentField = campos[i].substring(0, 3);
                subfields += campos[i].charAt(3);

                obj_marcAux = this.obtainField(record, mfnInt, currentField, subfields, campos[i].charAt(3));
                for (MARC21 obj_marc : obj_marcAux) {
                    outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
                    outputIndex = 0;
                    outputRow[outputIndex++] = mfn;
                    outputRow[outputIndex++] = obj_marc.getSecuence();
                    outputRow[outputIndex++] = new Long(obj_marc.getField());
                    outputRow[outputIndex++] = obj_marc.getIndicators();
                    outputRow[outputIndex++] = obj_marc.getLeadersubfields() + "";
                    outputRow[outputIndex++] = obj_marc.getValue() + "";
                    putRow(data.outputRowMeta, outputRow);

                }
            } else {
                logBasic("Field {0} is not well formatted. Omitting", campos[i]);
            }
        }

        //if ((linesInput%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesInput);
        return retval;
    }

    public static String createRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.toString();
    }

    /**
     * Lectura de campos básicos del leader del registro MARC
     *
     * @param record
     * @param outputIndex
     * @throws KettleStepException
     */
    private void getBasicControlFields(Record record, Integer outputIndex) throws KettleStepException {
        Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        ControlField field;
        //Muestra el lenguaje de cada registro 
        // get control field with tag 001/005/008
        field = (ControlField) record.getVariableField("001");
        //mfn = field != null ? field.getData() : createRandomString(12);
        mfn = UUID.randomUUID().toString();
        
        //Atributos del ControlField 005
        //Date and Time of Latest Transaction
        field = (ControlField) record.getVariableField("005");
        outputRow[outputIndex++] = mfn;
        outputRow[outputIndex++] = "0";
        outputRow[outputIndex++] = new Long(5); // ???
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = field != null ? field.getData() : "";

        putRow(data.outputRowMeta, outputRow);

        //CAMPOS DE LA CABECERA
        Leader leader = record.getLeader();

        //1005 Estado del registro
        outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        outputIndex = 0;
        outputRow[outputIndex++] = mfn;
        outputRow[outputIndex++] = "0";
        outputRow[outputIndex++] = new Long(1005);
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = leader.getRecordStatus() + "";
        putRow(data.outputRowMeta, outputRow);

        //1006 Tipo de registro
        outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        outputIndex = 0;
        outputRow[outputIndex++] = mfn;
        outputRow[outputIndex++] = "0";
        outputRow[outputIndex++] = new Long(1006);
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = leader.getTypeOfRecord() + "";
        putRow(data.outputRowMeta, outputRow);

        //1007 
        outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        outputIndex = 0;
        outputRow[outputIndex++] = mfn;
        outputRow[outputIndex++] = "0";
        outputRow[outputIndex++] = new Long(1007);
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = leader.toString().charAt(6) + "";
        putRow(data.outputRowMeta, outputRow);

        //1017 
        outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        outputIndex = 0;
        outputRow[outputIndex++] = mfn;
        outputRow[outputIndex++] = "0";
        outputRow[outputIndex++] = new Long(1017);
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = leader.toString().charAt(16) + "";
        putRow(data.outputRowMeta, outputRow);

        //1018 
        outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        outputIndex = 0;
        outputRow[outputIndex++] = mfn;
        outputRow[outputIndex++] = "0";
        outputRow[outputIndex++] = new Long(1018);
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = "";
        outputRow[outputIndex++] = leader.toString().charAt(17) + "";
        putRow(data.outputRowMeta, outputRow);

    }

    /**
     * Obtencion de Datos de un Campo MARC21
     *
     * @param record
     * @param mfn
     * @param field
     * @paman leaderSubfields
     * @param subfield
     * @return
     */
    private List<MARC21> obtainField(Record record, String mfn, String field, String leaderSubfields, char subfield) {
        //Tomo todos los subcampos del campo 
        List<MARC21> objMarc = new ArrayList<MARC21>();
        List<VariableField> marcFields = record.getVariableFields(field);
        int cont = 0;
        for (VariableField marcFieldAux : marcFields) {
            String data = "";
            DataField marcField = (DataField) marcFieldAux;
            String tag = "";
            String indicators = "";
            if (marcField != null) {
                tag = marcField.getTag();
                indicators = "" + marcField.getIndicator1() + marcField.getIndicator2();
                indicators.trim();
                try {
                    List<Subfield> subfields = marcField.getSubfields(subfield);
                    for (Subfield marcSubfield : subfields) {
                        char code = marcSubfield.getCode();
                        String fullCode = marcSubfield.toString().substring(0, 2);
                        data = marcSubfield.getData();

                    }
                } catch (NullPointerException e) {

                }
            }
            objMarc.add(new MARC21(mfn, String.valueOf(cont++), field, indicators, subfield, leaderSubfields, data));
        }
        return objMarc;
    }

    /**
     * This method is called by PDI once the step is done processing.
     *
     * The dispose() method is the counterpart to init() and should release any
     * resources acquired for step execution like file handles or database
     * connections.
     *
     * The meta and data implementations passed in can safely be cast to the
     * step's respective implementations.
     *
     * It is mandatory that super.dispose() is called to ensure correct
     * behavior.
     *
     * @param smi step meta interface implementation, containing the step
     * settings
     * @param sdi	step data interface implementation, used to store runtime
     * information
     */
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (MARC21LoaderMeta) smi;
        data = (MARC21LoaderData) sdi;

        super.dispose(smi, sdi);
    }

    //
    // Run is were the action happens!
    //
    //
    public void run() {
        logBasic("Starting to run...");

        try {
            while (processRow(meta, data) && !isStopped());
        } catch (Exception e) {
            logError("Unexpected error", e);
            setErrors(1);
            stopAll();
        } finally {
            dispose(meta, data);
            logBasic("Finished, processed " + linesInput + " rows, written " + linesWritten + " lines.");
            markStop();
        }
    }
}
