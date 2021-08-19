package tetris.block.container;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import tetris.GameException;

public class DiScanner {

	public static List<Class<?>> scanPackageAndGetClass(Class<?> callerClass, Class<?> targetClass) {
		//Class<?>[] classes;
		List<Class<?>> classes;
		List<Class<?>> targetAnnotatedClasses = new LinkedList<>();
		String packageName = callerClass.getPackage().getName();

		packageName = packageName.substring(0, packageName.indexOf("."));

		try {
			classes = getClasses(packageName);

			/*
			if (classes.size() == 0) {
				classes = getClassesFromJar(callerClass, packageName);
			}
			*/

			for (Class<?> c : classes) {
				Annotation[] annotations = c.getDeclaredAnnotations();
				for (Annotation annotation : annotations) {
					/*if (annotation instanceof TetrisBlock) {
						list.add(c);
						System.out.println(targetClass.isAssignableFrom(annotation.getClass()));
						break;
					}*/
					if (targetClass.isAssignableFrom(annotation.getClass())) {
						targetAnnotatedClasses.add(c);
						break;
					}
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			throw new GameException(e);
		}

		return targetAnnotatedClasses;
	}

	/**
	 * Scans all classes accessible from the context class loader which belong to
	 * the given package and subpackages.
	 *
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<?> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = (URL) resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		List<Class<?>> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	private static List<Class<?>> getClassesFromJar(Class<?> callerClass, String packageName) {
		List<Class<?>> classes = new ArrayList<>();
		try {
			List<Path> result = getPathsFromResourceJAR(callerClass, packageName);
			for (Path path : result) {

				String filePathInJAR = path.toString();

				if (filePathInJAR.startsWith("/")) {
					filePathInJAR = filePathInJAR.substring(1, filePathInJAR.length());
				}

				filePathInJAR = filePathInJAR.replace("/", ".");

				if (filePathInJAR.endsWith(".class")) {
					filePathInJAR = filePathInJAR.substring(0, filePathInJAR.length() - 6);
				}
				classes.add(Class.forName(filePathInJAR));
			}

		} catch (URISyntaxException | IOException | ClassNotFoundException e) {
			throw new GameException(e);
		}
		return classes;
	}

	// Get all paths from a folder that inside the JAR file
	private static List<Path> getPathsFromResourceJAR(Class<?> callerClass, String folder)
			throws URISyntaxException, IOException {

		List<Path> result;

		// get path of the current running JAR
		String jarPath = callerClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

		URI uri = URI.create("jar:file:" + jarPath);
		try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
			result = Files.walk(fs.getPath(folder)).filter(Files::isRegularFile).collect(Collectors.toList());
		}

		return result;
	}

}
