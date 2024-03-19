package com.application.markus.easymeal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class TodasRecetas extends Fragment {

    ArrayList<InfoGeneralReceta> r;

    ListView lstOpciones;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.application.markus.easymeal.R.layout.activity_todas_recetas, container, false);

        lstOpciones = (ListView)view.findViewById(com.application.markus.easymeal.R.id.llista);

        r = new ArrayList<>();

        DataBaseConnection dbc = new DataBaseConnection();
        dbc.getDataAllRecipes(getActivity(), lstOpciones);

        lstOpciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View v, int position, long id)
            {
                TextView txt = (TextView) v.findViewById(com.application.markus.easymeal.R.id.nombreUsuario);
                String username = txt.getText().toString();

                TextView txt1 = (TextView) v.findViewById(com.application.markus.easymeal.R.id.nomRecepta);
                String recipeName = txt1.getText().toString();

                Intent intent = new Intent(getActivity(), SpecificRecipe.class);
                Bundle b = new Bundle();
                b.putString("USERNAME", username);
                b.putString("RECIPENAME", recipeName);

                intent.putExtras(b);

                startActivity(intent);

            }
        });


        AdView mAdView = (AdView) view.findViewById(com.application.markus.easymeal.R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return view;
    }
}