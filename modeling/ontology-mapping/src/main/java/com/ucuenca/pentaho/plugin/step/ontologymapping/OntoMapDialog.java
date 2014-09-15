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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;

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
	
	private CTabFolder wTabFolder;
	  private FormData fdTabFolder;

	  private CTabItem wSelectTab, wRemoveTab, wMetaTab;

	  private Composite wSelectComp, wRemoveComp, wMetaComp;
	  private FormData fdSelectComp, fdRemoveComp, fdMetaComp;
	  
	  private Label wlRemove;
	  private TableView wRemove;
	  private FormData fdlRemove, fdRemove;

	  private Label wlMeta;
	  private TableView wMeta;
	  private FormData fdlMeta, fdMeta;
	  
	  private Button wGetSelect, wGetRemove, wGetMeta, wDoMapping;
	  private FormData fdGetSelect, fdGetRemove, fdGetMeta;


	  private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();


	  
	  private Label wlFields;
	  private TableView wFields;
	  private FormData fdlFields, fdFields;
	  
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
	    shell.setText( BaseMessages.getString( PKG, "ConstantDialog.DialogTitle" ) );

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
	    
	    // The folders!
	    wTabFolder = new CTabFolder( shell, SWT.BORDER );
	    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

	    // ////////////////////////
	    // START OF SELECT TAB ///
	    // ////////////////////////

	    wSelectTab = new CTabItem( wTabFolder, SWT.NONE );
	    wSelectTab.setText( "Classification" );

	    wSelectComp = new Composite( wTabFolder, SWT.NONE );
	    props.setLook( wSelectComp );

	    FormLayout selectLayout = new FormLayout();
	    selectLayout.marginWidth = margin;
	    selectLayout.marginHeight = margin;
	    wSelectComp.setLayout( selectLayout );


	    wlFields = new Label( shell, SWT.NONE );
	    wlFields.setText( BaseMessages.getString( PKG, "ConstantDialog.Fields.Label" ) );
	    props.setLook( wlFields );
	    fdlFields = new FormData();
	    fdlFields.left = new FormAttachment( 0, 0 );
	    fdlFields.top = new FormAttachment( wStepname, margin );
	    wlFields.setLayoutData( fdlFields );

	    final int FieldsCols = 10;
	    final int FieldsRows = meta.getFieldName().length;

	    ColumnInfo[] colinf = new ColumnInfo[FieldsCols];
	    colinf[0] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Name.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
	    colinf[1] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Type.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta
	          .getTypes() );
	    colinf[2] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Format.Column" ), ColumnInfo.COLUMN_TYPE_FORMAT, 2 );
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

	    wFields =
	      new TableView(
	        transMeta, wSelectComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

	    fdFields = new FormData();
	    fdFields.left = new FormAttachment( 0, 0 );
	    fdFields.top = new FormAttachment( wlFields, margin );
	    fdFields.right = new FormAttachment( 100, 0 );
	    fdFields.bottom = new FormAttachment( 100, -50 );
	    wFields.setLayoutData( fdFields );
	    
	    //-----------
	    
	    
	    
	    fdSelectComp = new FormData();
	    fdSelectComp.left = new FormAttachment( 0, 0 );
	    fdSelectComp.top = new FormAttachment( 0, 0 );
	    fdSelectComp.right = new FormAttachment( 100, 0 );
	    fdSelectComp.bottom = new FormAttachment( 100, 0 );
	    wSelectComp.setLayoutData( fdSelectComp );

	    wSelectComp.layout();
	    wSelectTab.setControl( wSelectComp );
	    
	    fdTabFolder = new FormData();
	    fdTabFolder.left = new FormAttachment( 0, 0 );
	    fdTabFolder.top = new FormAttachment( wStepname, margin );
	    fdTabFolder.right = new FormAttachment( 100, 0 );
	    fdTabFolder.bottom = new FormAttachment( 100, -50 );
	    wTabFolder.setLayoutData( fdTabFolder );
	    
	 // ///////////////////////////////////////////////////////////
	    // / END OF SELECT TAB
	    // ///////////////////////////////////////////////////////////

	    // ///////////////////////////////////////////////////////////
	    // START OF REMOVE TAB
	    // ///////////////////////////////////////////////////////////
	    wRemoveTab = new CTabItem( wTabFolder, SWT.NONE );
	    wRemoveTab.setText( "Annotation" );

	    FormLayout contentLayout = new FormLayout();
	    contentLayout.marginWidth = margin;
	    contentLayout.marginHeight = margin;

	    wRemoveComp = new Composite( wTabFolder, SWT.NONE );
	    props.setLook( wRemoveComp );
	    wRemoveComp.setLayout( contentLayout );

	    wlRemove = new Label( wRemoveComp, SWT.NONE );
	    wlRemove.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Remove.Label" ) );
	    props.setLook( wlRemove );
	    fdlRemove = new FormData();
	    fdlRemove.left = new FormAttachment( 0, 0 );
	    fdlRemove.top = new FormAttachment( 0, 0 );
	    wlRemove.setLayoutData( fdlRemove );

	    final int RemoveCols = 1;
	    final int RemoveRows = meta.getFieldName().length;

	    ColumnInfo[] colrem = new ColumnInfo[RemoveCols];
	    colrem[0] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ConstantDialog.Name.Column" ),
	        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(
	          PKG, "ConstantDialog.Name.Column" ) + "..." }, false );
	    fieldColumns.add( colrem[0] );
	    wRemove =
	      new TableView(
	        transMeta, wRemoveComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colrem, RemoveRows, lsMod, props );
	    
	    /*
	    wGetRemove = new Button( wRemoveComp, SWT.PUSH );
	    wGetRemove.setText( BaseMessages.getString( PKG, "SelectValuesDialog.GetRemove.Button" ) );
	    wGetRemove.addListener( SWT.Selection, lsGet );
	    fdGetRemove = new FormData();
	    fdGetRemove.right = new FormAttachment( 100, 0 );
	    fdGetRemove.top = new FormAttachment( 50, 0 );
	    wGetRemove.setLayoutData( fdGetRemove );
		*/
	    fdRemove = new FormData();
	    fdRemove.left = new FormAttachment( 0, 0 );
	    fdRemove.top = new FormAttachment( wlRemove, margin );
	    //fdRemove.right = new FormAttachment( wGetRemove, -margin );
	    fdRemove.right = new FormAttachment( 100, 0 );
	    fdRemove.bottom = new FormAttachment( 100, 0 );
	    wRemove.setLayoutData( fdRemove );

	    fdRemoveComp = new FormData();
	    fdRemoveComp.left = new FormAttachment( 0, 0 );
	    fdRemoveComp.top = new FormAttachment( 0, 0 );
	    fdRemoveComp.right = new FormAttachment( 100, 0 );
	    fdRemoveComp.bottom = new FormAttachment( 100, 0 );
	    wRemoveComp.setLayoutData( fdRemoveComp );

	    wRemoveComp.layout();
	    wRemoveTab.setControl( wRemoveComp );

	    // ///////////////////////////////////////////////////////////
	    // / END OF REMOVE TAB
	    // ///////////////////////////////////////////////////////////

	    // ////////////////////////
	    // START OF META TAB ///
	    // ////////////////////////

	    wMetaTab = new CTabItem( wTabFolder, SWT.NONE );
	    wMetaTab.setText( "Relation" );

	    wMetaComp = new Composite( wTabFolder, SWT.NONE );
	    props.setLook( wMetaComp );

	    FormLayout metaLayout = new FormLayout();
	    metaLayout.marginWidth = margin;
	    metaLayout.marginHeight = margin;
	    wMetaComp.setLayout( metaLayout );

	    wlMeta = new Label( wMetaComp, SWT.NONE );
	    wlMeta.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Meta.Label" ) );
	    props.setLook( wlMeta );
	    fdlMeta = new FormData();
	    fdlMeta.left = new FormAttachment( 0, 0 );
	    fdlMeta.top = new FormAttachment( 0, 0 );
	    wlMeta.setLayoutData( fdlMeta );

	    final int MetaRows = meta.getFieldName().length;

	    ColumnInfo[] colmeta =
	      new ColumnInfo[] {
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Fieldname" ),
	          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(
	            PKG, "SelectValuesDialog.ColumnInfo.Loading" ) }, false ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Renameto" ),
	          ColumnInfo.COLUMN_TYPE_TEXT, false ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Type" ),
	          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getAllTypes(), false ),
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
	        /*new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Encoding" ),
	          ColumnInfo.COLUMN_TYPE_CCOMBO, getCharsets(), false ),*/
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Decimal" ),
	          ColumnInfo.COLUMN_TYPE_TEXT, false ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Grouping" ),
	          ColumnInfo.COLUMN_TYPE_TEXT, false ),
	        new ColumnInfo(
	          BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Currency" ),
	          ColumnInfo.COLUMN_TYPE_TEXT, false ), };
	    colmeta[5].setToolTip( BaseMessages.getString( PKG, "SelectValuesDialog.ColumnInfo.Storage.Tooltip" ) );
	    fieldColumns.add( colmeta[0] );
	    wMeta =
	      new TableView(
	        transMeta, wMetaComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colmeta, MetaRows, lsMod, props );

	    /*
	    wGetMeta = new Button( wMetaComp, SWT.PUSH );
	    wGetMeta.setText( BaseMessages.getString( PKG, "SelectValuesDialog.GetMeta.Button" ) );
	    wGetMeta.addListener( SWT.Selection, lsGet );
	    fdGetMeta = new FormData();
	    fdGetMeta.right = new FormAttachment( 100, 0 );
	    fdGetMeta.top = new FormAttachment( 50, 0 );
	    wGetMeta.setLayoutData( fdGetMeta );
		*/
	    fdMeta = new FormData();
	    fdMeta.left = new FormAttachment( 0, 0 );
	    fdMeta.top = new FormAttachment( wlMeta, margin );
	    //fdMeta.right = new FormAttachment( wGetMeta, -margin );
	    fdMeta.right = new FormAttachment( 100, 0 );
	    fdMeta.bottom = new FormAttachment( 100, 0 );
	    wMeta.setLayoutData( fdMeta );

	    fdMetaComp = new FormData();
	    fdMetaComp.left = new FormAttachment( 0, 0 );
	    fdMetaComp.top = new FormAttachment( 0, 0 );
	    fdMetaComp.right = new FormAttachment( 100, 0 );
	    fdMetaComp.bottom = new FormAttachment( 100, 0 );
	    wMetaComp.setLayoutData( fdMetaComp );

	    wMetaComp.layout();
	    wMetaTab.setControl( wMetaComp );

	    // ///////////////////////////////////////////////////////////
	    // / END OF META TAB
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
	        wFields.setSize( size.x - 10, size.y - 50 );
	        wFields.table.setSize( size.x - 10, size.y - 50 );
	        wFields.redraw();
	      }
	    };
	    shell.addListener( SWT.Resize, lsResize );

	    // Set the shell size, based upon previous time...
	    setSize();

	    getData();
	    meta.setChanged( changed );

	    shell.open();
	    while ( !shell.isDisposed() ) {
	      if ( !display.readAndDispatch() ) {
	        display.sleep();
	      }
	    }
	    return stepname;
	}
	
	/**
	 * Implementation
	 * Copy information from the meta-data input to the dialog fields.	
	 */
	public void getData() {
		int i;
		if ( log.isDebug() ) {
		  logDebug( "getting fields info..." );
		}
		
		for ( i = 0; i < meta.getFieldName().length; i++ ) {
		  if ( meta.getFieldName()[i] != null ) {
		    TableItem item = wFields.table.getItem( i );
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
		
		    wFields.setRowNums();
		    wFields.optWidth( true );
		
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

	    int i;
	    // Table table = wFields.table;

	    int nrfields = wFields.nrNonEmpty();

	    meta.allocate( nrfields );

	    //CHECKSTYLE:Indentation:OFF
	    //CHECKSTYLE:LineLength:OFF
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

	    }

	    dispose();
	}
}
