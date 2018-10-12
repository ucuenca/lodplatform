/**
 * *****************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************
 */
package com.ucuenca.pentaho.plugin.step.oai;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.digester.Digester;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.ucuenca.pentaho.plugin.auxiliary.GetXPath;
import com.ucuenca.pentaho.plugin.oai.ListMetadataFormats;
import com.ucuenca.pentaho.plugin.oai.ListRecords;
import com.ucuenca.pentaho.plugin.oai.Schema;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class OAILoaderDialog extends BaseStepDialog implements
        StepDialogInterface {

    private static Class<?> PKG = OAILoaderMeta.class; // for i18n purposes

    // variables globales para uso general
    private OAILoaderMeta meta;

    private int max_field_length;

    // componentes de la interface
    private Label lbURI;
    private TextVar txtURI;

    private Label lbResponseDateField;
    private Text txtResponseDateField;

    private Label lbPrefijo;
    private CCombo cbmPrefix;
    private Text txtXpath;
    private Button Xpath;
    private Button getFormats;
    private Button getFields;
    //must be included for DataBase Data Loading
    private Button precatchDataButton;

    private FormData fdlbURI, fdtxtURI, fdlbPrefijo, fdtxtPrefijo,
            fbcbmPrefix, fdXpath, fdGetFormats, fdlbResponseDateField, fdtxtResponseDateField, fdGetFields;

    List<Schema> schemas = null;
    private String prefix;
    private String valueUri = "Input URI";

    private Schema schema = new Schema();
    private String xpath;
    private String Uri;
    GetXPath getPathOai;

    int electedItem;

    private int middle;
    private int margin;

    private RowMetaInterface fields;

    public OAILoaderDialog(Shell parent, Object in, TransMeta transMeta,
            String sname) {
        super(parent, (BaseStepMeta) in, transMeta, sname);
        meta = (OAILoaderMeta) in;
        //must be included for DataBase Data Loading
        meta.setTransMeta(transMeta);
        try {
            fields = transMeta.getPrevStepFields(stepname);
            getMaxFieldLength();
        } catch (KettleStepException ex) {
            Logger.getLogger(OAILoaderDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void getMaxFieldLength() {
        max_field_length = 5;
        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                ValueMetaInterface value = fields.getValueMeta(i);
                if (value != null && value.getName() != null) {
                    int len = fields.getValueMeta(i).getName().length();
                    if (len > max_field_length) {
                        max_field_length = len;
                    }
                }
            }
        }
    }

    public String open() {

        // store some convenient SWT variables
        Shell parent = getParent();
        Display display = parent.getDisplay();

        // SWT code for preparing the dialog
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN
                | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, meta);

        // place for modifyListener
        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                meta.setChanged();

                if (!valueUri.equals(txtURI.getText())) {
                    prefix = null;
                    xpath = null;
                    Xpath.setEnabled(false);
                    cbmPrefix.setEnabled(false);
                }

            }
        };
        //changed = meta.hasChanged();

        // ------------------------------------------------------- //
        // SWT code for building the actual settings dialog //
        // ------------------------------------------------------- //
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "OAILoader.Shell.Title"));

        middle = props.getMiddlePct();
        margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname
                .setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.top = new FormAttachment(0, margin);
        fdlStepname.right = new FormAttachment(middle, -margin);
        wlStepname.setLayoutData(fdlStepname);

        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        // add components to grupLayout  ResponseDateTime
        lbResponseDateField = new Label(shell, SWT.RIGHT);
        lbResponseDateField.setText(BaseMessages.getString(PKG, "OAILoader.FieldName.ResponseDateField"));
        props.setLook(lbResponseDateField);
        fdlbResponseDateField = new FormData();
        fdlbResponseDateField.left = new FormAttachment(0, 0);
        fdlbResponseDateField.right = new FormAttachment(middle, -margin);
        fdlbResponseDateField.top = new FormAttachment(wStepname, margin);
        lbResponseDateField.setLayoutData(fdlbResponseDateField);

        txtResponseDateField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        txtResponseDateField.setText(meta.getResponseDateField());
        props.setLook(txtResponseDateField);
        //txtResponseDateField.addModifyListener(lsMod);
        fdtxtResponseDateField = new FormData();
        fdtxtResponseDateField.left = new FormAttachment(middle, 0);
        fdtxtResponseDateField.right = new FormAttachment(100, 0);
        fdtxtResponseDateField.top = new FormAttachment(wStepname, margin);
        txtResponseDateField.setLayoutData(fdtxtResponseDateField);

        
        getFields = new Button(shell, SWT.PUSH | SWT.SINGLE | SWT.MEDIUM | SWT.BORDER);
        props.setLook(getFields);

        getFields.setText(BaseMessages.getString(PKG,"OAILoader.InputData.Fields"));
        getFields.setToolTipText(BaseMessages.getString(PKG,"System.Tooltip.BrowseForFileOrDirAndAdd"));
        fdGetFields = new FormData();
        fdGetFields.right = new FormAttachment(100, 0);
        fdGetFields.top = new FormAttachment(wStepname, margin);
        getFields.setLayoutData(fdGetFields);

        fdtxtResponseDateField.right = new FormAttachment(getFields, -margin);
        //cbmPrefix.setLayoutData(fbcbmPrefix);

        getFields.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                try {
                    if (fields != null) {
                        EnterSelectionDialog esd = new EnterSelectionDialog(shell, fields.getFieldNames(/*max_field_length*/), BaseMessages.getString(PKG, "OAILoader.Fields"), BaseMessages.getString(PKG, "OAILoader.SelectAField"));
                        esd.setAvoidQuickSearch();
                        String v = esd.open(0);
                        
                        if (v!=null)
                        {
                            txtResponseDateField.setText(v);
                        }
                    }
                } catch (Exception ex) {

                }

            }

        });
        
        
        
        
        
        
        
        
        
        
        
        
        
        // add components to grupLayout
        lbURI = new Label(shell, SWT.RIGHT);
        lbURI.setText(BaseMessages.getString(PKG, "OAILoader.FieldName.Label"));
        props.setLook(lbURI);
        fdlbURI = new FormData();
        fdlbURI.left = new FormAttachment(0, 0);
        fdlbURI.right = new FormAttachment(middle, -margin);
        fdlbURI.top = new FormAttachment(txtResponseDateField, margin);
        lbURI.setLayoutData(fdlbURI);

        txtURI = new TextVar(transMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        txtURI.setText(meta.getInputURI());
        props.setLook(txtURI);
        txtURI.addModifyListener(lsMod);
        fdtxtURI = new FormData();
        fdtxtURI.left = new FormAttachment(middle, 0);
        fdtxtURI.right = new FormAttachment(100, 0);
        fdtxtURI.top = new FormAttachment(txtResponseDateField, margin);
        txtURI.setLayoutData(fdtxtURI);

        // button for get all formats of the server OAI-PHM
        lbPrefijo = new Label(shell, SWT.RIGHT);

        lbPrefijo.setText(BaseMessages.getString(PKG,
                "OAILoader.FieldName.Prefix"));
        props.setLook(lbPrefijo);
        fdlbPrefijo = new FormData();
        fdlbPrefijo.left = new FormAttachment(0, 0);
        fdlbPrefijo.right = new FormAttachment(middle, -margin);
        fdlbPrefijo.top = new FormAttachment(txtURI, margin);
        lbPrefijo.setLayoutData(fdlbPrefijo);

        cbmPrefix = new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        cbmPrefix.setEditable(true);
        props.setLook(cbmPrefix);
        cbmPrefix.addModifyListener(lsMod);
        fbcbmPrefix = new FormData();
        fbcbmPrefix.left = new FormAttachment(middle, 0);
        fbcbmPrefix.top = new FormAttachment(txtURI, margin);
        fbcbmPrefix.height = 23;
        cbmPrefix.setEditable(false);
        cbmPrefix.setEnabled(false);
        cbmPrefix.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent arg0) {
                prefix = cbmPrefix.getText();
                electedItem = cbmPrefix.getSelectionIndex();
              // schema =  getSchemaSelected (schemas , cbmPrefix.getText());
             //   schema = (Schema) schemas.get(schemas.indexOf(cbmPrefix.getText()));
                schema = (Schema) schemas.get(cbmPrefix.getSelectionIndex());
                txtXpath.setText("");
                Xpath.setEnabled(true);
            }

            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        getFormats = new Button(shell, SWT.PUSH | SWT.SINGLE | SWT.MEDIUM | SWT.BORDER);
        props.setLook(getFormats);

        getFormats.setText(BaseMessages.getString(PKG,
                "OAILoader.InputData.Formats"));
        getFormats.setToolTipText(BaseMessages.getString(PKG,
                "System.Tooltip.BrowseForFileOrDirAndAdd"));
        fdGetFormats = new FormData();
        fdGetFormats.right = new FormAttachment(100, 0);
        fdGetFormats.top = new FormAttachment(txtURI, margin);
        getFormats.setLayoutData(fdGetFormats);

        fbcbmPrefix.right = new FormAttachment(getFormats, -margin);
        cbmPrefix.setLayoutData(fbcbmPrefix);

        getFormats.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                Pattern pat = Pattern.compile("^http://.*");
                Matcher mat = pat.matcher(txtURI.getText());
                if (true || mat.matches()) { // entonces es una uri
                    //meta.setInputURI(txtURI.getText());	
                    valueUri = txtURI.getText();
                    cbmPrefix.setText("");
                    txtXpath.setText("");
                    listPrefix(txtURI.getText());
                } else {

                    MessageBox dialog
                            = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                    dialog.setText("ERROR");
                    dialog.setMessage(BaseMessages.getString(PKG, "OAILoader.Manager.ERRORURI"));

                    dialog.open();
                    cbmPrefix.setEnabled(false);
                    cbmPrefix.setText("");
                    txtXpath.setText("");
                }

            }

        });

        Xpath = new Button(shell, SWT.PUSH);
        props.setLook(Xpath);

        Xpath.setText(BaseMessages.getString(PKG, "OAILoader.ButtonName.Title"));
        Xpath.setToolTipText(BaseMessages.getString(PKG,
                "System.Tooltip.BrowseForFileOrDirAndAdd"));
        fdXpath = new FormData();
        fdXpath.left = new FormAttachment(middle, 0);
        fdXpath.right = new FormAttachment(50, 0);
        fdXpath.top = new FormAttachment(cbmPrefix, margin);
        Xpath.setLayoutData(fdXpath);
        Xpath.setEnabled(false);
        Xpath.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    getLoopPathList();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (ParserConfigurationException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (SAXException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (TransformerException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        txtXpath = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(txtXpath);
        txtXpath.addModifyListener(lsMod);
        fdtxtPrefijo = new FormData();
        fdtxtPrefijo.left = new FormAttachment(Xpath, 0);
        fdtxtPrefijo.right = new FormAttachment(100, 0);
        fdtxtPrefijo.top = new FormAttachment(getFormats, margin);
        txtXpath.setLayoutData(fdtxtPrefijo);
        txtXpath.setEditable(false);

        //must be included for DataBase Data Loading
        precatchDataButton = new Button(shell, SWT.PUSH);
        props.setLook(precatchDataButton);
        precatchDataButton.setText(BaseMessages.getString(PKG, "OAILoader.PrecatchButton.Title"));
        precatchDataButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                

                OAILoaderData data = (OAILoaderData) meta.getStepData();
                setDialogMetadata();
                data.getDataLoader().setStepName(meta.getStepName());

                MessageBox dialog1 = new MessageBox(shell, SWT.ICON_WORKING | SWT.OK);
                dialog1.setText("Dialog");
                dialog1.setMessage(BaseMessages.getString(PKG, "Precatching.StartDialog.Text"));
                dialog1.open();
                data.initOAIHarvester(meta, data, true);
                DBLoader dbloader = new DBLoader(data);
                Thread thread = new Thread(dbloader);
                thread.start();
                try {
                    thread.join();
                    if (dbloader.status != null) {
                        MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                        dialog.setText("Dialog");
                        dialog.setMessage(BaseMessages.getString(PKG, "Precatching.FinishDialog.Text"));
                        dialog.open();
                    } else if (dbloader.exception != null) {
                        new ErrorDialog(shell, "Dialog", "Message: ", dbloader.exception);
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        });

        // OK and cancel buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        //must be included for DataBase Data Loading
        BaseStepDialog.positionBottomButtons(shell,
                new Button[]{wOK, wCancel, precatchDataButton}, margin, txtXpath);

        // Add listeners for cancel and OK
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        // default listener (for hitting "enter")
        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);

        txtURI.addSelectionListener(lsDef);
        txtResponseDateField.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window and cancel the
        // dialog properly
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        // Set/Restore the dialog size based on last position on screen
        // The setSize() method is inherited from BaseStepDialog
        setSize();

        // populate the dialog with the values from the meta object
        populateDialog();

        // restore the changed flag to original value, as the modify listeners
        // fire during dialog population
        meta.setChanged(changed);

        // open dialog and enter event loop
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        // at this point the dialog has closed, so either ok() or cancel() have
        // been executed
        // The "stepname" variable is inherited from BaseStepDialog
        return stepname;
    }

    //must be included for DataBase Data Loading
    /**
     * Thread in charge to execute Database data loading
     *
     * @author depcc
     *
     */
    private class DBLoader implements Runnable {

        private OAILoaderData data;

        String status;

        Exception exception;

        private DBLoader(OAILoaderData data) {
            this.data = data;
        }

        public void run() {
            try {
                int x = 0;
                while (data.getData(meta, data)) {
                    x++;
                }
                data.getDataLoader().logBasic(BaseMessages.getString(PKG,
                        "OAILoader.Dialog.TotalRequests.Label") + x);
                status = "OK";
            } catch (KettleException ex) {
                ex.printStackTrace();
                exception = ex;
            }
        }
    }

    /**
     * This helper method puts the step configuration stored in the meta object
     * and puts it into the dialog controls.
     */
    private void populateDialog() {
        wStepname.selectAll();
        if (!meta.getInputURI().equals("Input URI")) {
            prefix = meta.getPrefix();
            listPrefix(meta.getInputURI());
            valueUri = meta.getInputURI();
            Iterator<Schema> i = schemas.iterator();
            int setElement = 0;
            while (i.hasNext()) {
                Schema schema1 = i.next();
                if (schema1.prefix.equals(prefix)) {
                    this.schema = schema1;
                    break;
                }
                setElement++;
            }
            electedItem = setElement;

            cbmPrefix.setText(meta.getPrefix());
            txtXpath.setText(meta.getXpath());
            cbmPrefix.setEnabled(true);

        }
    }

    /**
     * Called when the user cancels the dialog.
     */
    private void cancel() {
        // The "stepname" variable will be the return value for the open()
        // method.
        // Setting to null to indicate that dialog was cancelled.
        stepname = null;
        // Restoring original "changed" flag on the met aobject
        meta.setChanged(changed);
        // close the SWT dialog window
        dispose();
    }

    /**
     * Called when the user confirms the dialog
     */
    private void ok() {
        //must be included for DataBase Data Loading
        this.setDialogMetadata();

        dispose();
    }

    //must be included for DataBase Data Loading
    /**
     * Method in charge to set the metadata obtained on the dialog
     *
     */
    private void setDialogMetadata() {
        stepname = wStepname.getText();
        meta.setStepName(stepname);
        meta.setInputURI(txtURI.getText());
        meta.setResponseDateField(txtResponseDateField.getText());
        meta.setPrefix(cbmPrefix.getText());   
       //  schema = getSchemaSelected (schemas , cbmPrefix.getText());
        schema = (Schema) schemas.get(cbmPrefix.getSelectionIndex());
        meta.setSchema(schema.schema);
        meta.setNamespace(schema.namespace);
        meta.setXpath(txtXpath.getText());
        meta.setChanged(true);
    }

   /* private Schema getSchemaSelected (List<Schema> schemas , String schemaPrefix) {
       for (Schema s :  schemas )
       {
           if (s.prefix.equals(schemaPrefix)){
           return s;
           }
       }
       return null;
    }*/
    // my proccess
    private void getLoopPathList() throws IOException,
            ParserConfigurationException, SAXException, TransformerException {

        getPathOai = new GetXPath(prefix);
        getPathOai.getMeta().getListpath().clear();
        String[] list_xpath = null;

        schema = (Schema) schemas.get(electedItem);

        ListRecords listRecords;

        listRecords = new ListRecords(Uri, null, null, null, schema.prefix,
                schema);

        Document doc = listRecords.getDocument();
        NodeList nList = doc.getElementsByTagName("record");
        // nList.item(10);
        // para llamar el metodo para cargar las rutas
        boolean delete = false ;
        int aux = 0;
        while  (!delete && aux < nList.getLength()){
            
        delete = getPathOai.getPath(nList.item(aux));
        aux++;
        }
    
        list_xpath = new String[getPathOai.getMeta().getListpath().size()];

        for (int k = 0; k < getPathOai.getMeta().getListpath().size(); k++) {
            list_xpath[k] = getPathOai.getMeta().getListpath().get(k);
        }

        if (list_xpath.length > 0) {
            EnterSelectionDialog s = new EnterSelectionDialog(shell,
                    list_xpath, BaseMessages.getString(PKG,
                            "OAILoader.Dialog.SelectALoopPath.Title"),
                    BaseMessages.getString(PKG,
                            "OAILoader.Dialog.SelectALoopPath.Message"));
            xpath = s.open();
            if (xpath != null) {
                txtXpath.setText(xpath);
            }
        } else {
            MessageBox dialog
                    = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            dialog.setText("ERROR");
            dialog.setMessage(BaseMessages.getString(PKG, "OAILoader.Manager.FORMAT"));

            dialog.open();

        }

    }

    public void listPrefix(String ruta) {

        if (ruta != null) {

            System.out.println(ruta);
            ListMetadataFormats metadata;
            try {
                metadata = new ListMetadataFormats(ruta);
                Digester digester = new Digester();
                digester.setValidating(false);
                digester.addObjectCreate("OAI-PMH/ListMetadataFormats",
                        ArrayList.class);
                digester.addObjectCreate(
                        "OAI-PMH/ListMetadataFormats/metadataFormat",
                        Schema.class);
                digester.addBeanPropertySetter(
                        "OAI-PMH/ListMetadataFormats/metadataFormat/metadataPrefix",
                        "prefix");
                digester.addBeanPropertySetter(
                        "OAI-PMH/ListMetadataFormats/metadataFormat/schema",
                        "schema");
                digester.addBeanPropertySetter(
                        "OAI-PMH/ListMetadataFormats/metadataFormat/metadataNamespace",
                        "namespace");
                digester.addSetNext(
                        "OAI-PMH/ListMetadataFormats/metadataFormat", "add");

                schemas = (List) digester.parse(new StringReader(metadata.toString()));
                Iterator<Schema> i = schemas.iterator();
                cbmPrefix.removeAll();
                List <Schema> NewSchema = new ArrayList();

                while (i.hasNext()) {
                    Schema schema = i.next();

                    for (Format f : Format.values()) {
                        if (f.isState() && f.getName().equals(schema.prefix)) {
                            NewSchema.add(schema);
                            cbmPrefix.add(schema.prefix);
                            break;
                        } else if (!f.isState() && f.getName().equals(schema.prefix)) {

                            cbmPrefix.add(schema.prefix + " (" + BaseMessages.getString(PKG, "OAILoader.Manager.FORMAT") + ")");
                            NewSchema.add(schema);
                            break;
                        }
                    }
                   
                }
                 schemas = NewSchema;
                cbmPrefix.setEnabled(true);
                Uri = ruta;

            } catch (Exception e1) {
                MessageBox dialog
                        = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                dialog.setText("ERROR");
                dialog.setMessage(BaseMessages.getString(PKG, "OAILoader.Manager.ERROR"));

                dialog.open();
            }
        }

    }

    public enum Format {
        XOAI("xoai", true),
        QDC("qdc", true),
        UKETD_DC("uketd_dc", true),
        OAI_DC("oai_dc", true),
        MARCXML("marcxml", true),
        DIM("dim", false),
        MARC("marc", false),
        ETDMS("etdms", false),
        RDF("rdf", false),
        MODS("mods", false),
        METS("mets", false),
        ORE("ore", false),
        DIDL("didl", false);

        private final String name;
        private final boolean state;

        Format(String name, boolean state) {

            this.name = name;
            this.state = state;
        }

        public String getName() {
            return name;
        }

        public boolean isState() {
            return state;
        }

    }

}
