package com.ucuenca.pentaho.plugin.step.marc21loader;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

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
public class MARC21LoaderDialog extends BaseStepDialog implements StepDialogInterface
{
	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = MARC21LoaderMeta.class; // for i18n purposes
	
	//Componentes para la solicitud de datos
	private Label        wlShape;
	private Button       wbShape;
//	private Button       wbcShape;
	private Text         wMarcURL;
	private FormData     fdlShape, fdbShape, fdbcShape, fdShape;

	private Label        wlDbf;
//	private Button       wbDbf;
//	private Button       wbcDbf;
	
	private Label wlBatch, wlXML;
    private Button wBatch, wXML;
    private FormData fdlBatch, fdlXML, fdBatch, fdXML;
    
	private Text         wMarcFields;
	private FormData     fdlDbf, fdbDbf, fdbcDbf, fdDbf;

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private MARC21LoaderMeta input;
	
	private boolean backup_changed;
	
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
	public MARC21LoaderDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MARC21LoaderMeta)in;
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
	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		backup_changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("MARC Input");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText("Step name ");
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		
		//Batch mode check
		
		wlBatch = new Label( shell, SWT.RIGHT );
	    //wlBatch.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Unspecified.Label" ) );
	    wlBatch.setText( "Batch" );
	    props.setLook( wlBatch );
	    fdlBatch = new FormData();
	    fdlBatch.top = new FormAttachment( wStepname, 0 );
	    fdlBatch.left = new FormAttachment( 0, 0 );
	    fdlBatch.right = new FormAttachment( middle, 0 );
	    //fdlBatch.bottom = new FormAttachment( wMarcURL, 0 );
	    wlBatch.setLayoutData( fdlBatch );

	    wBatch = new Button( shell, SWT.CHECK );
	    props.setLook( wBatch );
	    fdBatch = new FormData();
	    fdBatch.top = new FormAttachment( wStepname, 0 );
	    fdBatch.left = new FormAttachment( middle, margin );
	    fdBatch.right = new FormAttachment( 100, 0 );
	    //fdBatch.bottom = new FormAttachment( wMarcURL, 0 );
	    wBatch.setLayoutData( fdBatch );

		//Solicitud del directorio del archivo MARC
		//variable que contiene el PATH wlShape
		// Shape line
		wlShape=new Label(shell, SWT.RIGHT);
		wlShape.setText("Name of the marcfile (.mrc) ");
 		props.setLook(wlShape);
		fdlShape=new FormData();
		fdlShape.left = new FormAttachment(0, 0);
		fdlShape.top  = new FormAttachment(wBatch, margin);
		fdlShape.right= new FormAttachment(middle, -margin);
		wlShape.setLayoutData(fdlShape);
		
		wbShape=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShape);
		wbShape.setText("&Browse...");
		fdbShape=new FormData();
		fdbShape.right= new FormAttachment(100, 0);
		fdbShape.top  = new FormAttachment(wBatch, margin);
		wbShape.setLayoutData(fdbShape);

		/*wbcShape=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbcShape);
		wbcShape.setText("&Variable...");
		fdbcShape=new FormData();
		fdbcShape.right= new FormAttachment(wbShape, -margin);
		fdbcShape.top  = new FormAttachment(wStepname, margin);
		wbcShape.setLayoutData(fdbcShape);*/

		wMarcURL=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMarcURL);
		wMarcURL.addModifyListener(lsMod);
		fdShape=new FormData();
		fdShape.left = new FormAttachment(middle, 0);
		fdShape.right= new FormAttachment(200, 0);
		fdShape.top  = new FormAttachment(wBatch, margin);
		wMarcURL.setLayoutData(fdShape);
		
		//Generación de marcxml
		
		wlXML = new Label( shell, SWT.RIGHT );
	    //wlBatch.setText( BaseMessages.getString( PKG, "SelectValuesDialog.Unspecified.Label" ) );
	    wlXML.setText( "Generate MARC XML" );
	    props.setLook( wlXML );
	    fdlXML = new FormData();
	    fdlXML.top = new FormAttachment( wMarcURL, 0 );
	    fdlXML.left = new FormAttachment( 0, 0 );
	    fdlXML.right = new FormAttachment( middle, 0 );
	    //fdlBatch.bottom = new FormAttachment( wMarcURL, 0 );
	    wlXML.setLayoutData( fdlXML );
		
		wXML = new Button( shell, SWT.CHECK );
	    props.setLook( wXML );
	    fdXML = new FormData();
	    fdXML.top = new FormAttachment( wMarcURL, 0 );
	    fdXML.left = new FormAttachment( middle, margin );
	    fdXML.right = new FormAttachment( 100, 0 );
	    //fdBatch.bottom = new FormAttachment( wMarcURL, 0 );
	    wXML.setLayoutData( fdXML );
	    
	    
		
		// Dbf line
		wlDbf=new Label(shell, SWT.RIGHT);
		wlDbf.setText("Fields to load separate with @");
 		props.setLook(wlDbf);
		fdlDbf=new FormData();
		fdlDbf.left = new FormAttachment(0, 0);
		fdlDbf.top  = new FormAttachment(wXML, margin);
		fdlDbf.right= new FormAttachment(middle, -margin);
		wlDbf.setLayoutData(fdlDbf);
		/*wbDbf=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbDbf);
		wbDbf.setText("&Browse...");
		fdbDbf=new FormData();
		fdbDbf.right= new FormAttachment(100, 0);
		fdbDbf.top  = new FormAttachment(wMarcURL, margin);
		wbDbf.setLayoutData(fdbDbf);

		wbcDbf=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbcDbf);
		wbcDbf.setText("&Variable...");
		fdbcDbf=new FormData();
		fdbcDbf.right= new FormAttachment(wbDbf, -margin);
		fdbcDbf.top  = new FormAttachment(wMarcURL, margin);
		wbcDbf.setLayoutData(fdbcDbf);*/

		wMarcFields=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMarcFields);
		wMarcFields.addModifyListener(lsMod);
		fdDbf=new FormData();
		fdDbf.left = new FormAttachment(middle, 0);
		//*******************************************
		fdDbf.right= new FormAttachment(100,0);
		fdDbf.top  = new FormAttachment(wXML, margin);
		wMarcFields.setLayoutData(fdDbf);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		wMarcURL.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				wMarcURL.setToolTipText(transMeta.environmentSubstitute(wMarcURL.getText()));
			}
		});
		
		//BROWSER
		wbShape.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					if( !wBatch.getSelection() ){
						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						dialog.setFilterExtensions(new String[] {"*.mrc;*.MRC", "*"});
						if (wMarcURL.getText()!=null)
						{
							dialog.setFileName(wMarcURL.getText());
						}
							
						dialog.setFilterNames(new String[] {"MARC files", "All files"});
							
						if (dialog.open()!=null)
						{
							String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
							wMarcURL.setText(str);
							wMarcFields.setText("020a@022a@040a@040b@041a@082a@082c@100a@100b@100d@110a@110e@110g@245a@245b@245c@245h@245n@245p@246a@246b@246f@246g@250a@250b@300a@300b@300c@300e@310a@362a@490a@490n@490p@490v@490x@500a@502a@504a@520a@653a@700a@700b@700d@700e@710a@710b@710e@710g@773g@773t@856a@856u@900a@900f@900k@900l@900m@900n@900o@900p@900q@900r@900y");
							/*if (str.toUpperCase().endsWith(".MRC") && (wMarcFields.getText()==null || wMarcFields.getText().length()==0) )
							{
							    String strdbf = str.substring(0,str.length()-4);
							    wMarcFields.setText(strdbf+".dbf");
							}*/
						}
					} else {
						DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
						//FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						//dialog.setFilterExtensions(new String[] {"*.mrc;*.MRC", "*"});
						if (wMarcURL.getText()!=null)
						{
							dialog.setFilterPath(wMarcURL.getText());
						}	
						//dialog.setFilterNames(new String[] {"MARC files", "All files"});
							
						if (dialog.open()!=null)
						{
							String str = dialog.getFilterPath();
							wMarcURL.setText(str);
							wMarcFields.setText("020a@022a@040a@040b@041a@082a@082c@100a@100b@100d@110a@110e@110g@245a@245b@245c@245h@245n@245p@246a@246b@246f@246g@250a@250b@300a@300b@300c@300e@310a@362a@490a@490n@490p@490v@490x@500a@502a@504a@520a@653a@700a@700b@700d@700e@710a@710b@710e@710g@773g@773t@856a@856u@900a@900f@900k@900l@900m@900n@900o@900p@900q@900r@900y");
							/*if (str.toUpperCase().endsWith(".MRC") && (wMarcFields.getText()==null || wMarcFields.getText().length()==0) )
							{
							    String strdbf = str.substring(0,str.length()-4);
							    wMarcFields.setText(strdbf+".dbf");
							}*/
						}
						
					}
				}
			}
		);

		// Listen to the Variable... button
		/*wbcShape.addSelectionListener
		(
			new SelectionAdapter()
			{
				@SuppressWarnings("unchecked")
				public void widgetSelected(SelectionEvent e) 
				{
					Properties sp = System.getProperties();
					Enumeration keys = sp.keys();
					int size = sp.values().size();
					String key[] = new String[size];
					String val[] = new String[size];
					String str[] = new String[size];
					int i=0;
					while (keys.hasMoreElements())
					{
						key[i] = (String)keys.nextElement();
						val[i] = sp.getProperty(key[i]);
						str[i] = key[i]+"  ["+val[i]+"]";
						i++;
					}
					
					EnterSelectionDialog esd = new EnterSelectionDialog(shell, str, "Select an Environment Variable", "Select an Environment Variable");
					if (esd.open()!=null)
					{
						int nr = esd.getSelectionNr();
						wMarcURL.insert("%%"+key[nr]+"%%");
						wMarcURL.setToolTipText(transMeta.environmentSubstitute(wMarcURL.getText()));
					}
				}
				
			}
		);*/


		wMarcFields.addModifyListener(new ModifyListener()
				{
					public void modifyText(ModifyEvent arg0)
					{
						wMarcFields.setToolTipText(transMeta.environmentSubstitute(wMarcFields.getText()));
					}
				});
				
	/*	wbDbf.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*.dbf;*.DBF", "*"});
					if (wMarcFields.getText()!=null)
					{
						dialog.setFileName(wMarcFields.getText());
					}
						
					dialog.setFilterNames(new String[] {"DBF files", "All files"});
						
					if (dialog.open()!=null)
					{
						String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
						wMarcFields.setText(str);
					}
				}
			}
		);*/

		// Listen to the Variable... button
		/*wbcDbf.addSelectionListener
		(
			new SelectionAdapter()
			{
				@SuppressWarnings("unchecked")
				public void widgetSelected(SelectionEvent e) 
				{
					Properties sp = System.getProperties();
					Enumeration keys = sp.keys();
					int size = sp.values().size();
					String key[] = new String[size];
					String val[] = new String[size];
					String str[] = new String[size];
					int i=0;
					while (keys.hasMoreElements())
					{
						key[i] = (String)keys.nextElement();
						val[i] = sp.getProperty(key[i]);
						str[i] = key[i]+"  ["+val[i]+"]";
						i++;
					}
					
					EnterSelectionDialog esd = new EnterSelectionDialog(shell, str, "Select an Environment Variable", "Select an Environment Variable");
					if (esd.open()!=null)
					{
						int nr = esd.getSelectionNr();
						wMarcFields.insert("${"+key[nr]+"}");
						wMarcFields.setToolTipText(transMeta.environmentSubstitute(wMarcFields.getText()));
					}
				}
				
			}
		);*/

		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
		
	// Read data from input (TextFileInputMeta)
	public void getData()
	{
	    if (input.getmarcFilename()!=null) wMarcURL.setText(input.getmarcFilename());
	    if (input.getmarcFields()!=null) wMarcFields.setText(input.getmarcFields());
	    if(input.getBatchMode()!=null) wBatch.setSelection(input.getBatchMode());
	    if(input.getGenMARCXML()!=null) wXML.setSelection(input.getGenMARCXML());

	    wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backup_changed);
		dispose();
	}
	
	private void ok()
	{
		stepname = wStepname.getText();
		input.setmarcFilename(wMarcURL.getText());
		input.setmarcFields(wMarcFields.getText());
		input.setBatchMode(wBatch.getSelection());
		input.setGenMARCXML(wXML.getSelection());
		
		dispose();
	}
}
