package com.example.gamerules.game;

import lombok.Getter;

@Getter
public enum Game {
    ROOT("Root", "root.pdf"),
    SPIRIT_ISLAND("Spirit Island", "spirit-island.pdf");

    private final String displayName,fileName;

    Game(String displayName, String fileName) {
        this.displayName = displayName;
        this.fileName = fileName;
    }
}

