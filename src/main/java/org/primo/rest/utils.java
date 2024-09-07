package org.primo.rest;

import spark.Response;

public class utils {
    public static void addCorsHeaders(Response res) {
        res.header("Access-Control-Allow-Methods", "GET, OPTIONS");
        res.header("Access-Control-Allow-Origin", "*");
        res.header("Access-Control-Allow-Headers", "content-type");
    }
}
