package com.example.korisnik.sumarskemape;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;


public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, MenuFragment.OnFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Marker marker;
    ImageView gpsBtnView;
    LocationManager locationManager;
    String mprovider;
    LatLng gpsLocation, getGpsLocation;
    public static GoogleMap mMap;
    public static HashMap<String, List<Marker>> markers = new HashMap<>();
    public static HashMap<String, List<Polyline>> polylines = new HashMap<>();
    public static List<String> kategorije = new ArrayList<>();
    DatabaseHandler db;
    public static RelativeLayout gpsLayout;
    public static boolean firstCall = true, languageChanged = false;
    public static String lang;
    private GoogleApiClient mLocationClient;
    private com.google.android.gms.location.LocationListener mListener;
    public static  TextView  textView;
    private double yourLatitude;
    private double yourLongitude;
    public static ArrayList<LatLng> lakeLocations;
    public static ArrayList<LatLng> picnicLocations;
    public static ArrayList<LatLng> streamLocations;
    public static ArrayList<Location> lakeLocationsL;
    public static ArrayList<Location>picnicLocationsL;
    public static ArrayList<Location>streamLocationsL;
    public static ArrayList<Marker>lakeMarker;
    public static ArrayList<Marker>picnicMarker;
    public static ArrayList<Marker>streamMarker;
    private View content;
    public static boolean setLakes=false;
    ArrayList<Polyline> polylinesList;
    Polyline polyline;
    ImageView infoImage;
    public static String checkMenu="";
    private String nearestLocation="";
    public String locationTitle;
    public static String distanceText="";
    public static String timeText="";
    public static LatLng searchedLocationGlobal;
    public static LatLng yourLocationGlobal;
    int toastCounter=0;
    public static boolean pathText=false;


    //[Lakes and Waterfalls, Miscellaneous, torrential streams, the main stream, bench, water source, springs, mountain home and catering, monument, monastery, picnic]
    public static List<String> categories = new ArrayList<>();
    private static List<PlaceMark> placeMarkList;

    private ListView categoriesLV;

    // lakes and waterfalls


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHandler(MainActivity.this);
        lakeLocations=new ArrayList<LatLng>();
        picnicLocations=new ArrayList<LatLng>();
        streamLocations=new ArrayList<LatLng>();
        lakeLocationsL=new ArrayList<Location>();
        picnicLocationsL=new ArrayList<Location>();
        streamLocationsL=new ArrayList<Location>();
        lakeMarker=new ArrayList<Marker>();
        picnicMarker=new ArrayList<Marker>();
        streamMarker=new ArrayList<Marker>();

        polylinesList=new ArrayList<Polyline>();



        if (languageChanged) {
            firstCall = false;

        }

        if (firstCall) {

            lang = "sr"; // your language
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());

            if (!MenuFragment.finish) {
                XmlParse mlx = new XmlParse(loadText(R.raw.doc));
                placeMarkList = mlx.process();
                MenuFragment.finish = true;
            }


            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (PlaceMark pm : placeMarkList) {
                        db.insertPlacemark(pm);
                    }
                }
            }).start();


        }

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text_view_top);

        if (firstCall) {
            final LinearLayout sl = (LinearLayout) findViewById(R.id.splash_layout);
            sl.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sl.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();

            firstCall = false;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        mprovider = locationManager.getBestProvider(criteria, false);
        gpsLocation = new LatLng(0, 0);
        // niz = new String[9];
        gpsBtnView = (ImageView) findViewById(R.id.gps_btn);
//
        // Initialize fragmet for maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        gpsLayout = (RelativeLayout) findViewById(R.id.gps_layout);

        // adding location client
        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationClient.connect();



        gpsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mprovider != null && !mprovider.equals("")) {

                    Location location = locationManager.getLastKnownLocation(mprovider);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(mprovider, 5000, 1, MainActivity.this);
                }
            }
        });

        // SETTING BUTTONS FOR DIFFERENT MAP TYPES
        ImageButton gpsNormal, gpsSattelite, gpsTerrain;
        gpsNormal = (ImageButton) findViewById(R.id.gps_normal);
        gpsSattelite = (ImageButton) findViewById(R.id.gps_sattelite);
        gpsTerrain = (ImageButton) findViewById(R.id.gps_terrain);

        //SETTING IMAGE FOR INFO WINDOW


        if (gpsNormal != null) {
            gpsNormal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    showCurrentLocation();
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(60000);
                    locationRequest.setFastestInterval(35000);
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, locationRequest, mListener);


                }
            });
        }

        if (gpsSattelite != null) {
            gpsSattelite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
            });
        }

        if (gpsTerrain != null) {
            gpsTerrain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                 //   findNearestRoot();
                }
            });
        }
        // categoriesLV = (ListView) findViewById(R.id.categorie_lv);

       /* categoriesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                List<PlaceMark> list = new ArrayList<>();
                String category = categories.get(position);
                list = getPlaceMarkList(category);

                for (PlaceMark m:list) {

                    //TODO: Crtanje za svaki placemark i provera da li je marker ili poligon
                }
            }
        });
*/



    }

    public void onClick(View view) {

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();

                getGpsLocation = new LatLng(latitude, longitude);

//                    // Moving camera to location of marker
                mMap.moveCamera(CameraUpdateFactory.newLatLng(getGpsLocation));

            }

        } else {
            Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);

        }
    }


    //Čitanje XML
    public String loadText(int resourceId) {
        // The InputStream opens the resourceId and sends it to the buffer
        StringBuilder sb = new StringBuilder();

        InputStream is = this.getResources().openRawResource(resourceId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String readLine = null;

        try {
            // While the BufferedReader readLine is not null
            while ((readLine = br.readLine()) != null) {
                // Log.d("TEXT", readLine);
                sb.append(readLine);
            }


            // Close the InputStream and BufferedReader
            is.close();
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("START");
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public static List<PlaceMark> getPlaceMarkList(String category) {

        List<PlaceMark> list = new ArrayList<>();

        for (PlaceMark m : placeMarkList) {

            if (m.getCategorie().contains(category))
                list.add(m);
        }

        return list;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
     /*   //set user's location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        */


        int lng;




        String snippetFruskaGora="Fruška gora (srp. Фрушка гора, mađ. Tarcal, lat. Alma Mons) planina je u severnom Sremu. Veći deo planine nalazi se u Srbiji (Vojvodina), dok se zapadni obronci planine nalaze u Hrvatskoj (Vukovarsko-srijemska županija).\n" +
                "Fruška gora se prostire dužinom od oko 75 km i širinom od 12 do 15 km i zahvata površinu od 255 km².\n" +
                "Sjeverno i istočno od Fruške gore nalazi se rijeka Dunav. Planina se proteže oko 80 km zapad-istok i oko 15 km sjever-jug s najvišim vrhom Crveni Čot, koji se nalazi na 539 m nadmorske visine. Obronci Fruške gore poznati su po vinogradima, koji imaju veliki ugled u regiji. Od 1960. godine veći dio planine (25.525 ha) pretvoren je u nacionalni park, prvi u Srbiji[1] — Nacionalni park Fruška gora.";

        // Creating marker
        LatLng fruskaGora = new LatLng(45.157085, 19.709358);
       mMap.addMarker(new MarkerOptions().position(fruskaGora).title("Nacionalni park Fruska Gora").snippet(snippetFruskaGora));
        // Moving camera to location of marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(fruskaGora));
        // Zoom camera
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);
        mMap.animateCamera(zoom);
//        mMap.addPolyline(new PolylineOptions().addAll(db.getPlaceMarkList(categories.get(2)).get(18).getPaths()));
//      System.out.println("POLI : "+db.getPlaceMarkList(categories.get(2)).get(18).getPaths());
        addMarkersAndLines();

        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(getApplicationContext(),"Kliknuli ste na infoWindow",Toast.LENGTH_SHORT).show();
                markerRoot(marker);
            }
        });

/*
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
               String title= marker.getTitle();

                switch (title){
                    case "Kudos":
                        infoImage.setImageResource(R.drawable.kudos);
                        break;
                    case "Kapavica":
                        infoImage.setImageResource(R.drawable.kapavica);
                        break;
                    case "Podvezirac":
                        infoImage.setImageResource(R.drawable.podvezirac_jezero);
                        break;
                }
                return false;
            }
        });
        */

    }

    public void addMarkersAndLines() {
        int x = 0;

    }


    @Override
    public void categorySelected(String category) {
        System.out.println("Selected " + category);
        boolean hasPath = false;
        List<Polyline> polylineList = new ArrayList<>();
        List<Marker> markerList = new ArrayList<>();
        List<PlaceMark> placeMarks = db.getPlaceMarkList(category);
        System.out.println("PlacemarksFromCategory: " + placeMarks.size());
        for (PlaceMark m : placeMarks) {
            if (m.isHasPath()) {
                hasPath = true;
                System.out.println("CRTANJE POLI" + m);
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.addAll(m.getPaths()).color(Color.BLUE);
                Polyline poly = mMap.addPolyline(polylineOptions);
                polylineList.add(poly);

            } else {
                hasPath = false;
                System.out.println("CRTANJE MARKERA" + m);
                Marker marker = (mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(m.getLat(), m.getLon()))
                        .title(m.getName())));
                markerList.add(marker);
            }
        }

        if (hasPath) {
            polylines.put(category, polylineList);
        } else {
            markers.put(category, markerList);
        }
    }

    @Override
    public void languageChanged() {
        languageChanged = true;
    }

    public static void showGps(boolean show) {
        if (show)
            gpsLayout.setVisibility(View.VISIBLE);
        else
            gpsLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (languageChanged) {
            firstCall = false;

            languageChanged = false;
        } else {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(this);
            firstCall = true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(MainActivity.this, "Povezani ste na Google API klijenta!", Toast.LENGTH_SHORT).show();
        mListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(MainActivity.this, "Vasa lokacija je " + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                yourLatitude=location.getLatitude();
                yourLongitude=location.getLongitude();
                gotoLocation(location.getLatitude(), location.getLongitude(), 13);

                if (marker != null) {
                    marker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("Vasa lokacija").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).snippet("Vasa trenutna geografska sirina je:"+location.getLatitude()+"a geografska duzina je: "+location.getLongitude());
                marker = mMap.addMarker(markerOptions);

                yourLocationGlobal=new LatLng(location.getLatitude(),location.getLongitude());

               // updateDistanceAndTime(yourLocationGlobal,searchedLocationGlobal);

                if(searchedLocationGlobal!=null && yourLocationGlobal!=null) {

                    updateDistanceAndTime(yourLocationGlobal,searchedLocationGlobal);
                }
/*
                if(distanceText!="" & timeText!=""){
                    textView.setText("Razdaljina:" + distanceText + ", Vreme:" + timeText);
                }

                */

            }
        };

        /*

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(7000);
        locationRequest.setFastestInterval(3000);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, locationRequest, mListener);

         */
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);

        if (currentLocation == null) {
            Toast.makeText(MainActivity.this, "Can't connect to the map!", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 13);
            mMap.animateCamera(update);
        }
    }

    public void gotoLocation(double latitude, double longitude, float zoom) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, mListener);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            //     Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "Kliknite na dugme Moja Lokacija da bi se iscrtala putanja do lokacije!", Toast.LENGTH_SHORT).show();
                pathText=false;
                toastCounter=0;
                return;
            }else{
                pathText=true;
                 toastCounter++;
            }

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.GREEN);
            }

            timeText=duration;
            distanceText=distance;

            textView.setText("Razdaljina:" + distance + ", Vreme:" + duration);
            // Drawing polyline in the Google Map for the i-th route
          /*  if(polylinesList.size()==0){
                polyline= mMap.addPolyline(lineOptions);
                polylinesList.add(polyline);
            }else {
                for(Polyline polyline : polylinesList) {
                    polyline.remove();
                }

                }
            */
            mMap.addPolyline(lineOptions);
            }
        }


    public static double findDistance(double lat1, double lng1, double lat2, double lng2) {

        double l1 = toRadians(lat1);
        double l2 = toRadians(lat2);
        double g1 = toRadians(lng1);
        double g2 = toRadians(lng2);

        double dist = acos(sin(l1) * sin(l2) + cos(l1) * cos(l2) * cos(g1 - g2));
        if (dist < 0) {
            dist = dist + Math.PI;
        }

        return Math.round(dist * 6378100);
    }




    public  void findNearestRoot(ArrayList<LatLng> locationList) {
        LatLng tvojaLokacija = new LatLng(yourLatitude, yourLongitude);
        yourLocationGlobal=tvojaLokacija;
        double udaljenost = 0;
        double rastojanje = 0;
        LatLng trazenaLokacija = new LatLng(0, 0);
        for (int i = 0; i < locationList.size(); i++) {
            if (i == 0) {
                udaljenost = pronadjiRazdaljinu(locationList.get(0).latitude, locationList.get(0).longitude, tvojaLokacija.latitude, tvojaLokacija.longitude);
                trazenaLokacija = locationList.get(0);
            }
            rastojanje = pronadjiRazdaljinu(locationList.get(i).latitude, locationList.get(i).longitude, tvojaLokacija.latitude, tvojaLokacija.longitude);
            if (udaljenost > rastojanje) {
                udaljenost = rastojanje;
                trazenaLokacija = locationList.get(i);
                searchedLocationGlobal=trazenaLokacija;
            }


        }
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(tvojaLokacija, trazenaLokacija);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
      //  Toast.makeText(MainActivity.this, "Iscrtana je putanja do najblize lokacije!", Toast.LENGTH_SHORT).show();
        if(pathText){
            Toast.makeText(MainActivity.this, "Iscrtana je putanja do najblize lokacije!", Toast.LENGTH_LONG).show();
        }

    }
    public static double pronadjiRazdaljinu(double lat1, double lng1, double lat2, double lng2) {

        double l1 = toRadians(lat1);
        double l2 = toRadians(lat2);
        double g1 = toRadians(lng1);
        double g2 = toRadians(lng2);

        double dist = acos(sin(l1) * sin(l2) + cos(l1) * cos(l2) * cos(g1 - g2));
        if(dist < 0) {
            dist = dist + Math.PI;
        }

        return Math.round(dist * 6378100);
    }


    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        MyInfoWindowAdapter() {
            content = getLayoutInflater().inflate(R.layout.info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;

        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub

            render(marker,content);

            return content;
        }

        private void render(Marker marker,View view){

            content.setBackgroundColor(Color.parseColor("#74a16b"));

            TextView tvLocation = (TextView)content.findViewById(R.id.tvLocation);
            tvLocation.setTextColor(Color.WHITE);

            TextView tvSnippet=(TextView)content.findViewById(R.id.tvSnippet);
            tvSnippet.setTextColor(Color.WHITE);

            TextView tvAnouncement = (TextView) content.findViewById(R.id.tvAnouncement);
            tvAnouncement.setTextColor(Color.RED);

            infoImage=(ImageView) content.findViewById(R.id.imageViewInfo);

            String title= marker.getTitle();

            switch (title){
                case "Kudos":
                    infoImage.setImageResource(R.drawable.kudos);
                    break;
                case "Kapavica":
                    infoImage.setImageResource(R.drawable.kapavica);
                    break;
                case "Podvezirac":
                    infoImage.setImageResource(R.drawable.podvezirac_jezero);
                    break;
                case "Osovlje":
                    infoImage.setImageResource(R.drawable.osovlje);
                    break;
                case "Strazilovo":
                    infoImage.setImageResource(R.drawable.strazilovo);
                    break;
                case "Lipovaca":
                    infoImage.setImageResource(R.drawable.lipovaca);
                    break;
                case "Andrevlje":
                    infoImage.setImageResource(R.drawable.andrevlje);
                    break;
                case "Nacionalni park Fruska Gora":
                   infoImage.setImageResource(R.drawable.fruska_gora_slika);
                    break;
                case "Vasa lokacija":
                    infoImage.setImageResource(R.drawable.korisnik_lokacija);
                    break;
                case "Kumpula":
                    infoImage.setImageResource(R.drawable.kumpula);
                    break;
                case "Zjalina":
                    infoImage.setImageResource(R.drawable.zjalina);
                    break;
                case "Vranjas":
                    infoImage.setImageResource(R.drawable.vranjas);
                    break;
                case "Popovica":
                    infoImage.setImageResource(R.drawable.popovica);
            }


            LatLng latLng=marker.getPosition();

            tvLocation.setText(marker.getTitle());
            tvSnippet.setText(Html.fromHtml(marker.getSnippet()));
            tvAnouncement.setText("Kliknite na prozor za putanju do lokacije!");
        }
    }


    public static void setLakesAndWaterfalls(GoogleMap mMap){
        //lakes and waterfalls

        String podveziracSnippet="Maleno osamljeno jezero, skriveno u karlovačkim atarima, kao da se guši u ovom krajoliku. Usled pojedinih doba u godini, ponekad ostavlja utisak bare. Naziv je dobilo verovatno po brdu Vezirac ispod kog se nalazi, a samo brdo poznato je po spomeniku Vezirac i bici koja se tu odigrala.\n" +
                "\n" +
                "Retki su kupači i pecaroši na ovom jezeru.";
        String kudosSnippet="Nedaleko od Rume, u smeru ka Fruškoj gori, prostire se jezero Kudoš, poznatije kao Pavlovačko jezero. Nastalo je podizanjem brane na istoimenom potoku za potrebe navodnjavanja zemljišta. Površina mu je oko 75 hektara i dubina oko šest metara. Zbog raznovrsne flore ovo jezero je bogato ribom" +
                " i zbog toga ga sportski ribolovci posećuju tokom cele godine. Od ribljih vrsta najzastupljeniji su šaran, som, smuđ, amur i tostolobik, a primerci su, po pričama ribolovaca, kapitalni. Mnogim vrstama ptica ovo jezero je postalo prirodno stanište. Leti postaje popularna destinacija za sve stanovnike lokalnih mesta, ali i za one koji žive malo dalje a žele da uživaju u čarima jezera.";

        String kapavicaSnippet="Najmanje veštačko jezero na Fruškoj gori smešteno je duboko u šumi blizu sela Vizić. Pune ga obližnji izvor i podzemne vode. Nekada je služilo za navodnjavanje, a sada više ima svrhu malog izletišta. Za njega mahom znaju meštani Vizića. Na ovom mestu nećete moći da pecate i plivate jer veličina jezera i trska to ne omogućavaju. Put do jezerceta ostavlja nesvakidašnji utisak – priroda i napuštene vikendice ukazuju na nekadašnju živost ovog mesta koje se sada prepustilo tišini prirode.";



        LatLng podvezirac = new LatLng(45.204570, 19.911633);
        Marker m1= mMap.addMarker(new MarkerOptions().position(podvezirac).title("Podvezirac").snippet(podveziracSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        LatLng kudos = new LatLng(45.06485, 19.79359);
        Marker m2= mMap.addMarker(new MarkerOptions().position(kudos).title("Kudos").snippet(kudosSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        LatLng kapavica = new LatLng(45.179902, 19.467301);
       Marker m3= mMap.addMarker(new MarkerOptions().position(kapavica).title("Kapavica").snippet(kapavicaSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));


        lakeLocations.add(podvezirac);
        lakeLocations.add(kudos);
        lakeLocations.add(kapavica);


        Location podveziracL=new Location("Podvezirac");
        podveziracL.setLatitude(45.204570);
        podveziracL.setLongitude(19.911633);

        Location kudosL=new Location("Kudos");
        kudosL.setLatitude(45.06485);
        kudosL.setLongitude(19.79359);

        Location kapavicaL=new Location("Kapavica");
        kapavicaL.setLatitude(45.179902);
        kapavicaL.setLongitude(19.467301);

        lakeLocationsL.add(podveziracL);
        lakeLocationsL.add(kudosL);
        lakeLocationsL.add(kapavicaL);

        lakeMarker.add(m1);
        lakeMarker.add(m2);
        lakeMarker.add(m3);
    }

    public static void setPicnic(GoogleMap mMap){
        String lipovacaSnippet="Omiljeno izletište Šiđana i ljudi iz okolnih sela, nedaleko od Berkasova na zapadnom kraičku Fruške gore, jeste Lipovača. Pomalo zapušteno, ali živog duha, ovo izletište i dalje vrvi od ljudi, pogotovo u vreme praznika. Simpatične stazice, bungalovi i livade čine ovo mesto posebnim, a motel koji više ne radi daje specifičan šarm ovom mestu";
        String andrevljeSnippet="Lokaciju odlikuje velika livada, svojevrsno ostrvo unutar guste šume, koja je jedno od omiljenih stecišta izletnika i kampera za prvomajske praznike. Prostrana livada, sa postavljenim golovima za mali fudbal, ljuljaškama i klackalicama, pogodna je za porodični izlet u prirodi. Od Andrevlja postoji pešačka staza do Letenke i Partizanskog puta, a dolazak vozilom je moguć iz smera Čerevića.";
        String strazilovoSnippet="Stražilovo je vrh na Fruškoj gori, na 321 metru nadmorske visine. Poznato je fruškogorsko izletište, udaljeno četiri kilometra od Sremskih Karlovaca, a njegovom značaju najviše doprinosi grob poznatog srpskog pesnika Branka Radičevića. Popularno je mesto odmora i boravka u prirodi Karlovčana, a takođe i rado okupljalište mladih i umetnika.";
        String osovljeSnippet="Objekat iz 1965. godine je u vlasništvu kompanije NIS (Naftagas) sa prvobitnom namenom organizvanja banketa, proslava i odmora zaposlenih u naftnoji industriji. Nakon privatizacije NIS-a, kompanija je izgubila interes za ovim odmaralištem. Jedini redovni posetioci su čuvari koji održavaju ovo mesto od propadanja. U sklopu odmarališta je autentičan mali park za decu koji nostalgično podseća na stara dobra vremena.";

        LatLng lipovaca=new LatLng(45.14300,19.27727);
       Marker m1= mMap.addMarker(new MarkerOptions().position(lipovaca).title("Lipovaca").snippet(lipovacaSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        LatLng andrevlje=new LatLng(45.17406,19.64632);
       Marker m2= mMap.addMarker(new MarkerOptions().position(andrevlje).title("Andrevlje").snippet(andrevljeSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        LatLng strazilovo=new LatLng(45.169336,19.917636);
       Marker m3= mMap.addMarker(new MarkerOptions().position(strazilovo).title("Strazilovo").snippet(strazilovoSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        LatLng osovlje=new LatLng(45.16291,19.710427);
        Marker m4=mMap.addMarker(new MarkerOptions().position(osovlje).title("Osovlje").snippet(osovljeSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        picnicLocations.add(lipovaca);
        picnicLocations.add(andrevlje);
        picnicLocations.add(strazilovo);
        picnicLocations.add(osovlje);

        Location lipovacaL=new Location("Lipovaca");
        lipovacaL.setLatitude(45.14300);
        lipovacaL.setLongitude(19.27727);

        Location andrevljeL=new Location("Andrevlje");
        andrevljeL.setLatitude(45.17406);
        andrevljeL.setLongitude(19.64632);

        Location strazilovoL=new Location("Strazilovo");
        strazilovoL.setLatitude(45.169336);
        strazilovoL.setLongitude(19.917636);

        Location osovljeL=new Location("Osovlje");
        osovljeL.setLatitude(45.16291);
        osovljeL.setLongitude(19.710427);

        picnicLocationsL.add(lipovacaL);
        picnicLocationsL.add(andrevljeL);
        picnicLocationsL.add(strazilovoL);
        picnicLocationsL.add(osovljeL);

        picnicMarker.add(m1);
        picnicMarker.add(m2);
        picnicMarker.add(m3);
        picnicMarker.add(m4);


    }

    public static void setStreams(GoogleMap mMap){

        String kumpulaSnippet="Ovaj izvor se nalazi nedaleko od Bike Parka Bukovac. Pre neko što se popnete do parka, postoji odvajanje i betonski put koji vas vodi jednim delom. Dalje niz livadu u šumarku očekuje vas okrepljenje u ovom malom divljem vrtu. Do izvora se može doći i iz sela ulicom Gornja Baranja, pa dalje do izvora livadom, peške.";
        String zjalinaSnippet="Izvor Zjalina se nalazi u vrdničkom naselju Stara Kolonija, gde ga još zovu i Dobra voda.Do izvora Zjalina se stiže iz Vrdnika putem kroz Banju ka naselju Stara Kolonija. Sa nekoliko česama u tom naselju, ljudi i dan-danas toče izvorsku vodu, koje u bazenu ispod izvora ima u izobilju. Stara kolonija je najstarije, nekada rudarsko naselje Vrdnika.";
        String vranjasSnippet="Izvor Vranjaš se pominje još u II veku pre nove ere. Vodom (akvadukt) je snabdevao Sremsku Mitrovicu, odnosno tadašnji Sirmijum, jednu od četiri prestonice Rimskog carstva. Izvor je tokom svoje istorije bio mesto okupljanja vernika. Posvećen je Svetom Kozmi i Damjanu (Kuzma i Damjan), što nesumnjivo pokazuju i mozaici iznad vode. Iznad izvora izgrađena je crkva koja je deo najmlađeg fruškogorskog manastira koji je posvećen Svetom Vasiliju Ostroškom.";
        String popovicaSnippet="Izvor se nalazi nedaleko od jezera, ušuškan u šumi, u ne mnogo strmom useku. Postoje znakovi na drveću koji upućuju ka njegovom pravcu. Hladna voda teče u tankom mlazu Na putu do izvora prolazi se pokraj kućice interesantnog kolorita s ispisanom porukom dobrodošlice. Pokraj puta postoji i ručno oslikana mapa Nacionalnog parka Fruška gora.";

        LatLng kumpula=new LatLng(45.182648,19.889181);
      Marker m1=mMap.addMarker(new MarkerOptions().position(kumpula).title("Kumpula").snippet(kumpulaSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        LatLng zjalina=new LatLng( 45.146384,19.791676);
      Marker m2=mMap.addMarker(new MarkerOptions().position(zjalina).title("Zjalina").snippet(zjalinaSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        LatLng vranjas=new LatLng(45.092791,19.605588);
     Marker m3=mMap.addMarker(new MarkerOptions().position(vranjas).title("Vranjas").snippet(vranjasSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        LatLng popovica=new LatLng(45.185871,19.819653);
       Marker m4=mMap.addMarker(new MarkerOptions().position(popovica).title("Popovica").snippet(popovicaSnippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        streamLocations.add(kumpula);
        streamLocations.add(zjalina);
        streamLocations.add(vranjas);
        streamLocations.add(popovica);

        Location kumpulaL=new Location("Kumpula");
        kumpulaL.setLatitude(45.182648);
        kumpulaL.setLongitude(19.889181);

        Location zjalinaL=new Location("Zjalina");
        zjalinaL.setLatitude(45.146384);
        zjalinaL.setLongitude(19.791676);

        Location vranjasL=new Location("Vranjas");
        vranjasL.setLatitude(45.092791);
        vranjasL.setLongitude(19.605588);

        Location popovicaL=new Location("Popovica");
        popovicaL.setLatitude(45.185871);
        popovicaL.setLongitude( 19.819653);

        streamLocationsL.add(kumpulaL);
        streamLocationsL.add(zjalinaL);
        streamLocationsL.add(vranjasL);
        streamLocationsL.add(popovicaL);

        streamMarker.add(m1);
        streamMarker.add(m2);
        streamMarker.add(m3);
        streamMarker.add(m4);
    }


    public static  void goToLocation(GoogleMap mMapa){

        LatLng fruskaGora = new LatLng(45.157085, 19.709358);
        //mMap.addMarker(new MarkerOptions().position(fruskaGora).title("Nacionalni park Fruska Gora"));
        // Moving camera to location of marker
        mMapa.moveCamera(CameraUpdateFactory.newLatLng(fruskaGora));
        // Zoom camera
        CameraUpdate zoomMap = CameraUpdateFactory.zoomTo(9);
        mMapa.animateCamera(zoomMap);

    }
        public void markerRoot(Marker marker){

            LatLng tvojaLokacija = new LatLng(yourLatitude, yourLongitude);
            yourLocationGlobal=tvojaLokacija;

            LatLng trazenaLokacija=marker.getPosition();
            searchedLocationGlobal=trazenaLokacija;
            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(tvojaLokacija, trazenaLokacija);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);

        }


    public void findNearestPossiblyLocation(View v){
        if (checkMenu.equals("lake")) {
            findNearestRoot(lakeLocations);
          //  findNearestRootMarker2(lakeMarker);
        }

        if(checkMenu.equals("picnic")){
            findNearestRoot(picnicLocations);
           // findNearestRootMarker2(picnicMarker);
        }

        if(checkMenu.equals("stream")){
            findNearestRoot(streamLocations);
           // findNearestRootMarker2(streamMarker);
        }

    }

    public  void findNearestRootL(ArrayList<Location> locationList) {
        LatLng tvojaLokacija=null;
         tvojaLokacija = new LatLng(yourLatitude, yourLongitude);
        double udaljenost = 0;
        double rastojanje = 0;
        LatLng trazenaLokacija=new LatLng(0,0);
        LatLng trazenaLokacijaNew;
        Location trazenaLokacijaL=null; //new Location("locationL");
        for (int i = 0; i < locationList.size(); i++) {
            if (i == 0) {
                udaljenost = pronadjiRazdaljinu(locationList.get(0).getLatitude(), locationList.get(0).getLongitude(), tvojaLokacija.latitude, tvojaLokacija.longitude);
                trazenaLokacijaL = locationList.get(0);
            }
            rastojanje = pronadjiRazdaljinu(locationList.get(i).getLatitude(), locationList.get(i).getLongitude(), tvojaLokacija.latitude, tvojaLokacija.longitude);
            if (udaljenost > rastojanje) {
                udaljenost = rastojanje;
                trazenaLokacijaL = locationList.get(i);
                trazenaLokacijaNew=new LatLng(trazenaLokacijaL.getLatitude(),trazenaLokacijaL.getLongitude());
                trazenaLokacija=trazenaLokacijaNew;
                searchedLocationGlobal=trazenaLokacija;
                yourLocationGlobal=tvojaLokacija;

                locationTitle=trazenaLokacijaL.getProvider();
            }


        }
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(tvojaLokacija, trazenaLokacija);
      //  String url=getDirectionsUrl(yourLocationGlobal,searchedLocationGlobal);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
            if(toastCounter!=0) {
                Toast.makeText(MainActivity.this, "Vama najbliza lokacija je " + locationTitle, Toast.LENGTH_LONG).show();
                goToLocation(mMap);
            }

    }

    public  void findNearestRootMarker2(ArrayList<Marker> locationList) {
        LatLng tvojaLokacija = new LatLng(yourLatitude, yourLongitude);
        yourLocationGlobal=tvojaLokacija;
        double udaljenost = 0;
        double rastojanje = 0;
        LatLng trazenaLokacija = new LatLng(0, 0);
        for (int i = 0; i < locationList.size(); i++) {
            if (i == 0) {
                udaljenost = pronadjiRazdaljinu(locationList.get(0).getPosition().latitude, locationList.get(0).getPosition().longitude, tvojaLokacija.latitude, tvojaLokacija.longitude);
                trazenaLokacija = locationList.get(0).getPosition();
            }
            rastojanje = pronadjiRazdaljinu(locationList.get(i).getPosition().latitude, locationList.get(i).getPosition().longitude, tvojaLokacija.latitude, tvojaLokacija.longitude);
            if (udaljenost > rastojanje) {
                udaljenost = rastojanje;
                trazenaLokacija = locationList.get(i).getPosition();
                searchedLocationGlobal=trazenaLokacija;
                locationTitle=locationList.get(i).getTitle();
            }


        }
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(tvojaLokacija, trazenaLokacija);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
        //  Toast.makeText(MainActivity.this, "Iscrtana je putanja do najblize lokacije!", Toast.LENGTH_SHORT).show();
        if(pathText==true){
            Toast.makeText(MainActivity.this, "Iscrtana je putanja do najblize lokacije!", Toast.LENGTH_LONG).show();
        }
        if(yourLocationGlobal!=null && searchedLocationGlobal!=null) {
            Toast.makeText(MainActivity.this, "Vama najbliza lokacija je " + locationTitle, Toast.LENGTH_LONG).show();
            goToLocation(mMap);
        }
    }

    private void updateDistanceAndTime(LatLng tvojaLokacija,LatLng trazenaLokacija){
        String url = getDirectionsUrl(tvojaLokacija, trazenaLokacija);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
        Toast.makeText(MainActivity.this, "Update-ovana je lokacija!", Toast.LENGTH_SHORT).show();


    }


}



