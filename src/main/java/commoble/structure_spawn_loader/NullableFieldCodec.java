package commoble.structure_spawn_loader;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.OptionalFieldCodec;

/**
 * <p>Similar to OptionalFieldCodec, but *only* permits absent values.
 * OptionalFieldCodec silently catches any parsing error-results and
 * returns an empty optional in such cases.</p>
 * 
 * <p>NullableFieldCodec returns an empty optional when it encounters absent
 * values, but returns error results if encounters any other decoding issues.</p> 
 */
public class NullableFieldCodec<VALUE> extends OptionalFieldCodec<VALUE>
{
	/**
	 * Creates a field codec useable in RecordCodecBuilder for a field that permits absent or null values.
	 * Decodes absent or null values as an empty optional.
	 * Empty optionals are not encoded (resulting in an absent value).
	 * Note that attempting to encode a null value (rather than an empty optional) results in a NullPointerException.
	 * @param <VALUE> The type of the field's value
	 * @param name The name of the field
	 * @param elementCodec The codec to delegate to for encoding/decoding the field's values
	 * @return A codec for a field that permits absent or null values
	 */
	public static <VALUE> MapCodec<Optional<VALUE>> makeOptionalField(String name, Codec<VALUE> elementCodec)
	{
		return new NullableFieldCodec<>(name, elementCodec);
	}
	
	/**
	 * As with makeOptional field, but decodes values directly to the given type instead of wrapping in an optional.
	 * Absent or null values are decoded as the given default value.
	 * 
	 * @param <VALUE> The type of the field's value
	 * @param name The name of the field
	 * @param elementCodec The codec to delegate to for encoding/decoding the field's values
	 * @param defaultValue The value that will be decoded if the field is null or absent
	 * @return A codec for a field that permits absent or null values
	 */
	public static <VALUE> MapCodec<VALUE> makeDefaultableField(String name, Codec<VALUE> elementCodec, VALUE defaultValue)
	{
		return makeOptionalField(name, elementCodec)
			.xmap(o -> o.orElse(defaultValue), Optional::ofNullable);
	}
	
	private final String name;
	private final Codec<VALUE> elementCodec;
	
	protected NullableFieldCodec(String name, Codec<VALUE> elementCodec)
	{
		super(name, elementCodec);
		this.name = name;
		this.elementCodec = elementCodec;
	}

    @Override
    public <T> DataResult<Optional<VALUE>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T value = input.get(this.name);
        if (value == null) {
            return DataResult.success(Optional.empty());
        }
        return this.elementCodec.parse(ops, value)
        	.map(Optional::of);
    }

    
}
