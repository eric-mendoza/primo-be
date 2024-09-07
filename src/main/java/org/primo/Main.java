package org.primo;

import org.primo.controllers.GameController;
import org.primo.controllers.UserController;
import org.primo.database.SQLite;
import org.primo.rest.utils;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        // Connect to database
        SQLite.connect();
        SQLite.createTables();


        // Routes TODO: Move to a router class
        // Also the cors headers should be added by middleware
        // User
        post("/user", UserController::create);
        options("/user", (req, res) -> {
            utils.addCorsHeaders(res);
            return "";
        });

        get("/user", UserController::list);
        options("/user", (req, res) -> {
            utils.addCorsHeaders(res);
            return "";
        });

        get("/user/:id", UserController::get);
        options("/user/:id", (req, res) -> {
            utils.addCorsHeaders(res);
            return "";
        });

        // Game
        post("/game", GameController::create);
        options("/game", (req, res) -> {
            utils.addCorsHeaders(res);
            return "";
        });

        get("/game/:id", GameController::get);
        options("/game/:id", (req, res) -> {
            utils.addCorsHeaders(res);
            return "";
        });

        post("/game/:id/play", GameController::play);
        options("/game/:id/play", (req, res) -> {
            utils.addCorsHeaders(res);
            return "";
        });

        get("/game/:id/user/:userId", GameController::getUserStats);
        options("/game/:id/user/:userId", (req, res) -> {
            utils.addCorsHeaders(res);
            return "";
        });

        get("/game", GameController::list);
        options("/game", (req, res) -> {
            utils.addCorsHeaders(res);
            return "";
        });

        get("/game/:id/history", GameController::history);
        options("/game/:id/history", (req, res) -> {
            utils.addCorsHeaders(res);
            return "";
        });
    }
}