package com.example.ytka.aaaa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ytka.aaaa.DataBase.DBHelper;
import com.example.ytka.aaaa.DataBase.DataBaseHistory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Navigator.CallBusStop;
import Navigator.DirectionFinder;
import Navigator.DirectionFinderListener;
import Navigator.MyLocation;
import Navigator.Route;

import static android.graphics.Color.BLUE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private Button search, buildPath, meLocation, addCall, info, menu, returnToMap;

    private ToggleButton addToFavourites;//подсвеченна кнопка или нет

    private LinearLayout menuForPath, panelForManageDrive;
    private EditText beginLocation, endLocation;

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private MyLocation loc; Location cur;
     DirectionFinder directionFinder;//private
    private PopupWindow mPopupWindow;     private LinearLayout mRelativeLayout;

    private CallBusStop busStop;

    private boolean flagForCancel=false;

    static final String MapsActivityEnd = "com.example.ytka.aaaa.MapsActivity.End";
    static final String MapsActivityFlag = "com.example.ytka.aaaa.MapsActivity.Flag";


    final static int REQUEST_CODE_MENU_FAVOR = 1;

   private DBHelper dbHelper;//для работы с базами данных
    private DataBaseHistory dataBaseHistory;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        menuForPath = (LinearLayout) findViewById(R.id.menuForInputData);
        panelForManageDrive = (LinearLayout) findViewById(R.id.PanelForDrive);

        beginLocation = (EditText) findViewById(R.id.beginTarget);
        endLocation = (EditText) findViewById(R.id.endTarget);
        buildPath = (Button) findViewById(R.id.buildPath);
        meLocation = (Button)  findViewById(R.id.myLocation);
        returnToMap = (Button) findViewById(R.id.returnToMap);

        search = (Button) findViewById(R.id.searchPath);
        addCall = (Button) findViewById(R.id.addAlarm);
        info = (Button) findViewById(R.id.info);
        menu = (Button) findViewById(R.id.menu);
        addToFavourites = (ToggleButton) findViewById(R.id.addToFavor);

        dbHelper = new DBHelper(this);//база данных
        dataBaseHistory = new DataBaseHistory(this);

        mRelativeLayout = (LinearLayout) findViewById(R.id.PanelForDrive);

        loc = new MyLocation(this);//можно вызвать в конструкторе, но работает, не трогаем
        cur = loc.getLocation();

        View mapView = mapFragment.getView();
        if (mapView != null &&
                mapView.findViewById(1) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(1).getParent()).findViewById(2);
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 250);
        }

        {
            SQLiteDatabase sqLiteDatabase =dbHelper.getWritableDatabase();
            SQLiteDatabase sqLiteDatabaseHistory = dataBaseHistory.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.query(DBHelper.TABLE_FAVOR_ROUTE, null, null, null, null, null, null);
            Cursor cursorH = sqLiteDatabaseHistory.query(DataBaseHistory.TABLE_HISTORY, null, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                int endIndex = cursor.getColumnIndex(DBHelper.KEY_END_LOCATION);

                do {
                    FavorActivity.endList.add(cursor.getString(endIndex));
                } while (cursor.moveToNext());
            }

            if(cursorH.moveToFirst()){
                int endIndex = cursorH.getColumnIndex(DataBaseHistory.ROUTE);

                do{
                    HistoryActivity.listHistoryOfSearch.add(cursorH.getString(endIndex));
                }while(cursorH.moveToNext());
            } cursorH.close();
            cursor.close();
        }

        //создаем активного слушателя для кнопок
        View.OnClickListener clickButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase sqLiteDatabase =dbHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();

                switch (v.getId()) {
                    case R.id.searchPath:
                        menuForPath.setVisibility(View.VISIBLE);
                        info.setEnabled(false);
                        break;
                    case R.id.buildPath:
                        sendRequest();
                        menuForPath.setVisibility(View.GONE);
                        info.setEnabled(true);

                        //чтобы если надо кнопка избранно подсвечивалась, иначе не подсвечивалась
                        addToFavourites.setBackgroundResource(R.drawable.isbfalse);
                        addToFavourites.setChecked(false);
                        searhFavorToList();
                        break;
                    case R.id.myLocation:
                        beginLocation.setText("Мое местоположение");
                        break;
                    case R.id.returnToMap:
                        menuForPath.setVisibility(View.GONE);
                        info.setEnabled(true);
                        break;
                    case R.id.addAlarm:
                        if(flagForCancel==false) {
                            flagForCancel = true;
                            Toast.makeText(getApplicationContext(), "Будильник установлен", Toast.LENGTH_LONG).show();
                            addCall();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Будильник отменен", Toast.LENGTH_LONG).show();
                            flagForCancel = false;
                            busStop.kill();//убиваем поток
                        }
                        break;
                    case R.id.info:
                        showInfo();

                        break;
                    case R.id.menu:
                        Intent i = new Intent(MapsActivity.this, MenuActivity.class);
                        startActivityForResult(i, REQUEST_CODE_MENU_FAVOR);
                        break;

                    case R.id.addToFavor:
                        boolean checked = addToFavourites.isChecked();
                        String finalStop = endLocation.getText().toString();

                        if (checked) {
                            addToFavourites.setBackgroundResource(R.drawable.isbtrue);

                            //добавляем в базу данных
                            contentValues.put(DBHelper.KEY_END_LOCATION, finalStop);

                            sqLiteDatabase.insert(DBHelper.TABLE_FAVOR_ROUTE, null, contentValues);

                            FavorActivity.endList.add(endLocation.getText().toString());

                            // adapter2.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(),"Добавлено в избранное", Toast.LENGTH_LONG).show();
                            break;
                        }else {
                            addToFavourites.setBackgroundResource(R.drawable.isbfalse);

                            //удалить из базы данных
                            sqLiteDatabase.delete(DBHelper.TABLE_FAVOR_ROUTE, DBHelper.KEY_END_LOCATION + " = ?", new String[] {finalStop});

                            FavorActivity.endList.remove(FavorActivity.endList.indexOf((endLocation.getText().toString())));
                            Toast.makeText(getApplicationContext(),"Убранно из избранного", Toast.LENGTH_SHORT).show();

                            break;
                        }
                }
               sqLiteDatabase.close();
            }
        };
        search.setOnClickListener(clickButton);
        buildPath.setOnClickListener(clickButton);
        addCall.setOnClickListener(clickButton);
        info.setOnClickListener(clickButton);
        meLocation.setOnClickListener(clickButton);
        returnToMap.setOnClickListener(clickButton);

        menu.setOnClickListener(clickButton);
        addToFavourites.setOnClickListener(clickButton);


    }
    private void sendRequest() {
        String initalPlace = beginLocation.getText().toString();
        String ultimatePlace = endLocation.getText().toString();

        if (initalPlace.isEmpty() || ultimatePlace.isEmpty()) {
            Toast.makeText(this, "Укажите место", Toast.LENGTH_SHORT).show();
            return;
        }

        if(initalPlace.equals(ultimatePlace)){
            Toast.makeText(this, "Укажите разные точки", Toast.LENGTH_LONG).show();
        }

        try {
            //рассмитриваем случай, если там мое местоположение
            if(initalPlace.equals("Мое местоположение")){
                List<Address> address;
                Geocoder code;
                code = new Geocoder(this, Locale.getDefault());

                    try {
                        address = code.getFromLocation(loc.Latitude, loc.Longitude,1);
                        //getFromLocation возвращает List<Address> список адресов по документации, поэтому нельзя брать только строку
                        initalPlace = address.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

           directionFinder   = new DirectionFinder(this, initalPlace, ultimatePlace);
           directionFinder.execute();//получаем координаты конечной цели
           // new DirectionFinder(this, initalPlace, ultimatePlace, route).execute();

            HistoryActivity.listHistoryOfSearch.add(0, initalPlace+" - "+ultimatePlace);
            saveData(initalPlace+" - "+ultimatePlace);
            panelForManageDrive.setVisibility(View.VISIBLE);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){//если проблемы с координатами(выходят за границы)
            e.printStackTrace();
        }

    }

    private void addCall(){
        //сделать доп переменную, если нажата, то повторное нажите отмена и увдеоление
        //вызывать интерфейс как в поиске маршрута, чтобы изменить цвет кнопки
        String intalPlace = beginLocation.getText().toString();
        String ultimatePlace = endLocation.getText().toString();

        if(intalPlace.isEmpty() || ultimatePlace.isEmpty()){
            Toast.makeText(this, "Вы не указали маршрут", Toast.LENGTH_LONG).show();
            return;
        }

        if(intalPlace.equals(ultimatePlace)){
            Toast.makeText(this, "Укажите разные точки", Toast.LENGTH_LONG).show();
            return;
        }

        try {
                //route = directionFinder.routeCopy;
                busStop = new CallBusStop(directionFinder.routeCopy.endLocation, loc, this, directionFinder.routeCopy.duration.value);

                Thread t1 = new Thread(busStop);
                t1.start();

                notifyOk();
        }
        catch (Exception e){
            e.printStackTrace();
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

        LatLng ugatu = new LatLng(54.725620, 55.942579);
       // LatLng ugatu = new LatLng(loc.Latitude, loc.Longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ugatu, 15));

        mMap.addMarker(new MarkerOptions().position(ugatu).title("Marker in UGATU"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ugatu));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);//когда нажимаем на маркер, появляются кнопочки, которые перенаправляют в гугл мапс
        //а это конкуренты :)

    }

    @Override
    public void onStartBuild(){

        //если до этого был построен путь очищаем карту
        progressDialog = ProgressDialog.show(this, "Строим путь",      //показываем пользователю, что мы ещем путь
                // крутящееся колесико
                "Подождите, пожалуйста", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onBuild(List<Route> listRoute) {

        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

       // originMarkers.add(mMap.addMarker(new MarkerOptions()
            //    .title(listRoute.beginAddress)
             //   .position(route.endLocation)));

        for (Route route : listRoute) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.beginLocation, 16));
            // ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            // ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            /*originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.beginAddress)
                    .position(route.endLocation)));*/
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                   // .title(route.endAddress)
                    .title(endLocation.getText().toString())
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));

        }
    }

    protected  void onActivityResult(int requestCode, int resultCode, Intent data){


        if(resultCode!=RESULT_OK)
            return;

        if(requestCode==REQUEST_CODE_MENU_FAVOR){
            if(data==null)
                return;

            int flag = data.getIntExtra(MapsActivityFlag, 0);
            String whom = data.getStringExtra(MapsActivityEnd);

            switch (flag){
                case 1:
                    int pos=0;
                    for(int i=0; i<whom.length();i++){
                        if(whom.charAt(i)=='-'){
                            pos=i+1;
                            break;
                        }
                    }
                    beginLocation.setText(whom.substring(0, pos-2));
                    endLocation.setText(whom.substring(pos));
                    searhFavorToList();
                    sendRequest();
                    break;

                case 2:
                    beginLocation.setText("Мое местоположение");
                    endLocation.setText(whom);
                    sendRequest();

                    addToFavourites.setChecked(true);
                    addToFavourites.setBackgroundResource(R.drawable.isbtrue);
                    break;
            }

        }

    }

    private  void searhFavorToList(){
        int k = FavorActivity.endList.size();
        for(int i=0; i<k;i++ ){
            if(endLocation.getText().toString().equals(FavorActivity.endList.get(i))){
                addToFavourites.setChecked(true);
                addToFavourites.setBackgroundResource(R.drawable.isbtrue);
                break;
            }
        }
    }

    private void showInfo(){
        if(directionFinder.routeCopy==null){
            Toast.makeText(MapsActivity.this, "Ошибка, возможно,вы забыли ввести маршрут",Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(MapsActivity.this, directionFinder.routeCopy.duration.text+
                "  "+ directionFinder.routeCopy.distance.text, Toast.LENGTH_LONG).show();
    }


    public void saveData(String data){

        SQLiteDatabase sqLiteDatabaseH = dataBaseHistory.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DataBaseHistory.ROUTE, data);

        if(HistoryActivity.listHistoryOfSearch.size()% 5==0) {
           // sqLiteDatabaseH.delete(DataBaseHistory.TABLE_HISTORY, DataBaseHistory.KEY_ID + " = ?", new String[] {Integer.toString(4)});
            //sqLiteDatabaseH.update(DataBaseHistory.TABLE_HISTORY,contentValues,"_id = ?", new String[] {Integer.toString(4)});
           sqLiteDatabaseH.delete(DataBaseHistory.TABLE_HISTORY, null,null);
            //sqLiteDatabaseH.insert(DataBaseHistory.TABLE_HISTORY, null, contentValues);
        }
        else {
            sqLiteDatabaseH.insert(DataBaseHistory.TABLE_HISTORY, null, contentValues);
        }

        sqLiteDatabaseH.close();
    }

    //для уведомления
    private void notifyOk(){
        Intent resultIntent = new Intent(this, MapsActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("")
                        .setContentText("Go to map")
                        .setContentIntent(resultPendingIntent)
                        .setOngoing(true);

        final Notification notification = builder.build();

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(1, notification);


        notificationManager.cancel(1);
    }
}