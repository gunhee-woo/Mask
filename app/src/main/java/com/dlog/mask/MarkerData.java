package com.dlog.mask;

public class MarkerData {
    private String markerText;
    private int markerImage;

    public MarkerData(String markerText, int markerImage) {
        this.markerText = markerText;
        this.markerImage = markerImage;
    }

    public String getMarkerText() {
        return markerText;
    }

    public void setMarkerText(String markerText) {
        this.markerText = markerText;
    }

    public int getMarkerImage() {
        return markerImage;
    }

    public void setMarkerImage(int markerImage) {
        this.markerImage = markerImage;
    }
}
