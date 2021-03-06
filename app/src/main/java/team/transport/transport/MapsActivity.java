package team.transport.transport;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.util.Log.println;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        SensorEventListener {

    private GoogleMap mMap;

    private LocationManager lm;

    private double latitude;
    private double longitude;
    private double latitudeAvant;
    private double longitudeAvant;
    //private double altitude;
    //private float accuracy;

    private boolean Go = false;
    private boolean GeoBegin = false;

    //private ArrayList<Double> distances = new ArrayList<>();
    //ArrayList<ArrayList<Double>> memPosition = new ArrayList();

    private double sommeTotal = 0;
    private int nbElements = 0;
    private double moyenne = 0;
    private double sommeETCarre = 0;
    private double ecartType = 0;

    private int nbTrue;
    private int nbFalse;
    private boolean creux = false;
    private boolean creux2 = false;
    double latCreux; double longCreux;

    final String choix = "choixTransport";
    String modeTransport = "";

    // Le sensor manager (gestionnaire de capteurs)
    SensorManager sensorManager;

    // L'accéléromètre
    Sensor accelerometer;

    private double aX;
    private double aY;
    private double aZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        if (intent != null) {
            modeTransport = intent.getStringExtra(choix);
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Instancier l'accéléromètre
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        TextView textGo = (TextView) findViewById(R.id.textGo);
        textGo.setVisibility(View.GONE);

        readFile();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

    public void readFile() {

        File chemin = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File fichier = new File(chemin, "fichier.txt");

        String ligne;
        //StringBuilder text = new StringBuilder();

        try {

            FileReader fReader = new FileReader(fichier);
            BufferedReader bReader = new BufferedReader(fReader);

            int cpt=0;
            boolean isChoice = false;

            while((ligne = bReader.readLine()) != null) {

                if (cpt == 0) {

                    ligne = ligne.replaceAll(" :", "");

                    if (modeTransport.equals(ligne))
                        isChoice = true;

                    cpt++;
                }
                else {

                    cpt = 0;

                    if (isChoice) {

                        String tab[] = ligne.replaceAll("\"", "/").split("/");

                        double vitesse = Double.parseDouble(tab[11].replaceAll(",", "."));

                        sommeTotal += vitesse;
                        nbElements ++;
                        moyenne = (sommeTotal/nbElements);
                        sommeETCarre += Math.pow(vitesse, 2);

                    }
                }
            }

            ecartType = Math.sqrt((sommeETCarre / nbElements) - Math.pow(moyenne, 2));

            /*Toast.makeText(this, String.valueOf(moyenne) + " / "
                    + String.valueOf(ecartType), Toast.LENGTH_LONG).show();*/


        } catch (Exception e){
            //Toast.makeText(this, String.valueOf(e), Toast.LENGTH_LONG).show();
            return;
        }

    }

    public void changeGo(View view){
        Go = true;
        Toast.makeText(this, "C'est parti !", Toast.LENGTH_LONG).show();
        Button button = (Button) view;
        button.setVisibility(View.GONE);

        TextView textGo = (TextView) findViewById(R.id.textGo);
        textGo.setVisibility(View.VISIBLE);
    }

    public void writeInFile(java.text.DecimalFormat df, double currentVitesse) {

        try {
            File chemin = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File fichier = new File(chemin, "fichier.txt");
            FileWriter filewriter = new FileWriter(fichier, true);

            String toPrint = modeTransport + " :\n" + String.format("\"" + latitude + "\", ") + String.format(
                    "\"" + longitude + "\", ") + String.format("\"" + df.format(aX) + "\", \"" +
                    df.format(aY) + "\", \"" + df.format(aZ) + "\", \"") + df.format(currentVitesse) + "\", \"" +
                    currentDateFormat() + "\"\n";

            sommeTotal += currentVitesse;
            nbElements ++;
            moyenne = (sommeTotal/nbElements);
            sommeETCarre += Math.pow(currentVitesse, 2);
            ecartType = Math.sqrt((sommeETCarre / nbElements) - Math.pow(moyenne, 2));

            filewriter.write(toPrint);
            filewriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeMarqueur(View view) {
        Toast.makeText(this, "C'est marqué !", Toast.LENGTH_LONG).show();

        try {
            File chemin = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File fichier = new File(chemin, "fichier.txt");
            FileWriter filewriter = new FileWriter(fichier, true);

            String toPrint = "\nMarqueur\n\n";

            filewriter.write(toPrint);
            filewriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String currentDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmss");
        String currentTimeStamp = dateFormat.format(new Date());
        return currentTimeStamp;
    }

    /*static double DistanceTo(double lat1, double lon1, double lat2, double lon2, String unit)
    {
        double rlat1 = Math.PI * lat1/180;
        double rlat2 = Math.PI * lat2/180;
        //double rlon1 = Math.PI * lon1/180;
        //double rlon2 = Math.PI * lon2/180;

        double theta = lon1-lon2;
        double rtheta = Math.PI * theta/180;

        double dist = Math.sin(rlat1) * Math.sin(rlat2) + Math.cos(rlat1) * Math.cos(rlat2) * Math.cos(rtheta);
        dist = Math.acos(dist);
        dist = dist * 180/Math.PI;
        dist = dist * 60 * 1.1515;

        if (unit=="K") { dist = dist * 1.609344; }
        if (unit == "M") { dist = dist * 1.609344 * 1000; }
        if (unit == "N") { dist = dist * 0.8684; }

        return dist;
    }*/

    /*public boolean ecartType(double distance) {

        if (distances.size() < 3)
            return false;
        else {
            moyenne = 0;
            for (int i = 0; i < distances.size(); i++) {
                moyenne += distances.get(i);
            }
            moyenne /= distances.size();

            ecartType = 0;
            for (int i = 0; i < distances.size(); i++) {
                ecartType += Math.pow(distances.get(i) - moyenne, 2);
            }
            ecartType = Math.sqrt(ecartType / distances.size());

            if (distance > (moyenne - (ecartType*1.5)) && distance < (moyenne + (ecartType*1.5)))
                return true;
            else
                return false;
        }
    }*/

    public void drawPolyline(double currentVitesse) {

        /*if (ecartType(currentDistance) && currentDistance > 2 && currentDistance < (moyenne * 3)) {

            if (creux) {
                mMap.addPolyline((new PolylineOptions())
                        .add(new LatLng(latCreux, longCreux),
                                new LatLng(latitudeAvant, longitudeAvant))
                        .width(5).color(Color.BLUE)
                        .geodesic(true));
                creux = false;
            }

            mMap.addPolyline((new PolylineOptions())
                    .add(new LatLng(latitudeAvant, longitudeAvant),
                            new LatLng(latitude, longitude))
                    .width(5).color(Color.BLUE)
                    .geodesic(true));

            nbTrue++;
        }
        else {
            if (nbTrue >= 2) {
                if (!creux) {
                    creux = true;
                    latCreux = latitudeAvant;
                    longCreux = longitudeAvant;
                }
            }
            else {
                nbTrue = 0;
                creux = false;
            }
        }*/

        if (currentVitesse > 0) {

            if (nbFalse < 2) {
                if (creux) {
                    mMap.addPolyline((new PolylineOptions())
                            .add(new LatLng(latCreux, longCreux),
                                    new LatLng(latitudeAvant, longitudeAvant))
                            .width(5).color(Color.BLUE)
                            .geodesic(true));
                    creux = false;
                }

                mMap.addPolyline((new PolylineOptions())
                        .add(new LatLng(latitudeAvant, longitudeAvant),
                                new LatLng(latitude, longitude))
                        .width(5).color(Color.BLUE)
                        .geodesic(true));
            }

            nbTrue++;

        }
        else {
            if (nbTrue >= 2) {
                if (!creux) {
                    creux = true;
                    latCreux = latitudeAvant;
                    longCreux = longitudeAvant;
                }
                nbFalse = 0;
            }
            else {
                nbTrue = 0;
                creux = false;
                nbFalse ++;
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(this);
        sensorManager.unregisterListener(this, accelerometer);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitudeAvant = latitude;
        longitudeAvant = longitude;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        double currentVitesse = location.getSpeed() * 3.6;
        //altitude = location.getAltitude();
        //accuracy = location.getAccuracy();

        //double currentDistance = DistanceTo(latitudeAvant, longitudeAvant, latitude, longitude, "M");

        java.text.DecimalFormat df = new java.text.DecimalFormat("0.##");

        if (GeoBegin == false) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 18));
            GeoBegin = true;
        }

        if (Go) {

            this.writeInFile(df, currentVitesse);

            this.drawPolyline(currentVitesse);

            /*Geocoder geoCoder = new Geocoder(getBaseContext());
            List<Address> matches = null;
            try {
                matches = geoCoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address bestMatch = (matches.isEmpty() ? null : matches.get(0));

            bestMatch.getThoroughfare()*/


            String chaineAcceleration = "";

            if (currentVitesse < 2)
                chaineAcceleration = "Mouvement nulle ou quasi nulle : " + df.format(currentVitesse);
            else if (currentVitesse < (moyenne-ecartType))
                chaineAcceleration = "Mouvement lent : " + df.format(currentVitesse);
            else if (currentVitesse > (moyenne+ecartType))
                chaineAcceleration = "Mouvement rapide : " + df.format(currentVitesse);
            else
                chaineAcceleration = "Mouvement normal : " + df.format(currentVitesse);



            TextView textGo = findViewById(R.id.textGo);
            textGo.setText(String.format(chaineAcceleration + " ["
                    + df.format(moyenne - ecartType) + ";"
                    + df.format(moyenne + ecartType)
                    + "]"));

            // Accélération rapide, lente, arrêt

            // Chemin de fer ou une rue, Regarder si c'est possible et comment faire

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Récupérer les valeurs du capteur
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            aX = event.values[0];
            aY = event.values[1];
            aZ = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
