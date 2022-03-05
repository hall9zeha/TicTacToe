package com.barryzea.tictactoe.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Played {
    private String playerOneId;
    private String playerTwoId;
    private List<Integer> selectedCell;
    private boolean turnPlayerOne;
    private String winnerId;
    private Date created;
    private String goOutId;

    public Played() {
    }

    public Played(String playerOneId) {
        this.playerOneId = playerOneId;
        this.playerTwoId = "";
        this.selectedCell = new ArrayList<>();
        for(int i=0; i<9; i++){
            this.selectedCell.add(new Integer(0));
        }
        this.turnPlayerOne = true;
        this.winnerId = "";
        this.created = new Date();
        this.goOutId = "";
    }

    public String getPlayerOneId() {
        return playerOneId;
    }

    public void setPlayerOneId(String playerOneId) {
        this.playerOneId = playerOneId;
    }

    public String getPlayerTwoId() {
        return playerTwoId;
    }

    public void setPlayerTwoId(String playerTwoId) {
        this.playerTwoId = playerTwoId;
    }

    public List<Integer> getSelectedCell() {
        return selectedCell;
    }

    public void setSelectedCell(List<Integer> selectedCell) {
        this.selectedCell = selectedCell;
    }

    public boolean isTurnPlayerOne() {
        return turnPlayerOne;
    }

    public void setTurnPlayerOne(boolean turnPlayerOne) {
        this.turnPlayerOne = turnPlayerOne;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getGoOutId() {
        return goOutId;
    }

    public void setGoOutId(String goOutId) {
        this.goOutId = goOutId;
    }
}
