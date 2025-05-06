package org.leanpoker.player;

import com.fasterxml.jackson.databind.JsonNode;

public class Player {

    static final String VERSION = "1.3";

    public static int betRequest(JsonNode request) {
        System.out.println("Request output: " + request.toPrettyString());
        return 1000;
    }

    public static void showdown(JsonNode game) {
        System.out.println("Showdown output: " + game.toPrettyString());
    }
}
