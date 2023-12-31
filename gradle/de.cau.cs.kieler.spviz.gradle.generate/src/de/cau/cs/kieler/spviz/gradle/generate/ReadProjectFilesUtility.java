/*
 * SPViz - Kieler Software Project Visualization for Projects
 * 
 * A part of Kieler
 * https://github.com/kieler
 * 
 * Copyright 2022 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */

package de.cau.cs.kieler.spviz.gradle.generate;

import java.io.IOException;
import java.io.Reader;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;

import de.cau.cs.kieler.spviz.gradle.generate.json.JsonProject;

/**
 * This is a utility class for {@linkplain ReadProjectFiles} with methods for
 * reading and parsing files.
 *
 * @author dams
 *
 */

public class ReadProjectFilesUtility {
	static final Logger LOGGER = System.getLogger(ReadProjectFilesUtility.class.getName());

	/**
	 * Reads the json File at the destination and puts it into a {@link JsonProject} for easy
	 * further access.
	 * 
	 * @param filePath The path to find the .json file
	 * @return The project data as generated by Gradle.
	 * @throws IOException
	 */
	static JsonProject readFileToJson(final Path filePath) throws IOException {
		Reader jsonReader = Files.newBufferedReader(filePath);
		
		return new Gson().fromJson(jsonReader, JsonProject.class);
	}

}
