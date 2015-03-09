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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;


import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepDialogInterface.
 * Classes implementing this interface need to:
 * 
 * - build and open a SWT dialog displaying the step's settings (stored in the step's meta object)
 * - write back any changes the user makes to the step's meta object
 * - report whether the user changed any settings when confirming the dialog 
 * 
 */
public class FusekiLoaderDialog extends BaseStepDialog implements StepDialogInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = FusekiLoaderMeta.class; // for i18n purposes
	private final static String USER_AGENT = "Mozilla/5.0";
	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private FusekiLoaderMeta meta;

	// text field holding the name of the field to add to the row stream
	private Text wHelloFieldName;
	private Text wChooseOutput;
	private Button wAddUri;
	private Button wLoadFile;
	private Button wChooseDirectory;
	
	private Listener lsReadUri;
	private Listener lsLoadFile;
	private Listener lsChooseDirectory;
	
	private static final Logger log = Logger.getLogger(FusekiLoader.class
			.getName());

	/**
	 * The constructor should simply invoke super() and save the incoming meta
	 * object to a local variable, so it can conveniently read and write settings
	 * from/to it.
	 * 
	 * @param parent 	the SWT shell to open the dialog in
	 * @param in		the meta object holding the step's settings
	 * @param transMeta	transformation description
	 * @param sname		the step name
	 */
	public FusekiLoaderDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (FusekiLoaderMeta) in;
	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of the step.
	 * It should open the dialog and return only once the dialog has been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the constructor) must
	 * be updated to reflect the new step settings. The changed flag of the meta object must 
	 * reflect whether the step configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and its changed flag
	 * must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has confirmed the dialog,
	 * or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables 
		Shell parent = getParent();
		Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);
		
		// Save the value of the changed flag on the meta object. If the user cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();
		
		// The ModifyListener used on all controls. It will update the meta object to 
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};
		
		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog        //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "FusekiLoader.Shell.Title")); 

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
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

	
		// output field value
		Label wlValName = new Label(shell, SWT.RIGHT);
		wlValName.setText(BaseMessages.getString(PKG,
				"FusekiLoader.FieldName.Label"));
		props.setLook(wlValName);
		FormData fdlValName = new FormData();
		fdlValName.left = new FormAttachment(0, 0);
		fdlValName.right = new FormAttachment(middle, -margin);
		 fdlValName.top = new FormAttachment(wStepname, margin);
		//fdlValName.top = new FormAttachment(10, margin);

		wlValName.setLayoutData(fdlValName);

		wHelloFieldName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wHelloFieldName);
		wHelloFieldName.addModifyListener(lsMod);
		FormData fdValName = new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		fdValName.right = new FormAttachment(100, 0);	
		fdValName.top = new FormAttachment(wlStepname, margin+10);
		wHelloFieldName.setLayoutData(fdValName);

		//------------
		wAddUri = new Button(shell, SWT.PUSH);
		wAddUri.setText(BaseMessages.getString(PKG,
				"FusekiLoader.FieldName.AddUri"));    
		
		
		wLoadFile = new Button(shell, SWT.PUSH);
		wLoadFile.setText(BaseMessages.getString(PKG,
				"FusekiLoader.FieldName.LoadFile"));
		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 
		BaseStepDialog.positionBottomButtons(shell, new Button[] {
				this.wAddUri, wLoadFile }, margin,
				wHelloFieldName);
	
		//text to choose output 

		Label wlValNameO = new Label(shell, SWT.RIGHT);
		wlValNameO.setText(BaseMessages.getString(PKG,
				"FusekiLoader.FieldName.LabelOutput"));
		props.setLook(wlValNameO);
		FormData fdlValNameO = new FormData();
		fdlValNameO.left = new FormAttachment(0, 0);
		fdlValNameO.right = new FormAttachment(middle, -margin);
		 fdlValNameO.top = new FormAttachment(wAddUri, margin);
		//fdlValName.top = new FormAttachment(10, margin);

		wlValNameO.setLayoutData(fdlValNameO);

		wChooseOutput = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wChooseOutput);
		//wChooseOutput.addModifyListener(lsMod);
		FormData fdValNameO = new FormData();
		fdValNameO.left = new FormAttachment(middle, 0);
		//fdValNameO.right = new FormAttachment(100, 0);	
		fdValNameO.top = new FormAttachment(wAddUri, margin);
		wChooseOutput.setLayoutData(fdValNameO);
		
		//booton to choose directory 
		wChooseDirectory = new Button(shell, SWT.PUSH | SWT.SINGLE | SWT.MEDIUM | SWT.BORDER);
		props.setLook(wChooseDirectory);

		wChooseDirectory.setText(BaseMessages.getString(PKG,
				"FusekiLoader.Output.ChooseDirectory"));
		wChooseDirectory.setToolTipText(BaseMessages.getString(PKG,
				"System.Tooltip.ChooseDirectory"));
		FormData fdChooseDirectory = new FormData();
		fdChooseDirectory = new FormData();
		fdChooseDirectory.right = new FormAttachment(100, 0);
		fdChooseDirectory.top = new FormAttachment(wAddUri, margin);
		wChooseDirectory.setLayoutData(fdChooseDirectory);

		fdValNameO.right = new FormAttachment(wChooseDirectory, -margin);
		wChooseOutput.setLayoutData(fdValNameO);
		
		//----------------------------
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wChooseDirectory);

		
		// Add listeners for cancel and OK
		lsCancel = new Listener() {
			public void handleEvent(Event e) {cancel();}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {ok();}
		};
		lsReadUri = new Listener() {
			public void handleEvent(Event e) {
				AddUri();
			}
		};
		lsLoadFile = new Listener() {
			public void handleEvent(Event e) {
				LoadFile();
			}
		};
		lsChooseDirectory = new Listener() {
			public void handleEvent(Event e) {
				ChooseDirectory();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
		wLoadFile.addListener(SWT.Selection, lsLoadFile);
		this.wChooseDirectory.addListener(SWT.Selection, lsChooseDirectory);
		this.wAddUri.addListener(SWT.Selection, lsReadUri);
		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {ok();}
		};
		wStepname.addSelectionListener(lsDef);
		wHelloFieldName.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {cancel();}
		});
		
		// Set/Restore the dialog size based on last position on screen
		// The setSize() method is inherited from BaseStepDialog
		setSize();

		// populate the dialog with the values from the meta object
		populateDialog();
		
		// restore the changed flag to original value, as the modify listeners fire during dialog population 
		meta.setChanged(changed);

		// open dialog and enter event loop 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}
	
	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		wHelloFieldName.setText(meta.getOutputField());	
	}

	/**
	 * Called when the user cancels the dialog.  
	 */
	private void cancel() {
		// The "stepname" variable will be the return value for the open() method. 
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
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to step name from the dialog control
		stepname = wStepname.getText(); 
		// Setting the  settings to the meta object
		meta.setOutputField(wHelloFieldName.getText());
		meta.setDirectory(wChooseOutput.getText());
		// close the SWT dialog window
		dispose();
	}
	
	private void AddUri() {
		 
		String data = wHelloFieldName.getText().trim();// read contents of text
		wHelloFieldName.setText(data);// to save without spaces
		if (data.equals("")) {  // si est vacio presente un error y no ejecuta nada

			MessageBox dialog = 
					  new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK);
					dialog.setText(BaseMessages.getString(PKG,"FusekiLoader.FieldName.AddUri"));
					dialog.setMessage(BaseMessages.getString(PKG,
					"FusekiLoader.FieldName.NoUri"));

					// open dialog and await user selection
					int returnCode = dialog.open(); 
			//
		} else {
			Pattern pat = Pattern.compile("^http.*");
			Matcher mat = pat.matcher(wHelloFieldName.getText());
			
		String myresult = ConsultUri(wHelloFieldName.getText().trim());// search
				// in

				if (myresult != null) {
					this.wHelloFieldName.setText(myresult);
					/**
					TableItem item = new TableItem(table, SWT.NONE, numt++);
					item.setText(0, String.valueOf(numt));
					item.setText(1, wHelloFieldName.getText().trim());
					item.setText(2, myresult);
					item.setText(3, BaseMessages.getString(PKG,
							"FusekiLoader.FieldName.mt2"));
					setNameOnto(wHelloFieldName.getText(),numt); //para guardar el nombre y no la busqueda
				*/
				}else { // solo aqui es el error pues si es una noUri como bibo que no se valida
					
					MessageBox dialog = 
							  new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK);
							dialog.setText(BaseMessages.getString(PKG,"FusekiLoader.FieldName.AddUri"));
							dialog.setMessage(BaseMessages.getString(PKG,
							"FusekiLoader.FieldName.NoUri"));

							// open dialog and await user selection
							int returnCode = dialog.open(); 
					

				}
				
				
				

	

		}
	}// fin add URI
	
	
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
						mysearching.trim());

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}

			// System.out.println(var.get(key) === "bibo" );
			// json = JSON.stringify(eval("(" + response.toString() + ")"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return loudScreaming;
	}
	
	private void LoadFile() {

		try {
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			dialog.setText(BaseMessages.getString(PKG, "FusekiLoader.FieldName.Choose"));
			String result = dialog.open();
			/**
			TableItem item = new TableItem(table, SWT.NONE, numt++);
			item.setText(0, String.valueOf(numt));
			item.setText(1, dialog.getFileName());  //nombre del archivo
			item.setText(2, dialog.getFilterPath() + "/" + dialog.getFileName());
			item.setText(3, BaseMessages.getString(PKG, "FusekiLoader.FieldName.mt3"));*/
			wHelloFieldName.setText(dialog.getFilterPath() + "/" + dialog.getFileName());
		} catch (Exception e) {
			log.log(Level.SEVERE, e.toString(), e);
		}

	}
	
	private void ChooseDirectory() {

		try {

			DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
			dialog.setText(BaseMessages.getString(PKG, "FusekiLoader.FieldName.LabelOutput"));
			String result = dialog.open();
			/**
			TableItem item = new TableItem(table, SWT.NONE, numt++);
			item.setText(0, String.valueOf(numt));
			item.setText(1, dialog.getFileName());  //nombre del archivo
			item.setText(2, dialog.getFilterPath() + "/" + dialog.getFileName());
			item.setText(3, BaseMessages.getString(PKG, "FusekiLoader.FieldName.mt3"));*/
			this.wChooseOutput.setText(dialog.getFilterPath());
		} catch (Exception e) {
			log.log(Level.SEVERE, e.toString(), e);
		}

	}
}
