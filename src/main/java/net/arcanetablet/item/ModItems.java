package net.arcanetablet.item;

import net.arcanetablet.ArcaneTabletMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class ModItems {

    public static final RegistryKey<ItemGroup> ARCANE_TABLET_GROUP_KEY =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(ArcaneTabletMod.MOD_ID, "arcanetablet_group"));

    public static final Item ARCANE_TABLET = registerItem("arcane_tablet",
            settings -> new ArcaneTabletItem(settings.maxCount(1).rarity(Rarity.RARE)));

    public static final ItemGroup ARCANE_TABLET_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ARCANE_TABLET))
            .displayName(Text.literal("Arcane Tablet"))
            .entries((displayContext, entries) -> {
                entries.add(ARCANE_TABLET);
            })
            .build();

    private static <T extends Item> T registerItem(String name, Function<Item.Settings, T> itemFactory) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ArcaneTabletMod.MOD_ID, name));
        T item = itemFactory.apply(new Item.Settings().registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void registerModItems() {
        ArcaneTabletMod.LOGGER.info("Registering Mod Items for " + ArcaneTabletMod.MOD_ID);
        Registry.register(Registries.ITEM_GROUP, ARCANE_TABLET_GROUP_KEY, ARCANE_TABLET_GROUP);
    }
}
