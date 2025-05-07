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

    static final String VERSION = "1.27";

    static final String EARLY_POSITION = "EARLY";
    static final String MIDDLE_POSITION = "MIDDLE";
    static final String LATE_POSITION = "LATE";

    public static void printIt(JsonNode request, String str) {
        System.out.println("VERSION: " + VERSION +
            " gameId: " + request.get("game_id").toString() + " content: " + str);
    }

    public static int betRequest(JsonNode request) {

        printIt(request, "Request output: " + request.toPrettyString());
        printIt(request, "Players: " + getPlayerByName(request, request.get("players")));
        JsonNode holeCardsNode = getPlayerByName(request, request.get("players")).get("hole_cards");

        JsonNode communityCards = request.get("community_cards");

        JsonNode allCards = getAllCards(communityCards, holeCardsNode);

        printIt(request, "Players: " + holeCardsNode);

        int currentPlayer = request.get("in_action").asInt();
        int bet = request.get("players").get(currentPlayer).get("bet").asInt();
        int theCall = request.get("current_buy_in").asInt() - bet;

        printIt(request, "Current game_id" + request.get("game_id"));

        int minimumRaise = 1;
        try {
            minimumRaise = request.get("minimum_raise").asInt();
        } catch (Exception e) {
            printIt(request, "An exception occurred accessing minimum raise" + e.getMessage());
        }
        int newRaise =  theCall + minimumRaise;

        printIt(request, "All cards " + allCards);

        String position = getPosition(request);
        printIt(request, "Our position: " + position);

        // Position-based strategy
        if (position.equals(LATE_POSITION)) {
            // In late position, we can be more aggressive
            if (isPotentialFlush(request, allCards)) {
                printIt(request, "Late position with potential flush, raising higher: " + (newRaise * 2));
                return newRaise * 2; // More aggressive raise in late position
            }

            if (is10OrHigher(request, allCards)) {
                printIt(request, "Late position with 10 or higher, calling or raising: " + newRaise);
                return newRaise; // Raise instead of just calling in late position
            }

            if (hasOneOrTwoPairs(request, allCards)) {
                printIt(request, "Late position with pairs, raising aggressively: " + (newRaise * 2));
                return newRaise * 2; // More aggressive with pairs in late position
            }

            // In late position, sometimes play mediocre hands
            if (isMediocreHand(request, allCards)) {
                printIt(request, "Late position with mediocre hand, calling: " + theCall);
                return theCall;
            }
        } else if (position.equals(MIDDLE_POSITION)) {
            // In middle position, standard strategy
            if (isPotentialFlush(request, allCards)) {
                printIt(request, "Is potential flush and raising " + newRaise);
                return newRaise;
            }

            if (is10OrHigher(request, allCards)) {
                printIt(request, "Is 10 or higher " + allCards + " " + theCall);
                return theCall;
            }

            if (hasOneOrTwoPairs(request, allCards)) {
                printIt(request, "Has pairs, should be raising " + allCards + " The raise: " + newRaise);
                return newRaise;
            }
        } else {
            // In early position, be more conservative
            if (isPotentialFlush(request, allCards)) {
                printIt(request, "Early position with potential flush, just calling: " + theCall);
                return theCall; // Just call with flush draws in early position
            }

            if (is10OrHigher(request, allCards) && isHighPair(request, allCards)) {
                printIt(request, "Early position with high pair, raising: " + newRaise);
                return newRaise;
            } else if (is10OrHigher(request, allCards)) {
                printIt(request, "Early position with 10 or higher but no pair, just calling: " + theCall);
                return theCall;
            }

            if (hasOneOrTwoPairs(request, allCards) && isHighPair(request, allCards)) {
                printIt(request, "Early position with high pair, should be raising: " + newRaise);
                return newRaise;
            }
        }


        // fold, be more specific
        System.out.println("We are folding");
        return 0;
    }

    public static void showdown(JsonNode game) {
    }

    public static JsonNode getPlayerByName(JsonNode request, JsonNode playersNode) {
        if (!playersNode.isArray()) {
            printIt(request, "Returned null from getPlayerByName");
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


    public static boolean hasOneOrTwoPairs(JsonNode request, JsonNode allCards) {
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
        printIt(request, "All cards " + allCards.toPrettyString() + " Has pairs: " + moreThanZeroPairs);
        return moreThanZeroPairs;
    }

    public static boolean is10OrHigher(JsonNode request, JsonNode allCards) {
        Map<String, Integer> hasHighRank = new HashMap<>();

        if (allCards.isArray()) {
            for (JsonNode cardNode : allCards) {
                if (cardNode.has("rank") && isHighRank(cardNode)) {
                    String rank = cardNode.get("rank").asText();
                    printIt(request, "RAnk in check 10 or higher: " + rank);
                    hasHighRank.put(rank, hasHighRank.getOrDefault(rank, 0) + 1);
                } else {
                    printIt(request, "Rank is not 10 or higher");
                }
            }
        }

        boolean notEmpty = !hasHighRank.isEmpty();
        printIt(request, "All cards " + allCards.toPrettyString() + " Has high rank count: " + notEmpty);
        return notEmpty;
    }

    public static boolean isPotentialFlush(JsonNode request, JsonNode allCards) {
        boolean hasAllSameSuit = true; // Start with true assumption

        if (allCards.isArray() && !allCards.isEmpty()) {
            String suit = allCards.get(0).get("suit").asText();
            printIt(request, "Checking if all cards match suit: " + suit);

            for (JsonNode cardNode : allCards) {
                String currentSuit = cardNode.get("suit").asText();
                if (!suit.equals(currentSuit)) {
                    printIt(request, "Found different suit: " + currentSuit);
                    hasAllSameSuit = false;
                    break; // Exit the loop as soon as we find a different suit
                }
            }
        } else {
            // No cards or not an array
            hasAllSameSuit = false;
        }

        printIt(request, "All cards " + allCards.toPrettyString() + " Has all same suit: " + hasAllSameSuit);
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

    public static String getPosition(JsonNode request) {
        int dealerPosition = request.get("dealer").asInt();
        int currentPlayer = request.get("in_action").asInt();
        int totalPlayers = request.get("players").size();

        // Calculate relative position
        int relativePosition = (currentPlayer - dealerPosition + totalPlayers) % totalPlayers;

        // Determine position type
        if (relativePosition <= totalPlayers / 3) {
            return EARLY_POSITION;
        } else if (relativePosition <= 2 * totalPlayers / 3) {
            return MIDDLE_POSITION;
        } else {
            return LATE_POSITION;
        }
    }

    public static boolean isHighPair(JsonNode request, JsonNode allCards) {
        Map<String, Integer> rankCounts = new HashMap<>();

        if (allCards.isArray()) {
            for (JsonNode cardNode : allCards) {
                if (cardNode.has("rank")) {
                    String rank = cardNode.get("rank").asText();
                    rankCounts.put(rank, rankCounts.getOrDefault(rank, 0) + 1);
                }
            }
        }

        // Check for pairs of 10 or higher
        for (Map.Entry<String, Integer> entry : rankCounts.entrySet()) {
            if (entry.getValue() >= 2) { // It's a pair
                String rank = entry.getKey();
                if (rank.equals("10") || rank.equals("J") || rank.equals("Q") || rank.equals("K") || rank.equals("A")) {
                    printIt(request, "Found high pair: " + rank);
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isMediocreHand(JsonNode request, JsonNode allCards) {
        // Check for connected cards like 8-9, 7-8, etc.
        if (allCards.isArray() && allCards.size() >= 2) {
            int card1Value = getCardValue(allCards.get(0).get("rank").asText());
            int card2Value = getCardValue(allCards.get(1).get("rank").asText());

            // Connected cards or one-gap cards
            if (Math.abs(card1Value - card2Value) <= 2) {
                printIt(request, "Found connected cards: " + card1Value + " and " + card2Value);
                return true;
            }

            // Same suit (potential flush)
            if (allCards.get(0).get("suit").asText().equals(allCards.get(1).get("suit").asText())) {
                printIt(request, "Found suited cards");
                return true;
            }
        }

        return false;
    }

    private static int getCardValue(String rank) {
        switch (rank) {
            case "A": return 14;
            case "K": return 13;
            case "Q": return 12;
            case "J": return 11;
            default:
                try {
                    return Integer.parseInt(rank);
                } catch (NumberFormatException e) {
                    return 0;
                }
        }
    }
}
