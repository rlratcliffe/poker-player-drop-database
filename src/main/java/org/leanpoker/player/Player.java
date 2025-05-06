package org.leanpoker.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class Player {

    static final String VERSION = "1.11";

    public static int betRequest(JsonNode request) {
        System.out.println("Request output: " + request.toPrettyString());
        System.out.println("Players: " + getPlayerByName(request.get("players")));
        JsonNode holeCardsNode = getPlayerByName(request.get("players")).get("hole_cards");

        JsonNode communityCards = request.get("community_cards");

        JsonNode allCards = getAllCards(communityCards, holeCardsNode);

        System.out.println("Players: " + holeCardsNode);

        int currentPlayer = request.get("in_action").asInt();
        int bet = request.get("players").get(currentPlayer).get("bet").asInt();
        int theCall = request.get("current_buy_in").asInt() - bet;

        // community cards
        // do more than call, and raise
        // maybe care about only larger pairs
        if (hasOneOrTwoPairs(allCards) || is10OrHigher(allCards)) {
            return theCall;
        }
        // fold, be more specific

        return 0;
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


    public static boolean hasOneOrTwoPairs(JsonNode allCards) {
        Map<String, Integer> rankCounts = new HashMap<>();

        if (allCards.isArray()) {
            for (JsonNode cardNode : allCards) {
                if (cardNode.has("rank")) {
                    String rank = cardNode.get("rank").asText();
                    rankCounts.put(rank, rankCounts.getOrDefault(rank, 0) + 1);
                }
            }
        }

        // Count how many pairs we have
        int pairCount = 0;
        for (Integer count : rankCounts.values()) {
            if (count == 2) {
                pairCount++;
            }
        }

        boolean moreThanZeroPairs = pairCount > 0;
        System.out.println("All cards " + allCards.toPrettyString() + " Has pairs: " + moreThanZeroPairs);
        return moreThanZeroPairs;
    }

    public static boolean is10OrHigher(JsonNode allCards) {
        Map<String, Integer> hasHighRank = new HashMap<>();

        if (allCards.isArray()) {
            for (JsonNode cardNode : allCards) {
                if (cardNode.has("rank") && isHighRank(cardNode)) {
                    String rank = cardNode.get("rank").asText();
                    hasHighRank.put(rank, hasHighRank.getOrDefault(rank, 0) + 1);
                }
            }
        }

        // Count how many pairs we have
        int highRankCount = 0;
        for (Integer count : hasHighRank.values()) {
            if (count == 2) {
                highRankCount++;
            }
        }

        boolean moreThanZeroHighRankCount = highRankCount > 0;
        System.out.println("All cards " + allCards.toPrettyString() + " Has high rank count: " + moreThanZeroHighRankCount);
        return moreThanZeroHighRankCount;
    }

    private static boolean isHighRank(JsonNode cardNode) {
        String rank = cardNode.get("rank").asText();

        return rank.equals("A") || rank.equals("K") || rank.equals("Q") || rank.equals("J") || rank.equals("10");
    }

    private static JsonNode getAllCards(JsonNode array1, JsonNode array2) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode combinedArray = mapper.createArrayNode();

        if (array1.isArray()) {
            combinedArray.addAll((ArrayNode) array1);
        }

        if (array2.isArray()) {
            combinedArray.addAll((ArrayNode) array2);
        }

        return combinedArray;
    }
}
