package com.example.gamerules.game;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({"reasoning", "answer", "sourcePages", "foundInRules"})
public class RuleAnswer {
    private String reasoning;
    private String answer;
    private List<Integer> sourcePages;
    private boolean foundInRules;
}
