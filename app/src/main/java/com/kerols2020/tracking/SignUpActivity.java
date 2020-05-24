package com.kerols2020.tracking;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    EditText email,password,confirm_password,name;
    TextView have_an_account;
    FirebaseAuth auth;
    Button sign_up;
    ProgressDialog progressDialog;
    String EMAIL,PASS,CON_PASS,NAME;
    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        progressDialog=new ProgressDialog(SignUpActivity.this);
        auth=FirebaseAuth.getInstance();
        reference=database.getInstance().getReference();
        email=findViewById(R.id.emailSignUp);
        name=findViewById(R.id.nameSignUp);
        password=findViewById(R.id.passwordSignUp);
        confirm_password=findViewById(R.id.confirmPasswordSignUp);
        have_an_account=findViewById(R.id.haveAccount);
        sign_up=findViewById(R.id.SignUp);
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTheProcedureOfSignUp();
            }
        });
        have_an_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoginActivity();
            }
        });
    }

    private void checkTheProcedureOfSignUp()
    {
        PASS=password.getText().toString();
        CON_PASS=confirm_password.getText().toString();
        EMAIL=email.getText().toString();
        NAME=name.getText().toString();
        if (EMAIL.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"please, Enter your E-mail",Toast.LENGTH_LONG).show();
        }
       else if (NAME.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"please, Enter your Name",Toast.LENGTH_LONG).show();
        }
        else if (PASS.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"please, Enter your pass-word",Toast.LENGTH_LONG).show();
        }
        else if (CON_PASS.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"please, Enter confirmation pass-word",Toast.LENGTH_LONG).show();
        }
        else if (!PASS.equals(CON_PASS))
        {
            Toast.makeText(getApplicationContext(),"Error, pass-words are not the same",Toast.LENGTH_LONG).show();
        }
        else if (!EMAIL.contains(".com"))
        {
            Toast.makeText(getApplicationContext(),"Error, Invalid E-mail",Toast.LENGTH_LONG).show();
        }
        else if (!EMAIL.contains("@"))
        {
            Toast.makeText(getApplicationContext(),"Error, Invalid E-mail",Toast.LENGTH_LONG).show();
        }
        else if (EMAIL.charAt(0)=='.')
        {
            Toast.makeText(getApplicationContext(),"Error, Invalid E-mail",Toast.LENGTH_LONG).show();
        }
        else
        {
            signUpToApp();
        }

    }

    void showProgressDialog()
    {
        progressDialog.setTitle("Sigup....");
        progressDialog.setMessage("please wait......");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

    }
    void disMissProgressDialogAndMove()
    {
        progressDialog.dismiss();
    }
    private void signUpToApp()
    {
        PASS=password.getText().toString();
        CON_PASS=confirm_password.getText().toString();
        EMAIL=email.getText().toString();
        NAME=name.getText().toString();

        showProgressDialog();
        auth.createUserWithEmailAndPassword(EMAIL,PASS).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful())
                {
                    HashMap user=new HashMap<>();
                    user .put("password",PASS);
                    user.put("email",EMAIL);
                    user.put("name",NAME);
                    user.put("longitude",0.0);
                    user.put("latitude",0.0);
                    reference.child("user").child(auth.getCurrentUser().getUid()).setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        disMissProgressDialogAndMove();
                                        goToMainActivity();
                                    }
                                    else
                                    {
                                        disMissProgressDialogAndMove();
                                        Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                    }


                                }
                            });

                }
                else
                {
                    disMissProgressDialogAndMove();
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }




    void goToMainActivity()
    {
        Intent a= new Intent(SignUpActivity.this , MainActivity.class);
        startActivity(a);
        finish();
    }

    void goToLoginActivity()
    {
        Intent a= new Intent(SignUpActivity.this , LoginActivity.class);
        startActivity(a);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(auth.getCurrentUser()!=null)
        {
            Intent intent =new Intent(SignUpActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
