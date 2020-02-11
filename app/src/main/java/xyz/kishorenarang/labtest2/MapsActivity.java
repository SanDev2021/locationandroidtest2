package xyz.kishorenarang.labtest2;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener , GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener, GestureDetector.OnDoubleTapListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    Polygon p;
    List<Polyline> pl =  new ArrayList<Polyline>();
    List<Marker> marks = new ArrayList<Marker>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private List<LatLng> ll = new ArrayList<LatLng>();
    double allDistance = 0;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 4.0f ) );
        mMap.setOnPolylineClickListener(this);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMarkerClickListener(this);
    }
    List<MarkerOptions> mol = new ArrayList<MarkerOptions>();
    private void drawDistance(MarkerOptions p1, MarkerOptions p2)
    {
        LatLng midPoint = midPoint(p1.getPosition().latitude, p1.getPosition().longitude, p2.getPosition().latitude, p2.getPosition().longitude);
        IconGenerator iconGen = new IconGenerator(this);
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconGen.makeIcon(distance(p1.getPosition().latitude, p1.getPosition().longitude, p2.getPosition().latitude, p2.getPosition().longitude)+" KMS"))).
                position(midPoint).anchor(iconGen.getAnchorU(), iconGen.getAnchorV());
        this.marks.add(mMap.addMarker(markerOptions));
    }
    private void reload()
    {
        if(mol.size() > 1)
        {
            addPolyLine();
            drawDistance(mol.get(mol.size()-2),mol.get(mol.size()-1));
        }
        if(mol.size()==5 || longPressed)
        {
            mol.add(mol.get(0));
            addPolyLine();
            PolygonOptions po = new PolygonOptions();
            po.clickable(true);
            po.strokeColor(Color.RED);
            po.fillColor(0x30FF0000);
            Iterator i = mol.iterator();
            drawDistance(mol.get(mol.size()-2),mol.get(mol.size()-1));

            while(i.hasNext())
            {
                po.add(((MarkerOptions) i.next()).getPosition());
            }
            p = mMap.addPolygon(po);
        }
    }
    @Override
    public void onMapClick(LatLng latLng) {
        if(mol.size()<5)
        {
            Log.e("LAT", latLng.latitude+"");
            MarkerOptions mo = new MarkerOptions();
            mo.position(latLng);
            mo.title("Marker "+mol.size());
            mMap.addMarker(mo);
            mol.add(mo);
            reload(); }
        else
        {
            addPolyLine();
        }
    }
    public static LatLng midPoint(double lat1,double lon1,double lat2,double lon2){
        double dLon = Math.toRadians(lon2 - lon1);
        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
        //print out in degrees
        System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));
        return new LatLng(Math.toDegrees(lat3), Math.toDegrees(lon3));
    }
    private  String distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return "0";
        }
        else {
            String unit  = "K";
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            DecimalFormat df = new DecimalFormat("#.##");
            ;
            allDistance += dist;
            return (df.format(dist));
        }
    }
    private  void addPolyLine()
    {
        PolylineOptions po = new PolylineOptions();
        po.clickable(true);
        Iterator i = mol.iterator();
        while(i.hasNext())
        {
            while(i.hasNext())
            {
                po.add(((MarkerOptions) i.next()).getPosition());
            }
        }
        Polyline pl = mMap.addPolyline(po);
        this.pl.add(pl);
    }
    private LatLng[] getLatLng()
    {
        LatLng[] latLngs = new LatLng[ll.size()];
        for(int i=0;i<latLngs.length;i++)
        {
            latLngs[i] = ll.get(i);
        }
        return latLngs;
    }
    @Override
    public void onPolygonClick(Polygon polygon) {
        polygon.remove();
    }
    @Override
    public void onPolylineClick(Polyline polyline) {
polyline.remove();
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }
    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        ll.add(ll.get(0));
        addPolyLine();
        return true;
    }
    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.remove();
        removeFromMap(marker);
        return false;
    }
    private void removeFromMap(Marker marker)
    {
        if(mol.size() == 6)
        {
            mol.remove(5);
        }
        else
        {
          //  mol.remove(marker);
        }
        for(int i=0;i<mol.size();i++)
        {
            if(mol.get(i).getPosition().latitude == marker.getPosition().latitude && mol.get(i).getPosition().longitude == marker.getPosition().longitude )
            {
                mol.remove(i);

                if(p!=null)
                {
                    p.remove();
                }
                removeAll();
                reload();
                break;
            }
        }
    }
    private void removeAll()
    {
        for (int i=0;i<pl.size();i++)
        {
            pl.get(i).remove();
        }

        for (int i=0;i<marks.size();i++)
        {
            marks.get(i).remove();
        }
        allDistance = 0;
    }
    @Override
    public void onMapLongClick(LatLng latLng) {

        longPressed = true;
        reload();
    }
    boolean longPressed = false;
}
