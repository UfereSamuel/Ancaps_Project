package com.engineering.nhub.ancaps_project;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Adapters.PlaceAutoCompleteAdapter;

import Util.LocationHelper;
import Util.SessionManager;
import Util.Utils;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class HomePage extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,GoogleApiClient.OnConnectionFailedListener,
        RoutingListener,GoogleApiClient.ConnectionCallbacks,LocationListener,
        NavigationView.OnNavigationItemSelectedListener {

    private LocationHelper locationHelper;
    private GoogleMap map;
    private Location mLastLocation;
    private AutoCompleteTextView etOrigin;
    private  AutoCompleteTextView etDestination;
    private CameraUpdate center;
    private CameraUpdate zoom;
    protected GoogleApiClient mGoogleApiClient;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary,
            R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};
    //private SlidingUpPanelLayout slidingLayout;
    private Utils util;
    protected LatLng start;
    protected LatLng end;
    private PlaceAutoCompleteAdapter mAdapter;
    private AutocompleteFilter filter;
    private  MarkerOptions destinationMarker;
    private  MarkerOptions originMarker;
    //private SweetAlertDialog pDialog, progressDialog;
    //private CountAnimationTextView tvAmount;
    private Spinner  etWeight;
    private EditText etName;
    private EditText etPhone;
    private Typeface bold, regular;
    private AlertDialog alertDialog;
    private SweetAlertDialog pDialog, progressDialog;
    private int amt, amount;
    //private ArrayList<PricingModel> loads;
  //  private PricingAdapter loadAdapter;
    private ImageView imageRetry;
    private String weight, recipientName, recipientPhone;
   // final   String url = "https://ancaps.herokuapp.com/price/";
    private String origin, destination, pricingId, userId;
    private SessionManager sessionManager;
    private HashMap<String, String> user;
    private TextView tvSenderName, tvSenderPhone;
    private ImageView drawerImage;


    private static final LatLngBounds BOUNDS_JAMAICA= new LatLngBounds(new LatLng(-57.965341647205726, 144.9987719580531),
            new LatLng(72.77492067739843, -9.998857788741589));


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //using intent to start service background job
        Intent locationService = new Intent(HomePage.this, LocationService.class);
        startService(locationService);

        sessionManager= new SessionManager(getApplicationContext());

        user = sessionManager.getUserDetails();
        userId = user.get(SessionManager.KEY_OBJECT_ID);

        bold = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Bold.ttf");
        regular = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Regular.ttf");

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#03A9F4"));
        pDialog.getProgressHelper().setSpinSpeed(1.0f);
        pDialog.getProgressHelper().setBarWidth(6);
        pDialog.setTitleText("Processing...");
        pDialog.setCancelable(false);

        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(Color.parseColor("#03A9F4"));
        progressDialog.getProgressHelper().setSpinSpeed(1.0f);
        progressDialog.getProgressHelper().setBarWidth(6);
        progressDialog.setTitleText("Computing...");
        progressDialog.setCancelable(false);


        polylines = new ArrayList<>();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        MapsInitializer.initialize(this);
        mGoogleApiClient.connect();

        util = new Utils();

        filter = new AutocompleteFilter.Builder()
                .setCountry("NG")
                .build();


        //build the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        mAdapter = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_JAMAICA, filter);


        etOrigin = (AutoCompleteTextView)findViewById(R.id.origin);
        etDestination = (AutoCompleteTextView) findViewById(R.id.destination);
        imageRetry = (ImageView)findViewById(R.id.refreshIcon);
        imageRetry.setClickable(true);

        imageRetry.setVisibility(View.GONE);
        imageRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageRetry.setVisibility(View.GONE);

                //initTransactionLayout();
            }
        });



        locationHelper=new LocationHelper(this);
        locationHelper.checkpermission();


        if (locationHelper.checkPlayServices()) {
            // Building the GoogleApi client
            locationHelper.buildGoogleApiClient();
        }

        mLastLocation=locationHelper.getLocation();

        center = CameraUpdateFactory.newLatLng(new LatLng(18.013610, -77.498803));
        zoom = CameraUpdateFactory.zoomTo(16);

        etOrigin.setAdapter(mAdapter);
        etDestination.setAdapter(mAdapter);

        etOrigin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);
                // Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            places.release();
                            return;
                        }
                        // Get the Place object from the buffer.
                        final Place place = places.get(0);
                        start=place.getLatLng();
                        originMarker = new MarkerOptions();
                        originMarker.position(start);
                        originMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        map.addMarker(originMarker);

                        if(!etOrigin.getText().toString().trim().isEmpty()
                                && !etDestination.getText().toString().trim().isEmpty()){
                            if (Utils.Operations.isOnline(HomePage.this)) {
                                route();

                                pDialog.dismiss();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                       // initTransactionLayout();
                                    }
                                },7000 );

                            } else {
                                pDialog.dismiss();
                                MDToast.makeText(HomePage.this, "No Internet Connectivity",
                                        MDToast.LENGTH_SHORT, MDToast.TYPE_INFO).show();
                            }

                        }
                    }
                });

            }
        });

        etDestination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);

                // Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            pDialog.dismiss();
                            places.release();
                            return;
                        }

                        final Place place = places.get(0);
                        end=place.getLatLng();
                        destinationMarker = new MarkerOptions();
                        destinationMarker.position(end);
                        destinationMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        map.addMarker(destinationMarker);

                        if(!etOrigin.getText().toString().trim().isEmpty()
                                && !etDestination.getText().toString().trim().isEmpty()){
                            if (Utils.Operations.isOnline(HomePage.this)) {
                                route();
                                pDialog.dismiss();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                       // initTransactionLayout();
                                    }
                                },7000 );
                            } else {
                                pDialog.dismiss();
                                MDToast.makeText(HomePage.this, "No Internet Connectivity",
                                        MDToast.LENGTH_SHORT, MDToast.TYPE_INFO).show();
                            }


                        }

                    }
                });

            }
        });


         /*
        These text watchers set the start and end points to null because once there's
        * a change after a value has been selected from the dropdown
        * then the value has to reselected from dropdown to get
        * the correct location.
        * */
        etOrigin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int startNum, int before, int count) {
                if (start != null) {
                    start = null;
                }
                imageRetry.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if(end!=null)
                {
                    end=null;
                }
                imageRetry.setVisibility(View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        drawerImage = findViewById(R.id.imageDrawer);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // start = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
               map.moveCamera(center);
               map.animateCamera(zoom);



                map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition position) {
                        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                        mAdapter.setBounds(bounds);
                    }
                });

            }
        }, 2000);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLastLocation=locationHelper.getLocation();


    }

    @Override
    public void onConnectionSuspended(int i) {
        locationHelper.connectApiClient();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onRoutingFailure(RouteException e) {

        // The Routing request failed
        //  progressDialog.dismiss();
        if(e != null) {
            MDToast.makeText(this, "Error: " + e.getMessage(),    MDToast.TYPE_ERROR, MDToast.LENGTH_LONG
            ).show();
            //// Log.i("Yess", "Route Exception "+e.getMessage());
        }else {
            //  Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int j) {

        LatLngBounds.Builder builder =  LatLngBounds.builder();

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = map.addPolyline(polyOptions);
            polylines.add(polyline);
        }


        builder.include(originMarker.getPosition());
        builder.include(destinationMarker.getPosition());

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30); // offset from edges of the map 10% of screen

        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        map.moveCamera(center);
        map.animateCamera(zoom);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // toolbar.setVisibility(View.VISIBLE);
                //slidingLayout.setPanelState(EXPANDED);
//
                return false;
            }
        });

    }

    @Override
    public void onRoutingCancelled() {

    }

    public void route() {
        pDialog.show();

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(start, end)
                .build();
        routing.execute();
    }








    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.booking, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected( MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_payment) {
            // Handle the camera action

        } else if (id == R.id.nav_transactions) {
            startActivity(new Intent(HomePage.this, MainActivity.class));
// Handle navigation view item clicks here.

        if (id == R.id.nav_payment) {
            // Handle the camera action

        } else if (id == R.id.nav_your_history) {
            startActivity(new Intent(HomePage.this, MainActivity.class));

        } else if (id == R.id.nav_your_history) {

        } else if (id == R.id.nav_notification) {

        } else if (id == R.id.nav_share) {

        }else if (id == R.id.nav_help){

        }else if (id == R.id.nav_exit) {
           // sessionManager.logoutUser();
            finish();
        } else if (id == R.id.nav_exit) {
           //logout();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
        } else if (id == R.id.nav_your_history) {

        } else if (id == R.id.nav_notification) {

        } else if (id == R.id.nav_share) {

        }else if (id == R.id.nav_help){

        }else if (id == R.id.nav_exit) {
           // sessionManager.logoutUser();
            finish();
        } else if (id == R.id.nav_exit) {
           // logout();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}