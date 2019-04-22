package com.example.korisnik.sumarskemape;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuFragment extends android.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    protected  TextView textMeniView,txViewLokacija;
    protected ImageView imMenuView,imLogoView,gpsBtnView, jezikIV,jezikEnIV,carView;
    private ListView categoriesLV;
    private OnFragmentInteractionListener mListener;
    public static boolean finish = false;
    private LocationManager locationManager;
    private CategoriesAdapter cap;

    public boolean uKategorijama = false;
    public boolean srpski = XmlParse.srpski;

    public MenuFragment() {
        // Required empty public constructor
    }

    DatabaseHandler db ;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ManiFragment1.
     */
    // TODO: Rename and change types and number of parameters
    public static MenuFragment newInstance(String param1, String param2) {
        MenuFragment fragment = new MenuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.menu_fragment_layout, container, false);
        textMeniView = (TextView)view.findViewById(R.id.menu_fragment_text);
        txViewLokacija = (TextView)view.findViewById(R.id.menu_fragment_lokacija_text);
        imMenuView = (ImageView)view.findViewById(R.id.menu_fragment_img_button);
        imLogoView = (ImageView)view.findViewById(R.id.menu_fragment_logo);
        categoriesLV = (ListView) view.findViewById(R.id.categorie_lv);
        jezikIV = (ImageView) view.findViewById(R.id.jezik);
        jezikEnIV = (ImageView) view.findViewById(R.id.jezikEn);
        carView=(ImageView)view.findViewById(R.id.button_auto);

        List<String> str = new ArrayList<>();
        str.add("Jezera i vodopadi");
        str.add("Piknik");
        str.add("Potoci");
        str.add("Reke");
        str.add("Klupice");
        str.add("Izvori");
        str.add("Planinarski dom i ugostiteljstvo");
        str.add("Spomenici");
        str.add("Manastiri");
        str.add("Ostalo");

        db = new DatabaseHandler(getActivity());

         if (MainActivity.lang.equals("sr")) {
             jezikIV.setVisibility(View.GONE);
             jezikEnIV.setVisibility(View.VISIBLE);

             cap = new CategoriesAdapter(getActivity(), R.layout.category_list_row,str);
             categoriesLV.setAdapter(cap);
             XmlParse.srpski = false;
         }
        else {
             jezikEnIV.setVisibility(View.GONE);
             jezikIV.setVisibility(View.VISIBLE);

             cap = new CategoriesAdapter(getActivity(), R.layout.category_list_row,MainActivity.categories);
             categoriesLV.setAdapter(cap);
             XmlParse.srpski = true;
         }

    jezikIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jezikIV.setVisibility(View.GONE);
                jezikEnIV.setVisibility(View.VISIBLE);

                MainActivity.lang = "sr"; // your language
                Locale locale = new Locale(MainActivity.lang);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getActivity().getResources().updateConfiguration(config,
                        getActivity().getResources().getDisplayMetrics());

//                Intent intent = new Intent(getContext(),MainActivity.class);
//                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
//                startActivity(intent);
                languageChanged();
                getActivity().recreate();
            }
        });

        jezikEnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jezikEnIV.setVisibility(View.GONE);
                jezikIV.setVisibility(View.VISIBLE);

                MainActivity.lang = "en";
                Locale locale = new Locale( MainActivity.lang);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getActivity().getResources().updateConfiguration(config,
                        getActivity().getResources().getDisplayMetrics());

                languageChanged();
                getActivity().recreate();

            }
        });

        imMenuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cap.notifyDataSetChanged();
                if(uKategorijama) {
                    imMenuView.setImageResource(R.drawable.meni);
                    uKategorijama = false;
                    MainActivity.showGps(true);
                }
                else {
                    imMenuView.setImageResource(R.drawable.arrow_big);
                    uKategorijama = true;
                    MainActivity.showGps(false);
                }

                if(categoriesLV.getVisibility() == View.GONE)
                    categoriesLV.setVisibility(View.VISIBLE);
                else
                    categoriesLV.setVisibility(View.GONE);

            }
        });

/*
        carView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity=new MainActivity();
                mainActivity.findNearestRoot();
            }
        });

*/
        categoriesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                List<PlaceMark> list ;
                MainActivity.mMap.clear();
                String category = MainActivity.categories.get(position);
                onCategorySelected(category);

                categoriesLV.setVisibility(View.GONE);
                if(uKategorijama) {
                    MainActivity.showGps(true);

                    imMenuView.setImageResource(R.drawable.meni);
                    uKategorijama = false;
                }


                switch (position){
                    case 0:
                        MainActivity.mMap.clear();
                        Toast.makeText(getActivity(), "Jezera i vodopadi!", Toast.LENGTH_SHORT).show();
                       // MainActivity.setLakes=true;
                        //MainActivity.setLakesAndWaterfalls();
                        MainActivity.checkMenu="lake";
                        MainActivity.setLakesAndWaterfalls(MainActivity.mMap);
                        MainActivity.goToLocation(MainActivity.mMap);
                        MainActivity.textView.setText("");
                        MainActivity.distanceText="";
                        MainActivity.timeText="";
                        MainActivity.searchedLocationGlobal=null;
                        MainActivity.yourLocationGlobal=null;
                        MainActivity.pathText=false;
                        break;
                    case 1:
                        MainActivity.mMap.clear();
                        Toast.makeText(getActivity(), "Piknik!", Toast.LENGTH_SHORT).show();
                        MainActivity.checkMenu="picnic";
                        MainActivity.setPicnic(MainActivity.mMap);
                        MainActivity.goToLocation(MainActivity.mMap);
                        MainActivity.textView.setText("");
                        MainActivity.searchedLocationGlobal=null;
                        MainActivity.yourLocationGlobal=null;
                        MainActivity.pathText=false;
                        break;
                    case 2:
                        MainActivity.mMap.clear();
                        Toast.makeText(getActivity(), "Potoci!", Toast.LENGTH_SHORT).show();
                        MainActivity.checkMenu="stream";
                        MainActivity.setStreams(MainActivity.mMap);
                        MainActivity.goToLocation(MainActivity.mMap);
                        MainActivity.textView.setText("");
                        MainActivity.searchedLocationGlobal=null;
                        MainActivity.yourLocationGlobal=null;
                        MainActivity.pathText=false;
                        break;
                    case 3:
                        Toast.makeText(getActivity(), "Reke!", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(getActivity(), "Klupice!!", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(getActivity(), "Izvori!", Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Toast.makeText(getActivity(), "Planinski dom i ugostiteljstvo!", Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(getActivity(), "Spomenici!", Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(getActivity(), "Manastiri!!", Toast.LENGTH_SHORT).show();
                        break;
                    case 9:
                        Toast.makeText(getActivity(), "Ostalo!", Toast.LENGTH_SHORT).show();
                        break;
                }
//                for (Map.Entry<String,List<Marker>> lm: MainActivity.markers.entrySet()) {
//                    for (Marker m : lm.getValue()){
//                        m.setVisible(false);
//                    }
//                }
//
//                for (Map.Entry<String,List<Polyline>> lp: MainActivity.polylines.entrySet()) {
//                    for (Polyline p : lp.getValue()){
//                        p.setVisible(false);
//                    }
//                }
//
//
//                for (Marker m: MainActivity.markers.get(category)) {
//                    m.setVisible(true);
//                }
//                for (Polyline p: MainActivity.polylines.get(category)) {
//                    p.setVisible(true);
//                }
//
//
//                categoriesLV.setVisibility(View.GONE);
//                if(uKategorijama) {
//                    imMenuView.setImageResource(R.drawable.meni);
//                    uKategorijama = false;
//                }

                

            }
        });

        return view;
    }

    public void onCategorySelected(String category) {
        if (mListener != null) {
            mListener.categorySelected(category);
        }
    }

    public void languageChanged() {
        if (mListener != null) {
            mListener.languageChanged();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void categorySelected(String category);
        void languageChanged();
    }

    public static void setAdapter(){

    }


}
