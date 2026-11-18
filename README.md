# GameRules – lauamängureeglite assistent

REST API, mis vastab küsimustele lauamängude reeglite kohta. Vastused põhinevad mängude reeglitel (PDF) ja need koostab Google Gemini mudel Spring AI kaudu.

Iseseisev õppeprojekt, Vali IT! (BCS Koolitus), 2026.

*Mängud:* Root, Spirit Island

## Kuidas see töötab
1. Kasutaja saadab küsimuse REST API kaudu, nt Postmanist.
2. Rakendus loeb valitud mängu reeglid PDF-failist.
3. Süsteemiprompt ja few-shot näited suunavad mudelit vastama ainult reeglite põhjal.
4. Google Gemini koostab vastuse JSON-vormingus.
5. Kui küsimus ei puuduta mängu reegleid, keeldub mudel vastamast.

## Tehnoloogiad
- Java, Spring Boot
- Spring AI (Google GenAI, PDF Document Reader)
- Google Gemini
- Gradle
- Postman (testimine)

## Testimine
API-t testisin Postmanis:
- kehtivad küsimused mõlema mängu reeglite kohta
- negatiivsed juhud: reegliväline küsimus, olematu mäng

## Märkus
Mängude reeglid kuuluvad mängude kirjastajatele ja on projektis kasutusel ainult õppe eesmärgil.
