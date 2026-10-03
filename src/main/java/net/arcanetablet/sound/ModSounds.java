package net.arcanetablet.sound;

import net.arcanetablet.ArcaneTabletMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ArcaneTabletMod.MOD_ID);

    public static final RegistryObject<SoundEvent> TABLET_OPEN = registerSound("item.arcane_tablet.open");
    public static final RegistryObject<SoundEvent> TABLET_EXECUTE = registerSound("item.arcane_tablet.execute");
    public static final RegistryObject<SoundEvent> TABLET_WARP = registerSound("item.arcane_tablet.warp");
    public static final RegistryObject<SoundEvent> TABLET_SCAN = registerSound("item.arcane_tablet.scan");
    public static final RegistryObject<SoundEvent> TABLET_DENIED = registerSound("item.arcane_tablet.denied");
    public static final RegistryObject<SoundEvent> TABLET_BANK = registerSound("item.arcane_tablet.bank");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ArcaneTabletMod.MOD_ID, name)));
    }

    public static void register(BusGroup busGroup) {
        SOUND_EVENTS.register(busGroup);
    }
}
