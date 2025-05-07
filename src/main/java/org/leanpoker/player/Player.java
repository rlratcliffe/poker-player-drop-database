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

    static final String VERSION = "1.21";

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

        System.out.println("Current game_id" + request.get("game_id").toString());

        int minimumRaise = 1;
        try {
            minimumRaise = request.get("minimum_raise").asInt();
        } catch (Exception e) {
            System.out.println("An exception occurred accessing minimum raise" + e.getMessage());
        }
        int newRaise =  theCall + minimumRaise;

        System.out.println("All cards " + allCards.toPrettyString());

        // we are calling with higher cards and they are raising
        // look at logs of why w're calling with pairs instead of raising

        if (isPotentialFlush(allCards)) {
            System.out.println("Is potential flush and raising " + newRaise);
            return newRaise;
        }
        // straight
        // maybe care about only larger pairs
        if (is10OrHigher(allCards)) {
            System.out.println("Is 10 or higher " + allCards.toPrettyString() + " " + theCall);
            return theCall;
        } else if (hasOneOrTwoPairs(allCards)) {
            System.out.println("Has pairs, should be raising " + allCards.toPrettyString() + " " + newRaise);
            return newRaise;
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
                    System.out.println("RAnk in check 10 or higher: " + rank);
                    hasHighRank.put(rank, hasHighRank.getOrDefault(rank, 0) + 1);
                } else {
                    System.out.println("Rank is not 10 or higher");
                }
            }
        }

        boolean notEmpty = !hasHighRank.isEmpty();
        System.out.println("All cards " + allCards.toPrettyString() + " Has high rank count: " + notEmpty);
        return notEmpty;
    }

    public static boolean isPotentialFlush(JsonNode allCards) {
        boolean hasAllSameSuit = true; // Start with true assumption

        if (allCards.isArray() && !allCards.isEmpty()) {
            String suit = allCards.get(0).get("suit").asText();
            System.out.println("Checking if all cards match suit: " + suit);

            for (JsonNode cardNode : allCards) {
                String currentSuit = cardNode.get("suit").asText();
                if (!suit.equals(currentSuit)) {
                    System.out.println("Found different suit: " + currentSuit);
                    hasAllSameSuit = false;
                    break; // Exit the loop as soon as we find a different suit
                }
            }
        } else {
            // No cards or not an array
            hasAllSameSuit = false;
        }

        System.out.println("All cards " + allCards.toPrettyString() + " Has all same suit: " + hasAllSameSuit);
        return hasAllSameSuit;
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
