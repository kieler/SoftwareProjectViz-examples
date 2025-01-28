// ******************************************************************************
//
// Copyright (c) 2018-2025 by
// Scheidt & Bachmann System Technik GmbH, 24145 Kiel
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License 2.0 which is available at
// http://www.eclipse.org/legal/epl-2.0.
// 
// SPDX-License-Identifier: EPL-2.0
//
// ******************************************************************************

package de.cau.cs.kieler.spviz.osgi.generate.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Extractor for OSGI_INF folder from jar.
 *
 */
public class FileUtil {
	protected static final String BUNDLE_SYMBOLIC_NAME_TAG = "Bundle-SymbolicName";

	/**
	 * Copys the xml files from the jar to the temporary folder.
	 *
	 * @param entry        the jar entry with everything included in the jar file
	 * @param jarFile      needed to create the input stream
	 * @param tempFilePath the path of the temporary file
	 */
	private static void copyXmlFileFromJarToDirectory(final JarEntry entry, final JarFile jarFile,
			final Path tempFilePath) {
		try (final InputStream stream = jarFile.getInputStream(entry);) {
			final String fileName = entry.getName();
			final Path file = tempFilePath.resolve(fileName);
			Files.createDirectories(file.getParent());
			Files.createFile(file);
			Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			// NOOP if the file is unsuitable.
		}
	}

	/**
	 * Creates a OSGI-INF directory extracted from the jar file.
	 *
	 * @param rootPath the bundle path
	 * @param manifest the manifest for obtaining the name of the jar
	 * @throws IOException in case the rootPath is wrong
	 */
	// CHECKSTYLE IGNORE AbbreviationAsWordInNameCheck FOR NEXT 1 LINES - OSG is
	// what it is called.
	public static void createTemporaryOSGiInfDirectory(final Path rootPath, final Manifest manifest)
			throws IOException {
		final String symbolicName = getSymbolicName(manifest);
		final List<File> jarList = listFilesWithExtensionRecursively(rootPath, ".jar").stream() //
				.map(Path::toFile) //
				.filter(path -> path.getName().contains(symbolicName + "-")) //
				.collect(Collectors.toList());
		for (final File fileWithJarEnding : jarList) {
			extractXmlsFromJarIntoTempFolder(rootPath, fileWithJarEnding);
		}
	}

	private static final void deleteDirectory(final File file) {
		if (file.isDirectory()) {
			Arrays.stream(file.listFiles()) //
					.forEach(FileUtil::deleteDirectory);
		}
		file.delete();
	}

	public static void deleteTempFolder(final File rootFile) {
		final Path tempFolder = Paths.get(String.format("%s/%s", rootFile.getAbsolutePath(), "OSGI-INF"));
		deleteDirectory(tempFolder.toFile());
	}

	/**
	 * Extracts the OSGI-INF directory from a jar, if it exists, and puts it into
	 * the temporary folder.
	 *
	 * @param rootPath          root of the bundle
	 * @param fileWithJarEnding the jar which the OSGI-INF files are being extracted
	 *                          from
	 * @throws IOException in case there are file issues.
	 *
	 */
	private static void extractXmlsFromJarIntoTempFolder(final Path rootPath, final File fileWithJarEnding)
			throws IOException {
		final ArrayList<JarEntry> osgiInfFiles = new ArrayList<>();
		try (JarFile jarFile = new JarFile(fileWithJarEnding.getAbsolutePath())) {
			jarFile.entries().asIterator().forEachRemaining(entry -> { //
				if (entry.getName().startsWith("OSGI-INF/") && entry.getName().endsWith(".xml")) {
					osgiInfFiles.add(entry);
				}
			});
			osgiInfFiles.stream().forEach(entry -> copyXmlFileFromJarToDirectory(entry, jarFile, rootPath));
		}
	}

	private static String getSymbolicName(final Manifest manifest) {
		final String symbolicName = manifest.getMainAttributes().getValue(BUNDLE_SYMBOLIC_NAME_TAG).trim();
		return symbolicNameFromDeclaration(symbolicName);
	}

	private static List<Path> listFilesWithExtensionRecursively(final Path path, final String fileExtension)
			throws IOException {
		final Filter<Path> filter = filterPath -> filterPath.toString().endsWith(fileExtension)
				|| filterPath.toFile().isDirectory();
		final List<Path> paths = new LinkedList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, filter)) {
			for (final Path entry : stream) {
				if (Files.isDirectory(entry)) {
					paths.addAll(listFilesWithExtensionRecursively(entry, fileExtension));
				} else {
					paths.add(entry);
				}
			}
		}
		return paths;
	}

	private static String symbolicNameFromDeclaration(final String symbolicNameDeclaration) {
		return symbolicNameDeclaration.split(";")[0];
	}

}
