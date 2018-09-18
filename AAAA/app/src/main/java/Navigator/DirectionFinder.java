package Navigator;

import android.os.AsyncTask;

import com.example.ytka.aaaa.MapsActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ytka on 18.03.18.
 */

public class DirectionFinder {
    private static final String GOOGLE_KEY = "AIzaSyBGN_3YZ-TL7pQdsW0bmU_aMn4ZjyEEbNI";

    private final String URL_REQUEST = "https://maps.googleapis.com/maps/api/directions/json?";
    private String initalPlace;
    private String ultimatePlace;
    private DirectionFinderListener listener;

   // public LatLng routeCopy;//аааааа public!!!!!
    public Route routeCopy;

    //конструктор
    public DirectionFinder(DirectionFinderListener listener,String initalPlace, String ultimatePlace){
        this.listener = listener;
        this.initalPlace=initalPlace;
        this.ultimatePlace=ultimatePlace;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onStartBuild();
        new DowlandData().execute(createUrl()); //вызываем работу фоновой задачи, execute это не текущий метод, он относится к AsyncTask
        //return routeCopy;
    }

    private String createUrl() throws UnsupportedEncodingException{
        String URLinitalPlace = URLEncoder.encode(initalPlace, "utf-8");
        String URLultimatePlace = URLEncoder.encode(ultimatePlace, "utf-8");

        return URL_REQUEST + "origin=" + URLinitalPlace + "&destination=" + URLultimatePlace +"&mode=transit" + "&key=" + GOOGLE_KEY;

    }//&alternatives=true


    private class DowlandData extends AsyncTask<String, Void, String> {
        //либо сделать открытым, либо сделать функциюю для выдачи данных для вне

        //в <> 1 параметр это тип переаваемого значения, 2-ой параметр для оповещения в ходе выполнения, 3 тип возращаемого значения

        @Override
        protected String doInBackground(String... strings) {
            //String... значит, что метод не вкурсе сколько строк он будет принимать, их может быть одна, две и т.д.
            //у нас одна, это наша ссылочка, поэтому мы берем первый элемент массива строк
            try{


                URL url = new URL(strings[0]);    //создаем ссылочку, передаем в конструктор объекта URL ссылку типа String, там она
                //превратиться в в тип URL
                InputStream is = url.openConnection().getInputStream();
                //подключаемся(но фактически сетевое подключение не создается
                // и читаем параметры(в этой ссылке есть данные сколько км, сколько времени)
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuffer buffer = new StringBuffer();
                //по сути StringBuffer тоже самое, что и String, но String является неизменнным и когда мы хотим добавить
                //в строку что-то новое, то создается новый объект, а это плохо для памяти, ниже мы часто меняем строку, поэтому
                //лучше использовать Buffer

                String tmp;
                while((tmp=reader.readLine())!=null){   //пока читается
                    buffer.append(tmp+"\n");     //добавляем
                }

                reader.close();
                is.close(); //закрываем потоки
                return  buffer.toString();
            }
            catch (MalformedURLException e) {   //выбрасываем исключение, если неправильный URL
                e.printStackTrace();
            } catch (UnknownServiceException e){
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        /*onPostExecute() – выполняется после doInBackground() (может не вызываться, если AsyncTask был отменен).
        Имеет доступ к UI. Используйте его для обновления пользовательского интерфейса, как только ваша фоновая задача завершена.
         Данный обработчик при вызове синхронизируется с потоком GUI, поэтому внутри него вы можете безопасно изменять элементы
         пользовательского интерфейса.*/

        @Override
        protected void onPostExecute(String data){
            try{
                if(data==null)
                    return;

                //если коротко, то мы обрабатываем запрос
                JSONObject jsObj = new JSONObject(data);
                JSONArray jsArray = jsObj.getJSONArray("routes");
                ArrayList<Route> listRoute = new ArrayList<Route>();


                for(int i=0; i<jsArray.length();i++){
                    JSONObject jsonRoute = jsArray.getJSONObject(i);
                    Route route=new Route();

                    JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                    JSONArray jsLegs = jsonRoute.getJSONArray("legs");
                    JSONObject jsLeg = jsLegs.getJSONObject(0);
                    JSONObject jsonDistance = jsLeg.getJSONObject("distance");
                    JSONObject jsonDuration = jsLeg.getJSONObject("duration");
                    JSONObject jsonEndLocation = jsLeg.getJSONObject("end_location");
                    JSONObject jsonStartLocation = jsLeg.getJSONObject("start_location");

                    route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
                    route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
                    route.endAddress = jsLeg.getString("end_address");
                    route.beginAddress = jsLeg.getString("start_address");
                    route.beginLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
                    route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
                    route.points = decodePolyLine(overview_polylineJson.getString("points"));

                    listRoute.add(route);

                    //routeCopy=route.endLocation; //для того, чтобы получить в классе MapsActivity кооринаты конечной цели
                    routeCopy = route;




                }


                listener.onBuild(listRoute);


            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }

    //https://developers.google.com/maps/documentation/utilities/polylinealgorithm
    private List<LatLng> decodePolyLine(final String poly) {
        int length = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < length) {
            int elem;
            int shift = 0;
            int result = 0;
            do {
                elem = poly.charAt(index++) - 63;
                //poly.charAt(i) обращаемся к этому элементу, берем по элементно
                //если бы обращались так: poly[i], тогда бы нам возвращали целое слово
                result |= (elem & 0x1f) << shift;
                shift += 5;
            } while (elem >= 0x20);
            lat += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

            shift = 0;
            result = 0;
            do {
                elem = poly.charAt(index++) - 63;
                result |= (elem & 0x1f) << shift;
                shift += 5;
            } while (elem >= 0x20);
            lng += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));


            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }

}
