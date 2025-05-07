package org.leanpoker.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerTest {

    @Test
    @DisplayName("Bot should fold with 5♥ 9♠")
    public void testFiveOfHeartsNineOfSpades() throws IOException {
        // Test scenario: Player has 5♥ and 9♠, no community cards
        String jsonInput = "{\n" +
                "  \"players\":[\n" +
                "    {\n" +
                "      \"name\":\"Player 1\",\n" +
                "      \"stack\":1000,\n" +
                "      \"status\":\"active\",\n" +
                "      \"bet\":0,\n" +
                "      \"hole_cards\":[],\n" +
                "      \"version\":\"Version name 1\",\n" +
                "      \"id\":0\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\":\"Drop Database\",\n" +
                "      \"stack\":1000,\n" +
                "      \"status\":\"active\",\n" +
                "      \"bet\":0,\n" +
                "      \"hole_cards\":[\n" +
                "        {\"rank\": \"5\", \"suit\": \"hearts\"},\n" +
                "        {\"rank\": \"9\", \"suit\": \"spades\"}\n" +
                "      ],\n" +
                "      \"version\":\"Version name 2\",\n" +
                "      \"id\":1\n" +
                "    }\n" +
                "  ],\n" +
                "  \"tournament_id\":\"550d1d68cd7bd10003000003\",\n" +
                "  \"game_id\":\"550da1cb2d909006e90004b1\",\n" +
                "  \"round\":0,\n" +
                "  \"bet_index\":0,\n" +
                "  \"small_blind\":10,\n" +
                "  \"in_action\":1,\n" +
                "  \"orbits\":0,\n" +
                "  \"dealer\":0,\n" +
                "  \"community_cards\":[],\n" +
                "  \"current_buy_in\":20,\n" +
                "  \"minimum_raise\":10,\n" +
                "  \"pot\":30\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode request = mapper.readTree(jsonInput);

        // Execute the bot's decision
        int action = Player.betRequest(request);
        System.out.println("Bot decision: " + (action == 0 ? "FOLD" : action == 20 ? "CALL" : "RAISE " + action));

        // Based on your original algorithm:
        // - 5♥ 9♠ are not 10 or higher cards
        // - They're not the same suit, so not a potential flush
        // - They're not a pair
        // Therefore, we expect the bot to fold (return 0)
        assertEquals(0, action, "Bot should fold with 5♥ 9♠");
    }
}