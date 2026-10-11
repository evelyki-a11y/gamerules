package com.example.gamerules.game;

import lombok.Data;

import java.util.List;

@Data
public class RuleAnswer {
    private String answer;
    private List<Integer> sourcePages;
    private boolean foundInRules;
}
