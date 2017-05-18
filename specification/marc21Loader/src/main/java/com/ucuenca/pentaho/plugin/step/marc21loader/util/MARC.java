package com.ucuenca.pentaho.plugin.step.marc21loader.util;

public class MARC {

    private int numero_registro;
    private String campo;
    private String secuencia;
    private char subcampo;
    private String valor;

    public MARC() {

    }

    public MARC(int numero_registro, String secuencia, String campo, char subcampo, String valor) {
        this.numero_registro = numero_registro;
        this.secuencia = secuencia;
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

    public String getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(String secuencia) {
        this.secuencia = secuencia;
    }

}
