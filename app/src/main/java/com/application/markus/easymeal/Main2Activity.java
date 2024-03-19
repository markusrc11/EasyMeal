package com.application.markus.easymeal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Main2Activity extends AppCompatActivity {

    private String[] os = { "Android", "Windows Vista", "Windows 7",
            "Windows 8", "Ubuntu 12.04", "Ubuntu 12.10", "Mac OSX", "iOS 5",
            "iOS 6", "Solaris", "Kubuntu", "Suse" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.application.markus.easymeal.R.layout.activity_main2);
/*
        ArrayAdapter<String> adapterSugerencias = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, os);

        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.txtOS);

        // Numero de caracteres necesarios para que se empiece
        // a mostrar la lista
        textView.setThreshold(1);

        // Se establece el Adapter
        textView.setAdapter(adapterSugerencias);
        */
    }
}
