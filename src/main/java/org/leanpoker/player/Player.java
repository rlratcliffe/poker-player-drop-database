package org.leanpoker.player;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class Player {

    static final String VERSION = "1.6";

    public static int betRequest(JsonNode request) {
        System.out.println("Request output: " + request.toPrettyString());
        System.out.println("Players: " + getPlayerByName(request.get("players")));
        JsonNode holeCardsNode = getPlayerByName(request.get("players")).get("hole_cards");
        System.out.println("Players: " + holeCardsNode);



        if (isPair(holeCardsNode)) {
            return 100;
        }
        // 10 or higher

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

    public static boolean isPair(JsonNode cardsNode) {
        String rank1 = "";
        String rank2 = "";
        int index = 0;
        boolean isPair = false;
        if (cardsNode.isArray()) {
            for (JsonNode cardNode : cardsNode) {
                String rank = cardNode.get("rank").asText();
                System.out.println("Found card with rank: " + rank);

                if (index == 0) {
                    rank1 = rank;
                } else {
                    rank2 = rank;
                }

                index++;
            }

            if (rank1.equals(rank2)) {
                System.out.println("Rank 1 == rank 2");
                isPair = true;
            }
        }
        return isPair;
    }
}
