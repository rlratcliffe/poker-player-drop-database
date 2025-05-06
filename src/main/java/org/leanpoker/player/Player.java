package org.leanpoker.player;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class Player {

    static final String VERSION = "1.8";

    public static int betRequest(JsonNode request) {
        System.out.println("Request output: " + request.toPrettyString());
        System.out.println("Players: " + getPlayerByName(request.get("players")));
        JsonNode holeCardsNode = getPlayerByName(request.get("players")).get("hole_cards");
        System.out.println("Players: " + holeCardsNode);

        // maybe increase amount of pair
        if (isPair(holeCardsNode) || is10OrHigher(holeCardsNode)) {
            return 100;
        }
        // fold

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

    public static boolean is10OrHigher(JsonNode cardsNode) {
        int index = 0;
        boolean isHigh1 = false;
        boolean isHigh2 = false;
        if (cardsNode.isArray()) {
            for (JsonNode cardNode : cardsNode) {

                if (index == 0 && isHighRank(cardNode)) {
                    isHigh1 = true;
                } else if ((index == 1 && isHighRank(cardNode))) {
                    isHigh2 = true;
                }

                index++;
            }

        }
        boolean isBothHigh = isHigh1 && isHigh2;
        System.out.print("The cards: " + cardsNode.toPrettyString());
        System.out.println("isBothHigh " + isBothHigh);
        return isHigh1 && isHigh2;
    }

    private static boolean isHighRank(JsonNode cardNode) {
        String rank = cardNode.get("rank").asText();

        return rank.equals("A") || rank.equals("K") || rank.equals("Q") || rank.equals("J") || rank.equals("10");
    }
}
