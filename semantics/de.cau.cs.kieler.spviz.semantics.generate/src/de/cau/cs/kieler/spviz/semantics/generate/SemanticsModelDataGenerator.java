package de.cau.cs.kieler.spviz.semantics.generate;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.Optional;

import de.cau.cs.kieler.spviz.semantics.model.SemanticsProject;

/**
 * The model generator for Semantics projects.
 */
public final class SemanticsModelDataGenerator {

    static final Logger LOGGER = System.getLogger(SemanticsModelDataGenerator.class.getName());

    
    /**
     * Generates Semantics project data from a given project path. The generated Model
     * will be returned and also saved in a file.
     * 
     * @param projectFilePath The path to the project root folder
     * @param projectName     Descriptive name of the project
     * @param save    if true, model file will be saved under target/projectName.semantics
     * @param noVersions    Determine if the extractor should add the versions to any module name/id.
     * @return The generated Semantics project data.
     */
    public static SemanticsProject generateData(final String projectFilePath, final String projectName, Optional<String> modelSaveFilePath,
    		boolean noVersions) {

        final ReadProjectFiles reader = new ReadProjectFiles();
        LOGGER.log(System.Logger.Level.INFO, "Generating data for " + projectName);
        final SemanticsProject project = reader.generateData(new File(projectFilePath), projectName, noVersions);
        
        if (modelSaveFilePath.isPresent()) {

            LOGGER.log(System.Logger.Level.INFO, "Saving data for " + projectName);
            final String fileName = projectName + ".semantics";
            try {
                SemanticsModelSaveAndLoadUtility.saveData(fileName, project, modelSaveFilePath.get());
            } catch (final IOException e) {
                LOGGER.log(System.Logger.Level.ERROR, "There was a Problem while saving.", e);
                e.printStackTrace();
            }
        }
        LOGGER.log(System.Logger.Level.INFO, "Finished");

        return project;
    }

}

