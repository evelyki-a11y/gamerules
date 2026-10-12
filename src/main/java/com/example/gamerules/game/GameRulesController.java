package com.example.gamerules.game;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameRulesController {
    private final ChatClient chat;
    private final RulebookService rulebookService;

    public GameRulesController(ChatClient.Builder builder, RulebookService rulebookService) {
        this.chat = builder
                .defaultAdvisors(
                        new RegexJailbreakAdvisor()
                )
                .build();
        this.rulebookService = rulebookService;
    }

    @PostMapping("/games/{game}/ask")
    public String ask(@RequestBody String question, @PathVariable Game game) {
        String rules = rulebookService.loadRulebook(game);
        ResponseEntity<ChatResponse, RuleAnswer> response = chat.prompt()
                .system("""
                          You are a board game rules assistant.
                        Answer ONLY using the rulebook text in the rulebook below. Never use your own knowledge.
                          Before answering, think step by step in the "reasoning" field: find the relevant pages, write what the rule says, then draw a conclusion.
                          In "sourcePages", list the page numbers ([Page X]) where you found the answer.
                        If the answer is not in the rulebook, set "foundInRules" to false and answer: "I could not find this in the rulebook." Do not guess
                          If the question is not about the game rules, set "foundInRules" to false and answer: "I can only answer questions about the game rules."
                          Answer in the same language as the question.
                          Example:
                          Question: How many cards does each player draw at the start of the game?
                          Response: {
                          "reasoning": "The question is about the starting hand. Page 3 says: 'Each player draws 5 cards.' Page 4 adds that the first player draws 1 extra card. So the answer is 5 cards, and 6 for the first player.",
                          "answer": "Each player draws 5 cards. The first player draws 6 cards.",
                          "sourcePages": [3, 4],
                          "foundInRules": true
                          }
                          Example:
                                                  Question: How much does this game cost?
                                                  Response: {
                                                  "reasoning": "The question is about the price. Price information is not mentioned in the rules. So the answer is not in the rulebook.",
                                                  "answer": "I could not find this in the rulebook.",
                                                  "sourcePages": [],
                                                  "foundInRules": false
                                                  }
                        
                        """ + "\n\nRulebook:\n" + rules)
                .user(question)
                .call()
                .responseEntity(RuleAnswer.class, spec -> spec.useProviderStructuredOutput().validateSchema());

        String answer = response.getResponse().getResult().getOutput().getText();
        if (answer.length() > 4000 || answer.contains("You are a board game rules assistant")) {
            return "Answer was blocked by a safety check";
        }
        return answer;
    }
}
