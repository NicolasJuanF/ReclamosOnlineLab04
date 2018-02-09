package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDao;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDaoHTTP;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Estado;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.TipoReclamo;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,View.OnClickListener {

    private GoogleMap mMap;
    private Intent intent;
    private Button btnGuardarLugar;
    private List<Reclamo> listaReclamos;
    private ReclamoDao daoReclamo;
    private LatLng ubicacion_reclamo;
    private String bandera;
    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        intent = getIntent();

        bandera = intent.getStringExtra("bandera");
        ubicacion_reclamo = intent.getParcelableExtra("ubicacion");

        btnGuardarLugar = (Button) findViewById(R.id.btnGuardarLugar);
        daoReclamo = new ReclamoDaoHTTP();
        Log.d("bandera", bandera);

        btnGuardarLugar.setOnClickListener(this); ;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGuardarLugar:
                intent.putExtra("ubicacion" ,ubicacion_reclamo);
                setResult(RESULT_OK, intent);
                finish();

                break;
        }
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
        if(bandera.equals("lugar")){
            if(ubicacion_reclamo == null){
                ubicacion_reclamo = new LatLng(-31.6333, -60.7); // ubicacion de santa fe
            }
            mMap.addMarker(new MarkerOptions().position(ubicacion_reclamo).draggable(true));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion_reclamo,15));
            mMap.setOnMarkerDragListener(new onMarkerDrag());

        }else{//si bandera es "listar"

            Log.d("error" , "se ejecuta esto");
            //buscar todos los reclamos
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    listaReclamos = daoReclamo.reclamos();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            graficarPuntos();
                        }
                    });
                }
            };
            t = new Thread(r);
            t.start();
        }

        // Add a marker in Sydney and move the camera



    }

    private void graficarPuntos(){
        List<Reclamo> listaConUbicacion =  new ArrayList<>();;

        for (Reclamo r: listaReclamos){
            Log.d("ubicacion", "se ejecuto una vez");

            if (r.getLugar() != null){
                Log.d("lat", String.valueOf(r.getLugar().longitude));
                listaConUbicacion.add(r);
            }
        }

        if (listaConUbicacion.size() >=  1){ // si no es null tiene al menos un elemento

            Double limNorte = listaConUbicacion.get(0).getLugar().latitude, LimSur = listaConUbicacion.get(0).getLugar().latitude, limEste = listaConUbicacion.get(0).getLugar().longitude ,limOeste = listaConUbicacion.get(0).getLugar().longitude;

            for(Reclamo unReclamo : listaConUbicacion) {

                LatLng l = unReclamo.getLugar();
                if(l.latitude < LimSur) {
                    LimSur = l.latitude;
                }
                if(l.latitude > limNorte) {
                    limNorte = l.latitude;
                }
                if(l.longitude < limOeste) {
                    limOeste = l.longitude;
                }
                if(l.longitude > limEste) {
                    limEste = l.longitude;
                }

                mMap.addMarker(new MarkerOptions().position(l).title(unReclamo.getTitulo()));


            }
            if(listaConUbicacion.size() > 1) {
                // si hay más de un marcador agrego una linea desde el primer al último elemento
                mMap.addPolyline(
                        new PolylineOptions()
                                .add(listaConUbicacion.get(0).getLugar())
                                .add(listaConUbicacion.get(listaConUbicacion.size() - 1).getLugar())
                                .color(Color.RED))
                ;

            }
            LatLng coordenada1= new LatLng(LimSur, limOeste);
            LatLng coordenada2 = new LatLng(limNorte, limEste);

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(coordenada1, coordenada2), 10));
        }

    }
    private class onMarkerDrag implements GoogleMap.OnMarkerDragListener {

        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            ubicacion_reclamo = marker.getPosition();
        }
    }
}
