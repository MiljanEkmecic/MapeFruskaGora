package com.example.korisnik.sumarskemape;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Miljan on 7/4/2016.
 */
public class XmlParse {

    public static boolean srpski = true;
    private String xmlData;
    private ArrayList<PlaceMark> applications;

    public XmlParse(String xmlData){
        this.xmlData = xmlData;
        applications = new ArrayList<>();
    }

    public ArrayList<PlaceMark> getApplications() {
        return applications;
    }

    public List<PlaceMark> process(){
        List<PlaceMark> placeMarkList = new ArrayList<>();
        PlaceMark currentRecord = null;
        boolean inEntry = false;
        String textValue = "";
        String slika,folder = "";
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();
            boolean hasImage = false, inMLine = false;
            String categorie = "";

            while(eventType != XmlPullParser.END_DOCUMENT){
                String tagName = xpp.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if(tagName.equalsIgnoreCase("Placemark")){
                            inEntry = true;
                            currentRecord = new PlaceMark();

                            placeMarkList.add(currentRecord);
                        }

                        else if(tagName.equals("Data")){
                            String imageUrl = "";
                            imageUrl = xpp.getAttributeValue(0);
                            if(imageUrl.equals("image")){
                                hasImage = true;
                            }
                        }

                        else if(tagName.equals("MultiGeometry"))
                            inMLine = true;


                        break;

                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:


                        if(tagName.equalsIgnoreCase("names")) {
                            categorie = textValue;
                            MainActivity.categories.add(categorie);
                        }


                        if(inEntry) {
                            if(tagName.equalsIgnoreCase("PlaceMark")) {
                                applications.add(currentRecord);
                                inEntry = false;
                                currentRecord.setCategorie(categorie);
                            } else if(tagName.equalsIgnoreCase("name"))
                                currentRecord.setName(textValue);


                             else if(tagName.equalsIgnoreCase("longitude"))
                                currentRecord.setLon(Double.parseDouble(textValue));

                             else if(tagName.equalsIgnoreCase("altitude"))
                                currentRecord.setAlt(Double.parseDouble(textValue));

                            else if(tagName.equalsIgnoreCase("coordinates")) {
                                String []koor = textValue.split(" ");

                                if(!inMLine){
                                    String []koord = textValue.split(",");
                                    currentRecord.setLon(Double.parseDouble(koord[0]));
                                    currentRecord.setLat(Double.parseDouble(koord[1]));
                                    currentRecord.setAlt(Double.parseDouble(koord[2]));
                                }
                                else{
                                    //for (String koo: koor)
                                    currentRecord.addPath(textValue);

                                    inMLine = false;
                                }
                            }
                            else if(tagName.equalsIgnoreCase("styleUrl")) {
                                currentRecord.setStyleUrl((textValue));
                            }
                            else if(tagName.equalsIgnoreCase("heading")) {
                                currentRecord.setHeading(Double.parseDouble(textValue));
                            }
                            else if(tagName.equalsIgnoreCase("tilt"))
                                currentRecord.setTilt(Double.parseDouble(textValue));

                            else if(tagName.equalsIgnoreCase("range"))
                                currentRecord.setRange(Double.parseDouble(textValue));

                            else if(tagName.equalsIgnoreCase("altitudeMode"))
                                currentRecord.setAltMode(textValue);


                           else if(tagName.equals("value")){
                                if (hasImage){
                                    currentRecord.setImg(textValue);
                                    hasImage = false;
                                }

                            }
                        }
                        break;
                    default:
                        //System.out.print(inEntry);
                }
                eventType = xpp.next();
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }

        Collections.swap(MainActivity.categories, 1, MainActivity.categories.size() - 1);
        return placeMarkList;
    }
}
