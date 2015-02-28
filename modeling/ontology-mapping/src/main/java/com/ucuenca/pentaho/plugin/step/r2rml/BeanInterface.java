package com.ucuenca.pentaho.plugin.step.r2rml;

public abstract class BeanInterface {
	
	public String getFieldName(String attribute) {
		String fieldName = null;
		try {
			fieldName = ((Bean)this.getClass().getDeclaredField(attribute.toLowerCase()).getDeclaredAnnotations()[0]).fieldName();
		}catch(NoSuchFieldException nsfe) {
			
		}
		return fieldName;
	}

}
