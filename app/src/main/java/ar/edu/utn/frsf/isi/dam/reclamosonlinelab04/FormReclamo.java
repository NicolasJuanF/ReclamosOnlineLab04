package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDao;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDaoHTTP;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Estado;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.TipoReclamo;

public class FormReclamo extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "LogCat";
    private static final int REQUEST_IMAGE_CAPTURE = 1, PERMISSION_REQUEST_CAMERA = 2 , PERMISSION_REQUEST_RECORD_AUDIO = 3 , REQUEST_MAPA = 4;

    //para grabacion de audio
    private MediaRecorder recorder = null; //seteo en null el recorder
    private MediaPlayer reproductor = null;
    private Boolean grabando = false; //dependiendo el estado de esta variable, el boton graba o detiene la grabacion
    private File fichero; //archivo donde va a ser guardado el audio
    //para mapas
    private LatLng LtLn = null; //si estoy creando viene en null



    private Integer id_reclamo = 0;

    private ReclamoDao daoReclamo;

    Spinner frmReclamoCmbTipo;
    Button frmReclamoCancelar, frmReclamoGuardar, frmReclamoEliminar, frmReclamoCamara , frmReclamoRecAudio, frmReclamoPlayAudio,elegirLugar ;
    EditText frmReclamoTextReclamo, frmReclamoTextDetReclamo , frmReclamoTextLugar;
    ImageView frmReclamoImgFoto;

    List<TipoReclamo> listaTiposReclamo;
    ArrayAdapter<TipoReclamo> spinnerAdapter;

    Reclamo nuevoReclamo;
    Reclamo reclamoObtenido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        daoReclamo = new ReclamoDaoHTTP();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_reclamo);

        //Setear los tipos de reclamos en el spinner
        frmReclamoCmbTipo = (Spinner) findViewById(R.id.frmReclamoCmbTipo);
        listaTiposReclamo = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<TipoReclamo>(this, android.R.layout.simple_spinner_item, listaTiposReclamo);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frmReclamoCmbTipo.setAdapter(spinnerAdapter);
        frmReclamoRecAudio = (Button) findViewById(R.id.frmReclamoRecAudio);
        frmReclamoPlayAudio = (Button) findViewById(R.id.frmReclamoPlayAudio);
        elegirLugar = (Button) findViewById(R.id.elegirLugar);
        frmReclamoTextLugar = (EditText) findViewById(R.id.frmReclamoTextLugar);
        frmReclamoTextReclamo = (EditText) findViewById(R.id.frmReclamoTextReclamo);
        frmReclamoTextDetReclamo = (EditText) findViewById(R.id.frmReclamoTextDetReclamo);

        //Obtener el intent para saber en qué modo entramos = CREAR:0 | EDITAR:1
        if (getIntent().getAction() == "CREAR") {
            frmReclamoRecAudio.setEnabled(false);
            frmReclamoPlayAudio.setEnabled(false);
            Runnable r = new Runnable() {
                @Override
                public void run() {

                    List<TipoReclamo> rec = daoReclamo.tiposReclamo();
                    id_reclamo = Integer.valueOf('0');
                    listaTiposReclamo.clear();
                    listaTiposReclamo.addAll(rec);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            spinnerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
            Thread t = new Thread(r);
            t.start();
        }else {//si es modificar se el id
            frmReclamoPlayAudio.setEnabled(false);
            id_reclamo = Integer.valueOf(getIntent().getStringExtra("id"));//variable global

            // Habilitar o no audio
            File directory = getApplicationContext().getDir("audios", Context.MODE_PRIVATE);
            if(!directory.exists())
                directory.mkdir();
            fichero = new File(directory, "reclamo_" + id_reclamo + ".3gp");

            if(fichero.exists()) {//si existe se puede reproducir
                frmReclamoPlayAudio.setEnabled(true);
            }

            final Runnable ru = new Runnable() {
                @Override
                public void run() {
                    List<TipoReclamo> rec = daoReclamo.tiposReclamo();
                    listaTiposReclamo.clear();
                    listaTiposReclamo.addAll(rec);

                    //Obtener el reclamo con el id pasado
                    String id = getIntent().getStringExtra("id");//variable local
                    reclamoObtenido = daoReclamo.getReclamoById(Integer.valueOf(id));
                    LtLn = reclamoObtenido.getLugar();

                    Log.d("Detalle", String.valueOf(LtLn));

                    runOnUiThread(new Runnable() {
                        public void run() {

                            spinnerAdapter.notifyDataSetChanged();

                            frmReclamoTextReclamo.setText(reclamoObtenido.getTitulo());
                            frmReclamoTextDetReclamo.setText(reclamoObtenido.getDetalle());
                            if (LtLn != null ){
                                String latitud = String.valueOf(LtLn.latitude);
                                String longitud = String.valueOf(LtLn.longitude);

                                frmReclamoTextLugar.setText("Lat: " + latitud + "/ Long:"  + longitud);
                            }


                            Bitmap imagen = loadImageFromStorage();
                            if (imagen != null) {
                                frmReclamoImgFoto.setImageBitmap(imagen);
                            }

                            TipoReclamo tipo = reclamoObtenido.getTipo();

                            Log.d("Count",String.valueOf(spinnerAdapter.getCount()));
                            Log.d("Index", String.valueOf(spinnerAdapter.getPosition(tipo)));
                            Log.d("Tipo", tipo.getTipo());

                            for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                                TipoReclamo tipoRspinner = (TipoReclamo) spinnerAdapter.getItem(i);
                                if (tipoRspinner.getId() == tipo.getId()) {
                                    frmReclamoCmbTipo.setSelection(spinnerAdapter.getPosition(tipoRspinner));
                                }
                            }

                        }
                    });
                }
            };
            Thread th = new Thread(ru);
            th.start();

        }

        //Acciones de botones
        frmReclamoCamara = (Button) findViewById(R.id.frmReclamoCamara);
        frmReclamoCamara.setOnClickListener(this);


        frmReclamoRecAudio.setOnClickListener(this);
        frmReclamoPlayAudio.setOnClickListener(this);

        elegirLugar.setOnClickListener(this);

        frmReclamoCancelar = (Button) findViewById(R.id.frmReclamoCancelar);
        frmReclamoCancelar.setOnClickListener(this);
        frmReclamoGuardar = (Button) findViewById(R.id.frmReclamoGuardar);
        frmReclamoGuardar.setOnClickListener(this);

        frmReclamoEliminar = (Button) findViewById(R.id.frmReclamoEliminar);
        frmReclamoEliminar.setOnClickListener(this);

        frmReclamoImgFoto = (ImageView) findViewById(R.id.frmReclamoImgFoto);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.frmReclamoCamara:
                //Chequear si tengo el permiso para usar la cámara
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    //Si no los tenia, entonces pedirlos
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA );

                }
                else {
                    tomarFoto();
                }
                break;
            case R.id.frmReclamoEliminar:
                Runnable b = new Runnable() {
                    @Override
                    public void run() {
                        daoReclamo.borrar(reclamoObtenido);

                        setResult(RESULT_OK);
                        finish();
                    }
                };

                Thread tb = new Thread(b);
                tb.start();
                break;
            case R.id.frmReclamoCancelar:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.frmReclamoGuardar:
                Estado estado = new Estado(1,"Nuevo");
                TipoReclamo tipoReclamo = spinnerAdapter.getItem(frmReclamoCmbTipo.getSelectedItemPosition());

                nuevoReclamo = new Reclamo();
                nuevoReclamo.setTitulo(frmReclamoTextReclamo.getText().toString());
                nuevoReclamo.setDetalle(frmReclamoTextDetReclamo.getText().toString());
                nuevoReclamo.setFecha(new Date());
                nuevoReclamo.setEstado(estado);
                nuevoReclamo.setTipo(tipoReclamo);
                nuevoReclamo.setLugar(LtLn);

                //Obtener el intent para saber en qué modo entramos = CREAR:0 | EDITAR:1
                if (getIntent().getAction() == "CREAR") {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            daoReclamo.crear(nuevoReclamo);

                            setResult(RESULT_OK);
                            finish();
                        }
                    };

                    Thread t = new Thread(r);
                    t.start();
                }else {
                    nuevoReclamo.setId(reclamoObtenido.getId());

                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            daoReclamo.actualizar(nuevoReclamo);
                            BitmapDrawable bmd = (BitmapDrawable) frmReclamoImgFoto.getDrawable();

                            if(bmd != null) {
                                Bitmap imagen = bmd.getBitmap();
                                if (imagen != null) saveToInternalStorage(imagen);
                            }

                            setResult(RESULT_OK);
                            finish();
                        }
                    };

                    Thread t = new Thread(r);
                    t.start();
                }

                break;
            case R.id.frmReclamoRecAudio:
                //Chequear si tengo el permiso para usar la cámara
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    //Si no los tenia, entonces pedirlos
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSION_REQUEST_RECORD_AUDIO );

                }
                else { //si tenia permiso me fijo si no estaba grabando para empezar a grabar
                    if (grabando){
                        pararAudio();
                    }else{
                        grabarAudio();
                    }
                }
                break;
            case R.id.frmReclamoPlayAudio:
                reproducir();
                break;
            case R.id.elegirLugar:

                Intent intent = new Intent(FormReclamo.this, MapsActivity.class);
                intent.putExtra("bandera" , "lugar");
                if(LtLn  != null) {//si ya tenia un punto en el mapa
                    intent.putExtra("ubicacion" , LtLn);
                }
                startActivityForResult(intent, REQUEST_MAPA);

                break;

        }
    }

    public void tomarFoto() {
        Intent tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (tomarFotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(tomarFotoIntent,REQUEST_IMAGE_CAPTURE);
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        File directory = getApplicationContext().getDir( "imagenes" , Context. MODE_PRIVATE );
        if (!directory.exists()) directory.mkdir();
        File mypath= new File(directory, "reclamo_" + reclamoObtenido.getId()+ ".jpg" );

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private Bitmap loadImageFromStorage()
    {
        Bitmap b = null;
        File directory = getApplicationContext().getDir( "imagenes" , Context. MODE_PRIVATE );
        if (!directory.exists()) directory.mkdir();
        File mypath= new File(directory, "reclamo_" + reclamoObtenido.getId()+ ".jpg" );

        try {
            //File f = new File(path, "profile.jpg");

            b = BitmapFactory.decodeStream(new FileInputStream(mypath));
            //ImageView img=(ImageView)findViewById(R.id.imgPicker);
            //img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return b;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tomarFoto();
                }
                break;
            case PERMISSION_REQUEST_RECORD_AUDIO:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//true => tengo el permiso
                    grabarAudio();
                } else {//no tengo el permiso para grabar audio
                    //Toast.makeText(FormReclamo.this, "Pidio audio y rechazo", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    private void grabarAudio() {
        grabando=true;
        frmReclamoRecAudio.setText("PARAR");
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fichero.getAbsolutePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {//hubo error, no grabó
            Log.e(TAG, "prepare() failed");
            grabando=false;
        }
        recorder.start();
    }

    private void pararAudio(){
        grabando= false;
        frmReclamoRecAudio.setText("GRABAR AUDIO");
        recorder.stop();
        recorder.release();
        recorder = null;
        frmReclamoPlayAudio.setEnabled(true);
    }

    private void reproducir() {

        reproductor = new MediaPlayer();

        try {

            reproductor.setDataSource(String.valueOf(fichero));

            reproductor.prepare();

            reproductor.start();

        } catch (IOException e) {

            Log.e(TAG, "Fallo en reproducción");

        }

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            frmReclamoImgFoto.setImageBitmap(imageBitmap);
        }
        if (requestCode == REQUEST_MAPA && resultCode == RESULT_OK) {
            LtLn = data.getParcelableExtra("ubicacion");
            String latitud = String.valueOf(LtLn.latitude);
            String longitud = String.valueOf(LtLn.longitude);

            frmReclamoTextLugar.setText("Lat: " + latitud + "/ Long:"  + longitud);
        }


    }
}
