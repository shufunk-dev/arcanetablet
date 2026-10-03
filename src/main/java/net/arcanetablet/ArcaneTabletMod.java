package net.arcanetablet;

import net.arcanetablet.item.ModItems;
import net.arcanetablet.network.ModMessages;
import net.arcanetablet.sound.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ArcaneTabletMod.MOD_ID)
public class ArcaneTabletMod {
    public static final String MOD_ID = "arcanetablet";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ArcaneTabletMod(IEventBus modEventBus) {
        LOGGER.info("Initializing Arcane Tablet Mod (NeoForge 1.21.11 / 21.11.45)!");

        ModSounds.register(modEventBus);
        ModItems.register(modEventBus);

        modEventBus.addListener(ModMessages::registerPayloadHandlers);
    }
}
