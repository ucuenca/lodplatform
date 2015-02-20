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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.gui.PrimitiveGCInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.w3c.dom.Node;

import com.ucuenca.misctools.DatabaseLoader;

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
	
	private Label wlStep1;
	  private CCombo wStep1;
	  private FormData fdlStep1, fdStep1;

	  private Label wlStep2;
	  private CCombo wStep2;
	  private FormData fdlStep2, fdStep2;
	  
	  private Label wlBaseURI;
	  private Text wBaseURI;
	  private FormData fdlBaseURI, fdBaseURI;
	
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
	  
	  private Button wDelClassTable, wDelAnnTable, wDelRelTable, wGetSelect, wGetRemove, wGetMeta, wDoMapping;
	  private FormData fdDelClassTable, fdDelAnnTable, fdDelRelTable, fdGetSelect, fdGetRemove, fdGetMeta;


	  private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();


	  
	  private Label wlFields;
	  private TableView wClassTable;
	  private FormData fdlFields, fdFields;
	  
	  /**
	   * Fields from previous step
	   */
	  private RowMetaInterface prevFields;
	  
	  private TransMeta transMeta;
	  private Map<String, String[]> dataCache = new HashMap();
	  

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
	
	private void precatchingData() {
		try {
			List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
			OntoMapData data = (OntoMapData)meta.getStepData();
	
		      StepMeta stepMeta = infoStreams.get( 0 ).getStepMeta();
		      if ( stepMeta != null ) {
		        RowMetaInterface prev = transMeta.getStepFields( stepMeta );
		        if ( prev != null ) {
		          //BaseStepDialog.getFieldsFromPrevious( prev, wKeys1, 1, new int[] { 1 }, new int[] {}, -1, -1, null );
		        }
		      }
	      /*
	      data.ontologiesRowSet = getfindInputRowSet( infoStreams.get( 0 ).getStepname() );
	      if ( data.ontologiesRowSet == null ) {
	        throw new KettleException( BaseMessages.getString(
	          PKG, "MergeJoin.Exception.UnableToFindSpecifiedStep", infoStreams.get( 0 ).getStepname() ) );
	      }
	
	      data.dataRowSet = findInputRowSet( infoStreams.get( 1 ).getStepname() );
	      if ( data.dataRowSet == null ) {
	        throw new KettleException( BaseMessages.getString(
	          PKG, "MergeJoin.Exception.UnableToFindSpecifiedStep", infoStreams.get( 1 ).getStepname() ) );
	      }
	
	      data.ontologies = getRowFrom( data.ontologiesRowSet );
	      if ( data.ontologies != null ) {
	        data.ontologiesMeta = data.ontologiesRowSet.getRowMeta();
	      } else {
	        data.ontologies = null;
	        data.ontologiesMeta = getTransMeta().getStepFields( infoStreams.get( 0 ).getStepname() );
	      }
	
	      data.data = getRowFrom( data.dataRowSet );
	      if ( data.data != null ) {
	        data.dataMeta = data.dataRowSet.getRowMeta();
	      } else {
	        data.data = null;
	        data.dataMeta = getTransMeta().getStepFields( infoStreams.get( 1 ).getStepname() );
	      }*/
		}catch(Exception e) {
			e.printStackTrace();
			
		}
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
		this.precatchingData();
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
				getPrevStepsMeta();
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		};
	    
	 // Get the previous steps...
	    String[] previousSteps = transMeta.getPrevStepNames( stepname );
	    
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
	    fdStep1.right = new FormAttachment( 100, 0 );
	    wStep1.setLayoutData( fdStep1 );
	    wStep1.addSelectionListener(inputStepLs);

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
	    fdStep2.right = new FormAttachment( 100, 0 );
	    wStep2.setLayoutData( fdStep2 );
	    wStep2.addSelectionListener(inputStepLs);
	    
	 // Filename line
	    wlBaseURI = new Label( shell, SWT.RIGHT  | SWT.MEDIUM);
	    wlBaseURI.setText( BaseMessages.getString( PKG, "OntologyMapping.BaseURI.Label" ) );
	    props.setLook( wlBaseURI );
	    fdlBaseURI = new FormData();
	    fdlBaseURI.left = new FormAttachment( 0, 0 );
	    fdlBaseURI.right = new FormAttachment( middle, -margin );
	    fdlBaseURI.top = new FormAttachment( wStep2, margin );
	    wlBaseURI.setLayoutData( fdlBaseURI );
	    wBaseURI = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.MEDIUM | SWT.BORDER );
	    wBaseURI.setText( "http://" );
	    props.setLook( wBaseURI );
	    wBaseURI.addModifyListener( lsMod );
	    fdBaseURI = new FormData();
	    fdBaseURI.left = new FormAttachment( middle, 0 );
	    fdBaseURI.top = new FormAttachment( wStep2, margin );
	    fdBaseURI.right = new FormAttachment( 100, 0 );
	    wBaseURI.setLayoutData( fdBaseURI );
	    wBaseURI.addSelectionListener(inputStepLs);

	    // The folders!
	    wTabFolder = new CTabFolder( shell, SWT.BORDER );
	    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
	    
	    fdTabFolder = new FormData();
	    fdTabFolder.left = new FormAttachment( 0, 0 );
	    fdTabFolder.top = new FormAttachment( wBaseURI, margin );
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
	    
	    // ???
	    //final int FieldsRows = meta.getFieldName().length;

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
				int[] filters = colNr == 4 ? new int[]{6,8}:(colNr == 6 ? new int[]{4,8}:new int[]{4,6});
				
				String stepName = wStep2.getText();
				String [] values = new String[]{"No fields found"};
				try {
					List<String> filter= new ArrayList<String>();
					if(!StringUtils.isEmpty(tableItem.getText(filters[0]) )) filter.add(tableItem.getText(filters[0]));
					if(!StringUtils.isEmpty(tableItem.getText(filters[1]) )) filter.add(tableItem.getText(filters[1]));
					values = getStepFieldsMeta(stepName, filter.toArray());
				}catch(KettleException e) {
					logError(e.getMessage(), e);
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
				if(dataCache.containsKey(fieldName)) {
					rsField = dataCache.get(fieldName);
				} else {
					String sqlQuery = "SELECT DISTINCT " + fieldName 
							+ " FROM " + meta.getDataDbTable() + " WHERE TRANSID ='" + transMeta.getName().toUpperCase() 
							+ "' AND STEPID='" + meta.getDataStepName().toUpperCase() + "'";					
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
	    

	    /*
	    colinf[3] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Length.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
	    colinf[4] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Precision.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
	    colinf[5] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Currency.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
	    colinf[6] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Decimal.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
	    colinf[7] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Group.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
	    colinf[8] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Value.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
	    colinf[9] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Value.SetEmptyString" ),
	        ColumnInfo.COLUMN_TYPE_CCOMBO,
	        new String[] {
	          BaseMessages.getString( PKG, "System.Combo.Yes" ), BaseMessages.getString( PKG, "System.Combo.No" ) } );
		*/
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

	    // ???
	    //final int RemoveRows = meta.getFieldName().length;

	    ColumnInfo[] colann = new ColumnInfo[] {
	    /*
	    colrem[0] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Name.Column" ),
	        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(
	          PKG, "ConstantDialog.Name.Column" ) + "..." }, false );
	     */
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
  	        BaseMessages.getString( PKG, "OntologyMapping.Data.Field" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
  	        new String[]{"field1", "field2"}),	    
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "OntologyMapping.Value.Field" ), ColumnInfo.COLUMN_TYPE_CCOMBO, 
	        new String[]{"value1", "value2"})};
	    
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
	    colann[5].setComboValuesSelectionListener(cmbValuesLs);
	    // ???
	    fieldColumns.add( colann[0] );
	    
	    wAnnTable =
	      new TableView(
	        transMeta, wAnnotateComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colann, 0, lsMod, props );	    
	    
	    /*
	    wGetRemove = new Button( wAnnotateComp, SWT.PUSH );
	    wGetRemove.setText( BaseMessages.getString( PKG, "SelectValuesDialog.GetRemove.Button" ) );
	    wGetRemove.addListener( SWT.Selection, lsGet );
	    fdGetRemove = new FormData();
	    fdGetRemove.right = new FormAttachment( 100, 0 );
	    fdGetRemove.top = new FormAttachment( 50, 0 );
	    wGetRemove.setLayoutData( fdGetRemove );
		*/
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

	    //final int MetaRows = meta.getFieldName().length;

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
	          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getAllTypes(), false ),
	          /*
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Length" ),
	          ColumnInfo.COLUMN_TYPE_TEXT, false ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Precision" ),
	          ColumnInfo.COLUMN_TYPE_TEXT, false ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Storage.Label" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
	          new String[] {
	            BaseMessages.getString( PKG, "System.Combo.Yes" ), BaseMessages.getString( PKG, "System.Combo.No" ), } ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Format" ),
	          ColumnInfo.COLUMN_TYPE_FORMAT, 3 ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.DateLenient" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
	          new String[] {
	            BaseMessages.getString( PKG, "System.Combo.Yes" ), BaseMessages.getString( PKG, "System.Combo.No" ), } ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.DateFormatLocale" ),
	          ColumnInfo.COLUMN_TYPE_CCOMBO, EnvUtil.getLocaleList() ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.DateFormatTimeZone" ),
	          ColumnInfo.COLUMN_TYPE_CCOMBO, EnvUtil.getTimeZones() ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.LenientStringToNumber" ),
	          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {
	            BaseMessages.getString( PKG, "System.Combo.Yes" ), BaseMessages.getString( PKG, "System.Combo.No" ), } ),
	            */
	        /*new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Encoding" ),
	          ColumnInfo.COLUMN_TYPE_CCOMBO, getCharsets(), false ),*/
	          /*
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Decimal" ),
	          ColumnInfo.COLUMN_TYPE_TEXT, false ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Grouping" ),
	          ColumnInfo.COLUMN_TYPE_TEXT, false ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Currency" ),
	          ColumnInfo.COLUMN_TYPE_TEXT, false ),*/ };
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
	    // ????
	    fieldColumns.add( colrel[0] );
	    
	    wRelTable =
	      new TableView(
	        transMeta, wRelationComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colrel, 0, lsMod, props );

	    /*
	    wGetMeta = new Button( wRelationComp, SWT.PUSH );
	    wGetMeta.setText( BaseMessages.getString( PKG, "SelectValuesDialog.GetMeta.Button" ) );
	    wGetMeta.addListener( SWT.Selection, lsGet );
	    fdGetMeta = new FormData();
	    fdGetMeta.right = new FormAttachment( 100, 0 );
	    fdGetMeta.top = new FormAttachment( 50, 0 );
	    wGetMeta.setLayoutData( fdGetMeta );
		*/
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
	    
	    //
	    // Search the fields in the background
	    //
	
	    final Runnable runnable = new Runnable() {
	      public void run() {
	        StepMeta stepMeta = transMeta.findStep( stepname );
	        if ( stepMeta != null ) {
	          try {
	            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );
	            prevFields = row;
	            // Remember these fields...
	            for ( int i = 0; i < row.size(); i++ ) {
	              //inputFields.put( row.getValueMeta( i ).getName(), Integer.valueOf( i ) );
	            }
	            //setComboBoxes();
	          } catch ( KettleException e ) {
	            logError( BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Message" ) );
	          }
	        }
	      }
	    };
	    new Thread( runnable ).start();

	    // Set the shell size, based upon previous time...
	    super.setSize();
	    this.getData();
	    this.getMappingTableData(stepname);
	    meta.setChanged( changed );

	    shell.open();
	    while ( !shell.isDisposed() ) {
	      if ( !display.readAndDispatch() ) {
	        display.sleep();
	      }
	    }
	    return stepname;
	}
	
	private void getPrevStepsMeta() {
		String ontologyStepName = wStep1.getText();
		meta.setOntologyDbTable(this.stepDBTableLookup(transMeta.findStep(ontologyStepName)));
		meta.setOntologyStepName(ontologyStepName);		
		String dataStepName = wStep2.getText();
		meta.setDataDbTable(this.stepDBTableLookup(transMeta.findStep(dataStepName)));
		meta.setDataStepName(dataStepName);
		String baseURI = wBaseURI.getText();
		//baseURI Validation not implemented yet
		meta.setMapBaseURI(baseURI);
	}
	
	private String stepDBTableLookup(StepMeta stepMeta) {
		String tableName = null;
		if(stepMeta != null) {
			StepDataInterface stepData = stepMeta.getStepMetaInterface().getStepData(); 
			try {
				tableName = (String)stepData.getClass().getField("DBTABLE").get(stepData);
			}catch(NoSuchFieldException ne) {
				logError("NO 'DBTABLE' FIELD FOUND ON " + stepMeta.getName() + " STEP DATA CLASS");
			}catch(SecurityException se) {
				logError("NO 'DBTABLE' PUBLIC FIELD FOUND ON " + stepMeta.getName() + " STEP DATA CLASS");
			}catch(IllegalAccessException ae) {
				logError(ae.getMessage());
			}
		}
		return tableName;
	}
	
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
	
	private String[] findComboValues(TableItem tableItem, int currentCol) {
		int[] filters = currentCol == 4 ? new int[]{6,8}:(currentCol == 6 ? new int[]{4,8}:new int[]{4,6});
		
		String stepName = wStep2.getText();
		String [] values = new String[]{"No fields found"};
		try {
			List<String> filter= new ArrayList<String>();
			if(!StringUtils.isEmpty(tableItem.getText(filters[0]) )) filter.add(tableItem.getText(filters[0]));
			if(!StringUtils.isEmpty(tableItem.getText(filters[1]) )) filter.add(tableItem.getText(filters[1]));
			values = this.getStepFieldsMeta(stepName, filter.toArray());
		}catch(KettleException e) {
			
		}
		return values;
	}
	
	private String[] getClassificationIDS() {
		String [] classificationID = wClassTable.getItems(0);
		List<String> idList = new ArrayList<String>();
		for(String id: classificationID) {
			if(!id.equals("")) idList.add(id);
		}
		classificationID = idList.toArray(new String[idList.size()]);
		return classificationID.length > 0 ? classificationID:new String[]{"No Items found"};
	}
	
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
	
	private void getMappingTableData(String stepname) {
		OntoMapData data = ((OntoMapData)meta.getStepData());
		data.setTransName(transMeta.getName());
		data.setStepName(stepname);
		try {
			DatabaseLoader.getConnection();
			data.queryTable(wClassTable, OntoMapData.CLASSIFICATIONTABLE);
			data.queryTable(wAnnTable, OntoMapData.ANNOTATIONTABLE);
			data.queryTable(wRelTable, OntoMapData.RELATIONTABLE);
			DatabaseLoader.closeConnection();
		}catch(Exception e){
			e.printStackTrace();
			
		}
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
		/*
		int i;
		if ( log.isDebug() ) {
		  logDebug( "getting fields info..." );
		}
		*/
		/*
		List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();

	    wStep1.setText( Const.NVL( infoStreams.get( 0 ).getStepname(), "" ) );
	    wStep2.setText( Const.NVL( infoStreams.get( 1 ).getStepname(), "" ) );
		*/
		
		/*for ( i = 0; i < meta.getFieldName().length; i++ ) {
		  if ( meta.getFieldName()[i] != null ) {
		    TableItem item = wClassTable.table.getItem( i );
		    int col = 1;
		    item.setText( col++, meta.getFieldName()[i] );
		
		    String type = meta.getFieldType()[i];
		    String format = meta.getFieldFormat()[i];
		    String length = meta.getFieldLength()[i] < 0 ? "" : ( "" + meta.getFieldLength()[i] );
		    String prec = meta.getFieldPrecision()[i] < 0 ? "" : ( "" + meta.getFieldPrecision()[i] );
		
		    String curr = meta.getCurrency()[i];
		    String group = meta.getGroup()[i];
		    String decim = meta.getDecimal()[i];
		    String def = meta.getValue()[i];
		
		    item.setText( col++, Const.NVL( type, "" ) );
		    item.setText( col++, Const.NVL( format, "" ) );
		    item.setText( col++, Const.NVL( length, "" ) );
		    item.setText( col++, Const.NVL( prec, "" ) );
		    item.setText( col++, Const.NVL( curr, "" ) );
		    item.setText( col++, Const.NVL( decim, "" ) );
		    item.setText( col++, Const.NVL( group, "" ) );
		    item.setText( col++, Const.NVL( def, "" ) );
		    item
		      .setText( col++, meta.isSetEmptyString()[i]
		        ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString(
		          PKG, "System.Combo.No" ) );
		
	      }
	    }
	
		wClassTable.setRowNums();
		wClassTable.optWidth( true );*/
	
	    wStepname.selectAll();
	    wStepname.setFocus();
	  }
	
	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	/*private void populateDialog() {
		wStepname.selectAll();
		wHelloFieldName.setText(meta.getOutputField());	
	}*/

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
		if ( Const.isEmpty( wStepname.getText() ) ) {
	      return;
	    }
		stepname = wStepname.getText(); // return value
		if(meta.hasChanged()) {
		
			this.getPrevStepsMeta();
			try{
				OntoMapData data = ((OntoMapData)meta.getStepData());
				data.setTransName(transMeta.getName());
				data.setStepName(stepname);
				DatabaseLoader.getConnection();
				data.saveTable(wClassTable, OntoMapData.CLASSIFICATIONTABLE);
				data.saveTable(wAnnTable, OntoMapData.ANNOTATIONTABLE);
				data.saveTable(wRelTable, OntoMapData.RELATIONTABLE);
				DatabaseLoader.closeConnection();
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
	
		    infoStreams.get( 0 ).setStepMeta( transMeta.findStep( wStep1.getText() ) );
		    infoStreams.get( 1 ).setStepMeta( transMeta.findStep( wStep2.getText() ) );
		}

	    int i;
	    // Table table = wFields.table;

	    int nrfields = wClassTable.nrNonEmpty();

	    meta.allocate( nrfields );

	    //CHECKSTYLE:Indentation:OFF
	    //CHECKSTYLE:LineLength:OFF
	    /*
	    for ( i = 0; i < nrfields; i++ ) {
	      TableItem item = wFields.getNonEmpty( i );
	      meta.getFieldName()[i] = item.getText( 1 );
	      meta.isSetEmptyString()[i] = BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText( 10 ) );

	      meta.getFieldType()[i] = meta.isSetEmptyString()[i] ? "String" : item.getText( 2 );
	      meta.getFieldFormat()[i] = item.getText( 3 );
	      String slength = item.getText( 4 );
	      String sprec = item.getText( 5 );
	      meta.getCurrency()[i] = item.getText( 6 );
	      meta.getDecimal()[i] = item.getText( 7 );
	      meta.getGroup()[i] = item.getText( 8 );
	      meta.getValue()[i] = meta.isSetEmptyString()[i] ? "" : item.getText( 9 );

	      try {
	        meta.getFieldLength()[i] = Integer.parseInt( slength );
	      } catch ( Exception e ) {
	        meta.getFieldLength()[i] = -1;
	      }
	      try {
	        meta.getFieldPrecision()[i] = Integer.parseInt( sprec );
	      } catch ( Exception e ) {
	        meta.getFieldPrecision()[i] = -1;
	      }

	    }*/

	    dispose();
	}
	
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
}
