package com.dlog.mask;

public class PharmacyData {
    private String straddr;
    private String strCode;
    private String strCreate;
    private double dLat;
    private double dLng;
    private String strName;
    private String strRemain;
    private String strStock;
    private String strType;
    private float distance_to_curr_location;

    public PharmacyData() {};

    public PharmacyData(String straddr, String strCode, String strCreate, double dLat, double dLng, String strName, String strRemain, String strStock, String strType) {
        this.straddr = straddr;
        this.strCode = strCode;
        this.strCreate = strCreate;
        this.dLat = dLat;
        this.dLng = dLng;
        this.strName = strName;
        this.strRemain = strRemain;
        this.strStock = strStock;
        this.strType = strType;
    }

    public String getStraddr() {
        return straddr;
    }

    public void setStraddr(String straddr) {
        this.straddr = straddr;
    }

    public String getStrCode() {
        return strCode;
    }

    public void setStrCode(String strCode) {
        this.strCode = strCode;
    }

    public String getStrCreate() {
        return strCreate;
    }

    public void setStrCreate(String strCreate) {
        this.strCreate = strCreate;
    }

    public double getdLat() {
        return dLat;
    }

    public void setdLat(double dLat) {
        this.dLat = dLat;
    }

    public double getdLng() {
        return dLng;
    }

    public void setdLng(double dLng) {
        this.dLng = dLng;
    }

    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }

    public String getStrRemain() {
        return strRemain;
    }

    public void setStrRemain(String strRemain) {
        this.strRemain = strRemain;
    }

    public String getStrStock() {
        return strStock;
    }

    public void setStrStock(String strStock) {
        this.strStock = strStock;
    }

    public String getStrType() {
        return strType;
    }

    public void setStrType(String strType) {
        this.strType = strType;
    }

    public float getDistance_to_curr_location() {
        return distance_to_curr_location;
    }

    public void setDistance_to_curr_location(float distance_to_curr_location) {
        this.distance_to_curr_location = distance_to_curr_location;
    }
}
