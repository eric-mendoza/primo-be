package org.primo.database;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.InputStream;
import java.util.Properties;

public class Hibernate {
    private static SessionFactory sessionFactory;

    static void setUp() {
        try {
            // Load properties
            Properties properties = new Properties();
            try (InputStream input = Hibernate.class.getClassLoader().getResourceAsStream("hibernate.properties")) {
                if (input == null) {
                    throw new RuntimeException("Unable to find hibernate.properties");
                }
                properties.load(input);
            }

            // Create configuration
            Configuration configuration = new Configuration();
            configuration.addProperties(properties);

            // Add annotated classes
            configuration.addAnnotatedClass(org.primo.models.User.class);
            configuration.addAnnotatedClass(org.primo.models.Game.class);
            configuration.addAnnotatedClass(org.primo.models.GameUsers.class);
            configuration.addAnnotatedClass(org.primo.models.History.class);

            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            setUp();
        }
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
