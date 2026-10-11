package net.arcanetablet.item;

import net.arcanetablet.ArcaneTabletMod;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.function.Function;

public class ModItems {

    public static final ResourceKey<CreativeModeTab> ARCANE_TABLET_GROUP_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(ArcaneTabletMod.MOD_ID, "arcanetablet_group"));

    public static final Item ARCANE_TABLET = registerItem("arcane_tablet",
            settings -> new ArcaneTabletItem(settings.stacksTo(1).rarity(Rarity.RARE)));

    public static final CreativeModeTab ARCANE_TABLET_GROUP = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ARCANE_TABLET))
            .title(Component.literal("Arcane Tablet"))
            .displayItems((displayContext, entries) -> {
                entries.accept(ARCANE_TABLET);
            })
            .build();

    private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> itemFactory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ArcaneTabletMod.MOD_ID, name));
        T item = itemFactory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void registerModItems() {
        ArcaneTabletMod.LOGGER.info("Registering Mod Items for " + ArcaneTabletMod.MOD_ID);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ARCANE_TABLET_GROUP_KEY, ARCANE_TABLET_GROUP);
    }
}
