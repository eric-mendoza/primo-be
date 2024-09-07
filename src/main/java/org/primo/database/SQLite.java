package org.primo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite {
    // Create singleton
    private static Connection connection;

    public static Connection connect() {
        if (connection == null) {
            try {
                String url = "jdbc:sqlite:game.db";
                connection = DriverManager.getConnection(url);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return connection;
    }

    public static void createTables() {
        try (Statement stmt = connect().createStatement()) {
            String gamesTable = "CREATE TABLE IF NOT EXISTS game ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_seed TEXT NOT NULL,"
                    + "client_seed TEXT NOT NULL,"
                    + "nonce INTEGER NOT NULL,"
                    + "turn INTEGER NOT NULL,"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (turn) REFERENCES user(id)"
                    + ");";

            String gameUsersTable = "CREATE TABLE IF NOT EXISTS game_users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "game_id INTEGER NOT NULL,"
                    + "user_id INTEGER NOT NULL,"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (game_id) REFERENCES games(id),"
                    + "FOREIGN KEY (user_id) REFERENCES user(id)"
                    + ");";

            String historyTable = "CREATE TABLE IF NOT EXISTS history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "game_id INTEGER NOT NULL,"
                    + "user_id INTEGER NOT NULL,"
                    + "result INTEGER NOT NULL,"
                    + "winner int NOT NULL,"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (game_id) REFERENCES games(id),"
                    + "FOREIGN KEY (user_id) REFERENCES user(id)"
                    + ");";

            String userTable = "CREATE TABLE IF NOT EXISTS user ("
                    + "id INTEGER PRIMARY KEY,"
                    + "name TEXT NOT NULL,"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ");";

            stmt.execute(gamesTable);
            stmt.execute(gameUsersTable);
            stmt.execute(historyTable);
            stmt.execute(userTable);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}