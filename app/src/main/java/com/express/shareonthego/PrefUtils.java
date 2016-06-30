package com.express.shareonthego;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {

    public static final String PREF_INTRO = "pref_intro";

    public static boolean isIntro(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_INTRO, false);
    }

    public static void markIntro(final Context context, boolean b) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_INTRO, b).commit();
    }
}