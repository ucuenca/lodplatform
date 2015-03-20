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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.hp.hpl.jena.ontology.OntModelSpec;

import java.awt.Desktop;
import java.net.URI;

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
public class FusekiLoaderDialog extends BaseStepDialog implements
		StepDialogInterface {

	/**
	 * The PKG member is used when looking up internationalized strings. The
	 * properties file with localized keys is expected to reside in {the package
	 * of the class specified}/messages/messages_{locale}.properties
	 */
	private static Class<?> PKG = FusekiLoaderMeta.class; // for i18n purposes
	private final static String USER_AGENT = "Mozilla/5.0";
	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed
	private FusekiLoaderMeta meta;
	private FusekiLoaderData data;

	// text field holding the name of the field to add to the row stream
	private Text wHelloFieldName;
	private Text wChooseOutput;
	private Text wTextServName;
	private Text wTextServPort;
	private Text wTextHowService;

	private Button wLoadFile;
	private Button wChooseDirectory;
	private Button wOpenBrowser;

	private Listener lsReadUri;
	private Listener lsLoadFile;
	private Listener lsChooseDirectory;
	private Listener lsCheckService;
	private Listener lsOpenBrowser;
	private ModifyListener lsUpdateInstrucctions;
	private FormData fdmitabla;
	private Table table;
	 private static String OS = System.getProperty("os.name").toLowerCase();

	private static final Logger log = Logger.getLogger(FusekiLoader.class
			.getName());

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
	public FusekiLoaderDialog(Shell parent, Object in, TransMeta transMeta,
			String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (FusekiLoaderMeta) in;
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
		shell.setText(BaseMessages.getString(PKG, "FusekiLoader.Shell.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname
				.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
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
		// fdlValName.top = new FormAttachment(10, margin);

		wlValName.setLayoutData(fdlValName);

		wHelloFieldName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wHelloFieldName);
		wHelloFieldName.addModifyListener(lsMod);
		FormData fdValName = new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		// fdValName.right = new FormAttachment(100, 0);
		fdValName.top = new FormAttachment(wlStepname, margin + 10);
		wHelloFieldName.setLayoutData(fdValName);
		wHelloFieldName.setEditable(false); // ------------

		wLoadFile = new Button(shell, SWT.PUSH);
		wLoadFile.setText(BaseMessages.getString(PKG,
				"FusekiLoader.FieldName.LoadFile"));

		wLoadFile.setToolTipText(BaseMessages.getString(PKG,
				"System.Tooltip.ChooseFile"));
		FormData fdChooseFile = new FormData();
		fdChooseFile = new FormData();
		fdChooseFile.right = new FormAttachment(100, 0);
		fdChooseFile.top = new FormAttachment(wStepname, margin);
		wLoadFile.setLayoutData(fdChooseFile);

		fdValName.right = new FormAttachment(wLoadFile, 0);
		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		// label to say fuseki parameters
		Label wlabelFuseki = new Label(shell, SWT.RIGHT);
		wlabelFuseki.setText(BaseMessages.getString(PKG,
				"FusekiLoader.label.FusekiParameters"));
		props.setLook(wlabelFuseki);
		FormData fdlValName1 = new FormData();
		fdlValName1.left = new FormAttachment(0, 0);
		fdlValName1.right = new FormAttachment(middle, -margin);
		fdlValName1.top = new FormAttachment(wHelloFieldName, margin + 10);

		wlabelFuseki.setLayoutData(fdlValName1);

		// label to serviceName
		Label wlabelServiceName = new Label(shell, SWT.RIGHT);
		wlabelServiceName.setText(BaseMessages.getString(PKG,
				"FusekiLoader.label.FusekiservName"));
		props.setLook(wlabelServiceName);
		FormData fdlValservName = new FormData();
		fdlValservName.left = new FormAttachment(0, 0);
		fdlValservName.right = new FormAttachment(middle, -margin);
		fdlValservName.top = new FormAttachment(wlabelFuseki, margin + 5);

		wlabelServiceName.setLayoutData(fdlValservName);

		// text para service name
		wTextServName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wTextServName.setText("myservice");
		props.setLook(wTextServName);
		// wStepname.addModifyListener(lsMod);
		FormData fdtextservName = new FormData();
		fdtextservName.left = new FormAttachment(middle, 0);
		fdtextservName.top = new FormAttachment(wlabelFuseki, margin + 5);
		fdtextservName.right = new FormAttachment(100, 0);
		wTextServName.setLayoutData(fdtextservName);

		// label to service Port
		Label wlabelServicePort = new Label(shell, SWT.RIGHT);
		wlabelServicePort.setText(BaseMessages.getString(PKG,
				"FusekiLoader.label.FusekiservPort"));
		props.setLook(wlabelServicePort);
		FormData fdlValservPort = new FormData();
		fdlValservPort.left = new FormAttachment(0, 0);
		fdlValservPort.right = new FormAttachment(middle, -margin);
		fdlValservPort.top = new FormAttachment(wlabelServiceName, margin + 5);

		wlabelServicePort.setLayoutData(fdlValservPort);
		// text para service Port

		wTextServPort = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wTextServPort.setText("3030");
		props.setLook(wTextServPort);
		// wStepname.addModifyListener(lsMod);
		FormData fdtextservPort = new FormData();
		fdtextservPort.left = new FormAttachment(middle, 0);
		fdtextservPort.top = new FormAttachment(wlabelServiceName, margin + 5);
		// fdtextservPort.right = new FormAttachment(100, 0);
		wTextServPort.setLayoutData(fdtextservPort);
		// table to parameters
		// tabla
		table = new Table(shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] titles = { BaseMessages.getString(PKG, "Fuseki.table.col1"),
				BaseMessages.getString(PKG, "Fuseki.table.col2"),

		};
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
			column.setWidth(400);
			if (i == 0) {
				column.setWidth(100);
			}
			column.pack();
		}
		table.setSize(table.computeSize(SWT.DEFAULT, 200));
		// ---------------------------------------------------------
		final TableEditor editor = new TableEditor(table);
		// The editor must have the same size as the cell and must
		// not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
		// editing the second column
		final int EDITABLECOLUMN = 1;

		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Clean up any previous editor control
				Control oldEditor = editor.getEditor();
				if (oldEditor != null)
					oldEditor.dispose();

				// Identify the selected row
				TableItem item = (TableItem) e.item;
				
				if (item == null)
					return;

				// The control that will be the editor must be a child of the
				// Table
				Text newEditor = new Text(table, SWT.NONE);
				newEditor.setText(item.getText(EDITABLECOLUMN));
				newEditor.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent me) {
						Text text = (Text) editor.getEditor();
						editor.getItem()
								.setText(EDITABLECOLUMN, text.getText());
					}
				});
				newEditor.selectAll();
				newEditor.setFocus();
				editor.setEditor(newEditor, item, EDITABLECOLUMN);
			}
		});
		// ----------------------------------------------------------
		
		// table.setItemCount(3);// para ver las filas por defecto
		// parametrizando el forma data
		fdmitabla = new FormData();
		fdmitabla.left = new FormAttachment(1, 0);
		fdmitabla.right = new FormAttachment(100, 1);

		fdmitabla.top = new FormAttachment(wTextServPort, margin);

		// boton ok y cancel al ultimo

		table.setLayoutData(fdmitabla);
		// -------------dar datos
		TableItem item = new TableItem(table, SWT.NONE, 0);
		item.setText(0, "fuseki:dataset");
		item.setText(1, "myds");

		item = new TableItem(table, SWT.NONE, 1);
		item.setText(0, "fuseki:serviceReadGraphStore");
		item.setText(1, "data");
		item = new TableItem(table, SWT.NONE, 2);

		item.setText(0, "fuseki:serviceQuery");
		item.setText(1, "query");
		table.setEnabled(true);

		// ----------------------------------------
		// label to choose output

		Label wlValNameO = new Label(shell, SWT.RIGHT);
		wlValNameO.setText(BaseMessages.getString(PKG,
				"FusekiLoader.FieldName.LabelOutput"));
		props.setLook(wlValNameO);
		FormData fdlValNameO = new FormData();
		fdlValNameO.left = new FormAttachment(middle, 0);
		fdlValNameO.right = new FormAttachment(middle, -margin);
		fdlValNameO.top = new FormAttachment(table, margin + 5);
		// fdlValName.top = new FormAttachment(10, margin);

		wlValNameO.setLayoutData(fdlValNameO);
		// text to output
		wChooseOutput = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wChooseOutput);
		// wChooseOutput.addModifyListener(lsMod);
		FormData fdValNameO = new FormData();
		fdValNameO.left = new FormAttachment(middle, 0);
		// fdValNameO.right = new FormAttachment(100, 0);
		fdValNameO.top = new FormAttachment(table, margin + 5);
		wChooseOutput.setLayoutData(fdValNameO);
		wChooseOutput.setEditable(false);
		// booton to choose directory
		wChooseDirectory = new Button(shell, SWT.PUSH | SWT.SINGLE | SWT.MEDIUM
				| SWT.BORDER);
		props.setLook(wChooseDirectory);

		wChooseDirectory.setText(BaseMessages.getString(PKG,
				"FusekiLoader.Output.ChooseDirectory"));
		wChooseDirectory.setToolTipText(BaseMessages.getString(PKG,
				"System.Tooltip.ChooseDirectory"));
		FormData fdChooseDirectory = new FormData();
		fdChooseDirectory = new FormData();
		fdChooseDirectory.right = new FormAttachment(100, 0);
		fdChooseDirectory.top = new FormAttachment(table, margin + 5);
		wChooseDirectory.setLayoutData(fdChooseDirectory);

		fdValNameO.right = new FormAttachment(wChooseDirectory, -margin);
		wChooseOutput.setLayoutData(fdValNameO);

		// checkbox star service
		Button wCheckService = new Button(shell, SWT.CHECK);
		props.setLook(wCheckService);

		wCheckService.setText(BaseMessages.getString(PKG,
				"FusekiLoader.Check.label"));
		wCheckService.setToolTipText(BaseMessages.getString(PKG,
				"FukekiLoader.Check.tooltip"));
		FormData fdBotonCheck = new FormData();
		fdBotonCheck = new FormData();
		fdBotonCheck.right = new FormAttachment(middle, -margin);
		fdBotonCheck.top = new FormAttachment(wlValNameO, margin + 5);
		// fdBotonCheck.right = new FormAttachment(wOpenBrowser, margin);
		wCheckService.setLayoutData(fdBotonCheck);

		 wOpenBrowser = new Button(shell, SWT.PUSH);
		props.setLook(wOpenBrowser);

		wOpenBrowser.setText(BaseMessages.getString(PKG,
				"FusekiLoader.BotonBrowser"));
		wOpenBrowser.setToolTipText(BaseMessages.getString(PKG,
				"FukekiLoader.BotonBrowser.tooltip"));
		FormData fdBotonBrowser = new FormData();
		fdBotonBrowser = new FormData();
		fdBotonBrowser.left = new FormAttachment(wCheckService, margin);
		fdBotonBrowser.top = new FormAttachment(wlValNameO, margin + 5);
		wOpenBrowser.setLayoutData(fdBotonBrowser);
		wOpenBrowser.setEnabled(false);
		// ------------------------------------ how start service
		// label how to startService
		Label wLabelHowService = new Label(shell, SWT.RIGHT);
		wLabelHowService.setText(BaseMessages.getString(PKG,
				"FusekiLoader.label.HowService"));
		props.setLook(wLabelHowService);
		FormData fdlHowService = new FormData();
		fdlHowService.left = new FormAttachment(0, 0);
		fdlHowService.right = new FormAttachment(middle, -margin);
		fdlHowService.top = new FormAttachment(wCheckService, margin + 5);

		wLabelHowService.setLayoutData(fdlHowService);
		// text para service Port
		wTextHowService = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP
				| SWT.V_SCROLL);
		wTextHowService.setData(new GridData(GridData.FILL_BOTH));
		wTextHowService
				.setText("To start the service, go to the terminal and type ${outputDir}/fuseki-server --port=${servicePort} --config=config.ttl To access the service go to: http://localhost:${servicePort}/control-panel.tpl To perform some queries make a request to http://localhost:3030/${serviceName}/query?query=${your-query}");

		props.setLook(wTextHowService);
		// wStepname.addModifyListener(lsMod);
		FormData fdtextservHow = new FormData();
		fdtextservHow.left = new FormAttachment(middle, 0);
		fdtextservHow.top = new FormAttachment(wCheckService, margin + 10);
		fdtextservHow.right = new FormAttachment(100, 0);
		wTextHowService.setLayoutData(fdtextservHow);

		// ----------------------------
		BaseStepDialog.positionBottomButtons(shell,
				new Button[] { wOK, wCancel }, margin, wTextHowService);

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
		lsChooseDirectory = new Listener() {
			public void handleEvent(Event e) {
				ChooseDirectory();
			}
		};
		lsCheckService = new Listener() {
			public void handleEvent(Event e) {
				StartService();
			}
		};
		lsUpdateInstrucctions = new ModifyListener() {
			public void handleEvent(Event e) {
				UpdateInstrucctions();
			}

			public void modifyText(ModifyEvent arg0) {
				UpdateInstrucctions();

			}
		};
		lsOpenBrowser = new Listener() {
			public void handleEvent(Event e) {
				OpenBrowser();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
		wLoadFile.addListener(SWT.Selection, lsLoadFile);
		wOpenBrowser.addListener(SWT.Selection, lsOpenBrowser);
		wCheckService.addListener(SWT.Selection, lsCheckService);
		this.wChooseDirectory.addListener(SWT.Selection, lsChooseDirectory);
		
		wTextServPort.addListener(SWT.Verify, new Listener() {
		      public void handleEvent(Event e) {
		          String string = e.text;
		          char[] chars = new char[string.length()];
		          string.getChars(0, chars.length, chars, 0);
		          for (int i = 0; i < chars.length; i++) {
		            if (!('0' <= chars[i] && chars[i] <= '9')) {
		              e.doit = false;
		              return;
		            }
		          }
		        }
		      });
		
		VerifyListener verify = new VerifyListener() {
			  public void verifyText(VerifyEvent event) {
			    event.doit = false;
			 
			    if (Character.isDigit(event.character)
			        || Character.isAlphabetic(event.character)
			        || event.keyCode == SWT.ARROW_LEFT
			        || event.keyCode == SWT.ARROW_RIGHT
			        || event.keyCode == SWT.BS
			        || event.keyCode == SWT.DEL
			       ) {

			      event.doit = true;
			    }
			  }
			};
			wTextServName.addVerifyListener(verify);
		
		wTextServName.addModifyListener(lsUpdateInstrucctions);
		wTextServPort.addModifyListener(lsUpdateInstrucctions);
		wChooseOutput.addModifyListener(lsUpdateInstrucctions);

		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};
		wStepname.addSelectionListener(lsDef);
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

	protected void UpdateInstrucctions() {
		TableItem miti = table.getItem(0);
		meta.setFuDataset(miti.getText(1));

		TableItem miti2 = table.getItem(1);
		meta.setFuGraph(miti2.getText(1));

		TableItem miti3 = table.getItem(2);
		meta.setFuQuery(miti3.getText(1));
  String cmd = ";  ./fuseki-server --port=";
  if (isWindows()) { cmd = ";  fuseki-server --port="; }
		wTextHowService
				.setText("To start the service,first run spoon as Administrator, go to the terminal and type:  cd "
						+ wChooseOutput.getText() +"/fuseki"
						+ cmd
						+ wTextServPort.getText()
						+ " --config=config.ttl To access the service go to: http://localhost:"
						+ wTextServPort.getText()
						+ "/control-panel.tpl To perform some queries make a request to http://localhost:"
						+ wTextServPort.getText() + "/"
						+ this.wTextServName.getText() + "/query?query="
						+ meta.getFuQuery() + "");
		
		try {
	          File file = new File("plugins/steps/FusekiLoader/fuseki/README_to_PLAY.TXT");
	          BufferedWriter output = new BufferedWriter(new FileWriter(file));
	          output.write(wTextHowService.getText());
	          output.close();
	        } catch ( IOException e ) {
	           e.printStackTrace();
	        }

	}

	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		wHelloFieldName.setText(meta.getOutputField());
		wChooseOutput.setText(meta.getDirectory());
		wTextServName.setText(meta.getServiceName());
		
		wTextServPort.setText(meta.getPortName());
		table.removeAll();

		TableItem item = new TableItem(table, SWT.NONE, 0);
		item.setText(0, "fuseki:dataset");
		item.setText(1, meta.getFuDataset());

		item = new TableItem(table, SWT.NONE, 1);
		item.setText(0, "fuseki:serviceReadGraphStore");
		item.setText(1, meta.getFuGraph());
		item = new TableItem(table, SWT.NONE, 2);

		item.setText(0, "fuseki:serviceQuery");
		item.setText(1, meta.getFuQuery());
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
		stepname = wStepname.getText();
		// Setting the settings to the meta object
		if (wHelloFieldName.getText().equals("")) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setText("ERROR");
			dialog.setMessage(BaseMessages.getString(PKG,
					"FusekiLoader.input.empty"));
			dialog.open();
		}

		if (wChooseOutput.equals("")) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setText("ERROR");
			dialog.setMessage(BaseMessages.getString(PKG,
					"FusekiLoader.output.empty"));
			dialog.open();
		}
		// close the SWT dialog window
		meta.setOutputField(wHelloFieldName.getText());
		meta.setDirectory(wChooseOutput.getText());

		meta.setServiceName(wTextServName.getText());
		meta.setPortName(wTextServPort.getText());

		TableItem miti = table.getItem(0);
		meta.setFuDataset(miti.getText(1));

		TableItem miti2 = table.getItem(1);
		meta.setFuGraph(miti2.getText(1));

		TableItem miti3 = table.getItem(2);
		meta.setFuQuery(miti3.getText(1));

		dispose();
	}

	private void LoadFile() {

		try {
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			dialog.setText(BaseMessages.getString(PKG,
					"FusekiLoader.FieldName.Choose"));
			String result = dialog.open();
			/**
			 * TableItem item = new TableItem(table, SWT.NONE, numt++);
			 * item.setText(0, String.valueOf(numt)); item.setText(1,
			 * dialog.getFileName()); //nombre del archivo item.setText(2,
			 * dialog.getFilterPath() + "/" + dialog.getFileName());
			 * item.setText(3, BaseMessages.getString(PKG,
			 * "FusekiLoader.FieldName.mt3"));
			 */
			wHelloFieldName.setText(dialog.getFilterPath() + "/"
					+ dialog.getFileName());
			meta.setInputName(dialog.getFileName());
		} catch (Exception e) {
			log.log(Level.SEVERE, e.toString(), e);
		}

	}

	private void ChooseDirectory() {

		try {

			DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
			dialog.setText(BaseMessages.getString(PKG,
					"FusekiLoader.FieldName.LabelOutput"));
			String result = dialog.open();

			this.wChooseOutput.setText(dialog.getFilterPath());
			meta.setDirectory(dialog.getFilterPath());
		} catch (Exception e) {
			log.log(Level.SEVERE, e.toString(), e);
		}

	}

	private void StartService() {
		// comprobar si se ejecuto el plugin
		File fDest = new File(meta.getDirectory() + "/fuseki");
		if (!fDest.exists()) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setText("ERROR");
			dialog.setMessage(BaseMessages.getString(PKG,
					"FusekiLoader.Check.Error"));
			dialog.open();
		} else {
			/**
			System.out.println("cd " + meta.getDirectory() + "/fuseki ; "
					+ "./fuseki-server --port=" + wTextServPort.getText()
					+ " --config=config.ttl");
			
			String command = "cd " + meta.getDirectory() + "/fuseki ; "
					+ "./fuseki-server --port=" + wTextServPort.getText()
					+ " --config=config.ttl";
			*/
			
			String command ="./fuseki-server --port=" + wTextServPort.getText()
					+ " --config=config.ttl )"; 
			
			File dir = new File(meta.getDirectory() + "/fuseki");//path
			
			execute();
			//String output = executeCommand(command,dir);
			System.out.println(command);
			logBasic("ther service is ok, open in browser");
			wOpenBrowser.setEnabled(true);
			
			/**
			 * System.out.println(command); logBasic(output); command =
			 * "./fuseki-server --port=" + wTextServPort.getText() +
			 * " --config=config.ttl";
			 * 
			 * output = executeCommand(command); logBasic(output);
			 * System.out.println(command);
			 */
		}
	}

	private String executeCommand(String command,File dir) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command,null,dir);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

	private void OpenBrowser() {

		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI("http://localhost:3030/"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			openUrlInBrowser("http://localhost:3030/");
			
		}
			
	}

	private void openUrlInBrowser(String url) {    //open in mac 
		Runtime runtime = Runtime.getRuntime();
		String[] args = { "osascript", "-e", "open location \"" + url + "\"" };
		try {
			Process process = runtime.exec(args);
		} catch (IOException e) {
			// do what you want with this
		}
	}
	
	
	public void execute(){
		String command ="./fuseki-server --port=" + wTextServPort.getText()
				+ " --config=config.ttl "; 
		
		if (isWindows()) { System.out.println("Es un Windows");
		
		 command ="cmd fuseki-server --port=" + wTextServPort.getText()
				+ " --config=config.ttl "; 
		}
		
		File dir = new File(meta.getDirectory() + "/fuseki");//path
		
        ExecutorTask task = new ExecutorTask();
        task.setCommand(command);
        task.setDir(dir);
        Thread executorThread = new Thread(task);
        executorThread.start();

        /*try {
            Thread.sleep(1000);
            executorThread.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }
	 public static boolean isWindows() { return (OS.indexOf("win") >= 0); }

}//fin class


class ExecutorTask implements Runnable{
	 protected LogChannelInterface log;
	public String command;
	public File dir;
   public void run() {
	   StringBuffer output = new StringBuffer();
       Process p= null;

       try {
    	   p = Runtime.getRuntime().exec(command,null,dir);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
    	   
       } catch (IOException e) {
           e.printStackTrace();
           logBasic(" ERROR " + e +"The service was not created. Please execute spoon like administrator");
          
       } catch (InterruptedException e) {
    	   logBasic(" ERROR " + e +"The service was not created. Please execute spoon like administrator");
           return;
       }
   }
   
   public void setCommand(String s1){
	   this.command =s1;
	   
   }
   public void setDir(File s2){
	   this.dir =s2;
	   
   }
   public void logBasic( String message ) {
	    log.logBasic( message );
	  }
}