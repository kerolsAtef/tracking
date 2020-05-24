package com.kerols2020.tracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private double latitude,longitude;
    MyBackgroundService mService;
    boolean mBound=false;
    private final ServiceConnection mServiceConeection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           MyBackgroundService.LocalBinder binder=(MyBackgroundService.LocalBinder)service;
           mService=binder.getService();
           mBound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
           mService=null;
           mBound=false;
        }
    };


    RecyclerView list_of_friends;
    FirebaseAuth auth;
    DatabaseReference reference,friendRef;
    FirebaseDatabase database;
    AlertDialog.Builder alert;
    BottomNavigationView bottomNavigationView;
    EditText search_text;
    FirebaseRecyclerOptions<Friend> options;
    FirebaseRecyclerAdapter<Friend,MyViewHolder> adapter;
    ///////////////
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    boolean mLocationPermissionsGranted=false;
    /////////////////////
    String ID="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("tracking");
        alert.setMessage("app will start to track your location");
        alert.setPositiveButton("start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mService.requestLocationUpdates();
                dialog.cancel();}
        });
        alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("tracking");
                alert.setMessage("by cancellation the permission , your friends can't track your last location ");
                alert.setPositiveButton("start tracking", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mService.requestLocationUpdates();
                        dialog.cancel();
                    }
                });
                alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                     dialog.cancel();
                    }
                });
                alert.show();

            }
        });
        alert.show();


        Dexter.withActivity(this)
                .withPermissions(Arrays.asList(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )).withListener(new MultiplePermissionsListener()

                      {
                          @Override
                          public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                             // mService.requestLocationUpdates();
                              setButtonState(Common.requestingLocationUpdates(MainActivity.this));
                              bindService(new Intent(MainActivity.this,
                                      MyBackgroundService.class),
                                      mServiceConeection,
                                      Context.BIND_AUTO_CREATE
                                      );

                          }

                          @Override
                          public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                          }
                      }
        ).check();

        auth=FirebaseAuth.getInstance();
        reference=database.getInstance().getReference().child("user");
        friendRef=database.getInstance().getReference().child("user");
        list_of_friends=findViewById(R.id.friendsList);
         search_text=findViewById(R.id.search);
        list_of_friends.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        list_of_friends.setLayoutManager(linearLayoutManager);
        bottomNavigationView=findViewById(R.id.bottomNave);
        ID=auth.getCurrentUser().getUid();

        loadData("");
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString()==null)
                {
                    loadData("");
                }
                else
                {
                    loadData(s.toString());
                }
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                workingofBootomNave(menuItem);
                return true;
            }
        });

    }

    private void workingofBootomNave(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.logOutMenu:
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.ID:
                alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("your ID");
                alert.setMessage("to add some one, you should add his/her id  in add friend\n" + "your id is\n"+ID );
                alert.setPositiveButton("copy ID", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboardManager=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData=ClipData.newPlainText("String",ID);
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(getApplicationContext(),"copied",Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });
                alert.show();
                break;
            case R.id.changePasswordMenu:
                alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("change password");
                final EditText pass = new EditText(MainActivity.this);
                pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                pass.setHint("Enter new password");
                alert.setView(pass);
                alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String passWord = pass.getText().toString();
                        if (!passWord.isEmpty()&&pass.length()>=8)
                        {

                            reference.child(auth.getCurrentUser().getUid()).child("password")
                                    .setValue(passWord).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(getApplicationContext(), "password is changed successfully ", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(MainActivity.this, "Error: please, try again", Toast.LENGTH_SHORT).show();
                                }

                            });

                        }
                        else
                            Toast.makeText(MainActivity.this, "your passeord should be more than 8 characters", Toast.LENGTH_SHORT).show();



                    }
                });
                alert.show();
                break;
            case R.id.addMenu:
                startActivity(new Intent(MainActivity.this,AddFriendActivity.class));
                break;

            case R.id.requestsMenu:
                startActivity(new Intent(MainActivity.this,FriendsRequestsActivity.class));
                finish();
                break;
        }
    }

    private void loadData( final String data)
    {
        reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("friends"))
                {

                    Query query = reference.child(auth.getCurrentUser().getUid()).child("friends").orderByChild("name").startAt(data).endAt(data + "\uf8ff");
                    options = new FirebaseRecyclerOptions.Builder<Friend>().setQuery(query, Friend.class).build();
                    adapter = new FirebaseRecyclerAdapter<Friend, MyViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull final Friend friend) {
                            final String friendKey=getRef(position).getKey();
                            holder.name.setText(friend.getName());
                            holder.imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToLocation(friendKey);
                                }
                            });

                        }


                        @NonNull
                        @Override
                        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.list_row, parent, false);

                            return new MyViewHolder(view);

                        }
                    };
                    adapter.startListening();
                    list_of_friends.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goToLocation(String key)
    {
       Intent intent=new Intent(MainActivity.this,FriendLocation.class);
        startActivity(intent);

    }

    @Override
    protected void onStart() {
        super.onStart();
       // mService.requestLocationUpdates();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
            reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("friends"))
                        adapter.startListening();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    @Override
    protected void onStop() {
        if (mBound)
        {
            unbindService(mServiceConeection);
            mBound=false;
        }
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Common.KEY_REQUESTING_LOCATION_UPDATES))
        {
            setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUESTING_LOCATION_UPDATES,false));
        }
    }

    private void setButtonState(boolean aBoolean)
    {
            if (aBoolean)
            {
                //do no thing
            }
            else
            {
                // do every thing
            }
    }

 @Subscribe(sticky=true , threadMode= ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event)
    {
    if (event!=null)
    {

       latitude=(double)event.getLocation().getLongitude();
     longitude=(double)event.getLocation().getLongitude();
    }
   }

}
