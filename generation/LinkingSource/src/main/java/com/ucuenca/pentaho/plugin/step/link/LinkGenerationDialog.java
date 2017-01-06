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
import org.pentaho.di.ui.core.dialog.EnterValueDialog;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

public class LinkGenerationDialog extends BaseStepDialog implements StepDialogInterface
{       private static Class<?> PKG = LinkGenerationMeta.class;
	private LinkGenerationMeta input;
	private ValueMetaAndData value;

	/*private Label        wlValName;
	private Text         wValName;
	private FormData     fdlValName, fdValName;*/
        
        private Label  lbsparql1 , lbgraph1;
        private Label  lbsparql2, lbgraph2;
       
        private FormData  fdsparql1, fdsparql2 ,fdtsparql1,fdtsparql2 , fdtgraph1 , fdlgraph1 , fdtgraph2, fdlgraph2 ;
        private Text   txtsparlq1, txtsparql2 , txtgraph1 ,txtgraph2;
       
        private Label  lbfileinput , lbfileoutput ;
        private FormData  fdlfileinput, fdtfileinput , btfdfileinput , fdldiroutput , fdtdiroutput , btfdfileoutput , btfclearinput;
        private Text      txtfileinput ,txtfileoutput ;
        private Button    btfileinput ,btfileoutput ,btclearinput;

	private Label        wlValue;
	private Button       wbValue;
	private Text         wValue;
	private FormData     fdlValue, fdbValue, fdValue;
        
        private Label lbumbral1 , lbumbral2 ;
        private FormData fdlumbral1 ,fdtumbral1, fdlumbral2 , fdtumbral2;
        private Text   txtumbral1, txtumbral2;
	
	public LinkGenerationDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{  
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(LinkGenerationMeta)in;
		//value = input.getValue();
	}

	public String open()
	{  
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook( shell );
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("DummyPluginDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("DummyPluginDialog.StepName.Label")); //$NON-NLS-1$
        props.setLook( wlStepname );
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
                props.setLook( wStepname );
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		
                
                //yo
                lbsparql1 = new Label (shell,SWT.RIGHT);
                lbsparql1.setText (Messages.getString("LinkDialog.URI1.Label"));
                 props.setLook( lbsparql1 );
                fdsparql1=new FormData();
                fdsparql1.left = new FormAttachment(0, 0);
		fdsparql1.right= new FormAttachment(middle, -margin);
		fdsparql1.top  = new FormAttachment(wStepname, margin); 
                lbsparql1.setLayoutData(fdsparql1);
                txtsparlq1 = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
              props.setLook( txtsparlq1 );
                 txtsparlq1.addModifyListener(lsMod);
                  fdtsparql1 =new FormData();
                fdtsparql1.left = new FormAttachment(middle, 0);
		fdtsparql1.right= new FormAttachment(100, 0);
		fdtsparql1.top  = new FormAttachment(wStepname, margin);
		txtsparlq1.setLayoutData(fdtsparql1);
                
                lbgraph1 = new Label (shell,SWT.RIGHT);
                lbgraph1.setText (Messages.getString("LinkDialog.Graph1.Label"));
                 props.setLook( lbgraph1 );
                fdlgraph1=new FormData();
                fdlgraph1.left = new FormAttachment(0, 0);
		fdlgraph1.right= new FormAttachment(middle, -margin);
		fdlgraph1.top  = new FormAttachment(txtsparlq1, margin); 
                lbgraph1.setLayoutData(fdlgraph1);
                txtgraph1 = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
                props.setLook( txtgraph1 );
                 txtgraph1.addModifyListener(lsMod);
                  fdtgraph1 =new FormData();
                fdtgraph1.left = new FormAttachment(middle, 0);
		fdtgraph1.right= new FormAttachment(100, 0);
		fdtgraph1.top  = new FormAttachment(txtsparlq1, margin);
		txtgraph1.setLayoutData(fdtgraph1);
                
                 
                lbsparql2 = new Label (shell,SWT.RIGHT);
                lbsparql2.setText (Messages.getString("LinkDialog.URI2.Label"));
                props.setLook( lbsparql2 );
                fdsparql2=new FormData();
                fdsparql2.left = new FormAttachment(0, 0);
		fdsparql2.right= new FormAttachment(middle, -margin);
		fdsparql2.top  = new FormAttachment(txtgraph1, margin); 
                lbsparql2.setLayoutData(fdsparql2);
                txtsparql2 = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
                 props.setLook( txtsparql2 );
                 txtsparql2.addModifyListener(lsMod);
                  fdtsparql2 =new FormData();
                fdtsparql2.left = new FormAttachment(middle, 0);
		fdtsparql2.right= new FormAttachment(100, 0);
		fdtsparql2.top  = new FormAttachment(txtgraph1, margin);
		txtsparql2.setLayoutData(fdtsparql2);
                
                lbgraph2 = new Label (shell,SWT.RIGHT);
                lbgraph2.setText (Messages.getString("LinkDialog.Graph2.Label"));
                 props.setLook( lbgraph2 );
                fdlgraph2=new FormData();
                fdlgraph2.left = new FormAttachment(0, 0);
		fdlgraph2.right= new FormAttachment(middle, -margin);
		fdlgraph2.top  = new FormAttachment(txtsparql2, margin); 
                lbgraph2.setLayoutData(fdlgraph2);
                txtgraph2 = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
                props.setLook( txtgraph2 );
                 txtgraph2.addModifyListener(lsMod);
                  fdtgraph2 =new FormData();
                fdtgraph2.left = new FormAttachment(middle, 0);
		fdtgraph2.right= new FormAttachment(100, 0);
		fdtgraph2.top  = new FormAttachment(txtsparql2, margin);
		txtgraph2.setLayoutData(fdtgraph2);    



                  // file
                
                lbfileinput = new Label(shell, SWT.RIGHT);
		lbfileinput.setText(Messages.getString("LinkDialog.File.label"));
		props.setLook(lbfileinput);
		 fdlfileinput = new FormData();
		fdlfileinput.left = new FormAttachment(0, 0);
		fdlfileinput.right = new FormAttachment(middle, -margin);
		fdlfileinput.top = new FormAttachment(txtgraph2, margin);
		lbfileinput.setLayoutData(fdlfileinput);
                
                txtfileinput = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(txtfileinput);
		txtfileinput.addModifyListener(lsMod);
		txtfileinput.setToolTipText(Messages.getString("LinkDialog.tooltip.Txtfile"));
		fdtfileinput = new FormData();
		fdtfileinput.left = new FormAttachment(middle, 0);
		fdtfileinput.right = new FormAttachment(80, 0);
		fdtfileinput.top = new FormAttachment(txtgraph2, margin);
		txtfileinput.setLayoutData(fdtfileinput);
		txtfileinput.setEditable(false);
                
                btfileinput = new Button(shell, SWT.PUSH);
		props.setLook(btfileinput);
		btfileinput.setText(Messages.getString("LinkDialog.button.Txtbutton"));
		btfileinput.setToolTipText(BaseMessages.getString(PKG,
				"System.Tooltip.BrowseForFileOrDirAndAdd"));
		btfileinput.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				LoadFile();
			}
		});
		btfdfileinput = new FormData();
		btfdfileinput.left = new FormAttachment(txtfileinput, 0);
		btfdfileinput.top = new FormAttachment(txtgraph2, margin);
		btfileinput.setLayoutData(btfdfileinput);
                
                
                //clear
                
                btclearinput = new Button(shell, SWT.PUSH);
		props.setLook(btclearinput);
		btclearinput.setText(Messages.getString("LinkDialog.button.TxtClear"));
		btclearinput.setToolTipText(Messages.getString("LinkDialog.tooltip.TxtClear"));
		btclearinput.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				clean();
			}
		});
		btfclearinput = new FormData();
		btfclearinput.left = new FormAttachment(btfileinput, 0);
		btfclearinput.top = new FormAttachment(txtgraph2, margin);
		btclearinput.setLayoutData(btfclearinput);

                // COnfiguraciones
                
                lbumbral1 = new Label (shell,SWT.RIGHT);
                lbumbral1.setText (Messages.getString("LinkDialog.UMBRAL1.Label"));
                 props.setLook( lbumbral1 );
                fdlumbral1=new FormData();
               fdlumbral1.left = new FormAttachment(0, 0);
		fdlumbral1.right= new FormAttachment(middle, -margin);
		fdlumbral1.top  = new FormAttachment(txtfileinput, margin); 
               lbumbral1.setLayoutData(fdlumbral1);
                txtumbral1 = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
              props.setLook( txtumbral1 );
                 txtumbral1.addModifyListener(lsMod);
                 fdtumbral1 =new FormData();
                fdtumbral1.left = new FormAttachment(middle, 0);
		fdtumbral1.right= new FormAttachment(50, 0);
		fdtumbral1.top  = new FormAttachment(txtfileinput, margin);
		txtumbral1.setLayoutData(fdtumbral1);
                
                lbumbral2 = new Label (shell,SWT.RIGHT);
                lbumbral2.setText (Messages.getString("LinkDialog.UMBRAL2.Label"));
                props.setLook( lbumbral2 );
                fdlumbral2=new FormData();
                fdlumbral2.left = new FormAttachment(0, 0);
		fdlumbral2.right= new FormAttachment(middle, -margin);
		fdlumbral2.top  = new FormAttachment(txtumbral1, margin); 
                lbumbral2.setLayoutData(fdlumbral2);
                txtumbral2 = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
                 props.setLook( txtumbral2 );
                 txtumbral2.addModifyListener(lsMod);
                  fdtumbral2 =new FormData();
                fdtumbral2.left = new FormAttachment(middle, 0);
		fdtumbral2.right= new FormAttachment(100, 0);
		fdtumbral2.top  = new FormAttachment(txtumbral1, margin);
		txtumbral2.setLayoutData(fdtumbral2);
                
                
                         
                //Salida dir
                
                lbfileoutput = new Label(shell, SWT.RIGHT);
		lbfileoutput.setText(Messages.getString("LinkDialog.Dir.label"));
		props.setLook(lbfileoutput);
		 fdldiroutput = new FormData();
		fdldiroutput.left = new FormAttachment(0, 0);
		fdldiroutput.right = new FormAttachment(middle, -margin);
		fdldiroutput.top = new FormAttachment(txtumbral2, margin);
		lbfileoutput.setLayoutData(fdldiroutput);
                
                txtfileoutput = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(txtfileoutput);
		txtfileoutput.addModifyListener(lsMod);
		txtfileoutput.setToolTipText(Messages.getString("LinkDialog.tooltip.TxtDir"));
		fdtdiroutput = new FormData();
		fdtdiroutput.left = new FormAttachment(middle, 0);
		fdtdiroutput.right = new FormAttachment(80, 0);
		fdtdiroutput.top = new FormAttachment(txtumbral2, margin);
		txtfileoutput.setLayoutData(fdtdiroutput);
		txtfileoutput.setEditable(false);
                
                btfileoutput = new Button(shell, SWT.PUSH);
		props.setLook(btfileoutput);
		btfileoutput.setText(Messages.getString("LinkDialog.button.TxtDir"));
		btfileoutput.setToolTipText(BaseMessages.getString(PKG,
				"System.Tooltip.BrowseForFileOrDirAndAdd"));
		btfileoutput.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Directory();
			}
		});
		btfdfileoutput = new FormData();
		btfdfileoutput.left = new FormAttachment(txtfileoutput, 0);
		btfdfileoutput.top = new FormAttachment(txtumbral2, margin);
		btfileoutput.setLayoutData(btfdfileoutput);
                
               // btclearinput
                
                        
              
              /*  
		// ValName line
		wlValName=new Label(shell, SWT.RIGHT);
		wlValName.setText(Messages.getString("DummyPluginDialog.ValueName.Label")); //$NON-NLS-1$
        props.setLook( wlValName );
		fdlValName=new FormData();
		fdlValName.left = new FormAttachment(0, 0);
		fdlValName.right= new FormAttachment(middle, -margin);
		fdlValName.top  = new FormAttachment(txtumbral2, margin);
		wlValName.setLayoutData(fdlValName);
		wValName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook( wValName );
		wValName.addModifyListener(lsMod);
		fdValName=new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		fdValName.right= new FormAttachment(100, 0);
		fdValName.top  = new FormAttachment(txtumbral2, margin);
		wValName.setLayoutData(fdValName);

		// Value line
		wlValue=new Label(shell, SWT.RIGHT);
		wlValue.setText(Messages.getString("DummyPluginDialog.ValueToAdd.Label")); //$NON-NLS-1$
        props.setLook( wlValue );
		fdlValue=new FormData();
		fdlValue.left = new FormAttachment(0, 0);
		fdlValue.right= new FormAttachment(middle, -margin);
		fdlValue.top  = new FormAttachment(wValName, margin);
		wlValue.setLayoutData(fdlValue);

		wbValue=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook( wbValue );
		wbValue.setText(Messages.getString("System.Button.Edit")); //$NON-NLS-1$
		fdbValue=new FormData();
		fdbValue.right= new FormAttachment(100, 0);
		fdbValue.top  = new FormAttachment(wValName, margin);
		wbValue.setLayoutData(fdbValue);

		wValue=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
                props.setLook( wValue );
		wValue.addModifyListener(lsMod);
		fdValue=new FormData();
		fdValue.left = new FormAttachment(middle, 0);
		fdValue.right= new FormAttachment(wbValue, -margin);
		fdValue.top  = new FormAttachment(wValName, margin);
		wValue.setLayoutData(fdValue);

		wbValue.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				ValueMetaAndData v = (ValueMetaAndData) value.clone();
				EnterValueDialog evd = new EnterValueDialog(shell, SWT.NONE, v.getValueMeta(), v.getValueData());
				ValueMetaAndData newval = evd.open();
				if (newval!=null)
				{
					value = newval;
					getData();
				}
			}
		});
                    */        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

        //BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel}, margin, wValue);
            BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel}, margin, txtfileoutput);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
	//	wValName.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
		    if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	// Read data from input (TextFileInputInfo)
	public void getData()
	{   
            populateDialog();
            
		/*wStepname.selectAll();
		if (value!=null)
		{
			wValName.setText(value.getValueMeta().getName());
			wValue.setText(value.toString()+" ("+value.toStringMeta()+")"); //$NON-NLS-1$ //$NON-NLS-2$
		}*/
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value
		//value.getValueMeta().setName(wValName.getText());
		//input.setValue( value );
                setDialogMetadata();
		dispose();
	}
        
        private void populateDialog() {
        
        
        if (input.getSparql1() == null) {
			txtsparlq1.setText("");
		} else {
			txtsparlq1.setText(input.getSparql1());
		}
        
         if (input.getSparql2() == null) {
			txtsparql2.setText("");
		} else {
			txtsparql2.setText(input.getSparql2());
		}
          if (input.getFileinput() == null) {
			txtfileinput.setText("");
		} else {
			txtfileinput.setText(input.getFileinput());
		}
        
          if (input.getUmbral1() ==  null) {
			txtumbral1.setText("");
		} else {
			txtumbral1.setText(input.getUmbral1());
		}
        
           if (input.getUmbral2() == null) {
			txtumbral2.setText("");
		} else {
			txtumbral2.setText(input.getUmbral2());
		}
           
             if (input.getGraph1() == null) {
			txtgraph1.setText("");
		} else {
			txtgraph1.setText(input.getGraph1());
		}
             
               if (input.getGraph2() == null) {
			txtgraph2.setText("");
		} else {
			txtgraph2.setText(input.getGraph2());
		}
             if (input.getDiroutput() == null) {
			txtfileoutput.setText("");
		} else {
			txtfileoutput.setText(input.getDiroutput());
		}
        
        }
        
         private void setDialogMetadata() {
		stepname = wStepname.getText();
		//input.setStepName(stepname);
                input.setSparql1(txtsparlq1.getText());
                input.setSparql2(txtsparql2.getText());
                input.setFileinput(txtfileinput.getText());
                input.setUmbral1(txtumbral1.getText());
                input.setUmbral2(txtumbral2.getText());
                input.setGraph1(txtgraph1.getText());
                input.setGraph2(txtgraph2.getText());
                input.setDiroutput(txtfileoutput.getText());
                
		
        }
         
         private void LoadFile() {
		try {
			FileDialog dialog = new FileDialog(shell, 4096);
			dialog.setText(BaseMessages.getString(PKG,
					"LinkDialog.FileName.Choose"));
			String result = dialog.open();
			txtfileinput.setText(result);
                        if (!result.equals(""))
                        {
                            txtsparlq1.setEnabled(false);
                            txtsparql2.setEnabled(false);
                            txtgraph1.setEnabled(false);
                            txtgraph2.setEnabled(false);
                            txtumbral1.setEnabled(false);
                            txtumbral2.setEnabled(false);
                        }
		} catch (Exception e) {
		}
	}
         
         	private void Directory() {
		try {
			DirectoryDialog directorio = new DirectoryDialog(shell, 4096);
			directorio.setText(BaseMessages.getString(PKG,
					"LinkDialog.DirName.Choose"));
			String result = directorio.open();
			txtfileoutput.setText(result);
		} catch (Exception e) {
		}
	}
                
                        	private void clean () {
                            txtfileinput.setText("");
		            txtsparlq1.setEnabled(true);
                            txtsparql2.setEnabled(true);
                            txtgraph1.setEnabled(true);
                            txtgraph2.setEnabled(true);
                            txtumbral1.setEnabled(true);
                            txtumbral2.setEnabled(true);
	}
}