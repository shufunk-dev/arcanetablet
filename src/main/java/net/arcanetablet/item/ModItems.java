package net.arcanetablet.item;

import net.arcanetablet.ArcaneTabletMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ArcaneTabletMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArcaneTabletMod.MOD_ID);

    public static final RegistryObject<Item> ARCANE_TABLET = ITEMS.register("arcane_tablet",
            () -> new ArcaneTabletItem(new Item.Properties().setId(ITEMS.key("arcane_tablet")).stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<CreativeModeTab> ARCANE_TABLET_TAB = CREATIVE_MODE_TABS.register("arcanetablet_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ARCANE_TABLET.get()))
                    .title(Component.literal("Arcane Tablet"))
                    .displayItems((params, output) -> output.accept(ARCANE_TABLET.get()))
                    .build());

    public static void register(BusGroup busGroup) {
        ITEMS.register(busGroup);
        CREATIVE_MODE_TABS.register(busGroup);
    }
}
