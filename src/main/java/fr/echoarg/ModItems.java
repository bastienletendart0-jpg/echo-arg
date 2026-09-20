package fr.echoarg;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import fr.echoarg.items.JournalItem;
import fr.echoarg.items.MimicMaskItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item JOURNAL = new JournalItem(new FabricItemSettings().maxCount(1));
    public static final Item MIMIC_MASK = new MimicMaskItem(new FabricItemSettings().maxCount(1));

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier(ArgMod.MOD_ID, "journal"), JOURNAL);
        Registry.register(Registries.ITEM, new Identifier(ArgMod.MOD_ID, "mimic_mask"), MIMIC_MASK);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(c -> { c.add(JOURNAL); c.add(MIMIC_MASK); });
    }
}
