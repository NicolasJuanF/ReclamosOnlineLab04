package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class ReclamosInstanceId extends FirebaseInstanceIdService {
    public static final String TAG_TOKEN = "Token";
    public ReclamosInstanceId() {}

    @Override
    public  void onTokenRefresh(){
        // obtiene el token que lo identifica
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG_TOKEN, "Refreshed token"+refreshedToken);
        guardarToken(refreshedToken);
    }

    private void guardarToken(String tkn) {
        //guardarlo en un archivo
        // o en el servidor con un POST asociando un
        // nombre de usuario ficticio y hardcoded
    }

}




