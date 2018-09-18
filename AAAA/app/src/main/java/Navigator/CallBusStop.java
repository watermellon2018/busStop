package Navigator;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.telecom.Call;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.logging.Handler;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by ytka on 21.03.18.
 */

public class CallBusStop implements Runnable {

    private LatLng endLocation;
    private double latEnd, lonEnd;

   public boolean flagForCancel = true;

    private  MyLocation myLocation;
    private double latMe, lonMe;

    private Vibrator vibro;
    private AudioManager audio;
    private Context context; //:( I do not want this

    private long updateTime;


    public CallBusStop(LatLng endLocation, MyLocation myLocation, Context context, int time) {
        this.context = context;
        this.endLocation = endLocation;
        latEnd = this.endLocation.latitude;
        lonEnd = this.endLocation.longitude;
        updateTime =(long) time*1000;//время в секундах

        this.myLocation = myLocation;
        vibro = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    }

    @Override
    public void run() {
        int count =4;
       while (true && flagForCancel) {
           try {
               latMe = myLocation.Latitude;
               lonMe = myLocation.Longitude;

               double radius = sqrt(abs(pow(abs(latMe - latEnd), 2) - pow(abs(lonMe - lonEnd), 2)));
               if (radius < 0.004) {
                    Thread.sleep(7000);
                   double radiusCheck = sqrt(abs(pow(abs(latMe - latEnd), 2) - pow(abs(lonMe - lonEnd), 2)));

                   if(radius<=radiusCheck) {  //проверка для объездного пути
                       callClock();//подача сигнала
                       break;
                   }
               }
              // Thread.sleep(14000);
               if(updateTime/count>10) {
                   Thread.sleep(updateTime / count);
                   count *= 2;
               }else
                   Thread.sleep(8000);

           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           //предусмотреть вариант отключение будильника - предусмотренно, в цикле булевая переменная флаг
        }
    }

    private void callClock() throws InterruptedException {
        try {
            Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            /*Вместо того, чтобы возвращать фактический звуковой Uri мелодии звонка,
            это вернет символический Uri, который будет разрешен к фактическому звуку при воспроизведении.*/
           Ringtone ringtone = RingtoneManager.getRingtone(context, notify);
            ringtone.play();

            for(int i=0; i<3; i++) {
                vibro.vibrate(3000);
                Thread.sleep(4000);
            }
            Thread.sleep(2000);
            ringtone.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void kill(){
        this.flagForCancel = false;
    }
}