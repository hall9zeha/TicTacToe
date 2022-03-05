package com.barryzea.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.barryzea.tictactoe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin,  btnRegister;
    private ProgressBar pbLogin;
    private ScrollView formLoginScroll;
    private String email, password;
    private FirebaseAuth firebaseAuth;
    private boolean tryLogin=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth=FirebaseAuth.getInstance();
        setUpViews();
        setUpEvents();
    }



    private void setUpViews() {
        edtEmail=findViewById(R.id.editTextEmail);
        edtPassword=findViewById(R.id.editTextPassword);
        btnLogin=findViewById(R.id.buttonLogin);
        pbLogin=findViewById(R.id.progressBarLogin);
        formLoginScroll=findViewById(R.id.scrollViewFormLogin);
        btnRegister=findViewById(R.id.buttonRegisterNow);
    }
    private void setUpEvents() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email= edtEmail.getText().toString().trim();
                password =edtPassword.getText().toString();
                if(email.isEmpty()){
                    edtEmail.setError("El email es requerido");
                }
                else if(password.isEmpty()){
                    edtPassword.setError("La contraseña es requerida");
                }
                else {
                    changeVisibilityFormLogin(false);
                    loginUser();
                }
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            tryLogin=true;
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Error en la conexión" + task.getException(), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        Log.e("ERROR LOGIN", e.toString());
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user !=null){
            Intent intent= new Intent(LoginActivity.this, FindGameActivity.class);
            startActivity(intent);
        }
        else{

                changeVisibilityFormLogin(true);
            if(tryLogin) {
                edtPassword.setError("Alguno de los datos son es incorrecto");
                edtPassword.requestFocus();
            }
        }
    }
    private void changeVisibilityFormLogin(boolean statusForm) {
        pbLogin.setVisibility(statusForm ? View.GONE: View.VISIBLE);
        formLoginScroll.setVisibility(statusForm ?View.VISIBLE:View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user =firebaseAuth.getCurrentUser();

        updateUI(user);
    }
}