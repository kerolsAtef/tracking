package com.kerols2020.tracking;

import android.location.Location;

class SendLocationToActivity {
   private Location location;

    public SendLocationToActivity(Location location) {
       this.location=location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
