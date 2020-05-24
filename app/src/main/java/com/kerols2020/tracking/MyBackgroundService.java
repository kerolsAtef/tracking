package com.kerols2020.tracking;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

public class MyBackgroundService extends Service {

   private static final String CHANNEL_ID="my_channel";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION ="com.kerols2020.tracking"+".started_from_notification";
    private final IBinder mBinder=new LocalBinder();

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
   private static final long update_interval=4000;
    private static final long fast_update_interval=2000;
    private static final int NOTI_ID=1223;
    private boolean MChangingConfiguration=false;
    private NotificationManager mNotificationManager;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler mServiceHandler;
    private Location mLocation;


   public MyBackgroundService()
   {
     requestLocationUpdates();
   }

    @Override
    public void onCreate() {
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            requestLocationUpdates();
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread=new HandlerThread("kero");
        handlerThread.start();
        mServiceHandler=new Handler(handlerThread.getLooper());
        mNotificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel mChannel=new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                    );
            mNotificationManager.createNotificationChannel(mChannel);
        }




   }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MChangingConfiguration=true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       boolean startedFromNotification=intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,false);
       if (startedFromNotification)
       {
           removeLocationUpdates();
           stopSelf();
       }

       return START_NOT_STICKY;
        }

    public void removeLocationUpdates()
    {

        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Common.setRequestingLocationUpdates(this,false);
            stopSelf();
        }
        catch (SecurityException e)
        {
            Common.setRequestingLocationUpdates(this,true);
            Toast.makeText(getApplicationContext(),"Could not remove updates "+e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    private void getLastLocation()
    {

        try {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                 if (task.isSuccessful()&&task.getResult()!=null)
                 {
                     mLocation=task.getResult();
                 }
                 else
                     Toast.makeText(getApplicationContext(),"failed to get location",Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (SecurityException e)
        {
            Toast.makeText(getApplicationContext(),"lost location permission"+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void createLocationRequest()
    {
     locationRequest=new LocationRequest();
     locationRequest.setInterval(update_interval);
     locationRequest.setFastestInterval(fast_update_interval);
     locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void onNewLocation(Location lastLocation)
    {
    mLocation=lastLocation;
        EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));

    // update notification if run in foreground
       if (ServiceIsRunningAsForegroundService(this))
       {
           mNotificationManager.notify(NOTI_ID,getNotification());
       }

    }

    private Notification getNotification()
    {
        auth=FirebaseAuth.getInstance();
        reference=database.getInstance().getReference().child("user").child(auth.getCurrentUser().getUid());
        Intent intent=new Intent(this,MyBackgroundService.class);
        double lon=Common.getLongitude(mLocation);
        double lat=Common.getLatitude(mLocation);
        reference.child("longitude").setValue(lon);
        reference.child("latitude").setValue(lat);
        //String text= Common.getLocationText(mLocation);
        String text="tracking App";
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION,true);

        PendingIntent servicePendingIntent=PendingIntent.getService(this,0,intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                );
        PendingIntent activityPendingIntent=PendingIntent.getActivity(this,0,
                new Intent(this,MainActivity.class),0
                );

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_launch_black_24dp,"Launch",activityPendingIntent)
                .addAction(R.drawable.ic_cancel_black_24dp,"remove",servicePendingIntent)
                .setContentText(text)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
           builder.setChannelId(CHANNEL_ID);
        }
    return builder.build();
    }

    private boolean ServiceIsRunningAsForegroundService(Context context)
    {
        ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE))
            if (getClass().getName().equals(service.service.getClassName()))
                if (service.foreground)
                    return true;

        return false;
    }

    public void requestLocationUpdates()
    {
        Common.setRequestingLocationUpdates(this,true);
        startService(new Intent(getApplicationContext(),MyBackgroundService.class));
        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }
        catch (SecurityException e)
        {
            Toast.makeText(getApplicationContext(),"could not request it",Toast.LENGTH_LONG).show();
        }
    }

    public class LocalBinder extends Binder {

        MyBackgroundService getService()
        {
            return MyBackgroundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

       stopForeground(true);
       MChangingConfiguration=false;
       return mBinder;
    }


    @Override
    public void onRebind(Intent intent) {
       stopForeground(true);
       MChangingConfiguration=false;
       super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
       if (!MChangingConfiguration&&Common.requestingLocationUpdates(this))
            startForeground(NOTI_ID,getNotification());
       return true;
    }

    @Override
    public void onDestroy() {

       mServiceHandler.removeCallbacks(null);
       super.onDestroy();
    }
}
