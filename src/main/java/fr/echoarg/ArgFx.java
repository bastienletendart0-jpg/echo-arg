package fr.echoarg;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class ArgFx {
    static final Random R = new Random();
    public static final List<String> EVENTS = List.of("steps", "door", "knock", "breath", "scream", "heartbeat",
            "static", "glitch", "blackout", "whisper", "fakeleave", "fakejoin", "countdown");
    public static final List<String> SOUNDS = List.of("steps", "door", "knock", "breath", "scream", "heartbeat");
    static final String[] WHISPERS = {"Je t'entends respirer.", "Ne te retourne pas.", "Ce n'est pas moi qui te suis.",
            "Relis le journal.", "Il a ta voix.", "Tu n'es pas seul dans ce monde.", "Pourquoi tu cours ?", "J'apprends vite."};

    private record Task(int[] delay, Runnable run) {}
    private static final List<Task> TASKS = new ArrayList<>();
    public static void later(int ticks, Runnable r) { TASKS.add(new Task(new int[]{ticks}, r)); }
    public static void runTasks() {
        if (TASKS.isEmpty()) return;
        for (Task t : new ArrayList<>(TASKS)) {
            if (--t.delay()[0] <= 0) { TASKS.remove(t); t.run().run(); }
        }
    }

    public static String zalgo(String s) {
        String up = "\u0300\u0301\u0302\u0303\u0306\u0308\u030A\u0312\u033D\u0346";
        String dn = "\u0316\u0317\u0318\u0319\u031C\u0323\u0324\u0325\u0329\u032D";
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            b.append(c);
            if (c != ' ') {
                for (int i = 0; i < 1 + R.nextInt(3); i++) b.append(up.charAt(R.nextInt(up.length())));
                b.append(dn.charAt(R.nextInt(dn.length())));
            }
        }
        return b.toString();
    }

    public static Vec3d behind(ServerPlayerEntity p, double d) {
        Vec3d l = p.getRotationVec(1f);
        Vec3d h = new Vec3d(-l.x, 0, -l.z);
        if (h.lengthSquared() < 1e-4) h = new Vec3d(0, 0, 1);
        return p.getPos().add(h.normalize().multiply(d));
    }

    static void play(ServerPlayerEntity e, Vec3d p, SoundEvent s, float vol, float pitch) {
        e.getServerWorld().playSound(null, p.x, p.y, p.z, s, SoundCategory.HOSTILE, vol, pitch);
    }

    public static void title(ServerPlayerEntity p, String t, String sub, int in, int stay, int out) {
        p.networkHandler.sendPacket(new TitleFadeS2CPacket(in, stay, out));
        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(sub)));
        p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(t)));
    }

    public static void whisper(ServerPlayerEntity e, String text) {
        e.sendMessage(Text.literal(zalgo(text)).formatted(Formatting.DARK_GRAY, Formatting.ITALIC), false);
    }

    public static void showChapter(MinecraftServer srv, int n) {
        for (ServerPlayerEntity p : srv.getPlayerManager().getPlayerList()) {
            title(p, "§6Chapitre " + n, "§7" + ArgState.TITLES[n - 1], 10, 60, 20);
            p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE,
                    SoundCategory.MASTER, 0.6f, 0.7f);
        }
    }

    public static void event(String type, ServerPlayerEntity e) {
        MinecraftServer srv = e.getServer();
        switch (type) {
            case "steps" -> {
                for (int i = 0; i < 6; i++) {
                    int k = i;
                    later(i * 10, () -> play(e, behind(e, 8 - k), SoundEvents.BLOCK_STONE_STEP, 1f, 0.8f));
                }
            }
            case "door" -> {
                play(e, behind(e, 5), SoundEvents.BLOCK_WOODEN_DOOR_OPEN, 1f, 0.8f);
                later(30, () -> play(e, behind(e, 5), SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, 1f, 0.8f));
            }
            case "knock" -> {
                for (int i = 0; i < 3; i++)
                    later(i * 12, () -> play(e, behind(e, 3), SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.8f, 1f));
            }
            case "breath" -> play(e, behind(e, 1.5), SoundEvents.ENTITY_PLAYER_BREATH, 1f, 0.6f);
            case "scream" -> {
                play(e, behind(e, 2), SoundEvents.ENTITY_GHAST_SCREAM, 1f, 0.5f);
                play(e, behind(e, 2), SoundEvents.ENTITY_ENDERMAN_SCREAM, 1f, 0.6f);
            }
            case "heartbeat" -> {
                for (int i = 0; i < 6; i++)
                    later(i * 15, () -> play(e, e.getPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1f, 1f));
            }
            case "static" -> {
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, false, false));
                play(e, e.getPos(), SoundEvents.BLOCK_PORTAL_AMBIENT, 0.8f, 0.5f);
            }
            case "glitch" -> {
                e.sendMessage(Text.literal("§c" + zalgo("ERREUR : " + ArgState.investigator + " n'est pas seul")), false);
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0, false, false));
            }
            case "blackout" -> {
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, false, false));
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 200, 0, false, false));
                event("heartbeat", e);
                title(e, "§4IL EST DERRIÈRE TOI", "", 5, 50, 20);
            }
            case "whisper" -> whisper(e, WHISPERS[R.nextInt(WHISPERS.length)]);
            case "fakeleave" -> {
                srv.getPlayerManager().broadcast(Text.translatable("multiplayer.player.left", ArgState.mimic).formatted(Formatting.YELLOW), false);
                later(100, () -> srv.getPlayerManager().broadcast(Text.translatable("multiplayer.player.joined", ArgState.mimic).formatted(Formatting.YELLOW), false));
            }
            case "fakejoin" -> srv.getPlayerManager().broadcast(Text.translatable("multiplayer.player.joined", ArgState.mimic).formatted(Formatting.YELLOW), false);
            case "countdown" -> {
                for (int i = 0; i < 3; i++) {
                    int n = 3 - i;
                    later(i * 20, () -> title(e, "§c" + n, "", 0, 20, 0));
                }
                later(60, () -> event("scream", e));
            }
            default -> {}
        }
    }
}
