package com.ucuenca.pentaho.plugin.step.r2rml;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.vocabulary.XSD;

/**
 * XSD Schema datatypes processor
 * @author depcc
 *
 */
public class DataTypeProcessor {
	
	private static String[] dataCache = null;
	
	/**
	 * Gets XSD Schema data types
	 * @return
	 */
	public static String[] getDataTypes() {
		if(dataCache == null) {
			List<String> dataTypes = new ArrayList<String>();
			for(Field xsdField:XSD.class.getDeclaredFields()) {
				String fieldName = xsdField.getName();
				if(fieldName.substring(0, 1).equals( fieldName.substring(0, 1).toLowerCase() )) {
					fieldName = fieldName.substring(0, 1).equals("x") ? fieldName.substring(1):fieldName;
					dataTypes.add(fieldName);
				}
			}
			dataCache = dataTypes.toArray(new String[dataTypes.size()]);
		}
		return dataCache;
	}

}
