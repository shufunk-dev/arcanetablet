package net.arcanetablet;

import net.arcanetablet.item.ModItems;
import net.arcanetablet.network.ModMessages;
import net.arcanetablet.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcaneTabletMod implements ModInitializer {
    public static final String MOD_ID = "arcanetablet";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Arcane Tablet Mod!");
        ModSounds.registerModSounds();
        ModItems.registerModItems();
        ModMessages.registerPackets();
    }
}
