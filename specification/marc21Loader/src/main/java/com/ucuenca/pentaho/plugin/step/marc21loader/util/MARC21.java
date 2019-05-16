package com.ucuenca.pentaho.plugin.step.marc21loader.util;

public class MARC21 {

    private String recordNumber;
    private String secuence;
    private String field;
    private String indicators;
    private char subfield;
    private String leadersubfields;
    private String value;

    public MARC21() {

    }

    public MARC21(String recordNumber, String secuence, String field, String indicators, char subfield, String leaderSubfields, String value) {
        this.recordNumber = recordNumber;
        this.secuence = secuence;
        this.field = field;
        this.indicators = indicators;
        this.subfield = subfield;
        this.leadersubfields = leaderSubfields;
        this.value = value;
    }

    public String getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getIndicators() {
        return indicators;
    }

    public void setIndicators(String indicators) {
        this.indicators = indicators;
    }

    public char getSubfield() {
        return subfield;
    }

    public void setSubfield(char subfield) {
        this.subfield = subfield;
    }

    public String getLeadersubfields() {
        return leadersubfields;
    }

    public void setLeadersubfields(String leadersubfields) {
        this.leadersubfields = leadersubfields;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSecuence() {
        return secuence;
    }

    public void setSecuence(String secuence) {
        this.secuence = secuence;
    }


}
