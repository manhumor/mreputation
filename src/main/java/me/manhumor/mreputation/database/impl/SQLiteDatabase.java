package me.manhumor.mreputation.database.impl;

import me.manhumor.mreputation.MReputation;
import me.manhumor.mreputation.database.Database;

import java.io.File;
import java.sql.*;

public class SQLiteDatabase implements Database {
    private static final String reputation = "reputation";

    private String url;
    private Connection connection;

    private final MReputation instance = MReputation.getInstance();

    public SQLiteDatabase(File file) throws SQLException {
        try {
            url = "jdbc:sqlite:" + file.getAbsolutePath();
            Class.forName("org.sqlite.JDBC").getConstructor().newInstance();
            connection = connect();
        } catch (Exception e) {
            throw new SQLException(e);
        }

        try(Statement s = connection.createStatement()) {
            s.executeUpdate(String.format(
                    "CREATE TABLE IF NOT EXISTS `%s` (" +
                            "`playerName` TEXT NOT NULL, " +
                            "`reputation` INTEGER NOT NULL DEFAULT 0)"
                    , reputation));
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public int getReputation(String playerName) {
        String sql = "SELECT reputation FROM " + reputation + " WHERE playerName = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("reputation");
            } else {
                setReputation(playerName, 0);
                return 0;
            }
        } catch (SQLException e) {
            instance.getLogger().warning(e.getMessage());
        }
        return 0;
    }

    @Override
    public void setReputation(String playerName, int reputation) {
        String checkSql = "SELECT reputation FROM " + SQLiteDatabase.reputation + " WHERE playerName = ?";
        String insertSql = "INSERT INTO " + SQLiteDatabase.reputation + " (playerName, reputation) VALUES(?, ?)";
        String updateSql = "UPDATE " + SQLiteDatabase.reputation + " SET reputation = ? WHERE playerName = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, playerName);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, reputation);
                    updateStmt.setString(2, playerName);
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    insertStmt.setString(1, playerName);
                    insertStmt.setInt(2, reputation);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            instance.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            instance.getLogger().warning(ex.getMessage());
        }
    }
}

