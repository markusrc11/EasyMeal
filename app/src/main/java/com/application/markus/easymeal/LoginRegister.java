package com.application.markus.easymeal;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class LoginRegister extends AppCompatActivity {

    String username;
    String password;
    private EditText etUserName, etPassword, nombreReg, apodoReg, passReg, passRegConfirm;
    private TextView fechaReg;
    private Button btnLogin;
    private ImageView img;

    private TabHost tabs;

    boolean loginRealizado = false;

    private static String APP_DIRECTORY = "EasyMeal/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "Mis Fotos";

    private final String IMAGE_SERVER_URL = "http://easymeal.esy.es/images/usersImage/";
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;

    private String pathString;
    private Uri path;

    private Bitmap bmp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.application.markus.easymeal.R.layout.content_login_register);

        tabs = (TabHost) findViewById(android.R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("tab1Login");
        spec.setContent(com.application.markus.easymeal.R.id.loginContent);
        spec.setIndicator(getResources().getString(com.application.markus.easymeal.R.string.tab_inicio_sesion));

        tabs.addTab(spec);

        spec = tabs.newTabSpec("tab2Register");
        spec.setContent(com.application.markus.easymeal.R.id.RegistreContent);
        spec.setIndicator(getResources().getString(com.application.markus.easymeal.R.string.tab_registro));

        tabs.addTab(spec);

        tabs.setCurrentTab(0);

        etUserName = (EditText) findViewById(com.application.markus.easymeal.R.id.nomLogin);
        etPassword = (EditText) findViewById(com.application.markus.easymeal.R.id.passwordLogin);

        nombreReg = (EditText) findViewById(com.application.markus.easymeal.R.id.nombreRegistro);
        apodoReg = (EditText) findViewById(com.application.markus.easymeal.R.id.apodoRegistro);
        passReg = (EditText) findViewById(com.application.markus.easymeal.R.id.passwordRegistro);
        passRegConfirm = (EditText) findViewById(com.application.markus.easymeal.R.id.passwordRegistroConfirm);
        fechaReg = (TextView) findViewById(com.application.markus.easymeal.R.id.fechaNacRegistro);
        fechaReg.setKeyListener(null);
        btnLogin = (Button) findViewById(com.application.markus.easymeal.R.id.btnLogin);
        img = (ImageView) findViewById(com.application.markus.easymeal.R.id.imgUserRegister);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions();
            }
        });

        init();
    }

    private void init() {
        SharedPreferences prefs = getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);
        String nom = prefs.getString("username", getResources().getString(com.application.markus.easymeal.R.string.anonimo));

        if (!nom.equals("") && !nom.equals(getResources().getString(com.application.markus.easymeal.R.string.anonimo))) {
            tabs.getTabWidget().getChildTabViewAt(1).setEnabled(false);
            etUserName.setEnabled(false);
            etPassword.setEnabled(false);
            etUserName.setText(nom);

            btnLogin.setText(com.application.markus.easymeal.R.string.salir_usuario);
            loginRealizado = true;
        } else {
            tabs.getTabWidget().getChildTabViewAt(1).setEnabled(true);
            etUserName.setEnabled(true);
            etPassword.setEnabled(true);
            etUserName.setText("");

            btnLogin.setText(com.application.markus.easymeal.R.string.entrar_usuario);
            loginRealizado = false;
        }
    }

    public void login(View view) {

        if (!loginRealizado) {
            username = etUserName.getText().toString();
            password = etPassword.getText().toString();

            new DataBaseConnection().userLogin(username, password, getApplicationContext(), LoginRegister.this);
        } else {
            SharedPreferences prefs = getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit();

            editor.clear();

            editor.commit();

            init();
        }
    }


    public void insert(View view) {
        String name = nombreReg.getText().toString();
        String nick = apodoReg.getText().toString();
        String pass = passReg.getText().toString();
        String passConfirm = passRegConfirm.getText().toString();
        String birthdate = "";
        birthdate = fechaReg.getText().toString();

        if (name.length() > 1 && nick.length() > 3 && pass.length() > 5 && birthdate.length() > 0)
            if(pass.equals(passConfirm))
                new DataBaseConnection().userRegister(name, nick, pass, birthdate, IMAGE_SERVER_URL, pathString, getApplicationContext(), LoginRegister.this);
            else {
                View parentLayout = findViewById(com.application.markus.easymeal.R.id.layout);
                Snackbar snack = Snackbar.make(parentLayout, getString(com.application.markus.easymeal.R.string.contraseña_diferente),
                        Snackbar.LENGTH_LONG);

                snack.show();
            }
        else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            DialogoRegistro dialogo = new DialogoRegistro();
            dialogo.show(fragmentManager, "tagAlerta");
        }
    }

    public static class DialogoRegistro extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());

            builder.setMessage(com.application.markus.easymeal.R.string.faltan_campos)
                    .setTitle(com.application.markus.easymeal.R.string.informacion)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            return builder.create();
        }
    }

    public void datePicker(View v) {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR,-22);
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(LoginRegister.this, com.application.markus.easymeal.R.style.MyDialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String date = String.valueOf(year) + "-" + String.valueOf(monthOfYear)
                        + "-" + String.valueOf(dayOfMonth);
                fechaReg.setText(date);
            }
        }, yy, mm, dd);
        datePicker.getDatePicker().setMaxDate(new Date().getTime());
        datePicker.show();
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

        }
        catch (Exception e){}
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
