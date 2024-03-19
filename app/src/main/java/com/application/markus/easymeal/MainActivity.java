package com.application.markus.easymeal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS = 100;

    private TextView tv;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private String nomUsuari;
    List<String> allIngredients;
    Cursor c;
    private ImageView iv;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_nav_recetas);

        loadLanguage();

        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        new DataBaseConnection().getAllIngredients(this);
        c = new UsuariosSQLiteHelper(this).getNumberIng();


        navigationView = (NavigationView) findViewById(R.id.nav_view);

        //canviar pagina d'inici perk sorti l allista
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new TodasRecetas()).commit();
            //startActivity(new Intent(this, NewRecipe.class));


        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener(){
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem){

                        boolean fragmentTransaction = false;
                        Fragment fragment = null;
                        Intent intent;

                        switch (menuItem.getItemId()) {
                            case R.id.menuRecetas:
                                fragment = new TodasRecetas();
                                fragmentTransaction = true;
                                break;

                            case R.id.menu_misRecetas:
                                fragment = new MisRecetas();
                                fragmentTransaction = true;
                                break;

                            case R.id.menu_cuenta:
                                intent = new Intent(MainActivity.this,LoginRegister.class);
                                startActivity(intent);
                                menuItem.setChecked(false);
                                break;

                            case R.id.menuAjustes:
                                intent = new Intent(MainActivity.this,Ajustes.class);
                                startActivity(intent);
                                menuItem.setChecked(false);
                                break;

                            case R.id.menuAyuda:
                                if (mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();
                                } else
                                    enviarMail();
                                break;

                            case R.id.menu_SobreNosotros:
                                intent = new Intent(MainActivity.this,SobreNosotros.class);
                                startActivity(intent);
                                break;
                        }

                        if(fragmentTransaction) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.content_frame, fragment)
                                    .commit();

                            menuItem.setChecked(true);
                            getSupportActionBar().setTitle(menuItem.getTitle());
                        }

                        drawer.closeDrawers();

                        return true;
                    }
                }
        );

        mayRequestStoragePermission();

        anunci();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);
        nomUsuari = prefs.getString("username", getResources().getString(R.string.anonimo));
        String img = new UsuariosSQLiteHelper(this).getUserImage(nomUsuari);

        View header = navigationView.getHeaderView(0);
        tv = (TextView)header.findViewById(R.id.NomUsuariNavView);
        tv.setText(nomUsuari);

        iv = (ImageView) header.findViewById(R.id.imgUser);
        Log.d("TTTT",img);
        if(img.length() > 0) {
            Bitmap bmp = BitmapFactory.decodeFile(img);
            bmp = ImageRotation.modifyOrientation(bmp, img);

            iv.setImageBitmap(bmp);
        }
        else
            iv.setImageDrawable((getResources().getDrawable(R.drawable.logo)));

        if (!nomUsuari.equals("") && !nomUsuari.equals(getResources().getString(R.string.anonimo)))
            navigationView.getMenu().findItem(R.id.menu_misRecetas).setVisible(true);

        else
            navigationView.getMenu().findItem(R.id.menu_misRecetas).setVisible(false);
    }

    private void anunci()
    {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                enviarMail();
            }
        });

        requestNewInterstitial();
    }

    private void enviarMail()
    {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"markusrc11@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, nomUsuari);
        startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_email)));
    }
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void loadLanguage(){
        SharedPreferences prefs = getSharedPreferences("Idioma", Context.MODE_PRIVATE);
        String lang = prefs.getString("lang", "en");

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }


    public void updateIngredients(List<String> name){
        allIngredients = name;

        int num;

        if (c.moveToFirst())
        {
            num = c.getInt(0);
            if(allIngredients.size() > num)
                new UsuariosSQLiteHelper(this).updateIngredients(allIngredients);

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this,Ajustes.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean mayRequestStoragePermission() {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;

        if((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))){
            Snackbar.make(this.drawer, R.string.permisos_necesarios_snackbar,
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            }).show();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }

        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, R.string.permisos_aceptados, Toast.LENGTH_SHORT).show();
            }
        }else{
            showExplanation();
        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permisos_denegados);
        builder.setMessage(R.string.permisos_necesarios);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }
}
