package com.barryzea.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.barryzea.tictactoe.R;
import com.barryzea.tictactoe.common.Constants;
import com.barryzea.tictactoe.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtName, edtEmail, edtPasswd;
    private Button btnRegister;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String name, email, passwd;
    private ScrollView scrollFromRegister;
    private ProgressBar pbRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        firebaseAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        setUpViews();
        setUpEvents();

    }



    private void setUpViews() {
        edtEmail=findViewById(R.id.editTextEmailRegister);
        edtName=findViewById(R.id.editTextNameRegister);
        edtPasswd=findViewById(R.id.editTextPasswordRegister);
        btnRegister=findViewById(R.id.buttonRegister);
        pbRegister=findViewById(R.id.progressBarRegister);
        scrollFromRegister=findViewById(R.id.scrollViewFormRegister);
    }
    private void setUpEvents() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=edtName.getText().toString().trim();
                 email = edtEmail.getText().toString().trim().toLowerCase();
                 passwd=edtPasswd.getText().toString().trim();

                if(name.isEmpty()){
                    edtName.setError("El nombre es obligatorio");
                }
                else if(email.isEmpty()){
                    edtEmail.setError("El email es obligatorio");
                }
                else if(passwd.isEmpty()){
                    edtPasswd.setError("La contraseña es obligatoria");
                }
                else{
                    createUser();
                    showFormRegister(false);

                }
            }
        });
    }

    private void showFormRegister(boolean statusForm){
        pbRegister.setVisibility(statusForm?View.GONE:View.VISIBLE);
        scrollFromRegister.setVisibility(statusForm?View.VISIBLE:View.GONE);
    }
    private void createUser() {
        firebaseAuth.createUserWithEmailAndPassword(email,passwd).addOnCompleteListener(
                RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull  Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                }
        )
                .addOnFailureListener(RegisterActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        Log.e("LOGIN", e.toString());
                    }
                });

    }

    private void updateUI(FirebaseUser user) {
        if(user !=null){
            User newUser = new User(name, 0,0);
            db.collection(Constants.COLLECTION_USER).
                    document(user.getUid())
                    .set(newUser)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull  Task<Void> task) {
                            if(task.isSuccessful()) {
                                finish();
                                Intent intent = new Intent(RegisterActivity.this, FindGameActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(RegisterActivity.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull  Exception e) {
                            Log.e("ERROR", e.toString());
                        }
                    });


        }
        else{
            showFormRegister(true);
            edtPasswd.setError("Alguno de los datos son es incorrecto");
            edtPasswd.requestFocus();
        }
    }
}