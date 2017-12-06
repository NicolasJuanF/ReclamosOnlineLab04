package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
    private ReclamoDao daoReclamo;
    Spinner frmReclamoCmbTipo;
    Button frmReclamoCancelar, frmReclamoGuardar;
    EditText frmReclamoTextReclamo, frmReclamoTextDetReclamo;

    List<TipoReclamo> listaTiposReclamo;
    ArrayAdapter<TipoReclamo> spinnerAdapter;

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

        //Acciones de botones
        frmReclamoCancelar = (Button) findViewById(R.id.frmReclamoCancelar);
        frmReclamoCancelar.setOnClickListener(this);
        frmReclamoGuardar = (Button) findViewById(R.id.frmReclamoGuardar);
        frmReclamoGuardar.setOnClickListener(this);

        frmReclamoTextReclamo = (EditText) findViewById(R.id.frmReclamoTextReclamo);
        frmReclamoTextDetReclamo = (EditText) findViewById(R.id.frmReclamoTextDetReclamo);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.frmReclamoCancelar:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.frmReclamoGuardar:
                //Se guarda el reclamo
                Estado estado = new Estado(30,"Hola");
                TipoReclamo tipoReclamo = spinnerAdapter.getItem(frmReclamoCmbTipo.getSelectedItemPosition());


                final Reclamo reclamo = new Reclamo();
                reclamo.setTitulo(frmReclamoTextReclamo.getText().toString());
                reclamo.setDetalle(frmReclamoTextDetReclamo.getText().toString());
                reclamo.setFecha(new Date(20170303));
                reclamo.setEstado(estado);
                reclamo.setTipo(tipoReclamo);

                Runnable r = new Runnable() {
                    Reclamo r = reclamo;
                    @Override
                    public void run() {
                        daoReclamo.crear(reclamo);
                    }
                };

                Thread t = new Thread(r);
                t.start();

                setResult(RESULT_OK);
                finish();
                break;
        }
    }
}
