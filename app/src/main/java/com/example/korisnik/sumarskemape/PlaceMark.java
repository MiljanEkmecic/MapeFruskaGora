package com.example.korisnik.sumarskemape;

import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Miljan on 7/4/2016.
 */
public class PlaceMark {


    private String name;
    private String styleUrl;
    private String altMode;
    private String img;
    private String categorie;
    private String pathsStr = "";
    private double lat,lon,alt,heading, tilt,range;
    private boolean hasPath = false;

    private List<LatLng> paths = new ArrayList<>();

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public List<LatLng> getPaths() {
        return paths;
    }

    public void setPath(List<LatLng> paths) {
        this.paths = paths;
    }


    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public String getAltMode() {
        return altMode;
    }

    public void setAltMode(String altMode) {
        this.altMode = altMode;
    }

    public double getTilt() {
        return tilt;
    }

    public void setTilt(double tilt) {
        this.tilt = tilt;
    }

    public String getStyleUrl() {
        return styleUrl;
    }

    public void setStyleUrl(String styleUrl) {
        this.styleUrl = styleUrl;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public String getPathStr(){
        return pathsStr;
    }

    public void addPath(String text){

        hasPath = true;
        pathsStr = text;
        String[] three = text.split(" ");

        for (String path: three) {
            if(path.contains(",")) {
                String[] koor = path.split(",");
                paths.add(new LatLng(Double.parseDouble(koor[1]), Double.parseDouble(koor[0])));

            }

        }

    }

    public void setPath(String newPath){
        String[] koor;
        String []path = newPath.split(" ");
        for (String three: path) {
            koor = three.split(",");
            paths.add(new LatLng(Double.parseDouble(koor[1]), Double.parseDouble(koor[0])));
        }
    }

    @Override
    public String toString() {

        return getName() + " ," + getLon()+ " ," + getLat()+ " ," + getCategorie();
    }


    public boolean isHasPath() {
        return hasPath;
    }

    public void setHasPath(boolean hasPath) {
        this.hasPath = hasPath;
    }

    public String getPathsStr() {
        return pathsStr;
    }

    public void setPathsStr(String pathsStr) {
        this.pathsStr = pathsStr;
    }
}
