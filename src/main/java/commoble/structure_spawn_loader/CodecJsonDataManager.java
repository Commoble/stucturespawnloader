/*

The MIT License (MIT)

Copyright (c) 2020 Joseph Bettendorff a.k.a. "Commoble"

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */

package commoble.structure_spawn_loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.Runnables;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CodecJsonDataManager<T, FINALDATA> extends SimpleJsonResourceReloadListener
{
	// default gson if unspecified
	private static final Gson STANDARD_GSON = new Gson();
	
	/** The codec we use to convert jsonelements to Ts **/
	private final Codec<T> codec;
	
	/** Logger that will log data parsing errors **/
	private final Logger logger;
	private final String folderName;
	
	protected final Function<Map<ResourceLocation,T>,FINALDATA> postProcessor; 
	
	protected FINALDATA data = null;
	private Runnable syncOnReloadCallback = Runnables.doNothing();
	
	/**
	 * Creates a data manager with a standard gson parser
	 * @param folderName The name of the data folder that we will load from, vanilla folderNames are "recipes", "loot_tables", etc<br>
	 * Jsons will be read from data/all_modids/folderName/all_jsons<br>
	 * folderName can include subfolders, e.g. "some_mod_that_adds_lots_of_data_loaders/cheeses"
	 * @param codec A codec to deserialize the json into your T, see javadocs above class
	 * @param logger A logger that will log json parsing problems when they are caught.
	 * @param postProcessor A function to postprocess the parsed data, if desired. Function.identity() will leave the final data as a Map of ResourceLocation to T
	 */
	public CodecJsonDataManager(String folderName, Codec<T> codec, Logger logger, Function<Map<ResourceLocation,T>, FINALDATA> postProcessor)
	{
		this(folderName, codec, logger, postProcessor, STANDARD_GSON);
	}
	
	/**
	 * As above but with a custom GSON
	 * @param folderName The name of the data folder that we will load from, vanilla folderNames are "recipes", "loot_tables", etc<br>
	 * Jsons will be read from data/all_modids/folderName/all_jsons<br>
	 * folderName can include subfolders, e.g. "some_mod_that_adds_lots_of_data_loaders/cheeses"
	 * @param codec A codec to deserialize the json into your T, see javadocs above class
	 * @param logger A logger that will log json parsing problems when they are caught.
	 * @param postProcessor A function to postprocess the parsed data, if desired. Function.identity() will leave the final data as a Map of ResourceLocation to T
	 * @param gson A gson for parsing the raw json data into JsonElements. JsonElement-to-T conversion will be done by the codec,
	 * so gson type adapters shouldn't be necessary here
	 */
	public CodecJsonDataManager(String folderName, Codec<T> codec, Logger logger, Function<Map<ResourceLocation,T>, FINALDATA> postProcessor, Gson gson)
	{
		super(gson, folderName);
		this.folderName = folderName; // superclass has this but it's a private field
		this.codec = codec;
		this.logger = logger;
		this.postProcessor = postProcessor;
	}
	
	/**
	 * @return The parsed and postprocessed data from the last time data was loaded. Returns null if called before data is loaded.
	 */
	@Nullable
	public FINALDATA getData()
	{
		return this.data;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler)
	{
		this.logger.info("Beginning loading of data for data loader: {}", this.folderName);
		var data = this.mapValues(jsons);
		this.logger.info("Data loader for {} loaded {} jsons", this.folderName, data.size());
		this.data = this.postProcessor.apply(data);
		if (ServerLifecycleHooks.getCurrentServer() != null)
		{
			// if we're on the server and we are configured to send syncing packets, send syncing packets
			this.syncOnReloadCallback.run();
		}
	}

	private Map<ResourceLocation, T> mapValues(Map<ResourceLocation, JsonElement> inputs)
	{
		Map<ResourceLocation, T> newMap = new HashMap<>();

		for (Entry<ResourceLocation, JsonElement> entry : inputs.entrySet())
		{
			ResourceLocation key = entry.getKey();
			JsonElement element = entry.getValue();
			// if we fail to parse json, log an error and continue
			// if we succeeded, add the resulting T to the map
			this.codec.decode(JsonOps.INSTANCE, element)
				.get()
				.ifLeft(result -> newMap.put(key, result.getFirst()))
				.ifRight(partial -> this.logger.error("Failed to parse data json for {} due to: {}", key.toString(), partial.message()));
		}

		return newMap;
	}
}