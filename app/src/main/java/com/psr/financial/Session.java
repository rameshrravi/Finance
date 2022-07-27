package com.psr.financial;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {
    private static Session session;
    private SharedPreferences sharedPreferences;

    private static String isLoadAtFirstTime = "isLoadAtFirstTime";
    private static String dateOfInstalled = "dateOfInstalled";
    private static String currentVersion = "currentVersion";
    private static String isExtended = "isExtended";
    private static String isUpdatedNewDB = "isUpdatedNewDB";
    private static String isPhoneVerified = "isPhoneVerified";

    public static Session getInstance(Context context) {
        if (session == null) {
            session = new Session(context);
        }
        return session;
    }

    private Session(Context context) {
        sharedPreferences = context.getSharedPreferences("SessionPreference",Context.MODE_PRIVATE);
    }

    public void setLoadAtFirstTime(boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(Session.isLoadAtFirstTime, value);
        prefsEditor.commit();
    }

    public boolean isLoadAtFirstTime() {
        if (sharedPreferences!= null) {
            return sharedPreferences.getBoolean(Session.isLoadAtFirstTime, true);
        }
        return true;
    }

    public void setDateOfInstalled(String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString(Session.dateOfInstalled, value);
        prefsEditor.commit();
    }

    public String getDateOfInstalled() {
        if (sharedPreferences!= null) {
            return sharedPreferences.getString(Session.dateOfInstalled, "");
        }
        return "";
    }

    public void setCurrentVersion(int value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt(Session.currentVersion, value);
        prefsEditor.commit();
    }

    public int getCurrentVersion() {
        if (sharedPreferences!= null) {
            return sharedPreferences.getInt(Session.currentVersion, 0);
        }
        return 0;
    }

    public void setIsExtended(boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(Session.isExtended, value);
        prefsEditor.commit();
    }

    public boolean isExtended() {
        if (sharedPreferences!= null) {
            return sharedPreferences.getBoolean(Session.isExtended, false);
        }
        return true;
    }

    public void setIsUpdatedNewDB(boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(Session.isUpdatedNewDB, value);
        prefsEditor.commit();
    }

    public boolean isUpdatedNewDB() {
        if (sharedPreferences!= null) {
            return sharedPreferences.getBoolean(Session.isUpdatedNewDB, false);
        }
        return true;
    }

    public void setIsPhoneVerified(boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(Session.isPhoneVerified, value);
        prefsEditor.commit();
    }

    public boolean isPhoneVerified() {
        if (sharedPreferences!= null) {
            return sharedPreferences.getBoolean(Session.isPhoneVerified, false);
        }
        return true;
    }
}
