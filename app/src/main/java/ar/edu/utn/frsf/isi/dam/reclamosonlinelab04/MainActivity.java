package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDao;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDaoHTTP;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Estado;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.TipoReclamo;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemLongClickListener{
    private static final int CREAR = 0;
    private static final int EDITAR = 1;
    private static final String crear = "CREAR";
    private static final String editar = "EDITAR";

    private ReclamoDao daoReclamo;
    private ListView listViewReclamos;
    private List<Reclamo> listaReclamos;
    private ReclamoAdapter adapter;
    private Button btnNuevoReclamo;
    private Button btnVerTodo;

    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        daoReclamo = new ReclamoDaoHTTP();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listViewReclamos = (ListView) findViewById(R.id.mainListaReclamos);
        listViewReclamos.setOnItemLongClickListener(this);
        listaReclamos = new ArrayList<>();
        adapter = new ReclamoAdapter(this, listaReclamos);
        //new ReclamoAdapter(MainActivity.this, listaReclamos);
        listViewReclamos.setAdapter(adapter);

        //new AddReclamoTask().execute();
        actualizarLista();

        btnNuevoReclamo = (Button) findViewById(R.id.btnNuevoReclamo);
        btnNuevoReclamo.setOnClickListener(this);
        btnVerTodo = (Button) findViewById(R.id.btnVerTodo);
        btnVerTodo.setOnClickListener(this);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        //Se obtiene el item seleccionado para obtener el id que se pasa a la actividad de edición
        Reclamo reclamoSeleccionado = (Reclamo) listViewReclamos.getItemAtPosition(i);

        Intent intent = new Intent(this, FormReclamo.class);
        intent.setAction(editar);
        intent.putExtra("id", reclamoSeleccionado.getId().toString());
        startActivityForResult(intent, EDITAR);

        //Toast.makeText(this,reclamoSeleccionado.getId().toString(), Toast.LENGTH_LONG).show();

        return false;
    }

    /* No se usa. Después borrar */
    class AddReclamoTask extends AsyncTask<Void, Reclamo, Void> {
        @Override
        protected Void doInBackground(Void... unused) {
            List<Reclamo> reclamos = daoReclamo.reclamos();

            for (Reclamo reclamo: reclamos) {
                publishProgress(reclamo);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Reclamo... values) {
            listaReclamos.add(values[0]);
            adapter.notifyDataSetChanged();
        }
    }

    private void actualizarLista() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                List<Reclamo> rec = daoReclamo.reclamos();
                listaReclamos.clear();
                listaReclamos.addAll(rec);

                runOnUiThread(new Runnable() {
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };

        t = new Thread(r);
        t.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNuevoReclamo:
                //Abrir el formulario de reclamo
                Intent intent = new Intent(this, FormReclamo.class);
                intent.setAction(crear);
                startActivityForResult(intent, CREAR);
                break;
            case R.id.btnVerTodo:
                Intent intent_listar = new Intent(MainActivity.this, MapsActivity.class);
                intent_listar.putExtra("bandera", "listar");
                startActivity(intent_listar);

                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREAR) {
            switch (resultCode) {
                case RESULT_CANCELED:

                    break;
                case RESULT_OK:
                    //Actualizar la lista con el nuevo reclamo
                    Log.d("Ok","Salió");
                    actualizarLista();

                    break;
            }
        }

        else if (requestCode == EDITAR) {
            switch (resultCode) {
                case RESULT_CANCELED:

                    break;
                case RESULT_OK:
                    //Actualizar la lista con el reclamo borrado o editado
                    Log.d("Ok","Salió");
                    actualizarLista();

                    break;
            }
        }
    }
}
