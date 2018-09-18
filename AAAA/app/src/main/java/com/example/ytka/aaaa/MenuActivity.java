package com.example.ytka.aaaa;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class MenuActivity extends AppCompatActivity {

    Button history, favor, setting, aboutUs;
   public static ImageView avatar;

    static  final String MenuActivity = "com.example.ytka.aaaa.MenuActivity";
    static  final String MenuActivityReturn = "com.example.ytka.aaaa.MenuActivity.Return";
    static final int REQUEST_CODE_FAVOR = 1;
    static final int REQUEST_CODE_HISTORY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

       history = (Button) findViewById(R.id.historyOfSearch);
       favor = (Button) findViewById(R.id.favor);
       setting = (Button) findViewById(R.id.settings);
       aboutUs = (Button) findViewById(R.id.aboutUs);

      // avatar = (ImageView) findViewById(R.id.avatar);

        View.OnClickListener itemMenu = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.historyOfSearch:
                        Intent intentHistory = new Intent(MenuActivity.this, HistoryActivity.class);
                        startActivityForResult(intentHistory, REQUEST_CODE_HISTORY);
                        break;
                    case  R.id.favor:
                        Intent i = new Intent(MenuActivity.this, FavorActivity.class);
                        startActivityForResult(i, REQUEST_CODE_FAVOR);
                        break;
                    case R.id.settings:
                      ///  Intent settingIntent = new Intent(MenuActivity.this, SettingActivity.class);
                      //  startActivity(settingIntent);
                        break;
                    case R.id.aboutUs:
                        Intent intentUs = new Intent(MenuActivity.this, AboutUsActivity.class);
                        startActivity(intentUs);
                        break;
                }
            }
        };


        favor.setOnClickListener(itemMenu);
        history.setOnClickListener(itemMenu);
        setting.setOnClickListener(itemMenu);
        aboutUs.setOnClickListener(itemMenu);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode!=RESULT_OK)
            return;
        if(requestCode==REQUEST_CODE_FAVOR || requestCode==REQUEST_CODE_HISTORY){
            if(data==null)
                return;

            setResult(RESULT_OK,data);
            finish();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // onBackPressed(); // ну или что там надо делать
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}