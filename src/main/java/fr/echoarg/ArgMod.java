package fr.echoarg;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Random;
import java.util.UUID;

public class ArgMod implements ModInitializer {
    public static final String MOD_ID = "echoarg";
    private static final Random R = new Random();

    @Override
    public void onInitialize() {
        ModItems.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> ArgCommands.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(ArgMod::tick);
    }

    static void tick(MinecraftServer server) {
        ArgFx.runTasks();
        if (!ArgState.active) return;
        int t = server.getTicks();
        if (t % 20 != 0) return;

        ServerPlayerEntity e = server.getPlayerManager().getPlayer(ArgState.investigator);
        ServerPlayerEntity m = server.getPlayerManager().getPlayer(ArgState.mimic);
        if (m != null && ArgState.mimicMode)
            m.sendMessage(Text.literal("§4[MIMIC] §7/arg mimic say|sound|behind — ou clic droit avec le Masque"), true);
        if (e == null) return;

        UUID id = e.getUuid();
        if (t % 100 == 0) {
            ServerWorld w = e.getServerWorld();
            int d = 0;
            if (w.getLightLevel(e.getBlockPos()) < 5) d -= 2;
            if (w.isNight()) d -= 1;
            if (ArgState.mimicMode && m != null && m.getServerWorld() == w && m.squaredDistanceTo(e) < 400) d -= 3;
            if (d == 0) d = 2;
            ArgState.setSanity(id, ArgState.sanityOf(id) + d);
            randomEvent(e, ArgState.sanityOf(id));
        }
        e.sendMessage(bar(ArgState.sanityOf(id)), true);
    }

    static void randomEvent(ServerPlayerEntity e, int s) {
        if (s < 10 && R.nextInt(2) == 0) ArgFx.event("blackout", e);
        else if (s < 25 && R.nextInt(5) < 2) ArgFx.event("glitch", e);
        else if (s < 40 && R.nextInt(3) == 0) ArgFx.event("whisper", e);
        else if (s < 60 && R.nextInt(3) == 0) {
            String[] pool = {"steps", "door", "knock", "breath"};
            ArgFx.event(pool[R.nextInt(pool.length)], e);
        }
    }

    static Text bar(int s) {
        int full = s / 10;
        String color = s > 60 ? "§a" : s > 30 ? "§e" : "§c";
        return Text.literal("§7Santé mentale : " + color + "▮".repeat(full) + "§8" + "▮".repeat(10 - full) + " §7" + s + "%");
    }
}
