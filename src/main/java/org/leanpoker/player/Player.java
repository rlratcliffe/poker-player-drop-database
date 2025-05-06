package org.leanpoker.player;

import com.fasterxml.jackson.databind.JsonNode;

public class Player {

    static final String VERSION = "1.2";

    public static int betRequest(JsonNode request) {
        return 1000;
    }

    public static void showdown(JsonNode game) {
    }
}
