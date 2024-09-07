package org.primo.models;


import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.CreationTimestamp;
import org.primo.database.Hibernate;

import java.util.List;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @CreationTimestamp
    @Column(name = "created_at")
    private String createdAt;

    public User() {}

    public User(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public boolean createUser() {
        Session session = Hibernate.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.save(this);
            transaction.commit();

            System.out.println("User created: " + this.getName());
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

    public static List<User> listUsers() {
        Session session = Hibernate.getSessionFactory().openSession();
        List<User> users;

        try {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
            Root<User> userRoot = criteriaQuery.from(User.class);

            criteriaQuery.select(userRoot);

            Query query = session.createQuery(criteriaQuery);
            users = query.getResultList();

        } catch (Exception e) {
            throw e;
        } finally {
            // Close the session
            session.close();
        }
        return users;
    }

    public static User getUser(int id) {
        Session session = Hibernate.getSessionFactory().openSession();
        User user;

        try {
            user = session.get(User.class, id);
        } catch (Exception e) {
            throw e;
        } finally {
            // Close the session
            session.close();
        }
        return user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }
}
