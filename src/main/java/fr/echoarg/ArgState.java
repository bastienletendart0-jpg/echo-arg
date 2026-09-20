package fr.echoarg;

import java.util.*;

public class ArgState {
    public static boolean active = false, mimicMode = false;
    public static String investigator = "Edwin512", mimic = "The_Mark579";
    public static int chapter = 1;
    public static final Map<UUID, Integer> sanity = new HashMap<>();

    public static int sanityOf(UUID id) { return sanity.getOrDefault(id, 100); }
    public static void setSanity(UUID id, int v) { sanity.put(id, Math.max(0, Math.min(100, v))); }

    public static final String[] TITLES = {"Le Village Silencieux", "Écho", "Le Double", "Derrière Toi", "Le Mimic"};
    public static final String[] CLUES = {
    "§7Jour 1. Le village est trop calme. Quelqu'un copie mes pas... — E.",
    "§7Page 2 (déchirée) : §eLO PH UHJDUGH §8[Indice : un empereur romain]",
    "§7Page 3 : §b00110101 00110111 00111001 §8[Indice : 8 bits par caractère]",
    "§7Page 4 : §aUkVHQVJERSBERVJSSUVSRSBUT0k= §8[Indice : 64]",
    "§7Page 5 : §c..-. ..- .. ... §8/ §fXIOV AM A LI §8[Indice : morse + miroir]"
    };
}
