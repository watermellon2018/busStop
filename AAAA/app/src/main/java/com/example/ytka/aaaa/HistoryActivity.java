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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    public ListView history;
    static final ArrayList<String> listHistoryOfSearch = new ArrayList<>();
    private ImageView dogForEmpty;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       // beginLocationHistore = (ListView) findViewById(R.id.listBeginHistory);
        history = (ListView) findViewById(R.id.history);

       // final ArrayAdapter<String> adapterForHistory = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listHistoryOfSearch);
        final ArrayAdapter<String> adapterForHistory = new ArrayAdapter<String>(this, R.layout.list_item, listHistoryOfSearch);
        history.setAdapter(adapterForHistory);

        if(listHistoryOfSearch.isEmpty()) {
            textEmpty = (TextView) findViewById(R.id.emptyHistory);
            dogForEmpty = (ImageView) findViewById(R.id.imageEmptyDog);
           // dogForEmpty.setImageResource(R.drawable.hasky_mem);
            textEmpty.setVisibility(View.VISIBLE);
            dogForEmpty.setVisibility(View.VISIBLE);

        }


        history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                Intent intentHistory = new Intent();
                intentHistory.putExtra(MapsActivity.MapsActivityEnd, adapterForHistory.getItem(position));
                intentHistory.putExtra(MapsActivity.MapsActivityFlag, 1);
                Log.d("CallBusStop", "one");
                setResult(RESULT_OK, intentHistory);
                finish();
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
