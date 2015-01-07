/*******************************************************************************
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
 ******************************************************************************/

package com.ucuenca.pentaho.plugin.step;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;




import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.*;
/**
 * This class is part of the demo step plug-in implementation. It demonstrates
 * the basics of developing a plug-in step for PDI.
 * 
 * The demo step adds a new string field to the row stream and sets its value to
 * "Hello World!". The user may select the name of the new field.
 * 
 * This class is the implementation of StepDialogInterface. Classes implementing
 * this interface need to:
 * 
 * - build and open a SWT dialog displaying the step's settings (stored in the
 * step's meta object) - write back any changes the user makes to the step's
 * meta object - report whether the user changed any settings when confirming
 * the dialog
 * 
 */
public class GetPropertiesOWLDialog extends BaseStepDialog implements
		StepDialogInterface {

	/**
	 * The PKG member is used when looking up internationalized strings. The
	 * properties file with localized keys is expected to reside in {the package
	 * of the class specified}/messages/messages_{locale}.properties
	 */
	private static Class<?> PKG = GetPropertiesOWLMeta.class; // for purposes
	
	private final static String USER_AGENT = "Mozilla/5.0";
	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed
	private GetPropertiesOWLMeta meta;
	private static final Logger log = Logger.getLogger( GetPropertiesOWL.class.getName() );

	// text field holding the name of the field to add to the row stream
	private Text wHelloFieldName;
	private Button wLoadFile;
	private Button wAddUri;
	private Button wbdFilename; // Delete
	private Button wbeFilename; // Edit
	private Button wbaFilename; // Add or change
	private Button wbShowFiles; // dentro de la tabla
	private Button wbEraseRecord;// Erase Record
	private Listener lsLoadFile;
	private Listener lsReadUri;
	private Listener lsTable;
	private Listener lsBottonErase;
	private Label wlKeys;
	private TableView wKeys;
	private TableView wFields;
	private TextVar wExcludeFilemask;
	private FormData fdlFilenameList, fdFilenameList;
	private FormData fdFileComp;
	private FormData fdFields;
	private FormData fdbShowFiles;
	private FormData fdlFilename, fdbFilename, fdbdFilename, fdbeFilename,
			fdbaFilename, fdFilename;
	private TableView wFilenameList;
	private Composite wFileComp, wContentComp, wFieldsComp;

	private FormData fdmitabla;
	private FormData fdok;
	private FormData fdcancel;
	// the dropdown column which should contain previous fields from stream
	private ColumnInfo fieldColumn = null;
	private Table table;
	private int numt = 0;
	
	private int NumRowSelected;
	private LinkedList ListSource =  new LinkedList<String>();
	/**
	 * The constructor should simply invoke super() and save the incoming meta
	 * object to a local variable, so it can conveniently read and write
	 * settings from/to it.
	 * 
	 * @param parent
	 *            the SWT shell to open the dialog in
	 * @param in
	 *            the meta object holding the step's settings
	 * @param transMeta
	 *            transformation description
	 * @param sname
	 *            the step name
	 */
	public GetPropertiesOWLDialog(Shell parent, Object in, TransMeta transMeta,
			String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (GetPropertiesOWLMeta) in;
	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of
	 * the step. It should open the dialog and return only once the dialog has
	 * been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the
	 * constructor) must be updated to reflect the new step settings. The
	 * changed flag of the meta object must reflect whether the step
	 * configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and
	 * its changed flag must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has
	 * confirmed the dialog, or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables
		Shell parent = getParent();
		Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN
				| SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);

		// Save the value of the changed flag on the meta object. If the user
		// cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();

		// The ModifyListener used on all controls. It will update the meta
		// object to
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};

		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG,
				"GetPropertiesOWL.Shell.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line


		// output field value
		Label wlValName = new Label(shell, SWT.RIGHT);
		wlValName.setText(BaseMessages.getString(PKG,
				"GetPropertiesOWL.FieldName.Label"));
		props.setLook(wlValName);
		FormData fdlValName = new FormData();
		fdlValName.left = new FormAttachment(0, 0);
		fdlValName.right = new FormAttachment(middle, -margin);
		// fdlValName.top = new FormAttachment(wStepname, margin);
		fdlValName.top = new FormAttachment(wlStepname, margin);

		wlValName.setLayoutData(fdlValName);

		wHelloFieldName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wHelloFieldName);
		wHelloFieldName.addModifyListener(lsMod);
		FormData fdValName = new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		fdValName.right = new FormAttachment(100, 0);
		// fdValName.top = new FormAttachment(wStepname, margin);
		fdValName.top = new FormAttachment(wlStepname, margin);
		wHelloFieldName.setLayoutData(fdValName);

		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		wLoadFile = new Button(shell, SWT.PUSH);
		wLoadFile.setText(BaseMessages.getString(PKG,
				"GetPropertiesOWL.FieldName.LoadFile"));
		wAddUri = new Button(shell, SWT.PUSH);
		wAddUri.setText(BaseMessages.getString(PKG,
				"GetPropertiesOWL.FieldName.AddUri"));
		wbEraseRecord = new Button(shell, SWT.PUSH);
		
		wbEraseRecord.setText(BaseMessages.getString(PKG,
				"GetPropertiesOWL.FieldName.delete"));
		// BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK,
		// wCancel, wLoadFile }, margin, wHelloFieldName);
		BaseStepDialog.positionBottomButtons(shell, new Button[] {
				this.wAddUri, wLoadFile, wbEraseRecord }, margin,
				wHelloFieldName);

		// tabla
		table = new Table(shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		String[] titles = { BaseMessages.getString(PKG,"GetPropertiesOWL.FieldName.col1"), BaseMessages.getString(PKG,"GetPropertiesOWL.FieldName.col2"),BaseMessages.getString(PKG,"GetPropertiesOWL.FieldName.col3")};
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
			column.setWidth(350);
		}

		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}
		table.setSize(table.computeSize(SWT.DEFAULT, 200));
		table.setItemCount(6);// para ver las filas por defecto
		// parametrizando el forma data
		fdmitabla = new FormData();
		//fdmitabla.left = new FormAttachment(middle, 0);
		fdmitabla.left = new FormAttachment(1, 0);
		fdmitabla.right = new FormAttachment(100, 1);
		fdmitabla.top = new FormAttachment(wLoadFile, margin);

		// boton ok y cancel al ultimo

		table.setLayoutData(fdmitabla);

		BaseStepDialog.positionBottomButtons(shell,
				new Button[] { wOK, wCancel }, margin, table);
		/**
		 * fdok = new FormData(); fdok.left = new FormAttachment(wCancel,
		 * middle); //fdok.right = new FormAttachment(100, 0);
		 * 
		 * fdok.top = new FormAttachment(table, margin);
		 * wOK.setLayoutData(this.fdok); fdcancel = new FormData(); // fdok.left
		 * = new FormAttachment(wCancel, middle); fdcancel.right = new
		 * FormAttachment(wCancel,middle); fdcancel.top = new
		 * FormAttachment(table, margin); wCancel.setLayoutData(this.fdcancel);
		 */

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
		lsLoadFile = new Listener() {
			public void handleEvent(Event e) {
				LoadFile();
			}
		};
		lsReadUri = new Listener() {
			public void handleEvent(Event e) {
				AddUri();
			}
		};
		lsTable = new Listener() {
			public void handleEvent(Event e) {
				GetSelectedRow();
			}
		};
		lsBottonErase = new Listener() {
			public void handleEvent(Event e) {
				BorrarFila();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
		wLoadFile.addListener(SWT.Selection, lsLoadFile);
		this.wAddUri.addListener(SWT.Selection, lsReadUri);
		this.table.addListener(SWT.Selection, lsTable);
		this.wbEraseRecord.addListener(SWT.Selection, this.lsBottonErase);
		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};
		// wStepname.addSelectionListener(lsDef);
		wHelloFieldName.addSelectionListener(lsDef);

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
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have
		// been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}

	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		// wStepname.selectAll();
		if(meta.getOutputField().compareTo(BaseMessages.getString(PKG, "GetPropertiesOWL.FieldName.MensajeInicio"))!=0){
		
		
		
		
		
		String replace = meta.getOutputField().replace("[", "");		
		String replace1 = replace.replace("]", "");
		 ArrayList<String> myList = new ArrayList<String>(Arrays.asList(replace1
				.split(",")));
		
		//ListSource = meta.getListSourcetoProcess();
		
			for (int i=0;i<myList.size();i++){
				TableItem item = new TableItem(table, SWT.NONE, numt++);
			item.setText(0, String.valueOf(numt));
			item.setText(1,myList.get(i).toString() );
//--
			Pattern pat = Pattern.compile("^http://.*");
			Matcher mat = pat.matcher(myList.get(i).toString());
			if (mat.matches()) { // entonces es una uri
				item.setText(2, BaseMessages.getString(PKG, "GetPropertiesOWL.FieldName.mt2"));}else{item.setText(2,BaseMessages.getString(PKG, "GetPropertiesOWL.FieldName.mt2"));}
			//--
			
			wHelloFieldName.setText(myList.get(i).toString() );
		}
		}else{
			wHelloFieldName.setText(BaseMessages.getString(PKG, "GetPropertiesOWL.FieldName.MensajeInicio"));
		}//fin if 
	
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
		// The "stepname" variable will be the return value for the open()
		// method.
		// Setting to step name from the dialog control
		// stepname = wStepname.getText();
		stepname = "Get properties Owl ";
		// Setting the settings to the meta object
		
		// meta.setOutputField(this.table.getSelection().toString());
		
		//recorrer todas las filas
		
		
		
		for (int i=0;i<this.numt;i++)
		{
			
			
			TableItem miti = table.getItem(i);
			ListSource.add(miti.getText(1));
		 final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL
				        | SWT.DIALOG_TRIM);
				    dialog.setText(ListSource.get(i).toString());
				    dialog.setSize(250, 150);
			
			//JOptionPane.showMessageDialog(null, ListSource.get(i));
				  // meta.setOutputField(ListSource.get(i).toString()); //para que tome solo la seleccionada
					
			
		}
		meta.setOutputField(ListSource.toString()); 
		meta.setListSourcetoProcess(ListSource);
		//meta.setOutputField(ListSource.get(i)); //para que tome solo la seleccionada
		
		//-----------------------
		// close the SWT dialog window
		dispose();
	}

	private void LoadFile() {
		   
		   try{
			   FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			   dialog.setText("Choose the file .owl in your computer");
			   String result = dialog.open();
		   TableItem item = new TableItem(table, SWT.NONE, numt++);
			item.setText(0, String.valueOf(numt));
			item.setText(1, dialog.getFilterPath() +"/"+ dialog.getFileName());
			item.setText(2, "from file");
		   }catch(Exception e){
			   log.log( Level.SEVERE, e.toString(), e );
		   }
		//JFileChooser chooser = new JFileChooser();
		   /**
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Choose the file .owl in your computer");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			// System.out.println("getCurrentDirectory(): " +
			// chooser.getCurrentDirectory());
			JOptionPane.showMessageDialog(null, "getSelectedFile() : "
					+ chooser.getSelectedFile());
			// this.wHelloFieldName.setText(chooser.getSelectedFile().toString());
			// para agregar a la tabla swt
			// this.table

			TableItem item = new TableItem(table, SWT.NONE, numt++);
			item.setText(0, String.valueOf(numt));
			item.setText(1, chooser.getSelectedFile().toString());
			item.setText(2, "from file");
		} else {
			JOptionPane.showMessageDialog(null,
					"Please first you have to do almost any Selection ");
		}
		*/
		// The "stepname" variable will be the return value for the open()
		// method.
		// Setting to step name from the dialog control
		// stepname = wStepname.getText();
		// Setting the settings to the meta object
		// meta.setOutputField(wHelloFieldName.getText());
		// close the SWT dialog window
		// dispose();
	}

	private void AddUri() {

		if (wHelloFieldName.getText().compareTo(BaseMessages.getString(PKG,
				"GetPropertiesOWL.FieldName.MensajeInicio")) == 0) {
			 final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL
				        | SWT.DIALOG_TRIM);
				    dialog.setText(BaseMessages.getString(PKG,"GetPropertiesOWL.FieldName.IngUri"));
				    dialog.setSize(250, 150);
			//JOptionPane.showMessageDialog(null, "please enter the URI");
		} else {
			Pattern pat = Pattern.compile("^http.*");
			Matcher mat = pat.matcher(wHelloFieldName.getText());
			TableItem item = new TableItem(table, SWT.NONE, numt++);
			if (mat.matches()) { // entonces es una uri
			
				item.setText(0, String.valueOf(numt));
				item.setText(1, wHelloFieldName.getText());
				item.setText(2, BaseMessages.getString(PKG, "GetPropertiesOWL.FieldName.mt2"));
			} else {
				String myresult = ConsultUri(wHelloFieldName.getText());// search
																// in

				if (myresult != null) {
					item.setText(0, String.valueOf(numt));
					item.setText(1, myresult);
					item.setText(2, BaseMessages.getString(PKG, "GetPropertiesOWL.FieldName.mt2"));
				} else {
					//JOptionPane.showMessageDialog(null, " URI Not Found ,please write again");
					final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL
					        | SWT.DIALOG_TRIM);
					    dialog.setText(" URI Not Found ,please write again");
					    dialog.setSize(250, 150);
					wHelloFieldName.setText(BaseMessages.getString(PKG, "GetPropertiesOWL.FieldName.MensajeInicio"));
				}
			}

		}
	}

	/** to load URI from Web */
	private String ConsultUri(String mysearching) {

		String url = "http://prefix.cc/context";

		URL obj;
		String loudScreaming = null;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
		

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
		

			// ---------------

			// ------
			JsonObject var = JSON.parse(response.toString());

			try {
				JSONObject jsonRoot = new JSONObject(response.toString());
				loudScreaming = jsonRoot.getJSONObject("@context").getString(
						mysearching);

				

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(var.get(key) === "bibo" );
			// json = JSON.stringify(eval("(" + response.toString() + ")"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return loudScreaming;
	}

	private void GetSelectedRow() {
		// TODO
		String string = "";
		TableItem[] selection = table.getSelection();
		TableItem miti = selection[0];
		
		//SourcetoProcess = miti.getText(1);
		this.NumRowSelected = table.getSelectionIndex();
		
		/**
		 * for (int i = 0; i < selection.length; i++) { TableItem miti =
		 * selection[0];
		 * 
		 * }
		 */

	}

	private void BorrarFila() {
		// TODO
		int bandera=0;
		for (int i=0;i<table.getItemCount();i++){
			if (table.isSelected(i)){
				bandera=1;
			}
			
		}
		if(bandera==1){
	
		String string = "";
		TableItem[] selection = table.getSelection();
		table.remove(NumRowSelected);
		numt--;
		}
	}

}
