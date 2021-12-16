package commoble.structure_spawn_loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.world.StructureSpawnListGatherEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(StructureSpawnLoader.MODID)
public class StructureSpawnLoader
{
	public static final String MODID = "structure_spawn_loader";
	public static final String FOLDER = MODID;
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static final CodecJsonDataManager<StructureSpawnEntry, Map<StructureFeature<?>, Map<MobCategory, List<SpawnerData>>>> STRUCTURE_SPAWNS =
		new CodecJsonDataManager<StructureSpawnEntry,Map<StructureFeature<?>,Map<MobCategory,List<MobSpawnSettings.SpawnerData>>>>(
			FOLDER, StructureSpawnEntry.CODEC, LOGGER, StructureSpawnLoader::processStructureSpawnEntries);
	
	public StructureSpawnLoader()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		forgeBus.addListener(this::onAddServerReloadListeners);
		forgeBus.addListener(EventPriority.HIGH, this::onAddStructureSpawns);
	}
	
	private void onAddServerReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(STRUCTURE_SPAWNS);
	}
	
	private void onAddStructureSpawns(StructureSpawnListGatherEvent event)
	{
		StructureFeature<?> structure = event.getStructure();
		var spawnersByCategory = STRUCTURE_SPAWNS.data.getOrDefault(structure, ImmutableMap.of());
		spawnersByCategory.forEach(event::addEntitySpawns);
	}
	
	private static Map<StructureFeature<?>,Map<MobCategory,List<MobSpawnSettings.SpawnerData>>> processStructureSpawnEntries(Map<ResourceLocation, StructureSpawnEntry> data)
	{
		Map<StructureFeature<?>,Map<MobCategory,List<MobSpawnSettings.SpawnerData>>> results = new HashMap<>();
		for (StructureSpawnEntry entry : data.values())
		{
			// both structure and spawner field must be present or entry is ignored
			entry.structure().ifPresent(structure ->
				entry.spawner().ifPresent(spawner->
					results.computeIfAbsent(structure, s -> new HashMap<>())
						.computeIfAbsent(spawner.type.getCategory(), t -> new ArrayList<>())
						.add(spawner)));
		}
		return results;
	}
}
