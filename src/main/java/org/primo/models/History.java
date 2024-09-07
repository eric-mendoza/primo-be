package org.primo.models;

import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.CreationTimestamp;
import org.primo.database.Hibernate;

import java.util.List;

@Entity
@Table(name = "history")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "game_id")
    private int gameId;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "result")
    private int result;

    @Column(name = "winner")
    private int winner;

    @CreationTimestamp
    @Column(name = "created_at")
    private String createdAt;

    public static List<History> getGameStats(int gameId) {
        // Get history entries where a win was registered for the game
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction transaction = null;
        List<History> history = null;

        try{
            transaction = session.beginTransaction();
            history = session.createQuery("FROM History WHERE gameId = :gameId AND winner = 1")
                    .setParameter("gameId", gameId)
                    .list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }

        return history;
    }

    public static List<History> listByGameId(int gameId) {
        // Get history entries by game id
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction transaction = null;
        List<History> history = null;

        try{
            transaction = session.beginTransaction();
            history = session.createQuery("FROM History WHERE gameId = :gameId")
                    .setParameter("gameId", gameId)
                    .list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }

        return history;
    }

    public boolean createHistory() {
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.save(this);
            transaction.commit();

            System.out.println("History created");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            // Close the session
            session.close();
        }
        return true;
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

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public History() {}

    public History(int id, int gameId, int userId, int result, String createdAt) {
        this.id = id;
        this.gameId = gameId;
        this.userId = userId;
        this.result = result;
        this.createdAt = createdAt;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }

}
