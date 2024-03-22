package com.application.markus.easymeal;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewRecipe extends AppCompatActivity {

    private Button btnAddIng, btnAddPaso;
    private ImageView img;
    private List<String> ingListRecView, stepListRecView, listaI, listaP, listaQI, bddIngs;
    IngredientAdapter adapterI;
    PasosAdapter adapterS;
    private int countIng, countPasos;

    private static String APP_DIRECTORY = "EasyMeal/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "Mis Fotos";

    private final String IMAGE_SERVER_URL = "http://easymeal.esy.es/images/recipesImages/";
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;
    private String pathString="";
    private Uri path;

    EditText nombreReceta, descReceta;
    private Bitmap bmp;

    UsuariosSQLiteHelper udb;
    ArrayAdapter<String> adapterSugerencias;
    View generalView;
    ScrollView sv;
    TextView tv;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.application.markus.easymeal.R.layout.activity_new_recipe);

        generalView = findViewById(com.application.markus.easymeal.R.id.layoutNewRecipe);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final RecyclerView recListI = (RecyclerView) findViewById(com.application.markus.easymeal.R.id.recyclerViewIng);
        recListI.setHasFixedSize(true);
        LinearLayoutManager llmI = new LinearLayoutManager(this);
        llmI.setOrientation(LinearLayoutManager.VERTICAL);
        recListI.setLayoutManager(llmI);

        final RecyclerView recListP = (RecyclerView) findViewById(com.application.markus.easymeal.R.id.recyclerViewPasos);
        recListP.setHasFixedSize(true);
        LinearLayoutManager llmP = new LinearLayoutManager(this);
        llmP.setOrientation(LinearLayoutManager.VERTICAL);
        recListP.setLayoutManager(llmP);

        nombreReceta = (EditText) findViewById(com.application.markus.easymeal.R.id.etRecipeName);
        descReceta = (EditText) findViewById(com.application.markus.easymeal.R.id.etCreacionDesc);
        sv = (ScrollView) findViewById(com.application.markus.easymeal.R.id.scrollView);
        tv = (TextView) findViewById(com.application.markus.easymeal.R.id.txtPasos);

        initializeData();

        udb = new UsuariosSQLiteHelper(this);

        cursor = udb.getCursorBuscador();
        getIngredientsList();

        adapterSugerencias = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, bddIngs);

        countIng = countPasos = 0;

        btnAddIng = (Button) findViewById(com.application.markus.easymeal.R.id.btnAddIng);
        btnAddPaso = (Button) findViewById(com.application.markus.easymeal.R.id.btnAddPaso);


        adapterI = new IngredientAdapter(ingListRecView, recListI);
        recListI.setAdapter(adapterI);

        btnAddIng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countIng++;
                ingListRecView.add("");
                adapterI.notifyItemInserted(countIng);

                recListI.scrollToPosition(countIng);
            }
        });

        adapterS = new PasosAdapter(stepListRecView, recListP);
        recListP.setAdapter(adapterS);

        btnAddPaso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countPasos++;
                stepListRecView.add("");
                adapterS.notifyItemInserted(countPasos);

                recListP.scrollToPosition(countPasos);
            }
        });

        img = (ImageView) findViewById(com.application.markus.easymeal.R.id.imgCreacionReceta);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.application.markus.easymeal.R.menu.menu_new_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.application.markus.easymeal.R.id.addRecipe) {
            getDatos();
        } else if (id == android.R.id.home) {
            showExplanation();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewRecipe.this);
        builder.setTitle(com.application.markus.easymeal.R.string.salir_title_new_rec);
        builder.setMessage(com.application.markus.easymeal.R.string.salir_new_recipe);
        builder.setPositiveButton(com.application.markus.easymeal.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton(com.application.markus.easymeal.R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void initializeData() {
        listaI = new ArrayList<>();
        listaP = new ArrayList<>();
        listaQI = new ArrayList<>();
        bddIngs = new ArrayList<>();

        ingListRecView = new ArrayList<>();
        stepListRecView = new ArrayList<>();
        ingListRecView.add("");
        stepListRecView.add("");
    }

    private void getDatos() {
        List<Ingredient> li = adapterI.getIngQuant(bddIngs);

        if(li != null && li.size() > 0){
            List<Steps> lp = adapterS.getPasos();

            Log.d("PASOSM", String.valueOf(lp.size()));

            SharedPreferences prefs = getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);
            String nomUsuari = prefs.getString("username", getResources().getString(com.application.markus.easymeal.R.string.anonimo));

            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            InfoGeneralReceta igr = new InfoGeneralReceta(nomUsuari, "", nombreReceta.getText().toString(), pathString, date, descReceta.getText().toString(), lp, li);

            Log.d("PASOS SIZE", String.valueOf(igr.getPasos().size()));

            new DataBaseConnection().recipeInsert(igr, IMAGE_SERVER_URL, listaI, getApplicationContext(), this);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showExplanation();
    }

    private void getIngredientsList()
    {
        if (cursor.moveToFirst()) {
            do {
                bddIngs.add(cursor.getString(0));
            } while(cursor.moveToNext());
        }
    }


    public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

        private List<String> ingList;
        private RecyclerView recList;

        public IngredientAdapter(List<String> ingList, RecyclerView recList) {
            this.ingList = ingList;
            this.recList = recList;
        }

        @Override
        public int getItemCount() {
            return ingList.size();
        }

        @Override
        public void onBindViewHolder(IngredientViewHolder ingredientViewHolder, int i) {
            ingredientViewHolder.textView.setText(ingList.get(i));
            ingredientViewHolder.btnDelete.setTag(i);
        }

        @Override
        public IngredientViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(com.application.markus.easymeal.R.layout.layout_creacion_ing, viewGroup, false);

            return new IngredientViewHolder(itemView);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public List<Ingredient> getIngQuant(List<String> bddIng){
            List<Ingredient> li = new ArrayList<>();
            View child;

            for (int i = 0; i < recList.getChildCount(); i++) {
                child = recList.getChildAt(i);

                String ing = new IngredientViewHolder(child).textView.getText().toString();
                if(bddIng.contains(ing)) {
                    String quant = new IngredientViewHolder(child).et.getText().toString();

                    if(quant.length() > 0)
                        li.add(new Ingredient(ing, quant));
                    else {
                        new IngredientViewHolder(child).et.requestFocus();
                        snack(String.format(getString(com.application.markus.easymeal.R.string.falta_cantidad), ing));
                        return null;
                    }
                }
                else {
                    new IngredientViewHolder(child).textView.requestFocus();
                    snack(String.format(getString(com.application.markus.easymeal.R.string.ingredient_not_exist), ing));
                    return null;
                }
            }
            return li;
        }

        private void snack(String msg){
            Snackbar.make(generalView, msg,
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                }
            }).show();
        }



        public class IngredientViewHolder extends RecyclerView.ViewHolder {

            Button btnDelete;
            AutoCompleteTextView textView;
            EditText et;

            public IngredientViewHolder(View v) {
                super(v);
                btnDelete = (Button) v.findViewById(com.application.markus.easymeal.R.id.btnIngDelete);
                textView = (AutoCompleteTextView) v.findViewById(com.application.markus.easymeal.R.id.txtNewIng);
                et = (EditText) v.findViewById(com.application.markus.easymeal.R.id.etNewQuant);

                if (getItemCount() > 1)
                    textView.requestFocus();

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(countIng > 0) {
                            int pos = (int) v.getTag();

                            if(pos < ingList.size()) {
                                countIng--;

                                ingList.remove(pos);
                                notifyItemRemoved(pos);

                                recList.scrollToPosition(ingList.size());
                            }
                        }
                    }
                });

                // Numero de caracteres necesarios para que se empiece
                // a mostrar la lista
                textView.setThreshold(2);

                // Se establece el Adapter
                textView.setAdapter(adapterSugerencias);
            }
        }
    }


    public class PasosAdapter extends RecyclerView.Adapter<PasosAdapter.PasosViewHolder> {


        private List<String> pasosList;
        private RecyclerView recList;

        public PasosAdapter(List<String> pasosList, RecyclerView recList) {
            this.recList = recList;
            this.pasosList = pasosList;
        }

        @Override
        public int getItemCount() {
            return pasosList.size();
        }

        @Override
        public void onBindViewHolder(PasosViewHolder pasosViewHolder, int i) {
            pasosViewHolder.et.setText(pasosList.get(i));
            pasosViewHolder.btnDelete.setTag(i);
        }

        @Override
        public PasosViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(com.application.markus.easymeal.R.layout.layout_creacion_pasos, viewGroup, false);

            return new PasosViewHolder(itemView);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public List<Steps> getPasos(){
            List<Steps> lp = new ArrayList<>();
            View child;

            for (int i = 0; i < recList.getChildCount(); i++) {
                child = recList.getChildAt(i);

                String pas = new PasosViewHolder(child).et.getText().toString();

                lp.add(new Steps(String.valueOf(i + 1), pas, ""));
            }
            return lp;
        }

        public class PasosViewHolder extends RecyclerView.ViewHolder {

            Button btnDelete;
            EditText et;

            public PasosViewHolder(View v) {
                super(v);
                btnDelete = (Button) v.findViewById(com.application.markus.easymeal.R.id.btnPasosDelete);
                et = (EditText) v.findViewById(com.application.markus.easymeal.R.id.etLayoutPasos);

                if (getItemCount() > 1)
                    et.requestFocus();

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (countPasos > 0) {
                            int pos = (int) v.getTag();

                            if(pos < pasosList.size()) {
                                countPasos--;

                                pasosList.remove(pos);

                                notifyItemRemoved(pos);

                                recList.scrollToPosition(stepListRecView.size());
                            }
                        }
                    }
                });
            }
        }
    }

    private void showOptions() {
        final String foto = getResources().getString(com.application.markus.easymeal.R.string.menu_camara_tomar_foto);
        final String galeria = getResources().getString(com.application.markus.easymeal.R.string.menu_camara_galeria);
        final String cancelar = getResources().getString(com.application.markus.easymeal.R.string.menu_camara_cancelar);
        final String elegir = getResources().getString(com.application.markus.easymeal.R.string.menu_camara_elegir_opcion);
        final String selecciona = getResources().getString(com.application.markus.easymeal.R.string.menu_camara_selecciona_imagen);

        final CharSequence[] option = {foto, galeria, cancelar};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(elegir);
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (option[which] == foto) {
                    openCamera();
                } else if (option[which] == galeria) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, selecciona), SELECT_PICTURE);
                } else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    private void openCamera() {
        try {

            File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
            boolean isDirectoryCreated = file.exists();

            if (!isDirectoryCreated)
                isDirectoryCreated = file.mkdirs();

            if (isDirectoryCreated) {
                Long timestamp = System.currentTimeMillis() / 1000;
                String imageName = timestamp.toString() + ".jpg";

                pathString = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                        + File.separator + imageName;

                File newFile = new File(pathString);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", newFile));
                startActivityForResult(intent, PHOTO_CODE);
            }
        } catch (Exception ex) {
            Log.d("ERROR--> ", ex.getMessage());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", pathString);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        pathString = savedInstanceState.getString("file_path");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{pathString}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> Uri = " + uri);
                                }
                            });


                    bmp = BitmapFactory.decodeFile(pathString);
                    bmp = ImageRotation.modifyOrientation(bmp, pathString);

                    img.setImageBitmap(bmp);

                    break;

                case SELECT_PICTURE:
                    path = data.getData();
                    pathString = getRealPathFromURI(getApplicationContext(),path);

                    try {
                        bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);
                    } catch (Exception e) {
                        Log.d("ERROR --> ", e.getMessage());
                    }

                    bmp = ImageRotation.modifyOrientation(bmp, pathString);

                    img.setImageBitmap(bmp);

                    break;
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}

