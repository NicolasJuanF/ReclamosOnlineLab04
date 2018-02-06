package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
    static final int REQUEST_IMAGE_CAPTURE = 1, PERMISSION_REQUEST_CAMERA = 2;


    private ReclamoDao daoReclamo;
    Spinner frmReclamoCmbTipo;
    Button frmReclamoCancelar, frmReclamoGuardar, frmReclamoEliminar, frmReclamoCamara;
    EditText frmReclamoTextReclamo, frmReclamoTextDetReclamo;
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



        frmReclamoTextReclamo = (EditText) findViewById(R.id.frmReclamoTextReclamo);
        frmReclamoTextDetReclamo = (EditText) findViewById(R.id.frmReclamoTextDetReclamo);

        //Obtener el intent para saber en qué modo entramos = CREAR:0 | EDITAR:1
        if (getIntent().getAction() == "CREAR") {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    List<TipoReclamo> rec = daoReclamo.tiposReclamo();
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
        }else {
            final Runnable ru = new Runnable() {
                @Override
                public void run() {
                    List<TipoReclamo> rec = daoReclamo.tiposReclamo();
                    listaTiposReclamo.clear();
                    listaTiposReclamo.addAll(rec);

                    //Obtener el reclamo con el id pasado
                    String id = getIntent().getStringExtra("id");
                    reclamoObtenido = daoReclamo.getReclamoById(Integer.valueOf(id));
                    Log.d("Detalle",reclamoObtenido.getDetalle());

                    runOnUiThread(new Runnable() {
                        public void run() {
                            spinnerAdapter.notifyDataSetChanged();

                            frmReclamoTextReclamo.setText(reclamoObtenido.getTitulo());
                            frmReclamoTextDetReclamo.setText(reclamoObtenido.getDetalle());

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
                            Bitmap imagen = ((BitmapDrawable) frmReclamoImgFoto.getDrawable()).getBitmap();
                            if (imagen != null) saveToInternalStorage(imagen);

                            setResult(RESULT_OK);
                            finish();
                        }
                    };

                    Thread t = new Thread(r);
                    t.start();
                }

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

        if (requestCode == PERMISSION_REQUEST_CAMERA ){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            frmReclamoImgFoto.setImageBitmap(imageBitmap);
        }
    }
}
