package com.example.maps_demo;



import java.math.RoundingMode;
import java.text.DecimalFormat;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends FragmentActivity implements LocationListener {
  //private TextView latituteField;
  //private TextView longitudeField;
  private TextView distanceview;
  private LocationManager locationManager;
  private String provider;
  private Location location;
  private double lat;
  private double lng;
  private double storelat, storelng;
  private int flag=1, trflag=0, toneflag = 0;
  private double prevdist = 100;
  private Fragment fragment;
  LatLng fromPosition = new LatLng(13.687140112679154, 100.53525868803263);
  LatLng toPosition = new LatLng(13.683660045847258, 100.53900808095932);
  Context context;
  GoogleMap mMap;
  CircleOptions circleOptions;
  Marker carmarker;

  
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //latituteField = (TextView) findViewById(R.id.textView1);
    //longitudeField = (TextView) findViewById(R.id.textView2);
    Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/batmfa.ttf");
    distanceview = (TextView) findViewById(R.id.distanceview);
    Button findMyCar=(Button) findViewById(R.id.findCar);
    Button navigate=(Button) findViewById(R.id.bNavigation);
    Button mapbutton=(Button) findViewById(R.id.mapButton);
    Button clearMap=(Button) findViewById(R.id.clearMap);
    Button traceRoute=(Button) findViewById(R.id.traceRoute);
    Button storeLocation=(Button) findViewById(R.id.parkButton);
    distanceview.setTypeface(tf);
    findMyCar.setTypeface(tf);
    navigate.setTypeface(tf);
    mapbutton.setTypeface(tf);
    clearMap.setTypeface(tf);
    traceRoute.setTypeface(tf);
    storeLocation.setTypeface(tf);
    
    //distanceview.setText("Distance from car:");
    
    
    mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    // Get the location manager
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    // Define the criteria how to select the location provider -> use
    // default
    Criteria criteria = new Criteria();
    provider = locationManager.getBestProvider(criteria, false);
    location = locationManager.getLastKnownLocation(provider);

    // Initialize the location fields
    if (location != null) {
      System.out.println("Provider " + provider + " has been selected.");
      onLocationChanged(location);
    } else {
      //latituteField.setText("Location not available");
      //longitudeField.setText("Location not available");
    }  
    
    FragmentManager fm = getSupportFragmentManager();
    fragment = fm.findFragmentById(R.id.map);
    FragmentTransaction transaction = fm.beginTransaction();
    transaction.hide(fragment);
    transaction.commit();   
    
  }
  
  public void mapsView(View view) {
	  FragmentManager fm = getSupportFragmentManager();
	  FragmentTransaction transaction = fm.beginTransaction();
	  transaction.show(fragment);
	  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(storelat,storelng),18));
	  transaction.commit();
  }
  
  public void startMaps(View view) {
	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
	  Uri.parse("http://maps.google.com/maps?saddr="+lat+","+lng+"&daddr="+storelat+","+storelng));
	  startActivity(intent);
  }
    
  @Override
  public void onBackPressed() {
	  FragmentManager fm = getSupportFragmentManager();
      FragmentTransaction transaction = fm.beginTransaction();
      transaction.hide(fragment);
      transaction.commit();
  }

  /* Request updates at startup */
  @Override
  protected void onResume() {
    super.onResume();
    locationManager.requestLocationUpdates(provider, 400, 1, this);
  }

  /* Remove the locationlistener updates when Activity is paused */
  @Override
  protected void onPause() {
    super.onPause();
    locationManager.removeUpdates(this);  
  }

  @Override
  public void onLocationChanged(Location location) {
    lat = (double) (location.getLatitude());
    lng = (double) (location.getLongitude());
    mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
	
    if(trflag==1) {
    	circleOptions = new CircleOptions().center(new LatLng(lat, lng)).radius(0.1); // In meters
    	mMap.addCircle(circleOptions);
    }
    
    if(flag==2) {
    	trflag = 0;
    	circleOptions = new CircleOptions().center(new LatLng(lat, lng)).radius(0.1).strokeColor(Color.RED); // In meters
    	mMap.addCircle(circleOptions);
    	DecimalFormat df = new DecimalFormat("##.##");
    	df.setRoundingMode(RoundingMode.DOWN);
    	double dist = distance(lat,lng,storelat,storelng);
    	distanceview.setText("Distance from car:" + String.valueOf(df.format(dist) + "mts."));
    	if(dist>prevdist && toneflag==1) {
    		ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    		toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); 
    	}
    	prevdist = dist;
    	if(dist<3) {
    		toneflag = 0;
    		distanceview.setText("");
    		Toast toast = Toast.makeText(this, "You reached your car", Toast.LENGTH_SHORT);
    		toast.show();
    		flag=1;
    	}
    }
    
    location.getLatitude();
    float speed = location.getSpeed();
    //longitudeField.setText(Float.toString(speed));
   	
    //walking
    if(speed<0.25 && flag==0) {
   	  storelat = lat;
  	  storelng = lng;
  	  flag = 1;
  	  Toast toast = Toast.makeText(this, "Your car's location has been saved", Toast.LENGTH_SHORT);
  	  toast.show();
  	  LatLng carPosition = new LatLng(storelat, storelng);
  	  try{
  		  carmarker.remove();
  	  } catch(Exception e) {}
  	  carmarker = mMap.addMarker(new MarkerOptions().position(carPosition).title("My Car"));
	}
   	
   	
   	//Driving
   	if(speed>4.00) {
   		storelat=0.00;
   		storelng=0.00;
   		flag = 0;
   	}
   	
   	
  
  }
  
  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {

  }

  @Override
  public void onProviderEnabled(String provider) {
    Toast.makeText(this, "Enabled new provider " + provider,
        Toast.LENGTH_SHORT).show();

  }

  @Override
  public void onProviderDisabled(String provider) {
    Toast.makeText(this, "Disabled provider " + provider,
        Toast.LENGTH_SHORT).show();
  }
  
  
  public void storeLocation(View view) {
	  storelat = lat;
	  storelng = lng;
	  flag = 0;
	  Toast toast = Toast.makeText(this, "Your car's location has been saved", Toast.LENGTH_SHORT);
	  toast.show();
	  LatLng carPosition = new LatLng(storelat, storelng);
	  try{
		  carmarker.remove();
	  } catch(Exception e) {}
	  carmarker = mMap.addMarker(new MarkerOptions().position(carPosition).title("My Car"));
  }
  
  public void findCar(View view) {
	 if(storelat == 0 && storelng == 0) {
		 Toast toast = Toast.makeText(this, "Store a location first", Toast.LENGTH_SHORT);
		 toast.show();
	 }
	 else {
		 Toast toast = Toast.makeText(this, "Finding your car...", Toast.LENGTH_SHORT);
		 toast.show();
		 flag = 2;
		 toneflag = 1;
	 }
  }
  
  public void traceMap(View view) {
	  //mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
	  mMap.addMarker(new MarkerOptions().position(fromPosition).title("Your car"));
	  Toast toast = Toast.makeText(this, "Tracing route...", Toast.LENGTH_SHORT);
	  toast.show();
	  trflag = 1;
  }
  
  public void clearMap(View view) {
	  //mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
	  Toast toast = Toast.makeText(this, "Map cleared...", Toast.LENGTH_SHORT);
	  toast.show();
	  trflag = 0;
	  mMap.clear();
  }
  
  private double distance(double lat1, double lon1, double lat2, double lon2) {
		double dist = (Math.acos(Math.sin(lat1 * (Math.PI / 180.0)) * Math.sin(lat2 * (Math.PI / 180.0)) 
				+ Math.cos(lat1 * (Math.PI / 180.0)) * Math.cos(lat2 * (Math.PI / 180.0)) 
				* Math.cos((lon1 - lon2) * (Math.PI / 180.0))) * (180.0/Math.PI)) * 60 * 1.1515 * 1.609344 ;
		return (dist*1000);
  } 
  
} 

