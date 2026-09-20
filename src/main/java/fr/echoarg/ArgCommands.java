package fr.echoarg;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ArgCommands {
    static final Predicate<ServerCommandSource> OP = s -> s.hasPermissionLevel(2);
    static final Predicate<ServerCommandSource> MIM = s -> s.hasPermissionLevel(2)
            || (s.getEntity() instanceof ServerPlayerEntity p && p.getName().getString().equals(ArgState.mimic));

    static void msg(CommandContext<ServerCommandSource> c, String t) {
        c.getSource().sendFeedback(() -> Text.literal(t), false);
    }

    static ServerPlayerEntity player(CommandContext<ServerCommandSource> c, String name) {
        return c.getSource().getServer().getPlayerManager().getPlayer(name);
    }

    static int withInv(CommandContext<ServerCommandSource> c, Consumer<ServerPlayerEntity> f) {
        ServerPlayerEntity e = player(c, ArgState.investigator);
        if (e == null) { msg(c, "§c" + ArgState.investigator + " n'est pas connecté."); return 0; }
        f.accept(e);
        return 1;
    }

    public static void register(CommandDispatcher<ServerCommandSource> d) {
        var root = literal("arg");

        root.then(literal("start").requires(OP).executes(c -> {
            ArgState.active = true; ArgState.mimicMode = false; ArgState.chapter = 1; ArgState.sanity.clear();
            ArgFx.showChapter(c.getSource().getServer(), 1);
            msg(c, "§aARG lancé. Enquêteur : " + ArgState.investigator + " / Mimic : " + ArgState.mimic);
            return 1;
        }));
        root.then(literal("stop").requires(OP).executes(c -> {
            ArgState.active = false; ArgState.mimicMode = false; msg(c, "§cARG arrêté."); return 1;
        }));
        root.then(literal("status").requires(OP).executes(c -> {
            ServerPlayerEntity e = player(c, ArgState.investigator);
            msg(c, "§7Actif: " + ArgState.active + " | Mimic mode: " + ArgState.mimicMode + " | Chapitre: " + ArgState.chapter
                    + " | Santé mentale: " + (e == null ? "?" : ArgState.sanityOf(e.getUuid())));
            return 1;
        }));
        root.then(literal("chapter").requires(OP).then(argument("n", IntegerArgumentType.integer(1, 5)).executes(c -> {
            int n = IntegerArgumentType.getInteger(c, "n");
            ArgState.chapter = n;
            ArgFx.showChapter(c.getSource().getServer(), n);
            return 1;
        })));
        root.then(literal("event").requires(OP).then(argument("type", StringArgumentType.word())
                .suggests((c, b) -> CommandSource.suggestMatching(ArgFx.EVENTS, b))
                .executes(c -> {
                    String t = StringArgumentType.getString(c, "type");
                    if (!ArgFx.EVENTS.contains(t)) { msg(c, "§cÉvénement inconnu."); return 0; }
                    return withInv(c, e -> ArgFx.event(t, e));
                })));
        root.then(literal("whisper").requires(OP).then(argument("text", StringArgumentType.greedyString()).executes(c ->
                withInv(c, e -> ArgFx.whisper(e, StringArgumentType.getString(c, "text"))))));
        root.then(literal("sanity").requires(OP).then(argument("n", IntegerArgumentType.integer(0, 100)).executes(c ->
                withInv(c, e -> ArgState.setSanity(e.getUuid(), IntegerArgumentType.getInteger(c, "n"))))));
        root.then(literal("give").requires(OP).executes(c -> {
            if (c.getSource().getEntity() instanceof ServerPlayerEntity p) {
                p.giveItemStack(new ItemStack(ModItems.JOURNAL));
                p.giveItemStack(new ItemStack(ModItems.MIMIC_MASK));
                return 1;
            }
            return 0;
        }));
        root.then(literal("names").requires(OP)
                .then(literal("investigator").then(argument("name", StringArgumentType.word()).executes(c -> {
                    ArgState.investigator = StringArgumentType.getString(c, "name"); msg(c, "§aEnquêteur = " + ArgState.investigator); return 1; })))
                .then(literal("mimic").then(argument("name", StringArgumentType.word()).executes(c -> {
                    ArgState.mimic = StringArgumentType.getString(c, "name"); msg(c, "§aMimic = " + ArgState.mimic); return 1; }))));

        var mimic = literal("mimic").requires(MIM);
        mimic.then(literal("on").executes(c -> {
            ArgState.mimicMode = true;
            ServerPlayerEntity m = player(c, ArgState.mimic);
            if (m != null) ArgFx.title(m, "§4Tu es le MIMIC", "§7Reste caché. Imite. Rapproche-toi.", 10, 60, 20);
            return 1;
        }));
        mimic.then(literal("off").executes(c -> { ArgState.mimicMode = false; msg(c, "§7Mimic mode désactivé."); return 1; }));
        mimic.then(literal("say").then(argument("text", StringArgumentType.greedyString()).executes(c -> {
            String t = StringArgumentType.getString(c, "text");
            c.getSource().getServer().getPlayerManager().broadcast(Text.literal("<" + ArgState.investigator + "> " + t), false);
            return 1;
        })));
        mimic.then(literal("sound").then(argument("type", StringArgumentType.word())
                .suggests((c, b) -> CommandSource.suggestMatching(ArgFx.SOUNDS, b))
                .executes(c -> {
                    String t = StringArgumentType.getString(c, "type");
                    if (!ArgFx.SOUNDS.contains(t)) { msg(c, "§cSons : " + ArgFx.SOUNDS); return 0; }
                    return withInv(c, e -> {
                        ArgFx.event(t, e);
                        ArgState.setSanity(e.getUuid(), ArgState.sanityOf(e.getUuid()) - 3);
                    });
                })));
        mimic.then(literal("behind").executes(c -> {
            if (!(c.getSource().getEntity() instanceof ServerPlayerEntity m)) return 0;
            return withInv(c, e -> {
                Vec3d p = ArgFx.behind(e, 3);
                m.requestTeleport(p.x, e.getY(), p.z);
                ArgState.setSanity(e.getUuid(), ArgState.sanityOf(e.getUuid()) - 8);
            });
        }));
        root.then(mimic);
        d.register(root);
    }
}
