package com.kerols2020.tracking;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AddFriendActivity extends AppCompatActivity {

    EditText Id;
    TextView name;
    Button add, search;
    FirebaseAuth auth;
    DatabaseReference reference;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    boolean flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        auth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(AddFriendActivity.this);
        reference=database.getInstance().getReference().child("user");
        Id=findViewById(R.id.friendID);
        name=findViewById(R.id.friendAddedName);
        add=findViewById(R.id.addFriendButton);
        search=findViewById(R.id.searchAboutFriend);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchAboutFriend(Id.getText().toString());
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
    }

    private void sendRequest()
    {
        /*
        here will get name and id of sender a request and save it
        in child called friend_requests
        as when receiver open his notification will find it
         */
        if (!hisFriend(Id.getText().toString()))
        {

            reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final String NAME=dataSnapshot.child("name").getValue().toString();
                    reference.child(Id.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("friend_requests"))
                            {

                                int num= (int)dataSnapshot.child("friend_requests").getChildrenCount();
                                showProgressDialog();
                                storeDataAtFriendsRequest(NAME,num);

                            }
                            else {
                                showProgressDialog();
                                storeDataAtFriendsRequest(NAME,0);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
     }

    private boolean hisFriend(final String ID)
    {

     reference.addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if (dataSnapshot.hasChild("friends"))
             {
                 for (DataSnapshot snapshot : dataSnapshot.getChildren())
                 {
                     if (snapshot.child("friend_id").getValue().toString().equals(ID))
                     {
                         flag=true;
                         break;
                     }
                 }
             }


         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });
     return flag;
    }

    private void searchAboutFriend(final String ID)
    {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(ID)&&!auth.getCurrentUser().getUid().equals(ID))
                {
                 name.setVisibility(View.VISIBLE);
                 add.setVisibility(View.VISIBLE);
                 name.setText(dataSnapshot.child(ID).child("name").getValue().toString());
                }
                else if (auth.getCurrentUser().getUid().equals(ID))
                {
                    Toast.makeText(getApplicationContext(),"this is your ID",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Wrong ID",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    void showProgressDialog()
    {
        progressDialog.setTitle("sending....");
        progressDialog.setMessage("please wait......");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

    }
    void disMissProgressDialogAndMove()
    {
        progressDialog.dismiss();
    }
    void storeDataAtFriendsRequest(final String NAME, final int num)
    {
        HashMap request=new HashMap();
        request.put("id_of_friend",auth.getCurrentUser().getUid());
        request.put("name",NAME);

        reference.child(Id.getText().toString()).child("friend_requests").child(String.valueOf(num+1))
                .setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"the request sent successfully ",Toast.LENGTH_LONG).show();
                    disMissProgressDialogAndMove();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Error,please try again",Toast.LENGTH_LONG).show();
                    disMissProgressDialogAndMove();
                }
            }
        });
    }


}
