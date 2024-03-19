package com.application.markus.easymeal;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class SpecificRecipe extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(com.application.markus.easymeal.R.string.menu_nav_receta);
        setContentView(com.application.markus.easymeal.R.layout.activity_specific_recipe);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle info = this.getIntent().getExtras();

        String username = info.getString("USERNAME");
        String recipename = info.getString("RECIPENAME");

        DataBaseConnection dbc = new DataBaseConnection();
        dbc.getDataSpecificRecipe(username,recipename,this);
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
