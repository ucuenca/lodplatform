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

package com.ucuenca.pentaho.plugin.step.ontologymapping;

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.h2.jdbc.JdbcSQLException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import com.ucuenca.misctools.DatabaseLoader;
import com.ucuenca.pentaho.plugin.step.r2rml.DataTypeProcessor;
import org.pentaho.di.ui.core.widget.TextVar;

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
public class OntoMapDialog extends BaseStepDialog implements StepDialogInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = OntoMapMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private OntoMapMeta meta;
	
	String[] previousSteps;
	
	StepMeta rootDataStep;
	
	private Label wlStep1;
	  private CCombo wStep1;
	  private FormData fdlStep1, fdStep1;
	
	  private Label wlStep2;
	  private CCombo wStep2;
	  private FormData fdlStep2, fdStep2;
	  
	  private Label wlBaseURI;
	  private TextVar wBaseURI;
	  private FormData fdlBaseURI, fdBaseURI;
	  
	  private Label wlOutputDir;
	  private Text wOutputDir;
	  private FormData fdlOutputDir, fdOutputDir;
	
	  private CTabFolder wTabFolder;
	  private FormData fdTabFolder;
	
	  private CTabItem wClassifyTab, wAnnotateTab, wRelationTab;
	
	  private Composite wClassifyComp, wAnnotateComp, wRelationComp;
	  private FormData fdClassifyComp, fdAnnotateComp, fdRelationComp;
	  
	  private Label wlAnnotate;
	  private TableView wAnnTable;
	  private FormData fdlAnnotate, fdAnnotate;
	
	  private Label wlRelation;
	  private TableView wRelTable;
	  private FormData fdlMeta, fdRelation;
	  
	  private Button wDelClassTable, wDelAnnTable, wDelRelTable, wFindOntStep, wPreview;
	  private FormData fdDelClassTable, fdDelAnnTable, fdDelRelTable, fdFindOntStep, fdPreview;
	
	  private Label wlFields;
	  private TableView wClassTable;
	  private FormData fdlFields, fdFields;
	  
	  private TransMeta transMeta;
	  private Map<String, String[]> dataCache = new HashMap<String, String[]>();

	private Button wbOutputDir;

	private FormData fdbOutputDir;
	  

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
	public OntoMapDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (OntoMapMeta) in;
		this.transMeta = transMeta;
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
		//this.precatchingData();
		Shell parent = getParent();
	    Display display = parent.getDisplay();

	    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
	    props.setLook( shell );
	    setShellImage( shell, meta );

	    ModifyListener lsMod = new ModifyListener() {
	      public void modifyText( ModifyEvent e ) {
	        meta.setChanged();
	      }
	    };
	    changed = meta.hasChanged();

	    FormLayout formLayout = new FormLayout();
	    formLayout.marginWidth = Const.FORM_MARGIN;
	    formLayout.marginHeight = Const.FORM_MARGIN;

	    shell.setLayout( formLayout );
	    shell.setText( BaseMessages.getString( PKG, "OntologyMapping.DialogTitle" ) );

	    int middle = props.getMiddlePct();
	    int margin = Const.MARGIN;

	    // Filename line
	    wlStepname = new Label( shell, SWT.RIGHT );
	    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
	    props.setLook( wlStepname );
	    fdlStepname = new FormData();
	    fdlStepname.left = new FormAttachment( 0, 0 );
	    fdlStepname.right = new FormAttachment( middle, -margin );
	    fdlStepname.top = new FormAttachment( 0, margin );
	    wlStepname.setLayoutData( fdlStepname );
	    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    wStepname.setText( stepname );
	    props.setLook( wStepname );
	    wStepname.addModifyListener( lsMod );
	    fdStepname = new FormData();
	    fdStepname.left = new FormAttachment( middle, 0 );
	    fdStepname.top = new FormAttachment( 0, margin );
	    fdStepname.right = new FormAttachment( 100, 0 );
	    wStepname.setLayoutData( fdStepname );
	    
	    SelectionListener inputStepLs = new SelectionListener() {
			
			public void widgetSelected(SelectionEvent arg0) {
				try {
					getPrevStepsMeta();
				}catch(KettleException e) {
					showErrorMessage(e.getMessage());
				}
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		};
	    
	 // Get the previous steps...
	    //String[] previousSteps = transMeta.getPrevStepNames( stepname );
		this.setStepNames("");
	    
	    // First step
	    wlStep1 = new Label( shell, SWT.RIGHT );
	    wlStep1.setText( BaseMessages.getString( PKG, "OntologyMapping.stepName1.Label" ) );
	    props.setLook( wlStep1 );
	    fdlStep1 = new FormData();
	    fdlStep1.left = new FormAttachment( 0, 0 );
	    fdlStep1.right = new FormAttachment( middle, -margin );
	    fdlStep1.top = new FormAttachment( wStepname, margin );
	    wlStep1.setLayoutData( fdlStep1 );
	    wStep1 = new CCombo( shell, SWT.BORDER );
	    props.setLook( wStep1 );

	    if ( previousSteps != null ) {
	      wStep1.setItems( previousSteps );
	    }

	    wStep1.addModifyListener( lsMod );
	    fdStep1 = new FormData();
	    fdStep1.left = new FormAttachment( middle, 0 );
	    fdStep1.top = new FormAttachment( wStepname, margin );
	    fdStep1.right = new FormAttachment( 80, 0 );
	    wStep1.setLayoutData( fdStep1 );
	    wStep1.addSelectionListener(inputStepLs);
	    
	    wFindOntStep = new Button( shell, SWT.PUSH );
	    wFindOntStep.setText( BaseMessages.getString( PKG, "OntologyMapping.FindOntStep.Button" ) );
	    fdFindOntStep = new FormData();
	    fdFindOntStep.left = new FormAttachment( wStep1, 0 );
	    fdFindOntStep.top = new FormAttachment( wStepname, margin );
	    wFindOntStep.setLayoutData( fdFindOntStep );
	    
	    Listener lsFindOntStep = new Listener() {
	      public void handleEvent( Event e ) {
	    	  wStep1.setText("");
	    	  findOntologyStep(stepMeta);
	    	  if(wStep1.getText().equals("")) 
	    		  new ErrorDialog(shell, BaseMessages.getString(PKG, "OntologyStep.ErrorDialog.Title"), 
	    			  BaseMessages.getString(PKG, "OntologyStep.ErrorDialog.Label"), 
	    			  new KettleException(BaseMessages.getString( PKG, "OntologyMapping.FindOntStep.Exception")));
	      }
	    };
	    wFindOntStep.addListener( SWT.Selection, lsFindOntStep );

	    // Second step
	    wlStep2 = new Label( shell, SWT.RIGHT );
	    wlStep2.setText( BaseMessages.getString( PKG, "OntologyMapping.stepName2.Label" ) );
	    props.setLook( wlStep2 );
	    fdlStep2 = new FormData();
	    fdlStep2.left = new FormAttachment( 0, 0 );
	    fdlStep2.right = new FormAttachment( middle, -margin );
	    fdlStep2.top = new FormAttachment( wStep1, margin );
	    wlStep2.setLayoutData( fdlStep2 );
	    wStep2 = new CCombo( shell, SWT.BORDER );
	    props.setLook( wStep2 );

	    if ( previousSteps != null ) {
	      wStep2.setItems( previousSteps );
	    }

	    wStep2.addModifyListener( lsMod );
	    fdStep2 = new FormData();
	    fdStep2.top = new FormAttachment( wStep1, margin );
	    fdStep2.left = new FormAttachment( middle, 0 );
	    fdStep2.right = new FormAttachment( 80, 0 );
	    wStep2.setLayoutData( fdStep2 );
	    wStep2.addSelectionListener(inputStepLs);
	    
	    wPreview = new Button( shell, SWT.PUSH );
	    wPreview.setText( BaseMessages.getString( PKG, "OntologyMapping.PreviewData.Button" ) );
	    fdPreview = new FormData();
	    fdPreview.left = new FormAttachment( wStep2, 0 );
	    fdPreview.top = new FormAttachment( wStep1, margin );
	    wPreview.setLayoutData( fdPreview );
	    
	    lsPreview = new Listener() {
	      public void handleEvent( Event e ) {
	        dataPreview();
	      }
	    };
	    wPreview.addListener( SWT.Selection, lsPreview );
	    
	    wlBaseURI = new Label( shell, SWT.RIGHT  | SWT.MEDIUM);
	    wlBaseURI.setText( BaseMessages.getString( PKG, "OntologyMapping.BaseURI.Label" ) );
	    props.setLook( wlBaseURI );
	    fdlBaseURI = new FormData();
	    fdlBaseURI.left = new FormAttachment( 0, 0 );
	    fdlBaseURI.right = new FormAttachment( middle, -margin );
	    fdlBaseURI.top = new FormAttachment( wStep2, margin );
	    wlBaseURI.setLayoutData( fdlBaseURI );
	    wBaseURI = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.MEDIUM | SWT.BORDER );
	    wBaseURI.setText( "http://" );
	    props.setLook( wBaseURI );
	    wBaseURI.addModifyListener( lsMod );
	    fdBaseURI = new FormData();
	    fdBaseURI.left = new FormAttachment( middle, 0 );
	    fdBaseURI.top = new FormAttachment( wStep2, margin );
	    fdBaseURI.right = new FormAttachment( 80, 0 );
	    wBaseURI.setLayoutData( fdBaseURI );
	    //wBaseURI.addSelectionListener(inputStepLs);
	    
		wlOutputDir=new Label(shell, SWT.RIGHT | SWT.MEDIUM);
		wlOutputDir.setText( BaseMessages.getString( PKG, "OntologyMapping.OutputDir.Label" ) );
		props.setLook(wlOutputDir);
		fdlOutputDir=new FormData();
		fdlOutputDir.left = new FormAttachment(0, 0);
		fdlOutputDir.right= new FormAttachment(middle, -margin);
		fdlOutputDir.top  = new FormAttachment(wBaseURI, margin);
		wlOutputDir.setLayoutData(fdlOutputDir);		
		wOutputDir = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.MEDIUM | SWT.BORDER);
 		props.setLook(wOutputDir);
 		wOutputDir.addModifyListener(lsMod);
 		wOutputDir.setEditable(Boolean.FALSE);
		fdOutputDir=new FormData();
		fdOutputDir.left = new FormAttachment(middle, 0);
		fdOutputDir.top  = new FormAttachment(wBaseURI, margin);
		fdOutputDir.right= new FormAttachment(80, 0);
		wOutputDir.setLayoutData(fdOutputDir);
		
		wbOutputDir=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbOutputDir);
		wbOutputDir.setText( BaseMessages.getString( PKG, "OntologyMapping.OutputDir.Button" ) );
		fdbOutputDir=new FormData();
		//fdbOutputDir.left= new FormAttachment(100, margin);
		fdbOutputDir.left= new FormAttachment(wOutputDir, 0);
		fdbOutputDir.top  = new FormAttachment(wBaseURI, margin);
		wbOutputDir.setLayoutData(fdbOutputDir);

	    // The folders!
	    wTabFolder = new CTabFolder( shell, SWT.BORDER );
	    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
	    
	    fdTabFolder = new FormData();
	    fdTabFolder.left = new FormAttachment( 0, 0 );
	    fdTabFolder.top = new FormAttachment( wOutputDir, margin );
	    fdTabFolder.right = new FormAttachment( 100, 0 );
	    fdTabFolder.bottom = new FormAttachment( 100, -50 );
	    wTabFolder.setLayoutData( fdTabFolder );

	    // ////////////////////////
	    // START OF Classification TAB ///
	    // ////////////////////////

	    wClassifyTab = new CTabItem( wTabFolder, SWT.NONE );
	    wClassifyTab.setText( BaseMessages.getString(PKG, "OntologyMapping.Tab.Classification.Label") );

	    wClassifyComp = new Composite( wTabFolder, SWT.NONE );
	    props.setLook( wClassifyComp );

	    FormLayout selectLayout = new FormLayout();
	    selectLayout.marginWidth = margin;
	    selectLayout.marginHeight = margin;
	    wClassifyComp.setLayout( selectLayout );


	    wlFields = new Label( wClassifyComp, SWT.NONE );
	    wlFields.setText( BaseMessages.getString( PKG, "OntologyMapping.Classification.Table.Label" ) );
	    props.setLook( wlFields );
	    fdlFields = new FormData();
	    fdlFields.left = new FormAttachment( 0, 0 );
	    fdlFields.top = new FormAttachment( 0, 0 );
	    wlFields.setLayoutData( fdlFields );
	    
	    wDelClassTable = new Button(wClassifyComp, SWT.PUSH | SWT.SINGLE | SWT.MEDIUM | SWT.BORDER);
		props.setLook(wDelClassTable);
		wDelClassTable.setText(BaseMessages.getString(PKG,
				"OntologyMapping.Tab.Table.Button.Delete"));
		wDelClassTable.setToolTipText(BaseMessages.getString(PKG,
				"System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdDelClassTable = new FormData();
		fdDelClassTable.right = new FormAttachment(100, 0);
		fdDelClassTable.top = new FormAttachment(0, 0);
		wDelClassTable.setLayoutData(fdDelClassTable);

		wDelClassTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteTableRecords(OntoMapData.CLASSIFICATIONTABLE, wClassTable);
				
			}
		});

	    ColumnInfo[] colinf = new ColumnInfo[] {
  	      new ColumnInfo("ID", ColumnInfo.COLUMN_TYPE_TEXT, false ),
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Ontology.Name" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
	        new String[]{"biboEx", "foafEX"}),
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Ontology.Entity" ), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta
	          .getTypes() ),
	      
		  new ColumnInfo(
		        BaseMessages.getString( PKG, "OntologyMapping.Classification.RelativeURI.Label" ), ColumnInfo.COLUMN_TYPE_TEXT),
		  new ColumnInfo(
				BaseMessages.getString( PKG, "OntologyMapping.Classification.URIFieldID.Label" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
			new String[]{"field1", "field2"}),
	          
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Data.Field" ) + " 1", ColumnInfo.COLUMN_TYPE_CCOMBO, 
	        new String[]{"field1", "field2"}),
  	      new ColumnInfo(
  	        BaseMessages.getString( PKG, "OntologyMapping.Value.Field" ) + " 1", ColumnInfo.COLUMN_TYPE_CCOMBO, 
  	        new String[]{"value1", "value2"}),
  	      new ColumnInfo(
  	        BaseMessages.getString( PKG, "OntologyMapping.Data.Field" ) + " 2", ColumnInfo.COLUMN_TYPE_CCOMBO, 
  	        new String[]{"field1", "field2"}),
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Value.Field" ) + " 2", ColumnInfo.COLUMN_TYPE_CCOMBO, 
	        new String[]{"value1", "value2"}),
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Data.Field" ) + " 3", ColumnInfo.COLUMN_TYPE_CCOMBO, 
	        new String[]{"field1", "field2"}),
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Value.Field" ) + " 3", ColumnInfo.COLUMN_TYPE_CCOMBO, 
	        new String[]{"value1", "value2"}) };
	    colinf[0].setReadOnly(Boolean.TRUE);
	    
	    colinf[1].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				tableItem.setText(new String[]{String.valueOf(rowNr+1), String.format("C%03d", rowNr+1)});
				return getOntologiesList();
			}
		});
	    colinf[2].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				return getOntologyProperties(tableItem, true, colNr-1);
			}
		});
	    //Data FieldName
	    ComboValuesSelectionListener cmbFieldsLs = new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				int[] filters = colNr == 6 ? new int[]{8,10}:(colNr == 8 ? new int[]{6,10}:new int[]{6,8});
				
				String stepName = wStep2.getText();
				String [] values = new String[]{"No fields found"};
				StepMeta stemStep = getRootStep( transMeta.findStep(stepName) );
				if(stemStep !=  null) {
					stepName = stemStep.getName();
					try {
						List<String> filter= new ArrayList<String>();
						if(!StringUtils.isEmpty(tableItem.getText(filters[0]) )) filter.add(tableItem.getText(filters[0]));
						if(!StringUtils.isEmpty(tableItem.getText(filters[1]) )) filter.add(tableItem.getText(filters[1]));
						values = getStepFieldsMeta(stepName, filter.toArray());
					}catch(KettleException e) {
						logError(e.getMessage(), e);
					}
				}
				return values;
			}
		};
	    colinf[4].setComboValuesSelectionListener(cmbFieldsLs);
	    colinf[5].setComboValuesSelectionListener(cmbFieldsLs);
	    colinf[7].setComboValuesSelectionListener(cmbFieldsLs);
	    colinf[9].setComboValuesSelectionListener(cmbFieldsLs);
	    
	    //Data Field Value
	    ComboValuesSelectionListener cmbValuesLs = new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				List<String> result = new ArrayList<String>();
				String [] rsField = new String[]{"No values found"};
				String fieldName = tableItem.getText(colNr-1).toUpperCase().replaceAll(" ", "_");
				String stepName = wStep2.getText();
				stepName = getRootStep( transMeta.findStep(stepName) ).getName();
				if(dataCache.containsKey(fieldName)) {
					rsField = dataCache.get(fieldName);
				} else {
					String sqlQuery = "SELECT DISTINCT " + fieldName 
							+ " FROM " + meta.getDataDbTable() + " WHERE TRANSID ='" + transMeta.getName().toUpperCase() 
							+ "' AND STEPID='" + stepName.toUpperCase() + "'";
					try {
						DatabaseLoader.getConnection();
						ResultSet rs = DatabaseLoader.executeQuery(sqlQuery);
						while(rs.next()) result.add(rs.getString(1));
						rsField = result.toArray(new String[result.size()]);
						DatabaseLoader.closeConnection();
						dataCache.put(fieldName, rsField);
					}catch(Exception e) {
						logError(e.getMessage(), e);
					}
				}
				
				return rsField;
			}
		};
	    colinf[6].setComboValuesSelectionListener(cmbValuesLs);
	    colinf[8].setComboValuesSelectionListener(cmbValuesLs);
	    colinf[10].setComboValuesSelectionListener(cmbValuesLs);
	    
	    wClassTable =
	      new TableView(
	        transMeta, wClassifyComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 0, lsMod, props );
	    
	    fdFields = new FormData();
	    fdFields.left = new FormAttachment( 0, 0 );
	    fdFields.top = new FormAttachment( wDelClassTable, margin );
	    fdFields.right = new FormAttachment( 100, 0 );
	    fdFields.bottom = new FormAttachment( 100, 0 );
	    wClassTable.setLayoutData( fdFields );
	    
	    fdClassifyComp = new FormData();
	    fdClassifyComp.left = new FormAttachment( 0, 0 );
	    fdClassifyComp.top = new FormAttachment( 0, 0 );
	    fdClassifyComp.right = new FormAttachment( 100, 0 );
	    fdClassifyComp.bottom = new FormAttachment( 100, 0 );
	    wClassifyComp.setLayoutData( fdClassifyComp );

	    wClassifyComp.layout();
	    wClassifyTab.setControl( wClassifyComp );
	    
		// ///////////////////////////////////////////////////////////
	    // END OF Classification TAB
	    // ///////////////////////////////////////////////////////////

	    // ///////////////////////////////////////////////////////////
	    // START OF Annotation TAB
	    // ///////////////////////////////////////////////////////////
	    wAnnotateTab = new CTabItem( wTabFolder, SWT.NONE );
	    wAnnotateTab.setText( BaseMessages.getString(PKG, "OntologyMapping.Tab.Annotation.Label") );

	    FormLayout contentLayout = new FormLayout();
	    contentLayout.marginWidth = margin;
	    contentLayout.marginHeight = margin;

	    wAnnotateComp = new Composite( wTabFolder, SWT.NONE );
	    props.setLook( wAnnotateComp );
	    wAnnotateComp.setLayout( contentLayout );

	    wlAnnotate = new Label( wAnnotateComp, SWT.NONE );
	    wlAnnotate.setText( BaseMessages.getString( PKG, "OntologyMapping.Annotation.Table.Label" ) );
	    props.setLook( wlAnnotate );
	    fdlAnnotate = new FormData();
	    fdlAnnotate.left = new FormAttachment( 0, 0 );
	    fdlAnnotate.top = new FormAttachment( 0, 0 );
	    wlAnnotate.setLayoutData( fdlAnnotate );
	    
	    wDelAnnTable = new Button(wAnnotateComp, SWT.PUSH | SWT.SINGLE | SWT.MEDIUM | SWT.BORDER);
		props.setLook(wDelAnnTable);
		wDelAnnTable.setText(BaseMessages.getString(PKG,
				"OntologyMapping.Tab.Table.Button.Delete"));
		fdDelAnnTable = new FormData();
		fdDelAnnTable.right = new FormAttachment(100, 0);
		fdDelAnnTable.top = new FormAttachment(0, 0);
		wDelAnnTable.setLayoutData(fdDelAnnTable);

		wDelAnnTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteTableRecords(OntoMapData.ANNOTATIONTABLE, wAnnTable);
			}
		});

	    ColumnInfo[] colann = new ColumnInfo[] {
	      new ColumnInfo("ID", ColumnInfo.COLUMN_TYPE_TEXT, false ),
	      new ColumnInfo(
	    	        BaseMessages.getString( PKG, "OntologyMapping.Classification.ID" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
	    	        new String[]{"biboEx", "foafEX"}),
  	      new ColumnInfo(
  	        BaseMessages.getString( PKG, "OntologyMapping.Ontology.Name" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
  	        new String[]{"biboEx", "foafEX"}),
  	      new ColumnInfo(
  	        BaseMessages.getString( PKG, "OntologyMapping.Ontology.Property" ), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta
  	          .getTypes() ),
  	      new ColumnInfo(
  	        BaseMessages.getString( PKG, "OntologyMapping.Value.Data.Extraction" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
  	        new String[]{"value1", "value2"}),
  	      new ColumnInfo(
  	        BaseMessages.getString( PKG, "OntologyMapping.Data.Field" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
  	        new String[]{"field1", "field2"}),	    
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Value.Field" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
	        new String[]{"value1", "value2"}),
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Field.Datatype" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
	        DataTypeProcessor.getDataTypes()),
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Field.Language" ), ColumnInfo.COLUMN_TYPE_TEXT, //Cambiar a texto
                //BaseMessages.getString( PKG, "OntologyMapping.Field.Language" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
	        new String[]{"es-es", "en-us"})};
	    
	    colann[0].setReadOnly(Boolean.TRUE);
	    colann[1].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				
				String [] classificationID = getClassificationIDS();
				if(classificationID.length > 0) {
					tableItem.setText(new String[]{String.valueOf(rowNr+1), String.format("A%03d", rowNr+1)});
				}
				return classificationID;
			}
		});
	    
	    colann[2].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				return getOntologiesList();
			}
		});
	    
    	colann[3].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				return getOntologyProperties(tableItem, false, colNr-1);
			}
		});
	    
	    colann[4].setComboValuesSelectionListener(cmbFieldsLs);
	    colann[5].setComboValuesSelectionListener(cmbFieldsLs);
	    colann[6].setComboValuesSelectionListener(cmbValuesLs);
	    
	    wAnnTable =
	      new TableView(
	        transMeta, wAnnotateComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colann, 0, lsMod, props );	    
	    
	    fdAnnotate = new FormData();
	    fdAnnotate.left = new FormAttachment( 0, 0 );
	    fdAnnotate.top = new FormAttachment( wDelAnnTable, margin );
	    //fdAnnotate.right = new FormAttachment( wGetRemove, -margin );
	    fdAnnotate.right = new FormAttachment( 100, 0 );
	    fdAnnotate.bottom = new FormAttachment( 100, 0 );
	    wAnnTable.setLayoutData( fdAnnotate );

	    fdAnnotateComp = new FormData();
	    fdAnnotateComp.left = new FormAttachment( 0, 0 );
	    fdAnnotateComp.top = new FormAttachment( 0, 0 );
	    fdAnnotateComp.right = new FormAttachment( 100, 0 );
	    fdAnnotateComp.bottom = new FormAttachment( 100, 0 );
	    wAnnotateComp.setLayoutData( fdAnnotateComp );

	    wAnnotateComp.layout();
	    wAnnotateTab.setControl( wAnnotateComp );

	    // ///////////////////////////////////////////////////////////
	    // / END OF Annotation TAB
	    // ///////////////////////////////////////////////////////////

	    // ////////////////////////
	    // START OF Relation TAB ///
	    // ////////////////////////

	    wRelationTab = new CTabItem( wTabFolder, SWT.NONE );
	    wRelationTab.setText( BaseMessages.getString(PKG, "OntologyMapping.Tab.Relation.Label") );

	    wRelationComp = new Composite( wTabFolder, SWT.NONE );
	    props.setLook( wRelationComp );

	    FormLayout metaLayout = new FormLayout();
	    metaLayout.marginWidth = margin;
	    metaLayout.marginHeight = margin;
	    wRelationComp.setLayout( metaLayout );

	    wlRelation = new Label( wRelationComp, SWT.NONE );
	    wlRelation.setText( BaseMessages.getString( PKG, "OntologyMapping.Relation.Table.Label" ) );
	    props.setLook( wlRelation );
	    fdlMeta = new FormData();
	    fdlMeta.left = new FormAttachment( 0, 0 );
	    fdlMeta.top = new FormAttachment( 0, 0 );
	    wlRelation.setLayoutData( fdlMeta );
	    
	    wDelRelTable = new Button(wRelationComp, SWT.PUSH | SWT.SINGLE | SWT.MEDIUM | SWT.BORDER);
		props.setLook(wDelRelTable);
		wDelRelTable.setText(BaseMessages.getString(PKG,
				"OntologyMapping.Tab.Table.Button.Delete"));
		fdDelRelTable = new FormData();
		fdDelRelTable.right = new FormAttachment(100, 0);
		fdDelRelTable.top = new FormAttachment(0, 0);
		wDelRelTable.setLayoutData(fdDelRelTable);

		wDelRelTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteTableRecords(OntoMapData.RELATIONTABLE, wRelTable);
			}
		});

	    ColumnInfo[] colrel =
	      new ColumnInfo[] {
	    	new ColumnInfo("ID", ColumnInfo.COLUMN_TYPE_TEXT, false ),
	        new ColumnInfo(
	        		BaseMessages.getString( PKG, "OntologyMapping.Classification.ID" ) + " 1",
	          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(
	            PKG, "SelectValuesDialog.ColumnInfo.Loading" ) }, false ),
            new ColumnInfo(
            		BaseMessages.getString( PKG, "OntologyMapping.Ontology.Name" ),
            		ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getAllTypes(), false ),
            new ColumnInfo(
      	          BaseMessages.getString( PKG, "OntologyMapping.Ontology.Property" ),
      	          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getAllTypes(), false ),
	        new ColumnInfo(
	        		BaseMessages.getString( PKG, "OntologyMapping.Classification.ID" ) + " 2",
	          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getAllTypes(), false ) };
	    //colmeta[5].setToolTip( BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Storage.Tooltip" ) );
	    
	    colrel[1].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				
				String [] classificationID = getClassificationIDS();
				if(classificationID.length > 0) {
					tableItem.setText(new String[]{String.valueOf(rowNr+1), String.format("R%03d", rowNr+1)});
				}
				return classificationID;
			}
		});
	    
	    colrel[2].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				return getOntologiesList();
			}
		});
	    
    	colrel[3].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				return getOntologyProperties(tableItem, false, colNr-1);
			}
		});
	    
	    colrel[4].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
			
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				return getClassificationIDS();
			}
		});
	    
	    wRelTable =
	      new TableView(
	        transMeta, wRelationComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colrel, 0, lsMod, props );

	    fdRelation = new FormData();
	    fdRelation.left = new FormAttachment( 0, 0 );
	    fdRelation.top = new FormAttachment( wDelRelTable, margin );
	    //fdRelation.right = new FormAttachment( wGetMeta, -margin );
	    fdRelation.right = new FormAttachment( 100, 0 );
	    fdRelation.bottom = new FormAttachment( 100, 0 );
	    wRelTable.setLayoutData( fdRelation );

	    fdRelationComp = new FormData();
	    fdRelationComp.left = new FormAttachment( 0, 0 );
	    fdRelationComp.top = new FormAttachment( 0, 0 );
	    fdRelationComp.right = new FormAttachment( 100, 0 );
	    fdRelationComp.bottom = new FormAttachment( 100, 0 );
	    wRelationComp.setLayoutData( fdRelationComp );

	    wRelationComp.layout();
	    wRelationTab.setControl( wRelationComp );

	    // ///////////////////////////////////////////////////////////
	    // / END OF Relation TAB
	    // ///////////////////////////////////////////////////////////
	    
	    wTabFolder.setSelection( 0 );
	    
	    //--------------

	    wOK = new Button( shell, SWT.PUSH );
	    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
	    wCancel = new Button( shell, SWT.PUSH );
	    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

	    setButtonPositions( new Button[] { wOK, wCancel }, margin, wTabFolder );

	    // Add listeners
	    //BROWSER
  		wbOutputDir.addSelectionListener(
  			new SelectionAdapter() {
  				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wOutputDir.getText()!=null && wOutputDir.getText().length() > 0) {
						dialog.setFilterPath(wOutputDir.getText());
					}	
					if (dialog.open()!=null) {
						String str = dialog.getFilterPath();
						wOutputDir.setText(str);
					}	
  				}
  			}
  		);
	    
	    lsOK = new Listener() {
	      public void handleEvent( Event e ) {
	        ok();
	      }
	    };
	    lsCancel = new Listener() {
	      public void handleEvent( Event e ) {
	        cancel();
	      }
	    };

	    wOK.addListener( SWT.Selection, lsOK );
	    wCancel.addListener( SWT.Selection, lsCancel );

	    lsDef = new SelectionAdapter() {
	      public void widgetDefaultSelected( SelectionEvent e ) {
	        ok();
	      }
	    };

	    wStepname.addSelectionListener( lsDef );

	    // Detect X or ALT-F4 or something that kills this window...
	    shell.addShellListener( new ShellAdapter() {
	      public void shellClosed( ShellEvent e ) {
	        cancel();
	      }
	    } );

	    lsResize = new Listener() {
	      public void handleEvent( Event event ) {
	        Point size = shell.getSize();
	        wClassTable.setSize( size.x - 10, size.y - 50 );
	        wClassTable.table.setSize( size.x - 10, size.y - 50 );
	        wClassTable.redraw();
	      }
	    };
	    shell.addListener( SWT.Resize, lsResize );

	    // Set the shell size, based upon previous time...
	    super.setSize();
	    this.getData();
	    this.getMappingTableData(stepname);
	    meta.setChanged( changed );

		wTabFolder.setSelection(1);
		wTabFolder.setSelection(2);
		wTabFolder.setSelection(0);
	    shell.open();
	    while ( !shell.isDisposed() ) {
	      if ( !display.readAndDispatch() ) {
	        display.sleep();
	      }
	    }
	    return stepname;
	}
	
	/**
	 * Saves Previous Steps Required Metadata into the MetaObject
	 * 
	 */
	private void getPrevStepsMeta()throws KettleException {
		String ontologyStepName = wStep1.getText();
		meta.setOntologyDbTable(this.stepDBTableLookup(transMeta.findStep(ontologyStepName), false, "setOntologyStepName"));
		//meta.setOntologyStepName(ontologyStepName);		
		String dataStepName = wStep2.getText();		
		meta.setDataDbTable(this.stepDBTableLookup(transMeta.findStep(dataStepName), true, "setDataStepName"));
		//meta.setDataStepName(dataStepName);
		String baseURI = wBaseURI.getText();
		//baseURI Validation not implemented yet
		if( !baseURI.matches("^http://.*(/|#)$") ) 
			throw new KettleException( BaseMessages.getString(PKG, "OntologyMapping.exception.baseURI") );
		meta.setMapBaseURI(baseURI);
		meta.setOutputDir(wOutputDir.getText());
		meta.setOutFileName(stepname + "-R2RML.ttl");
	}
	
	/**
	 * Browse DB Table name on Previous Steps
	 * @param stepMeta Data step Meta
	 * @param lookBackward Boolean.TRUE if the process has to browse across every hop 
	 * @param stepNameSetter Name of the method in charge to set the data source step Name 
	 * @return table name for associated step
	 * @throws KettleException
	 */
	private String stepDBTableLookup(StepMeta stepMeta, Boolean lookBackward, String stepNameSetter)throws KettleException {
		String tableName = this.lookupGetterMethod(stepMeta.getName(), stepMeta.getStepMetaInterface().getStepData());
		tableName = tableName == null && lookBackward ? 
				this.getDBTableNameFromPreviousSteps(stepMeta, stepNameSetter):tableName;
		if(tableName == null) {
			throw new KettleException("NO 'DBTABLE' FIELD FOUND FROM " + stepMeta.getParentTransMeta().getName() + " STEP");
		} else {
			this.setMetaValueByMethodName(stepNameSetter, String.class, stepMeta.getName());
		}
		return tableName;
	}
	
	/**
	 * Lookup for the DB table name across the hop grid 
	 * @param stepMeta base step meta
	 * @param stepNameSetterMethod Name of the method in charge to set the data source step Name
	 * @return table name
	 * @throws KettleException
	 */
	private String getDBTableNameFromPreviousSteps(StepMeta stepMeta, String stepNameSetterMethod) throws KettleException{
		String tableName = null;
		for(StepMeta step: stepMeta.getParentTransMeta().findPreviousSteps(stepMeta)) { 
			tableName = this.lookupGetterMethod(step.getName(), step.getStepMetaInterface().getStepData());
			if(tableName != null) {
				if(stepNameSetterMethod != null) {
					this.setMetaValueByMethodName(stepNameSetterMethod, String.class, step.getName());
				}
				logBasic("DBTABLE FIELD FOUND ON " + step.getName() + " STEP DATA CLASS. VALUE ==> " + tableName);
				break;
			}
			if(step.getParentTransMeta().findPreviousSteps(step).size() > 0) {
				tableName = this.getDBTableNameFromPreviousSteps(step, stepNameSetterMethod);
				if(tableName != null) break;
			}
		}
		return tableName;
	}
	
	private void setMetaValueByMethodName(String stepNameSetterMethod, Class pType, Object pValue)throws KettleException{
		try {
			meta.getClass().getMethod(stepNameSetterMethod, pType).invoke(meta, pValue);
		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	/**
	 * Method in charge to lookup and execute the step getter method for DB table name
	 * @param stepName Name of step involved
	 * @param stepData step data interface
	 * @return table name
	 */
	private String lookupGetterMethod(String stepName, StepDataInterface stepData) {
		String value = null;
		try {
			value = (String)stepData.getClass().getField("DBTABLE").get(stepData);
		}catch(NoSuchFieldException ne) {
			logDebug("NO 'DBTABLE' FIELD FOUND ON " + stepName + " STEP DATA CLASS");
		}catch(SecurityException se) {
			logDebug("NO 'DBTABLE' PUBLIC FIELD FOUND ON " + stepName + " STEP DATA CLASS");
		}catch(IllegalAccessException ae) {
			logDebug(ae.getMessage());
		}
		return value;
	}
	
	/**
	 * Gets the Output Fields of a previous step
	 * @param stepName
	 * @param filter
	 * @return
	 * @throws KettleException
	 */
	private String[] getStepFieldsMeta(String stepName, Object... filter) throws KettleException{
		filter = filter != null ? filter:new String[]{};
		String [] fieldNames;
		if(stepName != null && stepName.length() > 0) {
			fieldNames = transMeta.getStepFields(stepName).getFieldNames();
		} else {
			throw new KettleException("No Data step defined");
		}
		Object [] fields = fieldNames;
		for(Object field:filter) {
			fields = ArrayUtils.removeElement(fields, field);
		}
		return Arrays.asList(fields).toArray(new String[fields.length]);
	}
	
	/**
	 * Gets  record IDs of the Classification Table
	 * @return
	 */
	private String[] getClassificationIDS() {
		String [] classificationID = wClassTable.getItems(0);
		List<String> idList = new ArrayList<String>();
		for(String id: classificationID) {
			if(!id.equals("")) idList.add(id);
		}
		classificationID = idList.toArray(new String[idList.size()]);
		return classificationID.length > 0 ? classificationID:new String[]{"No Items found"};
	}
	
	/**
	 * Query ontologies available for mapping
	 * @return
	 */
	private String[] getOntologiesList() {
		String [] rsField = new String[]{"No values found"};
		try {
			String[] fieldNames = transMeta.getStepFields(meta.getOntologyStepName()).getFieldNames();
			String fieldName = fieldNames[0].toUpperCase().replaceAll(" ", "_");
			if(dataCache.containsKey(fieldName)) {
				rsField = dataCache.get(fieldName);
			} else {
				List<String> result = new ArrayList<String>();
				String sqlQuery = "SELECT DISTINCT " + fieldName + " FROM "
						+ meta.getOntologyDbTable() + " WHERE " 
						+ fieldNames[fieldNames.length-1].toUpperCase().replaceAll(" ", "_")
						+ " = 'rdfs:class'"
						+ " AND TRANSID ='" + transMeta.getName().toUpperCase() + "'"
						+ " AND STEPID='" + meta.getOntologyStepName().toUpperCase() + "'";
				DatabaseLoader.getConnection();
				ResultSet rs = DatabaseLoader.executeQuery(sqlQuery);
				while(rs.next()) result.add(rs.getString(1));
				rsField = result.size() > 0 ? result.toArray(new String[result.size()]):rsField;
				DatabaseLoader.closeConnection();
				dataCache.put(fieldName, rsField);
			}
			
		}catch(Exception e) {
			logError(e.getMessage(), e);
		}
		return rsField;
	}
	
	/**
	 * Query Entity properties
	 * @param tableItem 
	 * @param isClass
	 * @param columnFilter
	 * @return
	 */
	private String[] getOntologyProperties(TableItem tableItem, Boolean isClass, Integer columnFilter) {
		List<String> result = new ArrayList<String>();
		String [] rsField = new String[]{"No values found"};
		try {
			String[] fieldNames = transMeta.getStepFields(meta.getOntologyStepName()).getFieldNames();
			String fieldName = fieldNames[1].toUpperCase().replaceAll(" ", "_");
			String sqlQuery = "SELECT " + fieldName + " FROM "
					+ meta.getOntologyDbTable() + " WHERE " 
					+ fieldNames[fieldNames.length-1].toUpperCase().replaceAll(" ", "_")
					+ (isClass ? " = 'rdfs:class'":" = 'rdfs:property'")
					+ " AND " + fieldNames[0].toUpperCase().replaceAll(" ", "_")
					+ " = '" + tableItem.getText(columnFilter) + "' "
					+ " AND TRANSID ='" + transMeta.getName().toUpperCase() + "'"
					+ " AND STEPID='" + meta.getOntologyStepName().toUpperCase() + "'";
			DatabaseLoader.getConnection();
			ResultSet rs = DatabaseLoader.executeQuery(sqlQuery);
			while(rs.next()) result.add(rs.getString(1));
			rsField = result.size() > 0 ? result.toArray(new String[result.size()]):rsField;
			DatabaseLoader.closeConnection();
			dataCache.put(fieldName, rsField);
		}catch(Exception e) {
			logError(e.getMessage(), e);
		}
		return rsField;
	}
	
	/**
	 * Query saved Mapping Table Data
	 * @param stepname
	 */
	private void getMappingTableData(String stepname) {
		OntoMapData data = ((OntoMapData)meta.getStepData());
		data.setTransName(transMeta.getName());
		data.setStepName(stepname);
		try {
			DatabaseLoader.getConnection();
			int rowCount = this.queryMappingRules(data);
			if(rowCount != meta.getSqlStack().size() && meta.getSqlStack().get(0) != null) {
				for(String sqlInsert:meta.getSqlStack()) {
					logBasic( BaseMessages.getString( PKG, "OntologyMapping.log.basic.rules.meta.Insert" ) );
					sqlInsert = sqlInsert.replaceAll("\\{0\\}", "'" + data.getTransName().toUpperCase() + "'");
					sqlInsert = sqlInsert.replaceAll("\\{1\\}", "'" + data.getStepName().toUpperCase() + "'");
					DatabaseLoader.executeUpdate(sqlInsert);
				}
				this.queryMappingRules(data);
			}
			DatabaseLoader.closeConnection();
		}catch(Exception e){
			e.printStackTrace();
			
		}
	}
	
	/**
	 * Query mapping rules from Schema
	 * @param data Step data interface
	 * @return total number of queried rows
	 * @throws Exception
	 */
	private int queryMappingRules(OntoMapData data) throws Exception {
		int rowCount = 0;
		try {
			logBasic( BaseMessages.getString( PKG, "OntologyMapping.log.basic.rules.Query" ) );
			rowCount += data.queryTable(wClassTable, OntoMapData.CLASSIFICATIONTABLE);
			rowCount += data.queryTable(wAnnTable, OntoMapData.ANNOTATIONTABLE);
			rowCount += data.queryTable(wRelTable, OntoMapData.RELATIONTABLE);
		}catch(JdbcSQLException e) {
			logDebug(e.getMessage());
			data.createDBTable(wClassTable, OntoMapData.CLASSIFICATIONTABLE);
			data.createDBTable(wAnnTable, OntoMapData.ANNOTATIONTABLE);
			data.createDBTable(wRelTable, OntoMapData.RELATIONTABLE);
		}
		return rowCount;
	}
	
	/**
	 * Implementation
	 * Copy information from the meta-data input to the dialog fields.	
	 */
	public void getData() {
		
		if(transMeta.findStep(meta.getOntologyStepName()) != null) {
			wStep1.setText( Const.NVL( meta.getOntologyStepName(), "" ) );
		}
		if(transMeta.findStep(meta.getDataStepName()) != null) {
			wStep2.setText( Const.NVL( meta.getDataStepName(), "" ) );
		}
		wBaseURI.setText( Const.NVL( meta.getMapBaseURI(), "http://" ) );
		wOutputDir.setText( Const.NVL( meta.getOutputDir(), "" ) );
	
	    wStepname.selectAll();
	    wStepname.setFocus();
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
		Boolean error = Boolean.FALSE;
		if ( Const.isEmpty( wStepname.getText() ) ) {
	      return;
	    }
		stepname = wStepname.getText(); // return value
		if(meta.hasChanged()) {
			try{
				this.getPrevStepsMeta();
				List<String> sqlStack = new ArrayList<String>();
				OntoMapData data = ((OntoMapData)meta.getStepData());
				data.setTransName(transMeta.getName());
				data.setStepName(stepname);
				DatabaseLoader.getConnection();
				sqlStack.addAll( data.saveTable(wClassTable, OntoMapData.CLASSIFICATIONTABLE) );
				sqlStack.addAll( data.saveTable(wAnnTable, OntoMapData.ANNOTATIONTABLE) );
				sqlStack.addAll( data.saveTable(wRelTable, OntoMapData.RELATIONTABLE) );
				DatabaseLoader.closeConnection();
				meta.setSqlStack(sqlStack);
				List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
				
			    infoStreams.get( 0 ).setStepMeta( transMeta.findStep( wStep1.getText() ) );
			    infoStreams.get( 1 ).setStepMeta( transMeta.findStep( wStep2.getText() ) );
			}catch(Exception e) {
				error = Boolean.TRUE;
				this.showErrorMessage(e.getMessage());
			}
		}

	    if(!error) dispose();
	}
	
	/**
	 * Delete DB Table records
	 * @param tableName DB table name
	 * @param tableView Dialog TableView involved
	 */
	private void deleteTableRecords(String tableName, TableView tableView) {
		try{
			OntoMapData data = ((OntoMapData)meta.getStepData());
			data.setTransName(transMeta.getName());
			data.setStepName(stepname);
			DatabaseLoader.getConnection();
			Boolean resultOK = data.deleteTableRecords(tableName);
			DatabaseLoader.closeConnection();
			tableView.removeAll();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showErrorMessage(String msg) {
		MessageBox dialog = 
				  new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				dialog.setText("ERROR");						
		dialog.setMessage(msg);
	    		    
	    dialog.open();
	}
	
	/**
	 * Sets the list of steps available on the transformation
	 * @param filter step to be filtered
	 */
	private void setStepNames(String filter) {
	    previousSteps = transMeta.getStepNames();
	    String[] nextSteps = transMeta.getNextStepNames( stepMeta );

	    List<String> entries = new ArrayList<String>();
	    for ( int i = 0; i < previousSteps.length; i++ ) {
	      if ( !previousSteps[i].equals( stepname ) && !filter.equals(previousSteps[i]) ) {
	        if ( nextSteps != null && nextSteps.length > 0 ) {
	          for ( int j = 0; j < nextSteps.length; j++ ) {
	            if ( !nextSteps[j].equals( previousSteps[i] ) ) {
	              entries.add( previousSteps[i] );
	            }
	          }
	        } else {
	        	entries.add( previousSteps[i] );
	        }
	      }
	    }
	    previousSteps = entries.toArray( new String[entries.size()] );
	}
	
	/**
	 * Find the ontology PDI step if available
	 * @param stepMeta base step to start looking backwards.
	 */
	private void findOntologyStep(StepMeta stepMeta) {
		for(StepMeta step: stepMeta.getParentTransMeta().findPreviousSteps(stepMeta)) {
			if(step.getStepMetaInterface().getClass().getCanonicalName()
					.equals( "com.ucuenca.pentaho.plugin.step.owl.GetPropertiesOWLMeta" )) {
				wStep1.setText(step.getName());
				return;
			}
			if(step.getParentTransMeta().findPreviousSteps(step).size() > 0) {
				this.findOntologyStep(step);
			}
		}
	}
	
	/**
	 * Data step preview process
	 */
	private void dataPreview() {
		String stepName = wStep2.getText();
		StepMeta rootStep = this.getRootStep(transMeta.findStep( stepName ));
		/*StepMetaInterface metaDataInterface = transMeta.findStep( stepName ) != null ?
				transMeta.findStep( stepName ).getStepMetaInterface():null;*/
		stepName = rootStep.getName();
		StepMetaInterface metaDataInterface = rootStep.getStepMetaInterface();
		if(metaDataInterface != null) {
		    try {
		      TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(this.transMeta, metaDataInterface, stepName);
	
		      EnterNumberDialog numberDialog = new EnterNumberDialog(this.shell, this.props.getDefaultPreviewSize(), "Preview", "Number of row to Preview");
		      int previewSize = numberDialog.open();
		      if (previewSize > 0) {
		        TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(this.shell, previewMeta, new String[] { stepName }, new int[] { previewSize });
		        progressDialog.open();
	
		        Trans trans = progressDialog.getTrans();
		        String loggingText = progressDialog.getLoggingText();
	
		        if ((!progressDialog.isCancelled()) && 
		          (trans.getResult() != null) && (trans.getResult().getNrErrors() > 0L)) {
		          EnterTextDialog etd = new EnterTextDialog(this.shell, "Error", "An Error was caused by", loggingText, true);
	
		          etd.setReadOnly();
		          etd.open();
		        }
	
		        PreviewRowsDialog prd = new PreviewRowsDialog(this.shell, this.transMeta, 0, stepName, progressDialog.getPreviewRowsMeta(stepName), progressDialog.getPreviewRows(stepName), loggingText);
		        prd.open();
		      }
		    } catch (Exception e) {
		      new ErrorDialog(this.shell, "Error Dialog", "Message: ", e);
		    }
		}
	  }
	
	/**
	 * Get the root step from a base step
	 * @param stepMeta base step
	 * @return root step
	 */
	private StepMeta getRootStep(StepMeta stepMeta) {
		StepMeta rootStep = null;
		for(StepMeta step: stepMeta.getParentTransMeta().findPreviousSteps(stepMeta)) { 
			if(step.getParentTransMeta().findPreviousSteps(step).size() > 0) {
				rootStep = this.getRootStep(step);
				if(rootStep != null) break;
			} else {
				rootStep = step;
				break;
			}
		}
		return rootStep;
	}
}
