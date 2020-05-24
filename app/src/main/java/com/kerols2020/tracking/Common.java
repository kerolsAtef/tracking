package com.kerols2020.tracking;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.util.prefs.Preferences;

public class Common {
    public static final String KEY_REQUESTING_LOCATION_UPDATES ="LocationUpdateEnable" ;

    public static String getLocationText(Location mLocation)
    {
        return mLocation==null? "unknown Location": new StringBuilder()
                .append(mLocation.getLatitude())
                .append("/")
                .append(mLocation.getLongitude())
                .append("/")
                .toString();
    }
    public static double getLongitude(Location location)
    {
        if (location!=null)
             return location.getLongitude();
        else
            return 0.0;
    }
    public static double getLatitude(Location location)
    {
        if (location!=null)
            return location.getLatitude();
        else
            return 0.0;
    }




    public static void setRequestingLocationUpdates(Context context, boolean b)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_REQUESTING_LOCATION_UPDATES,b)
        .apply();
    }

    public static boolean requestingLocationUpdates(Context context)
    {
    return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_REQUESTING_LOCATION_UPDATES,false);
    }
}
