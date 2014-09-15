package com.ucuenca.pentaho.plugin.step.marc21loader.util;

public class MARC {

	 private int numero_registro;
	    private String campo;
	    private char subcampo;
	    private String valor;

	    public MARC() {
	        
	    }
	    
	    public MARC(int numero_registro, String campo, char subcampo, String valor) {
	        this.numero_registro = numero_registro;
	        this.campo = campo;
	        this.subcampo = subcampo;
	        this.valor = valor;
	    }

	    
	    public int getNumero_registro() {
	        return numero_registro;
	    }

	    public void setNumero_registro(int numero_registro) {
	        this.numero_registro = numero_registro;
	    }

	    public String getCampo() {
	        return campo;
	    }

	    public void setCampo(String campo) {
	        this.campo = campo;
	    }

	    public char getSubcampo() {
	        return subcampo;
	    }

	    public void setSubcampo(char subcampo) {
	        this.subcampo = subcampo;
	    }

	    public String getValor() {
	        return valor;
	    }

	    public void setValor(String valor) {
	        this.valor = valor;
	    }
	
}
