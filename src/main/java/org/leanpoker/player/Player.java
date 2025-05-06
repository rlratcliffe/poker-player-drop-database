package org.leanpoker.player;

import com.fasterxml.jackson.databind.JsonNode;

public class Player {

    static final String VERSION = "1.4";

    public static int betRequest(JsonNode request) {
        System.out.println("Request output: " + request.toPrettyString());
        return 10;
    }

    public static void showdown(JsonNode game) {
    }
}
