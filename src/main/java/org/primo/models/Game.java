package org.primo.models;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.CreationTimestamp;
import org.primo.database.Hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "server_seed")
    private String serverSeed;

    @Column(name = "client_seed")
    private String clientSeed;

    @Column(name = "nonce")
    private String nonce;

    @Column(name = "turn")
    private int turn;

    @CreationTimestamp
    @Column(name = "created_at")
    private String createdAt;

    public Game() {}

    public Game(int id, String serverSeed, String clientSeed, String nonce, String createdAt) {
        this.id = id;
        this.serverSeed = serverSeed;
        this.clientSeed = clientSeed;
        this.nonce = nonce;
        this.createdAt = createdAt;
    }

    public static List<Game> listGames() {
        Session session = Hibernate.getSessionFactory().openSession();
        List<Game> games;

        try {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Game> criteriaQuery = criteriaBuilder.createQuery(Game.class);
            Root<Game> gameRoot = criteriaQuery.from(Game.class);

            criteriaQuery.select(gameRoot);

            Query query = session.createQuery(criteriaQuery);
            games = query.getResultList();

        } catch (Exception e) {
            throw e;
        } finally {
            // Close the session
            session.close();
        }
        return games;
    }

    public boolean createGame() {
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.save(this);
            transaction.commit();

            System.out.println("Game created: " + this.getId());
        } catch (Exception e) {  // TODO: Use
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

    public static Game getGame(int id) {
        Session session = Hibernate.getSessionFactory().openSession();
        Game game;

        try {
            game = session.get(Game.class, id);
        } catch (Exception e) {
            throw e;
        } finally {
            // Close the session
            session.close();
        }
        return game;
    }

    public List<Integer> generateNumbers() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            numbers.add(i);
        }

        // Generate seed for Random
        long seed = this.generateSeed();
        System.out.println("Seed: " + seed);
        Random random = new Random(seed);
        Collections.shuffle(numbers, random);

        return numbers;
    }

    public void addUsers(List<Integer> users) {
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            for (Integer user : users) {
                GameUsers gameUser = new GameUsers();
                gameUser.setGameId(this.getId());
                gameUser.setUserId(user);
                session.save(gameUser);
            }
            transaction.commit();

            System.out.println("Users added to game: " + this.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            // Close the session
            session.close();
        }
    }

    public List<User> getUsers() {
        Session session = Hibernate.getSessionFactory().openSession();
        List<User> users;

        try {
            users = session.createQuery("SELECT u FROM User u JOIN GameUsers gu ON u.id = gu.userId WHERE gu.gameId = :gameId")
                    .setParameter("gameId", this.getId())
                    .getResultList();
        } catch (Exception e) {
            throw e;
        } finally {
            // Close the session
            session.close();
        }
        return users;
    }

    public long generateSeed() {
        String combinedSeed = this.serverSeed + this.clientSeed + this.nonce;
        return combinedSeed.hashCode();
    }

    public long generateSeed(int offset) {
        // Sum offset to nonce
        int newNonce = Integer.parseInt(this.nonce) + offset;

        String combinedSeed = this.serverSeed + this.clientSeed + newNonce;
        return combinedSeed.hashCode();
    }

    public int generateNumber(int offset) {
        long seed = this.generateSeed(offset);
        Random random = new Random(seed);
        return random.nextInt(20) + 1;
    }

    public String getServerSeed() {
        return serverSeed;
    }

    public void setServerSeed(String serverSeed) {
        this.serverSeed = serverSeed;
    }

    public String getClientSeed() {
        return clientSeed;
    }

    public void setClientSeed(String clientSeed) {
        this.clientSeed = clientSeed;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public int getId() {
        return id;
    }

    public int getTurn() {
        return this.turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getSpinCount() {
        // Get from History table how many spins have been made
        Session session = Hibernate.getSessionFactory().openSession();
        int count;

        try {
            Long result = (Long) session.createQuery("SELECT count(h) FROM History h WHERE h.gameId = :gameId")
                    .setParameter("gameId", this.getId())
                    .uniqueResult();
            if (result != null) {
                count = result.intValue();
            } else {
                count = 0;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            // Close the session
            session.close();
        }
        return count;
    }

    // Check if number is prime between 1 and 20
    public static boolean isWinner(int number) {
        return number == 1 || number == 2 || number == 3 || number == 5 || number == 7 || number == 11 || number == 13 || number == 17 || number == 19;
    }

    public int getNextTurn(int offset) {
        List<User> users = this.getUsers();
        int numberUsers = users.size();
        int userIndex = offset % numberUsers;
        return users.get(userIndex).getId();
    }

    public void updateTurn() {
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.update(this);
            transaction.commit();

            System.out.println("Game updated: " + this.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            // Close the session
            session.close();
        }
    }
}
