package fr.echoarg.items;

import fr.echoarg.ArgState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class JournalItem extends Item {
    public JournalItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack s = user.getStackInHand(hand);
        if (!world.isClient) {
            NbtCompound n = s.getOrCreateNbt();
            if (user.isSneaking()) n.putInt("page", 0);
            int page = Math.min(n.getInt("page"), ArgState.CLUES.length - 1);
            user.sendMessage(Text.literal("§6[Journal — page " + (page + 1) + "/" + ArgState.CLUES.length + "]"), false);
            user.sendMessage(Text.literal(ArgState.CLUES[page]), false);
            n.putInt("page", Math.min(page + 1, ArgState.CLUES.length - 1));
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1f, 0.7f);
        }
        return TypedActionResult.success(s, world.isClient());
    }
}
