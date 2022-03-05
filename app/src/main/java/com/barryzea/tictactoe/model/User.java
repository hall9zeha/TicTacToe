package com.barryzea.tictactoe.model;

public class User {
    private String name;
    private int points;
    private int playedGames;

    public User() {
    }

    public User(String name, int points, int playedGames) {
        this.name = name;
        this.points = points;
        this.playedGames = playedGames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPlayedGames() {
        return playedGames;
    }

    public void setPlayedGames(int playedGames) {
        this.playedGames = playedGames;
    }
}
