package com.example.korisnik.sumarskemape;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Miljan on 7/6/2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Sumarskemape.db";
    public static final int DATABASE_VERSION = 13;

    public static final String PLACEMARK_ID = "id_placemark";
    public static final String PLACEMARK_TABLE_NAME = "placemarks";
    public static final String PLACEMARK_KATEGORIJA = "category";
    public static final String PLACEMARK_IME = "name";
    public static final String PLACEMARK_LATITDUA = "latitude";
    public static final String PLACEMARK_LONGITUDA = "longitude";
    public static final String PLACEMARK_ALTITUDA = "altitude";
    public static final String PLACEMARK_PATH = "path";
    public static final String PLACEMARK_IMG = "img";
    public static final String PLACEMARK_HASPATH = "haspath";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_PLACEMARK_TABLE = "CREATE TABLE " + PLACEMARK_TABLE_NAME + "(" + PLACEMARK_ID + " INTEGER PRIMARY KEY,"+ PLACEMARK_KATEGORIJA + " TEXT," +
                PLACEMARK_IME + " TEXT," + PLACEMARK_LATITDUA + " REAL," + PLACEMARK_LONGITUDA + " REAL," +
                PLACEMARK_ALTITUDA + " REAL," + PLACEMARK_PATH + " TEXT,"+ PLACEMARK_HASPATH + " INTEGER," + PLACEMARK_IMG + " TEXT" +")";

        db.execSQL(CREATE_PLACEMARK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + PLACEMARK_TABLE_NAME);
        onCreate(db);
    }

    //inserting single Placemark object in db
    public long insertPlacemark(PlaceMark placeMark) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();

        values.put(PLACEMARK_IME, placeMark.getName());
        values.put(PLACEMARK_KATEGORIJA, placeMark.getCategorie());
        values.put(PLACEMARK_LATITDUA, placeMark.getLat());
        values.put(PLACEMARK_LONGITUDA, placeMark.getLon());
        values.put(PLACEMARK_ALTITUDA, placeMark.getAlt());
        if (placeMark.isHasPath()) {
            values.put(PLACEMARK_PATH, placeMark.getPathsStr());
            values.put(PLACEMARK_HASPATH, 1);
        }
        else {
            values.put(PLACEMARK_PATH, 0);
            values.put(PLACEMARK_HASPATH, 0);
        }
        values.put(PLACEMARK_IMG, placeMark.getImg());


        long placemark_id = db.insert(PLACEMARK_TABLE_NAME, null, values);

        return placemark_id;
    }


    //Returns list of Placemarks by category passed
    public List<PlaceMark> getPlaceMarkList(String category){

        SQLiteDatabase db = this.getReadableDatabase();
        List<PlaceMark> list = new ArrayList<>();

        String query = "SELECT * FROM placemarks WHERE category =?";

        // Executing query
        Cursor cursor = db.rawQuery(query, new String [] {category});
        if (cursor != null)
            cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            PlaceMark pm = new PlaceMark();
            // Getting the data from Cursor and adding into new Placemark object
            pm.setCategorie((cursor.getString(cursor.getColumnIndex(PLACEMARK_KATEGORIJA))));
            pm.setName((cursor.getString(cursor.getColumnIndex(PLACEMARK_IME))));
            pm.setImg((cursor.getString(cursor.getColumnIndex(PLACEMARK_IMG))));
            pm.setLat(Double.parseDouble(cursor.getString(cursor.getColumnIndex(PLACEMARK_LATITDUA))));
            pm.setLon(Double.parseDouble(cursor.getString(cursor.getColumnIndex(PLACEMARK_LONGITUDA))));
            pm.setAlt(Double.parseDouble(cursor.getString(cursor.getColumnIndex(PLACEMARK_ALTITUDA))));

            if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(PLACEMARK_HASPATH))) == 1)
                pm.addPath(cursor.getString(cursor.getColumnIndex(PLACEMARK_PATH)));



            list.add(pm);

            cursor.moveToNext();
        }

        return list;
    }
}
