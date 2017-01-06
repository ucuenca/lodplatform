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

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * 
 * 
 * @author Matt
 * @since  24-mrt-2005
 */
public class LinkGenerationData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;

    public LinkGenerationData ()
	{
		super();
	}
}