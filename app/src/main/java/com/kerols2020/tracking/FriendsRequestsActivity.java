package com.kerols2020.tracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FriendsRequestsActivity extends AppCompatActivity {
    RecyclerView list_of_requests;
    FirebaseAuth auth;
    DatabaseReference reference,friendRef;
    FirebaseDatabase database;
    FirebaseRecyclerOptions<Request> options;
    FirebaseRecyclerAdapter<Request,MyViewHolderRequest> adapter;
    String friendName,friendID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_requests);
        auth=FirebaseAuth.getInstance();
        reference=database.getInstance().getReference().child("user").child(auth.getCurrentUser().getUid());
        friendRef=database.getInstance().getReference().child("user");
        list_of_requests=findViewById(R.id.list_of_requests);
        list_of_requests.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        list_of_requests.setLayoutManager(linearLayoutManager);
        loadData("");
    }

    private void loadData( final String data)
    {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("friend_requests"))
                {

                    Query query = reference.child("friend_requests").orderByChild("name").startAt(data).endAt(data + "\uf8ff");
                    options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(query, Request.class).build();
                    adapter = new FirebaseRecyclerAdapter<Request, MyViewHolderRequest>(options) {

                        @Override
                        protected void onBindViewHolder(@NonNull MyViewHolderRequest holder, int position, @NonNull final Request request) {
                            final String friendKey=getRef(position).getKey();
                            holder.name.setText(request.getName());

                            holder.accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    storeAsIAmHisFriend(friendKey);
                                    storeAsMyFriend(friendKey);
                                }
                            });
                            holder.cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    removeFromFriendsRequests(friendKey);
                                }
                            });
                        }


                        @NonNull
                        @Override
                        public MyViewHolderRequest onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.request_list_row, parent, false);

                            return new MyViewHolderRequest(view);

                        }
                    };
                    adapter.startListening();
                    list_of_requests.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeFromFriendsRequests(final String key)
    {
        reference.child("friend_requests").child(key).removeValue();
    }

    void storeAsMyFriend(final String key)
   {

       reference.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot.hasChild("friends"))
               {
                 final int number=(int)dataSnapshot.child("friends").getChildrenCount();
                   storedataToMe(key,number+1);

               }
               else
               {
                   storedataToMe(key,1);

               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

   }
    void storeAsIAmHisFriend(final String key)
    {

        reference.child("friend_requests").child(key).child("id_of_friend").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String friendID=dataSnapshot.getValue().toString();
                friendRef.child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("friends"))
                        {
                            final int number=(int)dataSnapshot.child("friends").getChildrenCount();
                            storedataToMyFriend(friendID,number+1);

                        }
                        else
                        {
                            storedataToMyFriend(friendID,1);

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
   void storedataToMe(final String key, final int number)
   {
       reference.child("friend_requests").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              final String friendName=dataSnapshot.child("name").getValue().toString();
              final String  friendID=dataSnapshot.child("id_of_friend").getValue().toString();
               HashMap friend =new HashMap();
               friend.put("name",friendName);
               friend.put("friend_id",friendID);
               reference.child("friends").child("friend"+number).setValue(friend).addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful())
                       {
                           Toast.makeText(getApplicationContext(),friendName+" can track your location now",Toast.LENGTH_LONG).show();
                           removeFromFriendsRequests(key);

                       }
                   }
               });

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
   }
    void storedataToMyFriend(final String friendID,final int number)
    {
        friendRef.child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 reference.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name=dataSnapshot.getValue().toString();
                        HashMap friend =new HashMap();
                        friend.put("name",name);
                        friend.put("friend_id",auth.getCurrentUser().getUid());
                        friendRef.child(friendID).child("friends").child("friend"+number).setValue(friend).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {

                                }
                            }
                        });

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(FriendsRequestsActivity.this,MainActivity.class));
        finish();
    }
}