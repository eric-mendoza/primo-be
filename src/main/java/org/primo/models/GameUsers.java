package org.primo.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "game_users")
public class GameUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "game_id")
    private int gameId;

    @Column(name = "user_id")
    private int userId;

    @CreationTimestamp
    @Column(name = "created_at")
    private String createdAt;

    public GameUsers() {}

    public GameUsers(int id, int gameId, int userId) {
        this.id = id;
        this.gameId = gameId;
        this.userId = userId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
