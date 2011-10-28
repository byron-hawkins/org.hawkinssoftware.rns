package org.hawkinssoftware.rns.core.aop;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ObservableClassLoader extends ClassLoader
{
	public interface Listener
	{
		void classLoaded(Class<?> type);
	}

	public static void launchApplication(String mainClassname)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException
	{
		ObservableClassLoader loader = new ObservableClassLoader();
		Class<?> mainType = loader.loadClass(mainClassname);
		Method mainMethod = mainType.getMethod("main", String[].class);
		mainMethod.invoke(null, new Object[] { new String[0] });
	}

	public static void addListener(Listener listener)
	{
		ClassLoader loader = listener.getClass().getClassLoader();
		if (!(loader instanceof ObservableClassLoader))
		{
			throw new IllegalStateException("The listener " + listener + " is not associated with any " + ObservableClassLoader.class.getSimpleName()
					+ " instance.");
		}

		((ObservableClassLoader) loader)._addListener(listener);
	}

	private final List<Listener> listeners = new ArrayList<Listener>();

	private ObservableClassLoader()
	{
		super(ObservableClassLoader.class.getClassLoader());
	}

	public void _addListener(Listener listener)
	{
		listeners.add(listener);
	}

	public void _removeListener(Listener listener)
	{
		listeners.remove(listener);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		return loadClass(name);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException
	{
		Class<?> loaded;

		if (name.startsWith("java.") || name.startsWith("org.hawkinssoftware.rns.core.aop.ObservableClassLoader"))
		{
			loaded = getClass().getClassLoader().loadClass(name);
		}
		else
		{
			try
			{
				InputStream in = getResourceAsStream(name.replace('.', '/') + ".class");
				byte[] bytes = new byte[in.available()];
				in.read(bytes);
				loaded = defineClass(name, bytes, 0, bytes.length);

				for (Listener listener : listeners)
				{
					listener.classLoaded(loaded);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException("Failed to load class " + name, e);
			}
		}

		return loaded;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		return loadClass(name);
	}
}
