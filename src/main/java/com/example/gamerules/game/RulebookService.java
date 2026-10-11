package com.example.gamerules.game;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RulebookService {
    private final Map<Game, String> cache = new HashMap<>();
    public String loadRulebook(Game game) {
        if (cache.containsKey(game)) {
            return cache.get(game);
        }
        PagePdfDocumentReader reader = new PagePdfDocumentReader("classpath:rulebooks/" + game.getFileName());
        List<Document> pages = reader.get();

        String text = "";
        int pageNumber = 1;
        for (Document page : pages) {
            text = text + "[Page " + pageNumber + "]\n" + page.getText() + "\n\n";
            pageNumber = pageNumber + 1;
        }
        cache.put(game, text);
        return text;
    }
}
