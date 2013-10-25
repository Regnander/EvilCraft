package evilcraft;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import evilcraft.api.BucketHandler;
import evilcraft.api.LoggerHelper;
import evilcraft.api.config.ConfigHandler;
import evilcraft.api.config.ExtendedConfig;
import evilcraft.blocks.BloodStainedDirtConfig;
import evilcraft.blocks.DarkBlockConfig;
import evilcraft.blocks.DarkOreConfig;
import evilcraft.blocks.DarkOreGlowingConfig;
import evilcraft.blocks.EvilBlockConfig;
import evilcraft.blocks.LightningBombConfig;
import evilcraft.blocks.LiquidBlockBloodConfig;
import evilcraft.entities.item.EntityLightningGrenadeConfig;
import evilcraft.entities.monster.WerewolfConfig;
import evilcraft.events.LivingDeathEventHook;
import evilcraft.events.TextureStitchEventHook;
import evilcraft.items.BloodExtractorConfig;
import evilcraft.items.BucketBloodConfig;
import evilcraft.items.DarkGemConfig;
import evilcraft.items.DarkStickConfig;
import evilcraft.items.LightningGrenadeConfig;
import evilcraft.items.WerewolfBoneConfig;
import evilcraft.items.WerewolfFleshConfig;
import evilcraft.liquids.BloodConfig;
import evilcraft.proxies.CommonProxy;
import evilcraft.worldgen.EvilWorldGenerator;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class EvilCraft {
    
    @SidedProxy(clientSide = "evilcraft.proxies.ClientProxy", serverSide = "evilcraft.proxies.CommonProxy")
    public static CommonProxy proxy;
    
    public static EvilCraft _instance;
    
    private static Set<ExtendedConfig> configs = ConfigHandler.getInstance(); // Order is necessary for some registrations
    static {
        // Fluids
        configs.add(new BloodConfig());
        
        // Blocks
        configs.add(new EvilBlockConfig());
        configs.add(new LiquidBlockBloodConfig());
        configs.add(new DarkOreConfig());
        configs.add(new DarkOreGlowingConfig());
        configs.add(new DarkBlockConfig());
        configs.add(new BloodStainedDirtConfig());
        configs.add(new LightningBombConfig());
        
        // Items
        configs.add(new WerewolfBoneConfig());
        configs.add(new WerewolfFleshConfig());
        configs.add(new LightningGrenadeConfig());
        configs.add(new BucketBloodConfig());
        configs.add(new BloodExtractorConfig());
        configs.add(new DarkGemConfig());
        configs.add(new DarkStickConfig());
        
        // Entities
        //Item
        configs.add(new EntityLightningGrenadeConfig());
        // Monster
        configs.add(new WerewolfConfig()); // http://www.minecraftwiki.net/wiki/Resource_pack#pack.mcmeta
    }
    public static Map<Class<? extends Entity>, Render> renderers = new HashMap<Class<? extends Entity>, Render>();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LoggerHelper.init();
        LoggerHelper.log(Level.INFO, "preInit()");
        
        // Save this instance, so we can use it later
        this._instance = this;
        
        // Run the ConfigHandler to make/read the config and fill in the game registry
        ConfigHandler.getInstance().handle(event);
    }
    
    @EventHandler
    public void init(FMLPreInitializationEvent event) {
        LoggerHelper.log(Level.INFO, "init()");
        
        GameRegistry.registerWorldGenerator(new EvilWorldGenerator());
        proxy.registerRenderers();
    }
    
    @EventHandler
    public void postInit(FMLPreInitializationEvent event) {
        LoggerHelper.log(Level.INFO, "postInit()");
        
        Recipes.registerRecipes();
        
        MinecraftForge.EVENT_BUS.register(BucketHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(new LivingDeathEventHook());
        MinecraftForge.EVENT_BUS.register(new TextureStitchEventHook());
        
    }
    
    public static void log(String message) {
        log(message, Level.INFO);
    }
    
    public static void log(String message, Level level) {
        LoggerHelper.log(level, message);
    }
}
