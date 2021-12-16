package commoble.structure_spawn_loader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.mojang.serialization.JsonOps;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.server.packs.PackType;

/**
 * Data generator for Structure Spawn Entries.
 * Recommended naming convention for json ids is yourmodid:structuremod/structure/entitymod/entitytype,
 * where yourmodid is the ID of the mod or datapack adding the entry, structuremod adds the structure, and entitymod adds the entitytype
 * @param gson A gson to use.
 * @param generator the data generator from GatherDataEvent
 * @param modid The modid folder to generate jsons in (usually your modid)
 * @param entries Map of json ids to structure spawn entries to generate (if modid is "yourmod" and the string key in the map is "id",
 * then a json will be generated at data/yourmod/structure_spawn_loader/id.json
 */
public record StructureSpawnEntryDataProvider(Gson gson, DataGenerator generator, String modid, Map<String, StructureSpawnEntry> entries) implements DataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void run(HashCache cache) throws IOException
	{
        Path resourcesFolder = this.generator.getOutputFolder();
        this.entries.forEach((id, entry) -> {
        	// "id" -> data/yourmod/structure_spawn_loader/id.json
            Path jsonLocation = resourcesFolder.resolve(String.join("/", PackType.SERVER_DATA.getDirectory(), this.modid(), StructureSpawnLoader.FOLDER, id + ".json"));
            StructureSpawnEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry)
                .resultOrPartial(s -> LOGGER.error("Structure Spawn Loader entry provider for mod {} failed to encode {}", this.modid(), jsonLocation, s))
                .ifPresent(jsonElement -> {
                    try
                    {
                        DataProvider.save(this.gson, cache, jsonElement, jsonLocation);
                    }
                    catch (IOException e)
                    {
                        LOGGER.error("Structure Spawn Loader provider for mod {} failed to save {}", this.modid(), jsonLocation, e);
                    }
                });

        });
	}

	@Override
	public String getName()
	{
		return "Structure Spawn Entry Data Provider for " + this.modid();
	}

}
