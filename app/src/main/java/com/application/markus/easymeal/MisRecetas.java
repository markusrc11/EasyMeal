package com.application.markus.easymeal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class MisRecetas extends Fragment {

    ListView lstOpciones;
    View snackbarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(com.application.markus.easymeal.R.layout.activity_mis_recetas, container, false);

        snackbarView = view;
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(com.application.markus.easymeal.R.id.fab);

        lstOpciones = (ListView)view.findViewById(com.application.markus.easymeal.R.id.lista_mis_recetas);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent intent = new Intent(getActivity(),NewRecipe.class);
                startActivity(intent);
            }
        });


        lstOpciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View v, int position, long id)
            {
                SharedPreferences prefs = getActivity().getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);
                String nom = prefs.getString("username", getResources().getString(com.application.markus.easymeal.R.string.anonimo));

                TextView txt1 = (TextView) v.findViewById(com.application.markus.easymeal.R.id.nomRecepta);
                String recipeName = txt1.getText().toString();

                Intent intent = new Intent(getActivity(), SpecificRecipe.class);
                Bundle b = new Bundle();
                b.putString("USERNAME", nom);
                b.putString("RECIPENAME", recipeName);

                intent.putExtras(b);

                startActivity(intent);

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = this.getActivity().getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);
        String nom = prefs.getString("username", getResources().getString(com.application.markus.easymeal.R.string.anonimo));

        if (!nom.equals("") && !nom.equals(getResources().getString(com.application.markus.easymeal.R.string.anonimo)))
        {
            DataBaseConnection dbc = new DataBaseConnection();
            dbc.getDataMyRecipes(getActivity(), lstOpciones, nom);
        }

        else
        {
            Snackbar.make(snackbarView, com.application.markus.easymeal.R.string.snackbar_no_user,
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),LoginRegister.class);
                    startActivity(intent);
                }
            }).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.application.markus.easymeal.R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
