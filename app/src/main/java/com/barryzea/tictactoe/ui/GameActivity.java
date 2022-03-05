package com.barryzea.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.barryzea.tictactoe.R;
import com.barryzea.tictactoe.common.Constants;
import com.barryzea.tictactoe.model.Played;
import com.barryzea.tictactoe.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private TextView tvPlayerOne, tvPlayerTwo;
    private List<ImageView> boxes;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String userId, gameId="", playerOneName="", playerTwoName="", winnerId="", namePlayer="";
    private Played played;
    private ListenerRegistration listenerRegistration=null;
    private User userPlayerOne, userPlayerTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
         setUpViews();
         setUpGame();
    }



    private void setUpViews() {
        tvPlayerOne=findViewById(R.id.textViewPlayerOne);
        tvPlayerTwo=findViewById(R.id.textViewPlayerTwo);
        boxes = new ArrayList<>();

        boxes.add((ImageView) findViewById(R.id.imageView0));
        boxes.add((ImageView) findViewById(R.id.imageView1));
        boxes.add((ImageView) findViewById(R.id.imageView2));
        boxes.add((ImageView) findViewById(R.id.imageView3));
        boxes.add((ImageView) findViewById(R.id.imageView4));
        boxes.add((ImageView) findViewById(R.id.imageView5));
        boxes.add((ImageView) findViewById(R.id.imageView6));
        boxes.add((ImageView) findViewById(R.id.imageView7));
        boxes.add((ImageView) findViewById(R.id.imageView8));



    }
    private void setUpGame() {
        firebaseAuth= FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        db=FirebaseFirestore.getInstance();

        userId=firebaseUser.getUid();

        Bundle extras= getIntent().getExtras();
        gameId = extras.getString(Constants.GAME_ID);
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        playListener();
    }

    private void playListener() {
        listenerRegistration = db.collection(Constants.PLAYED)
                .document(gameId)
                .addSnapshotListener(GameActivity.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable  DocumentSnapshot value, @Nullable  FirebaseFirestoreException error) {
                        if(error !=null ){
                            Toast.makeText(GameActivity.this, "Error al obtener los datos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //detectamos si el snapshot es un cambio local o del servidor
                        String source=value !=null && value.getMetadata().hasPendingWrites() ? "Local" : "Server";
                        if(value.exists() && source.equals("Server")){
                            played = value.toObject(Played.class);
                            if(playerOneName.isEmpty() || playerTwoName.isEmpty()){

                                getPlayerNames();

                            }
                            updateUI(played);

                        }
                        updateGamePlayers();
                    }
                });

    }

    private void updateGamePlayers() {
        if(played.isTurnPlayerOne()){
            tvPlayerOne.setTextColor(getResources().getColor(R.color.colorPrimary));
            tvPlayerTwo.setTextColor(getResources().getColor(R.color.gris));
        }
        else{
            tvPlayerOne.setTextColor(getResources().getColor(R.color.gris));
            tvPlayerTwo.setTextColor(getResources().getColor(R.color.colorAccent));
        }
        if(!played.getWinnerId().isEmpty()){
            winnerId=played.getWinnerId();
            showDialogGameOver();
        }
    }

    private void updateUI(Played played) {

        for(int i=0; i<9; i++){
            int box = played.getSelectedCell().get(i);
            ImageView ivCurrentBox=boxes.get(i);
            if(box==0){
                ivCurrentBox.setImageResource(R.drawable.ic_empty_square);
            }
            else if(box==1){
                ivCurrentBox.setImageResource(R.drawable.ic_player_one);
            }
            else if(box==2){
                ivCurrentBox.setImageResource(R.drawable.ic_player_two);
            }

        }


    }

    private void getPlayerNames() {
        db.collection(Constants.COLLECTION_USER)
                .document(played.getPlayerOneId())
                .get()
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        playerOneName=documentSnapshot.get("name").toString();
                        tvPlayerOne.setText(playerOneName);
                        userPlayerOne=documentSnapshot.toObject(User.class);
                        if(played.getPlayerOneId().equals(userId)){
                            namePlayer=playerOneName;
                        }
                    }
                });
        db.collection(Constants.COLLECTION_USER)
                .document(played.getPlayerTwoId())
                .get()
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        playerTwoName=documentSnapshot.get("name").toString();
                        tvPlayerTwo.setText(playerTwoName);
                        userPlayerTwo=documentSnapshot.toObject(User.class);
                        if(played.getPlayerTwoId().equals(userId)){
                            namePlayer=playerTwoName;
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        if(listenerRegistration !=null){
            listenerRegistration.remove();
        }
        super.onStop();
    }

    public void boxSelected(View view) {
        if(!played.getWinnerId().isEmpty()){
            Toast.makeText(this, "La partida ha terminado", Toast.LENGTH_SHORT).show();

        }
        else{
            if(played.isTurnPlayerOne() && played.getPlayerOneId().equals(userId)){
                //está jugando el jugador 1
                updateGame(view.getTag().toString());
            }
            else if(!played.isTurnPlayerOne() &&played.getPlayerTwoId().equals(userId)){
                //Está jugando el jugador 2
                updateGame(view.getTag().toString());
            }
            else{
                Toast.makeText(this, "No es tu turno todavía", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateGame(String numberBox) {
            int position = Integer.parseInt(numberBox);
            if(played.getSelectedCell().get(position) !=0){
                Toast.makeText(this, "Seleccione una casilla libre", Toast.LENGTH_SHORT).show();
            }
            else {
                if (played.isTurnPlayerOne()) {
                    boxes.get(position).setImageResource(R.drawable.ic_player_one);
                    //seteando el array celdas seleecionadas en firebase con 1 en la posicion seleccionada
                    played.getSelectedCell().set(position, 1);
                } else {
                    boxes.get(position).setImageResource(R.drawable.ic_player_two);
                    played.getSelectedCell().set(position, 2);
                }

                if(checkSolution()){
                    played.setWinnerId(userId);
                }
                else if (existEmpate()){
                    played.setWinnerId("empate");
                }
                else {
                    //cambio de turno
                    changeTurnPlayer();
                }


                //actualizar datos de la jugada
                db.collection(Constants.PLAYED)
                        .document(gameId)
                        .set(played)
                        .addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        })
                        .addOnFailureListener(GameActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Error", e.toString());
                            }
                        });
            }

    }

    private void changeTurnPlayer() {

        played.setTurnPlayerOne(!played.isTurnPlayerOne());
    }
    private boolean existEmpate(){
        boolean existEmpate=false;
        boolean existBoxEmpty=false;
        for(int i=0; i<9; i++){
            if(played.getSelectedCell().get(i) ==0){
                existBoxEmpty=true;
                break;
            }


        }
        if(!existBoxEmpty){
            existEmpate=true;
        }
        return existEmpate;
    }
    private boolean checkSolution(){
        boolean exist=false;


            List<Integer> box=played.getSelectedCell();
            //primera horizontal
        if(box.get(0) == box.get(1)
                && box.get(1) == box.get(2)
                && box.get(2) != 0) { // 0 - 1 - 2
             exist= true;
        } else if(box.get(3) == box.get(4)
                && box.get(4) == box.get(5)
                && box.get(5) != 0) { // 3 - 4 - 5
            exist = true;
        } else if(box.get(6) == box.get(7)
                && box.get(7) == box.get(8)
                &&box.get(8) != 0) { // 6 - 7 - 8
            exist = true;
        } else if(box.get(0) == box.get(3)
                && box.get(3) == box.get(6)
                && box.get(6) != 0) { // 0 - 3 - 6
            exist = true;
        } else if(box.get(1) == box.get(4)
                && box.get(4) == box.get(7)
                && box.get(7) != 0) { // 1 - 4 - 7
            exist = true;
        } else if(box.get(2) == box.get(5)
                && box.get(5) == box.get(8)
                && box.get(8) != 0) { // 2 - 5 - 8
            exist = true;
        } else if(box.get(0) == box.get(4)
                && box.get(4) == box.get(8)
                && box.get(8) != 0) { // 0 - 4 - 8
            exist = true;
        } else if(box.get(2) == box.get(4)
                && box.get(4) == box.get(6)
                && box.get(6) != 0) { // 2 - 4 - 6
            exist = true;
        }

        return exist;
    }
    public void showDialogGameOver(){
        AlertDialog.Builder build=new AlertDialog.Builder(this);
        View v =getLayoutInflater().inflate(R.layout.dialog_game_over, null);

        build.setTitle("Game Over");
        build.setView(v);

        TextView tvPoints=v.findViewById(R.id.textViewPuntos);
        TextView tvInfo=v.findViewById(R.id.textViewInformacion);
        LottieAnimationView animationView =v.findViewById(R.id.animation_view);

        if(winnerId.equals("empate")){
            tvInfo.setText(namePlayer + " Has empatado el juego ");
            tvPoints.setText("+1 punto");

            updatePlayerPoints( 1);
        }
        else if(winnerId.equals(userId)){
            tvInfo.setText("!" + namePlayer + " Has ganado ");
            tvPoints.setText("+3 puntos");
            updatePlayerPoints(3);
        }
        else{
            tvInfo.setText("!" + namePlayer + " Has perdido ");
            tvPoints.setText("0 puntos");
            animationView.setAnimation("thumbs_down_animation.json");
            updatePlayerPoints(0);
        }

        animationView.playAnimation();

        build.setCancelable(false);
        build.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            finish();
            }
        });
       Dialog dialog= build.create();
       dialog.show();

    }

    private void updatePlayerPoints( int points) {
            User updateUser= null;
            if(namePlayer.equals(userPlayerOne.getName())){
                userPlayerOne.setPoints(userPlayerOne.getPoints() + points);
                userPlayerOne.setPlayedGames(userPlayerOne.getPlayedGames() + 1 );
                updateUser=userPlayerOne;
            }
            else{
                userPlayerTwo.setPoints(userPlayerTwo.getPoints() + points);
                userPlayerTwo.setPlayedGames(userPlayerTwo.getPlayedGames() + 1 );
                updateUser=userPlayerTwo;
            }

            db.collection(Constants.COLLECTION_USER)
                    .document(userId)
                    .set(updateUser)
                    .addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    })
                    .addOnFailureListener(GameActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull  Exception e) {

                        }
                    });
    }
}