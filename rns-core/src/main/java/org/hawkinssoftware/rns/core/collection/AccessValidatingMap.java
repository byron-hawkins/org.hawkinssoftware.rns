package org.hawkinssoftware.rns.core.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class AccessValidatingMap<K, V> implements Map<K, V>, AccessValidatedCollection<Map<K, V>>
{
	public static <K, V> AccessValidatingMap<K, V> create(Map<K, V> map)
	{
		return new AccessValidatingMap<K, V>(map);
	}

	public static <K, V> AccessValidatingMap<K, V> create(Map<K, V> map, CollectionAccessValidator<Map<K, V>> validator)
	{
		AccessValidatingMap<K, V> vaildatingMap = create(map);
		vaildatingMap.setValidator(validator);
		return vaildatingMap;
	}

	private final Map<K, V> map;

	private CollectionAccessValidator<Map<K, V>> validator;

	private AccessValidatingMap(Map<K, V> map)
	{
		this.map = map;
	}

	@Override
	public void setValidator(CollectionAccessValidator<Map<K, V>> validator)
	{
		if (enabled)
		{
			this.validator = validator;
		}
	}

	@Override
	public int size()
	{
		if (validator != null)
		{
			validator.validateRead(this, "size");
		}
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		if (validator != null)
		{
			validator.validateRead(this, "isEmpty");
		}
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (validator != null)
		{
			validator.validateRead(this, "containsKey", key);
		}
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		if (validator != null)
		{
			validator.validateRead(this, "containsValue", value);
		}
		return map.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		if (validator != null)
		{
			validator.validateRead(this, "get", key);
		}
		return map.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		if (validator != null)
		{
			validator.validateWrite(this, "put", key, value);
		}
		return map.put(key, value);
	}

	@Override
	public V remove(Object key)
	{
		if (validator != null)
		{
			validator.validateWrite(this, "remove", key);
		}
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		if (validator != null)
		{
			validator.validateWrite(this, "putAll", m);
		}
		map.putAll(m);
	}

	@Override
	public void clear()
	{
		if (validator != null)
		{
			validator.validateWrite(this, "clear");
		}
		map.clear();
	}

	@Override
	public Set<K> keySet()
	{
		if (validator != null)
		{
			validator.validateRead(this, "keySet");
		}
		// TODO: ideally the returned key set must be wrapped
		return map.keySet();
	}

	@Override
	public Collection<V> values()
	{
		if (validator != null)
		{
			validator.validateRead(this, "values");
		}
		// TODO: ideally the returned collection must be wrapped
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		if (validator != null)
		{
			validator.validateRead(this, "entrySet");
		}
		// TODO: ideally the returned entry set must be wrapped, along with every entry
		return map.entrySet();
	}
}
