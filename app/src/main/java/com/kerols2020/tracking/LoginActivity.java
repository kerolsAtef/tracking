package com.kerols2020.tracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity {

    EditText email,password;
    TextView forgetPassword,login;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    AlertDialog.Builder alert;
    String EMAIL,PASS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog=new ProgressDialog(LoginActivity.this);
        auth=FirebaseAuth.getInstance();
        email=findViewById(R.id.emailLogin);
        password=findViewById(R.id.passwordLogin);
        forgetPassword=findViewById(R.id.forgetPassword);
        login=findViewById(R.id.Login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckProcedureOfLogIn();
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailWithNewPassWord();
            }
        });

    }

    private void sendEmailWithNewPassWord()
    {
        EMAIL=email.getText().toString();
        PASS=password.getText().toString();

        if(EMAIL.isEmpty())
        {
            Toast.makeText(getApplicationContext()," please, Enter your E-mail",Toast.LENGTH_LONG).show();
        }
        else
        {

            alert=new AlertDialog.Builder(LoginActivity.this);
            alert.setTitle("Reset Password");
            alert.setMessage("Do you want to reset your Password");
            alert.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    auth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"new pass-word sent successfully",Toast.LENGTH_LONG).show();
                            }
                            else
                            {

                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alert.show();
        }

    }

    private void LoginToApp()
    {
        EMAIL=email.getText().toString();
        PASS=password.getText().toString();

        showProgressDialog();
        auth.signInWithEmailAndPassword(EMAIL,PASS).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful())
                {
                    disMissProgressDialog();
                    goToNextActivity();
                }
                else
                {
                    disMissProgressDialog();
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    void showProgressDialog()
    {
        progressDialog.setTitle("Login....");
        progressDialog.setMessage("please wait......");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

    }
    void disMissProgressDialog()
    {
        progressDialog.dismiss();
    }
    void goToNextActivity()
    {
        Intent a= new Intent(LoginActivity.this , MainActivity.class);
        startActivity(a);
        finish();

    }
    void CheckProcedureOfLogIn()
    {
        EMAIL=email.getText().toString();
        PASS=password.getText().toString();

        if (EMAIL.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"please, Enter your E-mail",Toast.LENGTH_LONG).show();
        }
        else if (PASS.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"please, Enter your pass-word",Toast.LENGTH_LONG).show();
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
            LoginToApp();
        }
    }
}
