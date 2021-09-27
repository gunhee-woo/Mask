package com.dlog.mask;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesActivity {
    private SharedPreferences prefs;
    private SharedPreferences isNew;
    private SharedPreferences default_km;
    public SharedPreferencesActivity(Context context){
        prefs = context.getSharedPreferences("birth_end",Context.MODE_PRIVATE);
        isNew = context.getSharedPreferences("is_New",Context.MODE_PRIVATE);
        default_km = context.getSharedPreferences("default_km",Context.MODE_PRIVATE);
    }
    public String getPreferences(){
        return prefs.getString("birth_end","");
    }
    public String getIsNewPreferences(){return isNew.getString("is_New","");}
    public String getDefaultKMPreferences(){return default_km.getString("default_km","");}
    public void savePreferences(String value){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("birth_end",value);
        editor.commit();
    }
    public void saveIsNewPreferences(String value){
        SharedPreferences.Editor editor = isNew.edit();
        editor.putString("is_New",value);
        editor.commit();
    }
    public void saveDefaultKMPreferences(String value){
        SharedPreferences.Editor editor = default_km.edit();
        editor.putString("default_km",value);
        editor.commit();
    }
}

