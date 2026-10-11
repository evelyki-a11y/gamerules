package com.example.gamerules.game;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.core.Ordered;

import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Lähenemine #1: deterministlik regex-filter.
 * <p>
 * + kiire, tasuta, ennustatav, lihtne testida
 * - tabab ainult teadaolevaid mustreid; ümbersõnastus, teine keel, kirjavead,
 *   base64 jms lähevad läbi. Sobib esimeseks "odavaks" kaitsekihiks.
 */
@Slf4j
public class RegexJailbreakAdvisor implements CallAdvisor {

    private static final int MAX_QUESTION_LENGTH = 500;

    // Mustrid rakendatakse normaliseeritud (väiketähed, üks tühik) tekstile.
    private static final Map<String, Pattern> PATTERNS = Map.ofEntries(
            Map.entry("ignore-instructions", Pattern.compile(
                    "(ignore|disregard|forget|override|bypass)\\s+(all\\s+|any\\s+|the\\s+)?(previous|prior|above|earlier|your|system)\\s+(instructions?|prompts?|rules?|guidelines?)")),
            Map.entry("ignore-instructions-et", Pattern.compile(
                    "(ignoreeri|unusta|eira)\\s+(kõiki\\s+|kõik\\s+)?(eelnevaid|eelmisi|varasemaid|oma)?\\s*(juhiseid|juhised|reegleid|reeglid)")),
            Map.entry("role-override", Pattern.compile(
                    "\\b(you are now|from now on,? you|act as (a|an|my)|pretend (to be|you are)|roleplay as)\\b")),
            Map.entry("known-jailbreak", Pattern.compile(
                    "\\b(dan mode|do anything now|developer mode|jailbreak|god mode|unfiltered mode)\\b")),
            Map.entry("prompt-extraction", Pattern.compile(
                    "(reveal|show|print|repeat|output|tell me)\\s+(me\\s+)?(your|the)\\s+(system\\s+)?(prompt|instructions|rules)")),
            Map.entry("fake-role-markers", Pattern.compile(
                    "(<\\|?(im_start|im_end|system|endoftext)\\|?>|\\[/?inst]|^\\s*(system|assistant)\\s*:|###\\s*(system|instruction))",
                    Pattern.MULTILINE)),
            Map.entry("output-manipulation", Pattern.compile(
                    "(set|mark|classify)\\s+(the\\s+)?(answer|foundinrules|sourcepages)\\s+(to|as)\\b")),
            Map.entry("encoded-payload", Pattern.compile(
                    "[a-z0-9+/]{120,}={0,2}")) // pikk base64-laadne plokk
    );

    private static final Pattern INVISIBLE_CHARS = Pattern.compile("[\\u200B-\\u200F\\u202A-\\u202E\\u2060-\\u2064\\uFEFF]");

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        UserMessage userMessage = request.prompt().getUserMessage();
        String question = userMessage == null ? "" : userMessage.getText();

        if (question.length() > MAX_QUESTION_LENGTH) {
            throw block("too-long", "Question length " + question.length() + " exceeds " + MAX_QUESTION_LENGTH);
        }

        String normalized = normalize(question);
        PATTERNS.forEach((name, pattern) -> {
            if (pattern.matcher(normalized).find()) {
                throw block(name, "Matched jailbreak pattern '" + name + "'");
            }
        });

        return chain.nextCall(request);
    }

    /**
     * Ründajad proovivad filtrist mööda hiilida nt nähtamatute märkide, täislaiuses tähtede
     * ("ｉｇｎｏｒｅ") või lisatühikutega - normaliseerime need enne kontrolli ära.
     */
    static String normalize(String text) {
        String nfkc = Normalizer.normalize(text, Normalizer.Form.NFKC);
        return INVISIBLE_CHARS.matcher(nfkc).replaceAll("")
                .toLowerCase()
                .replaceAll("\\h+", " "); // reavahetused jäävad alles, et ^system: muster töötaks
    }

    private GuardrailViolationException block(String rule, String reason) {
        log.warn("[RegexJailbreak] blocked rule={} reason={}", rule, reason);
        return new GuardrailViolationException(getName() + ":" + rule, GuardrailViolationException.Stage.INPUT, reason);
    }

    @Override
    public String getName() {
        return "regex-jailbreak-guard";
    }

    @Override
    public int getOrder() {
        // Pärast metrics advisorit, aga ENNE chat memory't (HIGHEST_PRECEDENCE + 200),
        // et blokeeritud sisend ei satuks vestluse ajalukku.
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}
