package org.primo.controllers;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.primo.models.Game;
import org.primo.models.History;
import org.primo.models.User;
import org.primo.rest.utils;
import spark.Request;
import spark.Response;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import static spark.Spark.*;

class CreateGameRequest {
    @SerializedName("client_seed")
    public String clientSeed;

    public List<Integer> users;
}

class CreateGameResponse {
    @SerializedName("game_id")
    public int gameId;

    public List<Integer> numbers;

    public int turn; // user_id of the user whose turn it is

    public List<User> users;
}

class GetGameResponse {
    public int id;
    public int turn;
    public List<Integer> numbers;
}

class PlayResponse {
    public int number;
    public int turn;
    public boolean winner;
}

class GameUserStats {
    public int wins;
    public int losses;
}

class GameListResponse {
    public int id;
    public int turn;
    public List<Integer> numbers;
}

public class GameController {
    public static String create(Request req, Response res) {
        utils.addCorsHeaders(res);
        int status = 500;
        String message = "Unable to create game";
        res.type("application/json");

        // Read payload
        CreateGameRequest payload = new Gson().fromJson(req.body(), CreateGameRequest.class);

        // Create game
        Game game = new Game();
        game.setClientSeed(payload.clientSeed);
        game.setTurn(payload.users.get(0)); // First user in the list will start the game

        // Generate server seed
        SecureRandom random = new SecureRandom();
        game.setServerSeed(new BigInteger(130, random).toString(32));

        // Generate nonce
        random = new SecureRandom();
        game.setNonce(String.valueOf(random.nextInt(Integer.MAX_VALUE)));

        try {
            boolean created = game.createGame();
            if (created) {
                // Add users to the game
                // TODO: This transaction should be atomic, if one of the users fails to be added, the game should not be created
                // It won't be implemented due to time constraints.
                game.addUsers(payload.users);
                status = 201;
                message = "Game created successfully";
            } else {
                status = 400;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set response status
        res.status(status);

        CreateGameResponse response = new CreateGameResponse();
        response.gameId = game.getId();
        response.turn = game.getTurn();
        response.numbers = game.generateNumbers();
        response.users = game.getUsers();

        return new Gson().
                toJson(
                        new org.primo.rest.Response(status,
                                message,
                                new Gson().toJsonTree(response)
                        )
                );
    }

    public static String get(Request req, Response res) {
        utils.addCorsHeaders(res);
        int status = 500;
        String message = "Unable to get game";
        res.type("application/json");
        Game game = null;
        GetGameResponse response = new GetGameResponse();

        // Get game id
        int gameId = Integer.parseInt(req.params(":id"));

        try{
            game = Game.getGame(gameId);
            if (game != null) {
                status = 200;
                message = "Game retrieved successfully";
                response.id = game.getId();
                response.turn = game.getTurn();
                response.numbers = game.generateNumbers();
            } else {
                status = 404;
                message = "Game not found";
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
                                new Gson().toJsonTree(response)
                        )
                );
    }

    public static String play(Request req, Response res) {
        utils.addCorsHeaders(res);
        int status = 500;
        String message = "Unable to play game";
        res.type("application/json");

        // Get game id
        int gameId = Integer.parseInt(req.params(":id"));
        Game game;

        // Get gameId
        try{
            game = Game.getGame(gameId);
        } catch (Exception e) {
            return new Gson().
                    toJson(
                            new org.primo.rest.Response(404,
                                    "Game not found",
                                    new Gson().toJsonTree(null)
                            )
                    );
        }

        // Get spin count
        int offset = game.getSpinCount();
        PlayResponse response = new PlayResponse();
        response.number = game.generateNumber(offset);
        response.winner = Game.isWinner(response.number);

        // Register spin
        History history = new History();
        history.setGameId(gameId);
        history.setResult(response.number);
        history.setUserId(game.getTurn());
        history.setWinner(response.winner ? 1 : 0);

        try {
            history.createHistory();
        } catch (Exception e) {
            return new Gson().
                    toJson(
                            new org.primo.rest.Response(500,
                                    "Unable to register spin",
                                    new Gson().toJsonTree(null)
                            )
                    );
        }

        // Calculate next turn
        response.turn = game.getNextTurn(offset + 1); // calculate next turn
        game.setTurn(response.turn);
        try {
            game.updateTurn();
        } catch (Exception e) {
            return new Gson().
                    toJson(
                            new org.primo.rest.Response(500,
                                    "Unable to update game",
                                    new Gson().toJsonTree(null)
                            )
                    );
        }

        // Set response status
        status = 200;
        message = "Game played successfully";
        res.status(status);

        return new Gson().
                toJson(
                        new org.primo.rest.Response(status,
                                message,
                                new Gson().toJsonTree(response)
                        )
                );
    }

    public static String getUserStats(Request req, Response res) {
        utils.addCorsHeaders(res);

        int status = 500;
        String message = "Unable to get user stats";
        res.type("application/json");

        // Get user id
        int userId = Integer.parseInt(req.params(":userId"));

        // Get game id
        int gameId = Integer.parseInt(req.params(":id"));

        List<History> history = null;

        try {
            history = History.getGameStats(gameId);
            status = 200;
            message = "User stats retrieved successfully";
        } catch (Exception e) {
            return new Gson().
                    toJson(
                            new org.primo.rest.Response(500,
                                    "Unable to get user stats",
                                    new Gson().toJsonTree(null)
                            )
                    );
        }

        // Commpute wins and losses for user
        GameUserStats stats = new GameUserStats();
        stats.wins = 0;
        stats.losses = 0;
        for (History h : history) {
            if (h.getUserId() == userId) {
                stats.wins++;
            } else {
                stats.losses++;
            }
        }

        return new Gson().
                toJson(
                        new org.primo.rest.Response(status,
                                message,
                                new Gson().toJsonTree(stats)
                        )
                );
    }

    public static String list(Request req, Response res) {
        utils.addCorsHeaders(res);
        int status = 500;
        String message = "Unable to get games";
        res.type("application/json");
        List<Game> games = null;

        try{
            games = Game.listGames();
            status = 200;
            message = "Games retrieved successfully";
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set response status
        res.status(status);

        // Remove sensitive data
        GameListResponse[] response = new GameListResponse[games.size()];
        for (int i = 0; i < games.size(); i++) {
            response[i] = new GameListResponse();
            response[i].id = games.get(i).getId();
            response[i].turn = games.get(i).getTurn();
            response[i].numbers = games.get(i).generateNumbers();
        }

        return new Gson().
                toJson(
                        new org.primo.rest.Response(status,
                                message,
                                new Gson().toJsonTree(response)
                        )
                );
    }

    public static String history(Request req, Response res) {
        utils.addCorsHeaders(res);
        int status = 500;
        String message = "Unable to get history";
        res.type("application/json");
        List<History> history = null;

        // Read game id
        int gameId = Integer.parseInt(req.params(":id"));

        try{
            history = History.listByGameId(gameId);
            status = 200;
            message = "Game history retrieved successfully";
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set response status
        res.status(status);

        return new Gson().
                toJson(
                        new org.primo.rest.Response(status,
                                message,
                                new Gson().toJsonTree(history)
                        )
                );
    }
}

