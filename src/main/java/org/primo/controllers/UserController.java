package org.primo.controllers;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.primo.models.User;
import org.primo.rest.utils;
import spark.Request;
import spark.Response;

import java.util.List;

class UserRequest {
    public String name;
}

public class UserController {
    public static String create(Request req, Response res) {
        utils.addCorsHeaders(res);
        int status = 500;
        String message = "Unable to create user";
        res.type("application/json");

        // Get user data from request
        UserRequest payload = new Gson().fromJson(req.body(), UserRequest.class);

        // Create User
        User user = new User();
        user.setName(payload.name);

        try {
            boolean created = user.createUser();
            if (created) {
                status = 201;
                message = "User created successfully";
            } else {
                status = 400;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set response status
        res.status(status);

        return new Gson().
                toJson(
                        new org.primo.rest.Response(status,
                                message,
                                new Gson().toJsonTree(user)
                        )
                );
    }

    public static String list(Request req, Response res) {
        utils.addCorsHeaders(res);
        int status = 500;
        String message = "Unable to get users";
        res.type("application/json");
        List<User> users = null;

        try {
            users = User.listUsers();
            status = 200;
            message = "Users retrieved successfully";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Gson().
                toJson(
                        new org.primo.rest.Response(status,
                                message,
                                new Gson().toJsonTree(users)
                        )
                );
    }

    public static String get(Request req, Response res) {
        utils.addCorsHeaders(res);
        int status = 500;
        String message = "Unable to get user";
        res.type("application/json");
        User user = null;

        try {
            // TODO: Validate ID has the proper format, for simplicity omitting
            user = User.getUser(Integer.parseInt(req.params(":id")));
            if (user != null) {
                status = 200;
                message = "User retrieved successfully";

                // TODO: calculate the metrics
            } else {
                status = 404;
                message = "User not found";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Gson().
                toJson(
                        new org.primo.rest.Response(status,
                                message,
                                new Gson().toJsonTree(user)
                        )
                );
    }
}