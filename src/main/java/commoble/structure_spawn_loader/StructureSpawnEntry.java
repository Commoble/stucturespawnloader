package commoble.structure_spawn_loader;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public record StructureSpawnEntry(Optional<StructureFeature<?>> structure, Optional<MobSpawnSettings.SpawnerData> spawner)
{
	public static final Codec<StructureSpawnEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			NullableFieldCodec.makeOptionalField("structure", Registry.STRUCTURE_FEATURE.byNameCodec()).forGetter(StructureSpawnEntry::structure),
			NullableFieldCodec.makeOptionalField("spawner", MobSpawnSettings.SpawnerData.CODEC).forGetter(StructureSpawnEntry::spawner) // vanilla name convention, see biomes
		).apply(instance, StructureSpawnEntry::new));
	
	public StructureSpawnEntry(@Nonnull StructureFeature<?> structure, @Nonnull MobSpawnSettings.SpawnerData spawner)
	{
		this(Optional.of(structure), Optional.of(spawner));
	}
}