package me.manhumor.mreputation.database;

public interface Database {
    public int getReputation(String playerName);
    public void setReputation(String playerName, int reputation);
    public void close();
}
