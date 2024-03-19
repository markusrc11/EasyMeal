package com.application.markus.easymeal;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by markus on 03/05/2017.
 */


public class DataBaseConnection {

    ArrayList<InfoGeneralReceta> recTodasRecetas, recMisReceta;
    InfoGeneralReceta recetaEspecifica;
    ArrayList<Ingredient> vIngredientes;
    ArrayList<Steps> vPasos;
    List<String> vAllIngredients;

    String JSONAllIng, JSONMisRecets, JSONSpecificRecipe, JSONTodasRecetas, userImg;

    private static final String TAG_RESULTS="resultat";
    private static final String TAG_INFO ="info";
    private static final String TAG_ING ="ing";
    private static final String TAG_STEP ="steps";

    private static final String TAG_NICK = "nickname";
    private static final String TAG_IMG_U ="userImage";
    private static final String TAG_NAME = "name";
    private static final String TAG_IMG_R ="recipeImage";
    private static final String TAG_DATE ="publishDate";
    private static final String TAG_DESC ="description";
    private static final String TAG_QUANT ="quantity";
    private static final String TAG_STEPS_NUM ="step_num";
    private static final String TAG_IMG ="image";

    JSONArray allIngredients = null, misRecetas = null, todasRecetas = null, ing = null, steps = null;
    JSONObject info = null;

    public DataBaseConnection() {
    }

    public void userLogin(final String username, final String password, Context c, Activity a) {

        final Context context = c;
        final Activity activity=a;

        class LoginAsync extends AsyncTask<String, Void, String> {

            private Dialog loadingDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String espere = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_espere_porfavor);
                String cargando = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_cargando);

                loadingDialog = ProgressDialog.show(activity, espere, cargando);
            }

            @Override
            protected String doInBackground(String... params) {
                String uname = params[0];
                String pass = params[1];

                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", uname));
                nameValuePairs.add(new BasicNameValuePair("password", pass));
                String result = null;

                userImg = new UsuariosSQLiteHelper(activity).getUserImage(username);

                try{
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://easymeal.esy.es/login.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result){
                String s = result.trim();
                loadingDialog.dismiss();
                if(s.equalsIgnoreCase("success")) {
                    Toast.makeText(context, com.application.markus.easymeal.R.string.usuario_contraseña_correcto, Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = context.getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putString("username", username);
                    editor.putString("userimage",userImg);

                    editor.commit();

                    activity.finish();
                }
                else
                    Toast.makeText(context, com.application.markus.easymeal.R.string.usuario_contraseña_incorrecto, Toast.LENGTH_SHORT).show();
            }
        }

        LoginAsync la = new LoginAsync();
        la.execute(username, password);
    }


    public void userRegister(String name, final String nick, String pass, String birthdate, String serverURL, final String imagePath, Context c, Activity a){

        final Context context = c;
        final Activity activity=a;

        class userRegister extends AsyncTask<String, Void, String> {

            private Dialog loadingDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String espere = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_espere_porfavor);
                String cargando = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_cargando);

                loadingDialog = ProgressDialog.show(activity, espere, cargando);
                uploadImageToServer(imagePath, "usersImage/",activity);
            }

            protected String doInBackground(String... params) {
                String paramUsername = params[0];
                String paramNick = params[1];
                String paramPass = params[2];
                String paramBirthdate = params[3];
                String image = params[4];


                if(paramUsername.substring(paramUsername.length() - 1).equals(" "))
                    paramUsername = paramUsername.substring(0,paramUsername.length()-1);

                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name", paramUsername));
                nameValuePairs.add(new BasicNameValuePair("nickname", paramNick));
                nameValuePairs.add(new BasicNameValuePair("password", paramPass));
                nameValuePairs.add(new BasicNameValuePair("birthdate", paramBirthdate));
                nameValuePairs.add(new BasicNameValuePair("image", image));
                String result = null;

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://easymeal.esy.es/register.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();


                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }
                return result;
            }

            protected void onPostExecute(String result) {
                String s = result.trim();
                loadingDialog.dismiss();
                if(s.equalsIgnoreCase("user exists"))
                    Toast.makeText(context, com.application.markus.easymeal.R.string.usuario_existe, Toast.LENGTH_SHORT).show();

                else if(s.equalsIgnoreCase("insert success")) {
                    Toast.makeText(context, com.application.markus.easymeal.R.string.registro_correcto, Toast.LENGTH_LONG).show();

                    UsuariosSQLiteHelper udb = new UsuariosSQLiteHelper(activity);
                    udb.insertUser(nick,imagePath);

                    SharedPreferences prefs = context.getSharedPreferences("DatosUsuario", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putString("username", nick);
                    editor.putString("userimage",imagePath);

                    editor.commit();

                    activity.finish();
                }

                else
                    Toast.makeText(context, com.application.markus.easymeal.R.string.cant_register, Toast.LENGTH_LONG).show();
            }
        }
        userRegister userRegister = new userRegister();
        userRegister.execute(name,nick,pass,birthdate,serverURL + new File(imagePath).getName());
    }


    public void getDataAllRecipes(Activity a, final ListView lstOpciones) {
        final Activity activity = a;

        class GetDataJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String espere = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_espere_porfavor);

                loading = ProgressDialog.show(activity, espere, null, true, true);
            }

            @Override
            protected String doInBackground(String... params) {
                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
                HttpPost httppost = new HttpPost("http://easymeal.esy.es/allRecipes.php");

                // Depends on your web service
                httppost.setHeader("Content-type", "application/json");

                InputStream inputStream = null;
                String result = null;
                try {
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (Exception e) {
                    Log.d("ERROR --> ", e.getMessage());
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (Exception squish) {
                        Log.d("ERROR --> ", squish.getMessage());
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                JSONTodasRecetas = result;
                loading.dismiss();
                setArrayValuesTodasRecetas(activity, lstOpciones);
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

    public void setArrayValuesTodasRecetas(Activity activity, ListView lstOpciones) {
        try {
            JSONObject jsonObj = new JSONObject(JSONTodasRecetas);
            todasRecetas = jsonObj.getJSONArray(TAG_RESULTS);

            recTodasRecetas = new ArrayList<>();

            for (int i = 0; i < todasRecetas.length(); i++) {
                JSONObject c = todasRecetas.getJSONObject(i);
                String nick = c.getString(TAG_NICK);
                String imgU = c.getString(TAG_IMG_U);
                String name = c.getString(TAG_NAME);
                String imgR = c.getString(TAG_IMG_R);
                String fecha = c.getString(TAG_DATE);

                recTodasRecetas.add(new InfoGeneralReceta(nick, imgU, name, imgR, fecha, "",null,null));
            }

            AdaptadorTodasRecetas adaptador = new AdaptadorTodasRecetas(activity, recTodasRecetas);

            lstOpciones.setAdapter(adaptador);

        } catch (JSONException e) {
            Log.d("ERROR --> ", e.getMessage());
        } catch (Exception e) {
            Log.d("ERROR-->", e.getMessage());
        }
    }

    class AdaptadorTodasRecetas extends ArrayAdapter<InfoGeneralReceta> {

        public AdaptadorTodasRecetas(Context context, ArrayList<InfoGeneralReceta> datos) {
            super(context, com.application.markus.easymeal.R.layout.general_recipe_view, datos);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View item = inflater.inflate(com.application.markus.easymeal.R.layout.general_recipe_view, null);

            TextView lblTitulo = (TextView)item.findViewById(com.application.markus.easymeal.R.id.nomRecepta);
            lblTitulo.setText(recTodasRecetas.get(position).getNombre());

            TextView txtNom = (TextView)item.findViewById(com.application.markus.easymeal.R.id.nombreUsuario);
            txtNom.setText(recTodasRecetas.get(position).getNick());

            TextView txtFecha = (TextView)item.findViewById(com.application.markus.easymeal.R.id.fechaPublicacion);
            String oldDate = recTodasRecetas.get(position).getFecha();
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            Date date = null;
            try {
                date = dt.parse(oldDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
            txtFecha.setText(dt1.format(date));

            ImageView ivR = (ImageView)item.findViewById(com.application.markus.easymeal.R.id.recipeImage);
            new MostrarImagenRecetaAsync(ivR).execute(recTodasRecetas.get(position).getImgReceta());

            ImageView ivU = (ImageView)item.findViewById(com.application.markus.easymeal.R.id.imgUserTodasRecetas);
            new MostrarImagenRecetaAsync(ivU).execute(recTodasRecetas.get(position).getImgUser());

            return(item);
        }
    }



    public void getDataMyRecipes(Activity a, final ListView lstOpciones, String username){
        final Activity activity = a;

        class GetDataJSON extends AsyncTask<String, Void, String>{
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String espere = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_espere_porfavor);

                loading = ProgressDialog.show(activity, espere,null,true,true);
            }
            @Override
            protected String doInBackground(String... params) {
                String paramUsername = params[0];

                if(paramUsername.substring(paramUsername.length()).equals(" "))
                    paramUsername.substring(0,paramUsername.length()-1);

                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
                HttpPost httppost = new HttpPost("http://easymeal.esy.es/recipesByUser.php");

                // Depends on your web service
                //httppost.setHeader("Content-type", "application/json");

                InputStream inputStream = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                if(paramUsername.substring(paramUsername.length() - 1).equals(" "))
                    paramUsername = paramUsername.substring(0,paramUsername.length()-1);

                Log.d("USERRR","'"+paramUsername+"'");

                nameValuePairs.add(new BasicNameValuePair("username", paramUsername));
                String result = null;
                try {

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (Exception e) {
                    Log.d("ERROR --> ",e.getMessage());
                }
                finally {
                    try{if(inputStream != null)inputStream.close();}catch(Exception squish)
                    {
                        Log.d("ERROR --> ",squish.getMessage());
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result){
                JSONMisRecets=result;
                loading.dismiss();
                setArrayValuesMisRecetas(activity, lstOpciones);
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(username);
    }

    public void setArrayValuesMisRecetas(Activity activity, ListView lstOpciones) {
        try {
            JSONObject jsonObj = new JSONObject(JSONMisRecets);
            misRecetas = jsonObj.getJSONArray(TAG_RESULTS);

            recMisReceta = new ArrayList<>();

            for (int i = 0; i < misRecetas.length(); i++) {
                JSONObject c = misRecetas.getJSONObject(i);
                String nick = c.getString(TAG_NICK);
                String imgU = c.getString(TAG_IMG_U);
                String name = c.getString(TAG_NAME);
                String imgR = c.getString(TAG_IMG_R);
                String fecha = c.getString(TAG_DATE);

                recMisReceta.add(new InfoGeneralReceta(nick, imgU, name, imgR, fecha, "",null,null));
            }

            AdaptadorMisRecetas adaptador = new AdaptadorMisRecetas(activity, recMisReceta);

            lstOpciones.setAdapter(adaptador);

        } catch (JSONException e) {
            Log.d("ERROR --> ", e.getMessage());
        } catch (Exception e) {
            Log.d("ERROR-->", e.getMessage());
        }

    }

    class AdaptadorMisRecetas extends ArrayAdapter<InfoGeneralReceta> {

        public AdaptadorMisRecetas(Context context, ArrayList<InfoGeneralReceta> datos) {
            super(context, com.application.markus.easymeal.R.layout.user_recipe_view, datos);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View item = inflater.inflate(com.application.markus.easymeal.R.layout.user_recipe_view, null);

            TextView lblTitulo = (TextView)item.findViewById(com.application.markus.easymeal.R.id.nomRecepta);
            lblTitulo.setText(recMisReceta.get(position).getNombre());

            TextView txtFecha = (TextView)item.findViewById(com.application.markus.easymeal.R.id.fechaPublicacion);

            String oldDate = recMisReceta.get(position).getFecha();
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = null;
            try {
                date = dt.parse(oldDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
            txtFecha.setText(dt1.format(date));

            ImageView ivR = (ImageView)item.findViewById(com.application.markus.easymeal.R.id.recipeImage);
            new MostrarImagenRecetaAsync(ivR).execute(recMisReceta.get(position).getImgReceta());

            return(item);
        }
    }


    private class MostrarImagenRecetaAsync extends AsyncTask<String, Void, Bitmap> {

        ImageView iv;
        public MostrarImagenRecetaAsync(ImageView iv)
        {
            this.iv = iv;
        }

        @Override
        protected Bitmap doInBackground(String... values) {

            String url = values[0];
            Bitmap bm = null;

            try
            {
                InputStream is = new java.net.URL(url).openStream();
                bm = BitmapFactory.decodeStream(is);

            }
            catch (Exception e)
            {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            iv.setImageBitmap(result);
        }

        @Override
        protected void onCancelled() {
        }
    }



    public void getDataSpecificRecipe(String user, String rec, Activity a) {
        final Activity activity = a;

        class GetDataJSON extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String espere = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_espere_porfavor);

                loading = ProgressDialog.show(activity, espere, null, true, true);
            }

            @Override
            protected String doInBackground(String... params) {
                String uname = params[0];
                String rec = params[1];

                InputStream inputStream = null;
                String result = null;
                List<NameValuePair> parametros = new ArrayList<NameValuePair>();
                parametros.add(new BasicNameValuePair("username", uname));
                parametros.add(new BasicNameValuePair("recipe_name",rec));


                try {
                    DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
                    HttpPost httppost = new HttpPost("http://easymeal.esy.es/specificRecipe.php");

                    httppost.setEntity(new UrlEncodedFormEntity(parametros));


                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String ing = null;
                    while ((ing = reader.readLine()) != null) {
                        sb.append(ing + "\n");
                    }
                    result = sb.toString();
                } catch (Exception e) {
                    Log.d("ERROR --> ", e.getMessage());
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                    } catch (Exception squish) {
                        Log.d("ERROR --> ", squish.getMessage());
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                JSONSpecificRecipe = result;
                loading.dismiss();
                setArrayValuesSpecificReceta(activity);
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(user, rec);
    }

    public void setArrayValuesSpecificReceta(Activity activity){
        try {
            JSONObject jsonObj = new JSONObject(JSONSpecificRecipe);
            info = jsonObj.getJSONObject(TAG_INFO);
            ing = jsonObj.getJSONArray(TAG_ING);
            steps = jsonObj.getJSONArray(TAG_STEP);

            String nick = info.getString(TAG_NICK);
            String imgU = info.getString(TAG_IMG_U);
            String name = info.getString(TAG_NAME);
            String imgR = info.getString(TAG_IMG_R);
            String fecha = info.getString(TAG_DATE);
            String desc = info.getString(TAG_DESC);

            vIngredientes = new ArrayList<>();
            for(int i = 0; i < ing.length(); i++){
                JSONObject c = ing.getJSONObject(i);
                String ingr = c.getString(TAG_NAME);
                String quant = c.getString(TAG_QUANT);

                vIngredientes.add(new Ingredient(ingr,quant));
            }

            vPasos = new ArrayList<>();
            for(int i = 0; i < steps.length(); i++){
                JSONObject c = steps.getJSONObject(i);
                String stepNum = c.getString(TAG_STEPS_NUM);
                String description = c.getString(TAG_DESC);
                String img = c.getString(TAG_IMG);

                vPasos.add(new Steps(stepNum,description,img));
            }

            recetaEspecifica = new InfoGeneralReceta(nick,imgU,name,imgR,fecha,desc,vPasos,vIngredientes);
            setRecetaEspecifica(activity);

        } catch (JSONException e) {
            Log.d("ERROR --> ",e.getMessage());
        }
        catch (Exception e)
        {
            Log.d("ERROR-->",e.getMessage());
        }
    }

    private void setRecetaEspecifica(Activity activity)
    {
        try {
            TextView name = (TextView) activity.findViewById(com.application.markus.easymeal.R.id.nombreRecetaEspecifica);
            name.setText(recetaEspecifica.getNombre());

            TextView nick = (TextView) activity.findViewById(com.application.markus.easymeal.R.id.usuarioRecetaEspecifica);
            nick.setText(recetaEspecifica.getNick());

            TextView desc = (TextView) activity.findViewById(com.application.markus.easymeal.R.id.descripcioRecetaEspecifica);
            desc.setText(recetaEspecifica.getDescripcion());


            ImageView ivR = (ImageView)activity.findViewById(com.application.markus.easymeal.R.id.imgSpecificReceta);
            new MostrarImagenRecetaAsync(ivR).execute(recetaEspecifica.getImgReceta());

            ImageView ivU = (ImageView)activity.findViewById(com.application.markus.easymeal.R.id.imgUser);
            new MostrarImagenRecetaAsync(ivU).execute(recetaEspecifica.getImgUser());

            TextView ingRec = (TextView)activity.findViewById(com.application.markus.easymeal.R.id.txtIngredientsRecetaEspecifica);

            int size = recetaEspecifica.getIngredientes().size();
            for (int i = 0; i < size; i++)
            {
                String ing = recetaEspecifica.getIngredientes().get(i).getName();
                String quant = recetaEspecifica.getIngredientes().get(i).getQuantity();
                ingRec.append(quant + " " + ing + "\n");
            }

            TextView pasos = (TextView) activity.findViewById(com.application.markus.easymeal.R.id.pasosRecetaEspecifica);
            size = recetaEspecifica.getPasos().size();
            for (int i = 0; i < size; i++)
            {
                String step_num = recetaEspecifica.getPasos().get(i).getStep_num();
                String desc1 = recetaEspecifica.getPasos().get(i).getDesc();
                pasos.append(step_num + "- " + desc1 + "\n");
            }

        }
        catch (Exception e)
        {
            Log.d("ERROR--> ",e.getMessage());
        }
    }

    public void uploadImageToServer(final String path, final String dir, Activity a) {
        final Activity activity = a;

        class UploadFileToServer extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {
                return uploadFile();
            }

            @SuppressWarnings("deprecation")
            private String uploadFile() {
                String responseString = null;

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://easymeal.esy.es/fileUpload.php");

                try {
                    AndroidMultiPartEntity entity = new AndroidMultiPartEntity();

                    File sourceFile = new File(path);

                    // Adding file data to http body
                    entity.addPart("image", new FileBody(sourceFile));
                    entity.addPart("directory",
                            new StringBody(dir));

                    httppost.setEntity(entity);

                    // Making server call
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity r_entity = response.getEntity();

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        // Server response
                        responseString = EntityUtils.toString(r_entity);
                    } else {
                        responseString = "Error occurred! Http Status Code: "
                                + statusCode;
                    }

                } catch (ClientProtocolException e) {
                    responseString = e.toString();
                } catch (IOException e) {
                    responseString = e.toString();
                } catch (Exception e) {
                    responseString = e.toString();
                }

                return responseString;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(activity.getLocalClassName(), "Response from server: " + result);

                super.onPostExecute(result);
            }
        }
        UploadFileToServer g = new UploadFileToServer();
        g.execute();
    }

    public void recipeInsert(final InfoGeneralReceta igr, final String serverURL, List<String> los, Context c, Activity a){

        final Context context = c;
        final Activity activity=a;

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            private Dialog loadingDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String espere = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_espere_porfavor);
                String cargando = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_cargando);

                loadingDialog = ProgressDialog.show(activity, espere, cargando);
                if (igr.getImgReceta().length() > 0)
                    uploadImageToServer(igr.getImgReceta(), "recipesImages/", activity);

            }

            protected String doInBackground(String... params) {
                InputStream is = null;

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("nickname", igr.getNick()));
                nameValuePairs.add(new BasicNameValuePair("nombre_receta", igr.getNombre()));
                nameValuePairs.add(new BasicNameValuePair("img_receta", serverURL + new File(igr
                        .imgReceta).getName()));
                nameValuePairs.add(new BasicNameValuePair("fecha", igr.getFecha()));
                nameValuePairs.add(new BasicNameValuePair("desc", igr.getDescripcion()));

                JSONArray pasos = new JSONArray();
                JSONArray ingredientes = new JSONArray();
                JSONArray quant = new JSONArray();

                String result = null;


                for (int i = 0; i < igr.getPasos().size(); i++)
                    pasos.put(igr.getPasos().get(i).getDesc());
                nameValuePairs.add(new BasicNameValuePair("list_pasos", pasos.toString()));
                Log.d("PRAAw", pasos.toString());

                for (int i = 0; i < igr.getIngredientes().size(); i++)
                    ingredientes.put(igr.getIngredientes().get(i).getName());

                Log.d("SIZEE", String.valueOf(ingredientes.length()));
                nameValuePairs.add(new BasicNameValuePair("list_ing", ingredientes.toString()));
                Log.d("PRAAw", ingredientes.toString());

                for (int i = 0; i < igr.getIngredientes().size(); i++)
                    quant.put(igr.getIngredientes().get(i).getQuantity());
                nameValuePairs.add(new BasicNameValuePair("list_quant", quant.toString()));


                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://easymeal.esy.es/newRecipe.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();


                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }
                return result;
            }

            protected void onPostExecute(String result) {
                String s = result.trim();
                loadingDialog.dismiss();
                //Toast.makeText(context,"\'"+ s+"\'", Toast.LENGTH_SHORT).show();
                if(s.equalsIgnoreCase("Recipe insert successPasos insert successIng insert success")) {
                    Toast.makeText(context, com.application.markus.easymeal.R.string.recipe_correct, Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
                else
                    Toast.makeText(context, com.application.markus.easymeal.R.string.recipe_incorrect, Toast.LENGTH_SHORT).show();
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute();
    }

    public void getAllIngredients(Activity a){
        final Activity activity = a;

        vAllIngredients = new ArrayList<>();

        class GetDataJSON extends AsyncTask<String, Void, String>{
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String espere = activity.getResources().getString(com.application.markus.easymeal.R.string.dialog_espere_porfavor);

                loading = ProgressDialog.show(activity, espere,null,true,true);
            }
            @Override
            protected String doInBackground(String... params) {
                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
                HttpPost httppost = new HttpPost("http://easymeal.esy.es/getIngredients.php");

                // Depends on your web service
                httppost.setHeader("Content-type", "application/json");

                InputStream inputStream = null;
                String result = null;
                try {
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (Exception e) {
                    Log.d("ERROR --> ",e.getMessage());
                }
                finally {
                    try{if(inputStream != null)inputStream.close();}catch(Exception squish)
                    {
                        Log.d("ERROR --> ",squish.getMessage());
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result){
                JSONAllIng=result;
                loading.dismiss();

                setArrayValuesAllIngredients();
                ((MainActivity)activity).updateIngredients(vAllIngredients);
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

    public void setArrayValuesAllIngredients() {
        try {
            JSONObject jsonObj = new JSONObject(JSONAllIng);
            allIngredients = jsonObj.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < allIngredients.length(); i++) {
                JSONObject c = allIngredients.getJSONObject(i);
                String name = c.getString(TAG_NAME);

                vAllIngredients.add(name);
            }

        } catch (JSONException e) {
            Log.d("ERROR --> ", e.getMessage());
        } catch (Exception e) {
            Log.d("ERROR-->", e.getMessage());
        }
    }
}