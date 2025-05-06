package org.leanpoker.player;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class Player {

    static final String VERSION = "1.5";

    public static int betRequest(JsonNode request) {
        System.out.println("Request output: " + request.toPrettyString());
        System.out.println(getPlayerByName(request.get("players")));
        // 10 or higher
        // pair
        return 10;
    }

    public static void showdown(JsonNode game) {
    }

    public static JsonNode getPlayerByName(JsonNode playersNode) {
        if (!playersNode.isArray()) {
            System.out.println("Returned null from getPlayerByName");
            return null;
        }

        String name = "Drop Database";

        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                playersNode.elements(),
                                Spliterator.ORDERED),
                        false)
                .filter(player -> name.equals(player.get("name").asText()))
                .findFirst()
                .orElse(null);
    }
}
