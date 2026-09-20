# Écho ARG — mod Fabric 1.20.1

Rôles : **Edwin512** = enquêteur · **The_Mark579** = le Mimic (modifiable avec `/arg names ...`).

## Compiler le .jar
1. Installe **JDK 17** (Temurin/Adoptium).
2. Dans ce dossier : `./gradlew build` (Windows : `gradlew.bat build`).
3. Le mod est dans `build/libs/echoarg-1.0.0.jar` (pas le `-sources`).
4. Installe **Fabric Loader** pour 1.20.1 + **Fabric API 0.92.2+1.20.1**, mets le jar dans `mods/` (chez les DEUX joueurs).

## Commandes (op) — `/arg ...`
start · stop · status · chapter <1-5> · event <type> · whisper <texte> · sanity <0-100> · give · names investigator|mimic <pseudo>
Événements : steps door knock breath scream heartbeat static glitch blackout whisper fakeleave fakejoin countdown

## Commandes du Mimic (The_Mark579, sans être op)
`/arg mimic on|off` · `/arg mimic say <texte>` (message fake au nom d'Edwin512) · `/arg mimic sound <type>` · `/arg mimic behind` (TP derrière Edwin)

## Items (`/arg give`)
- **Journal Corrompu** : clic droit = page suivante (indices chiffrés : César, binaire, base64, morse+miroir). Sneak+clic = retour page 1.
- **Masque du Mimic** : clic droit = bruit derrière Edwin ; sneak+clic = invisibilité 30 s.

## Santé mentale (Edwin512)
Baisse dans le noir, la nuit, et près du Mimic (mode actif). Remonte de jour à la lumière. Effets : bruits < 60, chuchotements < 40, glitchs < 25, blackout < 10.
Les états sont en mémoire : `/arg start` après chaque redémarrage.
