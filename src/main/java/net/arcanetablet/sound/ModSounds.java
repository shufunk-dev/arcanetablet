package net.arcanetablet.sound;

import net.arcanetablet.ArcaneTabletMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent TABLET_OPEN = registerSound("item.arcane_tablet.open");
    public static final SoundEvent TABLET_EXECUTE = registerSound("item.arcane_tablet.execute");
    public static final SoundEvent TABLET_WARP = registerSound("item.arcane_tablet.warp");
    public static final SoundEvent TABLET_SCAN = registerSound("item.arcane_tablet.scan");
    public static final SoundEvent TABLET_DENIED = registerSound("item.arcane_tablet.denied");
    public static final SoundEvent TABLET_BANK = registerSound("item.arcane_tablet.bank");

    private static SoundEvent registerSound(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(ArcaneTabletMod.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerModSounds() {
        ArcaneTabletMod.LOGGER.info("Registering Custom Sounds for " + ArcaneTabletMod.MOD_ID);
    }
}
