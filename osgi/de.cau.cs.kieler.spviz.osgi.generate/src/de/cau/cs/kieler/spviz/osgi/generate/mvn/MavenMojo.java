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

package de.cau.cs.kieler.spviz.osgi.generate.mvn;

import java.io.File;
import java.lang.System.Logger;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.cau.cs.kieler.spviz.osgi.generate.OsgiModelDataGenerator;

/**
 *
 */
@Mojo(name = "generate-spviz-osgi")
public class MavenMojo extends AbstractMojo {
	static final Logger LOGGER = System.getLogger(MavenMojo.class.getName());
	@Parameter(name = "name", property = "SPVizName")
	private String name;

	@Parameter(name = "sourceDir", property = "SPVizSource")
	private String sourceDir;

	@Parameter(name = "targetDir", property = "SPVizTarget")
	private String targetDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println(String.format("%s -> %s -> %s", name, sourceDir, targetDir));
		File src = new File(sourceDir);
		File trg = new File(targetDir);
		if (!src.exists()) {
			LOGGER.log(System.Logger.Level.ERROR, "Specify a valid source directory.");
			throw new MojoFailureException("Invalid source directory");
		}
		if (!trg.exists()) {
			LOGGER.log(System.Logger.Level.ERROR, "Specify a valid target directory.");
			throw new MojoFailureException("Invalid target directory");
		}
		if (name == null || name.isBlank()) {
			LOGGER.log(System.Logger.Level.ERROR, "Specify a valid name.");
			throw new MojoFailureException("Invalid name");
		}
		OsgiModelDataGenerator.generateData(sourceDir, name, Optional.of(targetDir));

		LOGGER.log(System.Logger.Level.INFO,
				"OSGi model generation has finished. The files can be found in " + targetDir);
	}

}
