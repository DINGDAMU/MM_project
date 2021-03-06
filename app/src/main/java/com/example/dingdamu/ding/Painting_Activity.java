package com.example.dingdamu.ding;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by dingdamu on 10/05/16.
 */
public class Painting_Activity extends AppCompatActivity {
    ImageView cameraImage;
    TextView locationText,addressText;
    Uri imageUri;
    Geocoder geocoder;
    List<Address> addresses;
    double latitude,longitude;
    String resultLatLong,resultAddr;
    Button mSave,mCancel,mRetry;
    PostORM p = new PostORM();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);
        LocationService service = new LocationService(Painting_Activity.this);
        cameraImage = (ImageView)findViewById(R.id.image);
        locationText = (TextView)findViewById(R.id.locationText);
        addressText = (TextView)findViewById(R.id.addressText);
        mSave = (Button)findViewById(R.id.savePost);
        mCancel = (Button)findViewById(R.id.cancelPost);
        addresses = new ArrayList<>();
        mRetry = (Button)findViewById(R.id.retryButton);
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        final String updated = "Updated : "+today.monthDay+"-"+(today.month+1)+"-"+today.year+"   "+today.format("%k:%M:%S");

        imageUri = getIntent().getData();
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p.insertPost(Painting_Activity.this, imageUri.toString(), resultLatLong, resultAddr,updated);
                p.getAddressfromDB(Painting_Activity.this);
                p.getCoordinatesfromDB(Painting_Activity.this);
                Toast.makeText(Painting_Activity.this, "Created new Post", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Painting_Activity.this, Import.class);
                startActivity(i);
                Painting_Activity.this.finish();

            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Painting_Activity.this,"Post Discarded",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Painting_Activity.this,Import.class);
                startActivity(i);
                Painting_Activity.this.finish();

            }
        });

        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isFastDoubleClick()){
                    return;
                }else {
                    new PositionTask().execute();
                }


            }
        });

        Location gpsLocation = service.getLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation != null) {
            latitude = gpsLocation.getLatitude();
            longitude = gpsLocation.getLongitude();
            resultLatLong = "Latitude: " + gpsLocation.getLatitude() +
                    " Longitude: " + gpsLocation.getLongitude();
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addresses.isEmpty()||!isNetworkAvailable())
            {
                mSave.setVisibility(View.GONE);
                mRetry.setVisibility(View.VISIBLE);
            }
            else {

                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String state = addresses.get(0).getAddressLine(2);
                resultAddr = address + "," + city + ", " + state;
                locationText.setText(resultLatLong);
                addressText.setText(resultAddr);
            }
        }
        else
        {
            Toast.makeText(Painting_Activity.this,"Could not get location !",Toast.LENGTH_SHORT).show();
            mSave.setVisibility(View.GONE);
            mRetry.setVisibility(View.VISIBLE);
        }
        Picasso.with(this).load(imageUri.toString()).placeholder(R.drawable.placeholder).resize(1000,1000).into(cameraImage);
        service.removeUpdates();
        service.unregisterlistener();

    }


   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_painting, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class PositionTask extends AsyncTask<String,String, List<Address> > {

        private ProgressDialog pDialog;
        Geocoder geocoder;
        String resultLatLong, resultAddr;
        List<Address> addresses;
        double latitude, longitude;
        LocationService service;



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Painting_Activity.this);
            pDialog.setMessage("Getting your location ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();


        }

        @Override
        protected List<Address> doInBackground(String... params) {
            Looper.prepare();

           service = new LocationService(Painting_Activity.this);


            Location gpsLocation = service.getLocation(LocationManager.GPS_PROVIDER);


            if (gpsLocation != null) {


                latitude = gpsLocation.getLatitude();
                longitude = gpsLocation.getLongitude();
                resultLatLong = "Latitude: " + gpsLocation.getLatitude() +
                        " Longitude: " + gpsLocation.getLongitude();
                geocoder = new Geocoder(Painting_Activity.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    return addresses;
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            Looper.myLooper().quit();


            return null;


        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            pDialog.dismiss();
            super.onPostExecute(addresses);



            if (!isNetworkAvailable() || addresses==null) {
                Toast.makeText(Painting_Activity.this,"Could not get location !Please retry in "+Utils.clicktime/1000+" seconds!", Toast.LENGTH_SHORT).show();
                service.removeUpdates();
                service.unregisterlistener();
            } else {

                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String state = addresses.get(0).getAddressLine(2);
                resultAddr = address + "," + city + ", " + state;

                locationText.setText(resultLatLong);
                addressText.setText(resultAddr);
                service.removeUpdates();
                service.unregisterlistener();

            }


        }
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo.isConnected()) {
            return true;
        }
        return false;
    }



}


