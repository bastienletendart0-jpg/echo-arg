package fr.echoarg.items;

import fr.echoarg.ArgFx;
import fr.echoarg.ArgState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MimicMaskItem extends Item {
    public MimicMaskItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack s = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity me) {
            boolean allowed = me.getName().getString().equals(ArgState.mimic) || me.hasPermissionLevel(2);
            if (!allowed) {
                me.sendMessage(Text.literal("§8Le masque refuse de t'obéir."), true);
            } else if (user.isSneaking()) {
                me.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 600, 0, false, false));
                me.sendMessage(Text.literal("§4Camouflage actif (30 s)"), true);
                me.getItemCooldownManager().set(this, 600);
            } else {
                ServerPlayerEntity e = me.getServer().getPlayerManager().getPlayer(ArgState.investigator);
                if (e != null) {
                    String[] pool = {"steps", "door", "knock", "breath"};
                    ArgFx.event(pool[world.random.nextInt(pool.length)], e);
                    ArgState.setSanity(e.getUuid(), ArgState.sanityOf(e.getUuid()) - 5);
                    me.sendMessage(Text.literal("§4Il a entendu."), true);
                }
                me.getItemCooldownManager().set(this, 300);
            }
        }
        return TypedActionResult.success(s, world.isClient());
    }
}
