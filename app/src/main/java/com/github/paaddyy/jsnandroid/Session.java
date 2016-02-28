package com.github.paaddyy.jsnandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by paddy on 28.02.2016.
 */
public class Session {

    private SharedPreferences prefs;

    public Session(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setSpace(String space) {
        prefs.edit().putString("Space", space).commit();
    }

    public String getSpace() {
        String space = prefs.getString("space","");
        return space;
    }
}
