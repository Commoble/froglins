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

package commoble.froglins.util;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.Config.Entry;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.PartialResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;

public class TomlConfigOps implements DynamicOps<Object>
{
	public static final TomlConfigOps INSTANCE = new TomlConfigOps();

	@Override
	public Object empty()
	{
		return NullObject.NULL_OBJECT;
	}

	@Override
	public <U> U convertTo(DynamicOps<U> outOps, Object input)
	{
		if (input instanceof Config)
		{
			return this.convertMap(outOps, input);
		}
		if (input instanceof Collection)
		{
			return this.convertList(outOps, input);
		}
		if (input == null || input instanceof NullObject)
		{
			return outOps.empty();
		}
		if (input instanceof Enum)
		{
			return outOps.createString(((Enum<?>)input).name());
		}
		if (input instanceof Temporal)
		{
			return outOps.createString(input.toString());
		}
		if (input instanceof String)
		{
			return outOps.createString((String)input);
		}
		if (input instanceof Boolean)
		{
			return outOps.createBoolean((Boolean)input);
		}
		if (input instanceof Number)
		{
			return outOps.createNumeric((Number)input);
		}
		throw new UnsupportedOperationException("TomlConfigOps was unable to convert toml value: " + input);
	}

	@Override
	public DataResult<Number> getNumberValue(Object input)
	{
		return input instanceof Number
			? DataResult.success((Number)input)
			: DataResult.error("Not a number: " + input);
	}

	@Override
	public boolean compressMaps()
	{
		return false;
	}

	@Override
	public Object createNumeric(Number i)
	{
		return i;
	}

	@Override
	public DataResult<String> getStringValue(Object input)
	{
		if (input instanceof Config || input instanceof Collection)
		{
			return DataResult.error("Not a string: " + input);
		}
		else
		{
			return DataResult.success(String.valueOf(input));
		}
	}

	@Override
	public Object createString(String value)
	{
		return value;
	}

	@Override
	public DataResult<Object> mergeToList(Object list, Object value)
	{
		if (!(list instanceof Collection) && list != this.empty())
		{
			return DataResult.error("mergeToList called with not a list: " + list, list);
		}
		final Collection<Object> result = new ArrayList<>();
		if (list != this.empty())
		{
			Collection<? extends Object> listAsCollection = (Collection<? extends Object>)list;
			result.addAll(listAsCollection);
		}
		result.add(value);
		return DataResult.success(result);
	}

	@Override
	public DataResult<Object> mergeToMap(Object map, Object key, Object value)
	{
		if (!(map instanceof Config) && map != this.empty())
		{
			return DataResult.error("mergeToMap called with not a map: " + map, map);
		}
		DataResult<String> stringResult = this.getStringValue(key);
		Optional<PartialResult<String>> badResult = stringResult.error();
		if (badResult.isPresent())
		{
			return DataResult.error("key is not a string: " + key, map);
		}
		Optional<String> result = stringResult.result();
		return stringResult.flatMap(s ->{

			final Config output = TomlFormat.newConfig();
			if (map != this.empty())
			{
				Config oldConfig = (Config)map;
				output.addAll(oldConfig);
			}
			output.add(s, value);
			return DataResult.success(output);
		});
	}

	@Override
	public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object input)
	{
		if (!(input instanceof Config))
		{
			return DataResult.error("Not a Config: " + input);
		}
		final Config config = (Config)input;
		return DataResult.success(config.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())));
	}

	@Override
	public Object createMap(Stream<Pair<Object, Object>> map)
	{
		final Config result = TomlFormat.newConfig();
		map.forEach(p -> result.add(this.getStringValue(p.getFirst()).getOrThrow(false, s -> {}), p.getSecond()));
		return result;
	}

	@Override
	public DataResult<Stream<Object>> getStream(Object input)
	{
		if (input instanceof Collection)
		{
			Collection<Object> collection = (Collection<Object>)input;
			return DataResult.success(collection.stream());
		}
		return DataResult.error("Not a collection: " + input);
	}

	@Override
	public Object createList(Stream<Object> input)
	{
		return input.collect(Collectors.toList());
	}

	@Override
	public Object remove(Object input, String key)
	{
		if (input instanceof Config)
		{
			final Config result = TomlFormat.newConfig();
			final Config oldConfig = (Config)input;
			oldConfig.entrySet().stream()
				.filter(entry -> !Objects.equals(entry.getKey(), key))
				.forEach(entry -> result.add(entry.getKey(), entry.getValue()));
			return result;
		}
		return input;
	}

	@Override
	public String toString()
	{
		return "TOML";
	}

	@Override
	public RecordBuilder<Object> mapBuilder()
	{
		return DynamicOps.super.mapBuilder();
	}
	
	class TomlRecordBuilder extends RecordBuilder.AbstractStringBuilder<Object, Config>
	{

		protected TomlRecordBuilder()
		{
			super(TomlConfigOps.this);
		}

		@Override
		protected Config initBuilder()
		{
			return TomlFormat.newConfig();
		}

		@Override
		protected Config append(String key, Object value, Config builder)
		{
			builder.add(key, value);
			return builder;
		}

		@Override
		protected DataResult<Object> build(Config builder, Object prefix)
		{
			if (prefix == null || prefix instanceof NullObject)
			{
				return DataResult.success(builder);
			}
			if (prefix instanceof Config)
			{
				final Config result = TomlFormat.newConfig();
				final Config oldConfig = (Config)prefix;
				for (Entry entry : oldConfig.entrySet())
				{
					result.add(entry.getKey(), entry.getValue());
				}
				for (Entry entry : builder.entrySet())
				{
					result.add(entry.getKey(), entry.getValue());
				}
				return DataResult.success(result);
			}
			return DataResult.error("mergeToMap called with not a Config: " + prefix, prefix);
		}
		
	}
}
