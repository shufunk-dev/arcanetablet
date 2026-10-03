package net.arcanetablet;

import net.arcanetablet.event.ArcaneForgeEvents;
import net.arcanetablet.item.ModItems;
import net.arcanetablet.network.ModMessages;
import net.arcanetablet.sound.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ArcaneTabletMod.MOD_ID)
public class ArcaneTabletMod {
    public static final String MOD_ID = "arcanetablet";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ArcaneTabletMod(FMLJavaModLoadingContext context) {
        LOGGER.info("Initializing Arcane Tablet Mod (Forge 1.21.11 / 61.2.0)!");
        BusGroup modBusGroup = context.getModBusGroup();

        ModSounds.register(modBusGroup);
        ModItems.register(modBusGroup);

        ModMessages.registerPackets();
        MinecraftForge.EVENT_BUS.register(ArcaneForgeEvents.class);
    }
}
