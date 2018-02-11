package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDao;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDaoHTTP;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Estado;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;

public class ReclamosPushService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    private ReclamoDao daoReclamo;
    public ReclamosPushService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        String body = remoteMessage.getNotification().getBody();

        //SE SUPONE QUE EL MENSAJE VIENE "ID_RECLAMO";"ID_ESTADO"
        int index = body.indexOf(';');
        int id_reclamo = Integer.parseInt(body.substring(0,index));
        int id_estado = Integer.parseInt(body.substring(index+1));

        //seteo nuevos datos de reclamo
        daoReclamo = new ReclamoDaoHTTP();
        Reclamo reclamoObtenido = daoReclamo.getReclamoById(Integer.valueOf(id_reclamo));
        Estado estado = daoReclamo.getEstadoById(id_estado);
        reclamoObtenido.setEstado(estado);
        daoReclamo.actualizar(reclamoObtenido);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentTitle("Reclamo Actualizado")
                        .setContentText("El reclamo " + id_reclamo + " paso al ESTADO " + estado.getTipo());
                                Intent resultIntent = new Intent(this, FormReclamo.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
