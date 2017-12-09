package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST = 0;

    private ReclamoDao daoReclamo;
    private ListView listViewReclamos;
    private List<Reclamo> listaReclamos;
    private ReclamoAdapter adapter;
    private Button btnNuevoReclamo;

    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        daoReclamo = new ReclamoDaoHTTP();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listViewReclamos = (ListView) findViewById(R.id.mainListaReclamos);
        listaReclamos = new ArrayList<>();
        adapter = new ReclamoAdapter(this, listaReclamos);
        //new ReclamoAdapter(MainActivity.this, listaReclamos);
        listViewReclamos.setAdapter(adapter);

        //new AddReclamoTask().execute();
        actualizarLista();

        btnNuevoReclamo = (Button) findViewById(R.id.btnNuevoReclamo);
        btnNuevoReclamo.setOnClickListener(this);
    }

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
                startActivityForResult(intent, REQUEST);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST) {
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
    }
}
