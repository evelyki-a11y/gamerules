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
        this.chat = builder.build();
        this.rulebookService = rulebookService;
    }

    @PostMapping("/games/{game}/ask")
    public String ask(@RequestBody String question, @PathVariable Game game) {
        String rules = rulebookService.loadRulebook(game);
        ResponseEntity<ChatResponse, RuleAnswer> response = chat.prompt()
                .system("""
                        You are a board game rules assistant.
                        Answer ONLY using the rulebook text given by the user. Never use your own knowledge.
                        Before answering, think step by step in the "reasoning" field: find the relevant pages, write what the rule says, then draw a conclusion.
                        In "sourcePages", list the page numbers ([Page X]) where you found the answer.
                        If the answer is not in the rulebook, set "foundInRules" to false and answer: "I could not find this in the rulebook." Do not guess.
                        If the question is not about the game rules, set "foundInRules" to false and answer: "I can only answer questions about the game rules."
                        """)
                        .user("Rulebook:\n" + rules + "\n\nQuestion: " + question)
                        .call()
                .responseEntity(RuleAnswer.class, spec -> spec.useProviderStructuredOutput().validateSchema());

        return response.getResponse().getResult().getOutput().getText();
    }
}
