package com.application.markus.easymeal;

/**
 * Created by Mark on 26/01/2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class UsuariosSQLiteHelper extends SQLiteOpenHelper {

    protected static String TableIngredients = "ingredients", TableUsers = "users";


    //Sentencia SQL para crear la tabla de Usuarios
    String sqlIng = "CREATE TABLE " + TableIngredients + " (name VARCHAR(40))";
    String sqlLogin = "CREATE TABLE " + TableUsers + " (nick VARCHAR(15), image VARCHAR(80))";

    private static String name = "bdIngredients";
    private static int version = 3;
    private static CursorFactory cursorFactory = null;

    public UsuariosSQLiteHelper(Context contexto) {
        super(contexto, name, cursorFactory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creación de la tabla
        db.execSQL(sqlIng);
        db.execSQL(sqlLogin);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior,
                          int versionNueva) {
        //NOTA: Por simplicidad del ejemplo aquí utilizamos directamente
        //      la opción de eliminar la tabla anterior y crearla de nuevo
        //      vacía con el nuevo formato.
        //      Sin embargo lo normal será que haya que migrar datos de la
        //      tabla antigua a la nueva, por lo que este método debería
        //      ser más elaborado.


    }

    public void updateIngredients(List<String> ing) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TableIngredients);

        //Se crea la nueva versión de la tabla
        db.execSQL(sqlIng);

        insertIng(ing);
    }

    public void insertIng(List<String> ing) {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            for (int i = 0; i < ing.size(); i++)
                db.execSQL("INSERT INTO " + TableIngredients +
                        " (name) " +
                        " VALUES('" + ing.get(i) + "'); ");
            db.close();
        }
    }

    public Cursor getCursorBuscador() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT name" +
                " FROM " + TableIngredients + ";", null);
    }

    public Cursor getNumberIng() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT COUNT(name)" +
                " FROM " + TableIngredients + ";", null);
    }

    public void insertUser(String nick, String path) {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.execSQL("INSERT INTO " + TableUsers +
                    " (nick, image) " +
                    " VALUES ('" + nick + "', '" + path + "'); ");
            db.close();
        }
    }

    public String getUserImage(String nick) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT image" +
                " FROM " + TableUsers + "" +
                " WHERE nick = '" + nick + "';", null);

        if(c.moveToFirst())
            return c.getString(0);
        else
            return "";
    }
}