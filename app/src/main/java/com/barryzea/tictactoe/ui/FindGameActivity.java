package com.barryzea.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.*;

import com.airbnb.lottie.LottieAnimationView;
import com.barryzea.tictactoe.R;
import com.barryzea.tictactoe.common.Constants;
import com.barryzea.tictactoe.model.Played;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class FindGameActivity extends AppCompatActivity {
    private TextView tvCharge;
    private ProgressBar pbLoadingFind;
    private ScrollView scrollViewMenu, scrollViewLoading;
    private Button btnPlay, btnRanking;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String userUID, gameId="";
    private ListenerRegistration listenerRegistration=null;
    private LottieAnimationView animationView;
    private boolean found=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_game);
        setUpViews();
        initProgressbar();
        initFirebaseObjects();
        initEvents();

    }



    private void initFirebaseObjects() {
        firebaseAuth= FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        user=firebaseAuth.getCurrentUser();
        userUID=user.getUid();


    }

    private void setUpViews(){
        tvCharge=findViewById(R.id.textViewCharge);
        pbLoadingFind=findViewById(R.id.progressBarFindGame);
        scrollViewLoading=findViewById(R.id.scrollviewLoadingFind);
        scrollViewMenu=findViewById(R.id.scrollViewMenuGame);
        btnPlay=findViewById(R.id.buttonPlay);
        btnRanking=findViewById(R.id.buttonRanking);
        animationView=findViewById(R.id.animation_view);
    }
    private void initEvents() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMenuVisibility(false);
                findGameFree();
            }
        });
        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void findGameFree() {

        animationView.playAnimation();
        db.collection(Constants.PLAYED).whereEqualTo(Constants.PLAYER_TWO_ID, "")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull  Task<QuerySnapshot> task) {

                        if(task.getResult().size() ==0){
                            createNewPlay();
                        }
                        else{

                            tvCharge.setText("Buscando partidas libres");
                            for(DocumentSnapshot d :task.getResult().getDocuments() ){
                                if(!d.get("playerOneId").equals(userUID)){
                                    found=true;
                                    gameId=d.getId();
                                    Played play=d.toObject(Played.class);
                                    play.setPlayerTwoId(userUID);

                                    db.collection(Constants.PLAYED)
                                            .document(gameId)
                                            .set(play)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    tvCharge.setText("Partida encontrada");
                                                    animationView.setRepeatCount(0);
                                                    animationView.setAnimation("checked_animation.json");
                                                    animationView.playAnimation();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startGame();
                                                        }
                                                    },1500);

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull  Exception e) {
                                                    changeMenuVisibility(true);
                                                    Toast.makeText(FindGameActivity.this, "Error al entrar en la partida", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    break;
                                }
                            if(found) createNewPlay();
                            }


                            
                        }
                    }
                });

    }

    private void createNewPlay() {
        tvCharge.setText("Creando nueva partida");
        Played played=new Played(userUID);
        db.collection(Constants.PLAYED)
                .add(played)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        gameId=documentReference.getId();
                        waitingPlayer();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        changeMenuVisibility(true);
                        Toast.makeText(FindGameActivity.this, "Error al crear la nueva la partida", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void waitingPlayer() {
        tvCharge.setText("Esperando jugador ...");
        listenerRegistration=db.collection(Constants.PLAYED)
                .document(gameId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable  DocumentSnapshot value, @Nullable  FirebaseFirestoreException error) {
                        if(!value.get(Constants.PLAYER_TWO_ID).equals("")){
                            tvCharge.setText("Alguíen te ha desafiado, comienza la partida");
                            animationView.setRepeatCount(0);
                            animationView.setAnimation("checked_animation.json");
                            animationView.playAnimation();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startGame();
                                }
                            },2000);
                        }
                    }
                });
    }

    private void startGame() {
        if(listenerRegistration !=null){
            listenerRegistration.remove();
        }
        Intent i = new Intent(FindGameActivity.this, GameActivity.class);
        i.putExtra(Constants.GAME_ID, gameId);
        startActivity(i);
        gameId="";
    }

    private void initProgressbar(){
        tvCharge.setText("Cargando ...");
        pbLoadingFind.setIndeterminate(true);
        changeMenuVisibility(true);
    }

    private void changeMenuVisibility(boolean statusMenu) {
        scrollViewLoading.setVisibility(statusMenu ? View.GONE:View.VISIBLE);
        scrollViewMenu.setVisibility(statusMenu?View.VISIBLE: View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!gameId.equals("")){
            changeMenuVisibility(false);
            waitingPlayer();
        }else{
            changeMenuVisibility(true);
        }


    }

    @Override
    protected void onStop() {
        if(listenerRegistration !=null){
            listenerRegistration.remove();
        }
        if(gameId!="")
        {
            db.collection(Constants.PLAYED)
                    .document(gameId)
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull  Task<Void> task) {
                            gameId="";
                        }
                    });
        }
        super.onStop();
    }
}