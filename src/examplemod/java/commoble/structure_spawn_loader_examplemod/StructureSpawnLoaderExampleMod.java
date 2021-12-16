package commoble.structure_spawn_loader_examplemod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import commoble.structure_spawn_loader.StructureSpawnEntry;
import commoble.structure_spawn_loader.StructureSpawnEntryDataProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod(StructureSpawnLoaderExampleMod.MODID)
public class StructureSpawnLoaderExampleMod
{
	public static final String MODID = "structure_spawn_loader_examplemod";
	public static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.create();
	
	public StructureSpawnLoaderExampleMod()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::onGatherData);
	}
	
	// example of using the StructureSpawnEntryDataProvider to generate jsons via runData
	private void onGatherData(GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		Map<String, StructureSpawnEntry> entries = new HashMap<>();
		Consumer<StructureSpawnEntry> consumer = entry ->
		{
			ResourceLocation structureId = entry.structure().get().getRegistryName();
			ResourceLocation entityId = entry.spawner().get().type.getRegistryName();
			entries.put(
				String.join("/", structureId.getNamespace(), structureId.getPath(), entityId.getNamespace(), entityId.getPath()),
				entry);
		};
		consumer.accept(new StructureSpawnEntry(StructureFeature.VILLAGE, new MobSpawnSettings.SpawnerData(EntityType.EVOKER, 1000, 1, 4)));
		consumer.accept(new StructureSpawnEntry(StructureFeature.PILLAGER_OUTPOST, new MobSpawnSettings.SpawnerData(EntityType.EVOKER, 1000, 1, 4)));
		consumer.accept(new StructureSpawnEntry(StructureFeature.BASTION_REMNANT, new MobSpawnSettings.SpawnerData(EntityType.EVOKER, 1000, 1, 4)));
		StructureSpawnEntryDataProvider provider = new StructureSpawnEntryDataProvider(GSON, generator, MODID, entries);
		generator.addProvider(provider);
	}
}
