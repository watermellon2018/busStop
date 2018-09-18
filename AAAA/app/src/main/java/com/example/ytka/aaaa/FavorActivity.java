package com.example.ytka.aaaa;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ytka.aaaa.DataBase.DBHelper;

import java.util.ArrayList;

public class FavorActivity extends AppCompatActivity {

    public static ListView begin, end;
    public final static ArrayList<String> endList = new ArrayList<>();
    private TextView empty;
    private ImageView catForEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        end = (ListView) findViewById(R.id.listEnd);

        final ArrayAdapter<String> adapterEnd = new ArrayAdapter<>(this,R.layout.list_item, endList);
        end.setAdapter(adapterEnd);

        if(endList.isEmpty()) {
            empty = (TextView) findViewById(R.id.empty);
            catForEmpty = (ImageView) findViewById(R.id.imageEmptyCat);
            catForEmpty.setImageResource(R.drawable.catforempty);
            empty.setVisibility(View.VISIBLE);
            catForEmpty.setVisibility(View.VISIBLE);
        }

        end.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                DBHelper dbHelper = new DBHelper(FavorActivity.this);//для работы с базами данных
                SQLiteDatabase sqLiteDatabase =dbHelper.getWritableDatabase();
                sqLiteDatabase.delete(DBHelper.TABLE_FAVOR_ROUTE, DBHelper.KEY_END_LOCATION + " = ?", new String[] { adapterEnd.getItem(position)});

                endList.remove(position);
                adapterEnd.notifyDataSetChanged();

              //  setResult(RESULT_CANCELED);
                return false;
            }
        }) ;

        end.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                Intent intentFavorEnd = new Intent();
                intentFavorEnd.putExtra(MapsActivity.MapsActivityEnd, adapterEnd.getItem(position));
                intentFavorEnd.putExtra(MapsActivity.MapsActivityFlag, 2);
                setResult(RESULT_OK, intentFavorEnd);
                finish();
            }
        });

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
