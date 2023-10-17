/*
 * SPViz - Kieler Software Project Visualization for Projects
 * 
 * A part of Kieler
 * https://github.com/kieler
 * 
 * Copyright 2023 by
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
package de.cau.cs.kieler.spviz.semantics.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.cau.cs.kieler.spviz.semantics.model.SemanticsFactory;
import de.cau.cs.kieler.spviz.semantics.model.SemanticsProject;
import de.cau.cs.kieler.spviz.semantics.model.Product;
import de.cau.cs.kieler.spviz.semantics.model.Module;
import de.cau.cs.kieler.spviz.semantics.model.ComponentInterface;
import de.cau.cs.kieler.spviz.semantics.model.Identifiable;
import de.cau.cs.kieler.spviz.semantics.generate.model.Dependency;
import de.cau.cs.kieler.spviz.semantics.model.ComponentImplementation;

//=========================== INFORMATION FOR MODIFYING THIS GENERATOR ===========================\\
// This is a mockup class that should read all necessary project files/configurations to define   ||
// all artifacts, their containments and connections as defined in the .spvizmodel file. From the ||
// generated files, this is the one not complete yet that you need to program to convert the      ||
// project files into the model as listed below.                                                  ||
// You may just modify this class, it is generated from your .spvizmodel file but will never be   ||
// overwritten. So if the todo-list below is not complete for your model, just delete this plugin ||
// and let SPViz re-generate it.                                                                  ||
//================================================================================================//

// TODO: To complete the model generator, the following checklist needs to be completed:
// [x] Have a functioning .spvizmodel file to generate the model files and this generator scaffold
// [ ] extract the information for every product from your project
// [ ] connect all module artifacts to a parent product
// [ ] extract the information for every module from your project
// [ ] connect all module artifacts to all their module artifacts as defined by the dependency connection.
// [ ] connect all module artifacts to all their module artifacts as defined by the serviceProvision connection.
// [ ] connect all componentInterface artifacts to a parent module
// [ ] connect all componentImplementation artifacts to a parent module
// [ ] extract the information for every componentInterface from your project
// [ ] connect all componentInterface artifacts to all their componentImplementation artifacts as defined by the provided connection.
// [ ] extract the information for every componentImplementation from your project

 
/**
 * TODO: document me!
 */
public class ReadProjectFiles {

    static final java.lang.System.Logger LOGGER = System.getLogger(SemanticsModelDataGenerator.class.getName());

    final SemanticsProject project = SemanticsFactory.eINSTANCE.createSemanticsProject();

    private final Map<String, Product> products = new HashMap<>();
    private final Map<String, Module> modules = new HashMap<>();
    private final Map<String, ComponentInterface> componentInterfaces = new HashMap<>();
    private final Map<String, ComponentImplementation> componentImplementations = new HashMap<>();

    /**
     * Fills the HashMaps with Data for all artifacts.
     *
     * @param projectPath to extract project data from
     * @return SemanticsProject with all correctly connected project artifacts
     */
    public SemanticsProject generateData(final File projectPath, final String projectName) {
        project.setProjectName(projectName);
        
        // Find all modules by searching for .project files with a META-INF/MANIFEST.MF or a pom.xml
        // file next to it, providing the module name.
        // The build/ folder contains the projects, the other folders the modules.

        // For the modules/products, look for a dependencies.txt file containing the (direct and transient)
        // dependency information.
        // Then, search for definitions of META-INF/services/... files in the modules, describing the interfaces and their
        // implementations.
        // Finally, go through the folders of each module looking for .java files for all interface definitions.
        final List<Path> projectFilePaths = new ArrayList<Path>();
        findFiles(StaticVariables.PROJECT_FILE, projectPath, projectFilePaths);
        for (final Path filePath : projectFilePaths) {
        	if (filePath.startsWith(projectPath + "/build")) {
        		// The .project files in the build/ folder, these are the products.
        		parseProduct(filePath.getParent());
        	} else if (filePath.startsWith(projectPath + "/discontinued-plugins")
        			|| filePath.startsWith(projectPath + "/features")
        			|| filePath.startsWith(projectPath + "/oomph")
        			|| filePath.startsWith(projectPath + "/test")) {
        		// folders to ignore.
        	} else {
        		parseModule(filePath.getParent());
        		// The modules we look for.
        	}
        }
        
        // Clean up by removing all interfaces that are never provided anywhere.
        // Cache these in a list to avoid modifying the list we are iterating through.
        List<ComponentInterface> interfacesToRemove = new ArrayList<>();
        for (ComponentInterface theInterface : project.getComponentInterfaces()) {
        	if (theInterface.getConnectedProvidedComponentImplementations().isEmpty()) {
        		interfacesToRemove.add(theInterface);
        	}
        }
        for (ComponentInterface interfaceToRemove : interfacesToRemove) {
        	interfaceToRemove.getModules().clear();
        	project.getComponentInterfaces().remove(interfaceToRemove);
        }
        
        for (Module module : project.getModules()) {
        	if (module.getName().endsWith("-SNAPSHOT")) {
        		module.setName(module.getName().substring(0, module.getName().length() - "-SNAPSHOT".length()));
        	}
        }
        
        return project;

    }

	private void parseProduct(Path productRoot) {
		// Search for a pom.xml file and extract the project name/ID from there.
		// For the modules/products, look for a dependencies.txt file containing the (direct and transient)
        // dependency information.
		String name = StaticVariables.EMPTY_STRING;
		Product product = null;
		
		File pomFile = new File(productRoot.toFile(), StaticVariables.POM_FILE);
		if (pomFile.exists() && pomFile.isFile()) {
			name = parsePomFile(pomFile);
			
			// Create/update the project
			product = createOrFindProduct(name);
		}

        // The product cannot be parsed by its pom, so it is not a product.
		if (product == null) return;
		
        // Analyze and extract the (transient) dependencies.
		File dependenciesFile = new File(productRoot.toFile(), StaticVariables.DEPENDENCIES_FILE);
        if (dependenciesFile.exists() && dependenciesFile.isFile()) {
        	// extract and analyze the dependency tree arising from the depenencies.txt file.
        	analyzeDependencies(product, dependenciesFile);
        }
	}
    
    private void parseModule(Path moduleRoot) {
        // For the modules/products, look for a dependencies.txt file containing the (direct and transient)
        // dependency information.
        // Then, search for definitions of META-INF/services/... files in the modules, describing the interfaces and their
        // implementations.
        // Finally, go through the folders of each module looking for .java files for all interface definitions.
    	
		String name = StaticVariables.EMPTY_STRING;
		Module module = null;
    	// Search for a META-INF/MANIFEST.MF or a pom.xml file next to this module path, providing the module name.
        // First, search for the MANIFEST.MF file.
        File manifestFile = new File(moduleRoot.toFile(), StaticVariables.META_INF_FOLDER + StaticVariables.MANIFEST_FILE);
        if (manifestFile.exists() && manifestFile.isFile()) {
        	// We have a P2 Plugin. Parse the name from there.
        	try (final InputStream is = new FileInputStream(manifestFile.toString())) {
    			final Manifest manifest = new Manifest(is);
    			final Attributes attributes = manifest.getMainAttributes();
    			
    			String symbolicName = getAndRemove(attributes, StaticVariables.BUNDLE_SYMBOLIC_NAME);
    			name = symbolicName + ":" + getAndRemove(attributes, StaticVariables.BUNDLE_VERSION);
				// Replace .qualifier with -SNAPSHOT for consistency.
				name = replaceQualifier(name);
    			
    			// Create/update the module
    			module = createOrFindModule(name);
    			module.setExternal(false);
    		} catch (final IOException e) {
    			LOGGER.log(System.Logger.Level.ERROR, "There was an error with reading the manifest file " + e);
    		}
        }
        
        File pomFile = new File(moduleRoot.toFile(), StaticVariables.POM_FILE);
        if (module == null && pomFile.exists() && pomFile.isFile()) {
        	// We have a plain Maven module. Parse the name from there.
        	name = parsePomFile(pomFile);
			
			// Create/update the module
			module = createOrFindModule(name);
			module.setExternal(false);
        }
        
        // The module can neither be parsed by its Manifest, nor its pom, so it is not a module.
        if (module == null) return;
        
        // Analyze and extract the (transient) dependencies.
        File dependenciesFile = new File(moduleRoot.toFile(), StaticVariables.DEPENDENCIES_FILE);
        if (dependenciesFile.exists() && dependenciesFile.isFile()) {
        	// extract and analyze the dependency tree arising from the depenencies.txt file.
        	analyzeDependencies(module, dependenciesFile);
        }
        
        // Analyze and extract the service information for this module.
        analyzeServices(module, moduleRoot);
	}

    /**
     * For the module in the given folder, analyze the used Java services from the META-INF/services folder and all interfaces in the Java/Xtend code
     * and complete the model with that information.
     * 
     * @param module This module to analyze the services for.
     * @param moduleRoot The path of the directory of this module.
     */
    private void analyzeServices(Module module, Path moduleRoot) {
    	// Search for definitions of META-INF/services/... files in the modules, describing the interfaces and their
        // implementations.
        // Finally, go through the folders of each module looking for .java files for all interface definitions.
    	
    	File servicesFolder = new File(moduleRoot.toFile(), StaticVariables.SERVICES_FOLDER);
    	if (servicesFolder.exists() && servicesFolder.isDirectory()) {
    		// We have a services folder and need to extract the services from all files in this folder.
    		File[] serviceDefinitionFiles = servicesFolder.listFiles();
    		for (int fileIndex = 0; fileIndex < serviceDefinitionFiles.length; ++fileIndex) {
    			File serviceDefinitionFile = serviceDefinitionFiles[fileIndex];
    			if (serviceDefinitionFile.isFile()) {
    				// The file name is the name of the implemented interface.
    				String implementedInterfaceName = serviceDefinitionFile.getName();
    				ComponentInterface theInterface = createOrFindComponentInterface(implementedInterfaceName);
    				
    				
    				// Read this service definition file to find the fully qualified IDs of the implementing classes.
    				String serviceDefinitionsString = ReadProjectFilesUtility.readFileToString(serviceDefinitionFile.getAbsolutePath());
    				String[] serviceDefinitions = serviceDefinitionsString.split("\n");
    				for (int serviceIndex = 0; serviceIndex < serviceDefinitions.length; ++serviceIndex) {
    					String implementingComponentName = serviceDefinitions[serviceIndex];
    					
    					// Create and link this service component.
    					ComponentImplementation componentImpl = createOrFindComponentImplementation(implementingComponentName);
    					module.getComponentImplementations().add(componentImpl);
    					theInterface.getConnectedProvidedComponentImplementations().add(componentImpl);
    				}
    			}
    		}
    	}
    	
    	// Parse .java files for component interfaces in this module
		final List<Path> javaFilePaths = new ArrayList<Path>();
		findFiles(StaticVariables.JAVA_FILE, moduleRoot.toFile(), javaFilePaths);
		for (final Path javaPath : javaFilePaths) {
			parseJavaFile(javaPath, module);
		}
	}
    
    private void parseJavaFile(Path javaPath, Module parentModule) {
		final String fileContent = ReadProjectFilesUtility.readFileToString(javaPath.toString());
		
		final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource(fileContent.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {
			
			// Read all declarations and search for interfaces.
			@Override
			public boolean visit(final TypeDeclaration node) {
				if (node.isInterface()) {
					PackageDeclaration packageDecl = cu.getPackage();
					String packageName = "(default).";
					if (packageDecl != null) {
						packageName = packageDecl.getName().getFullyQualifiedName();
					}
					String interfaceName = packageName + "." + node.getName().getFullyQualifiedName();
					
					final ComponentInterface componentInterface = createOrFindComponentInterface(interfaceName);
					componentInterface.setExternal(false);
					if (!parentModule.getComponentInterfaces().contains(componentInterface) ) {
						parentModule.getComponentInterfaces().add(componentInterface);
					}
				}
				return true;
			}
			
		});
	}

	private String parsePomFile(File pomFile) {
    	String name = StaticVariables.EMPTY_STRING;
    	try {
			final Document pom = ReadProjectFilesUtility.readFileToDocument(pomFile);
			
			// Extract model data for this module
			final NodeList projectNodeChildren = findChildByTagName(pom.getChildNodes(), StaticVariables.PROJECT_TAG).getChildNodes();
//			final String groupId = findChildByTagName(projectNodeChildren, StaticVariables.GROUP_ID).getTextContent().strip();
			name = /*groupId + "." +*/ findChildByTagName(projectNodeChildren, StaticVariables.ARTIFACT_ID_TAG).getTextContent().strip()
					+ ":" + findChildByTagName(projectNodeChildren, StaticVariables.VERSION_TAG).getTextContent().strip();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LOGGER.log(Level.ERROR, "Could not parse pom.xml: " + pomFile.toString());
			return name;
		}
    	return name;
	}

	/**
     * Replace version numbers ending with ".qualifier" to end with "-SNAPTHOT" for consistency.
     * 
     * @param name The version number to check/replace
     * @return The modified name, or the given parameter if nothing has changed.
     */
	private String replaceQualifier(String name) {
		if (name.endsWith(".qualifier")) {
			name = name.subSequence(0, name.length() - ".qualifier".length()) + "-SNAPSHOT";
		}
		return name;
	}

	private void analyzeDependencies(Identifiable moduleOrProduct, File dependenciesFile) {
		// Go through the dependencies file line by line.
		// The first line repeats the current module.
		// Lines starting with "+- " are dependencies of the current module
		// The line starting with "\- " is the last dependency of the current module
		// Transient dependencies are then listed beneath each dependency, indented with either
		// a further "|  " or "   " (for the last dep.).
		// This applies hierarchically for further nested dependencies.
		// Maven dependencies are of the format <groupId>:<artifactId>:<packaging>:<version>:<something> [opt. version constraint selection]
		// P2 dependencies are of the format p2.eclipse.plugin:<artifactId>:eclipse-plugin:<version>:<something>
		
		String dependenciesFileString = ReadProjectFilesUtility.readFileToString(dependenciesFile.getAbsolutePath());
		String[] dependencyStrings = dependenciesFileString.split("\n");
		// Check the first line that it repeats the current module.
		Dependency thisModule = checkDependency(dependencyStrings[0]);
		if (!moduleOrProduct.getName().equals(thisModule.artifactId + ":" + thisModule.version)) {
			LOGGER.log(Level.ERROR, "The depepdencies.txt file does not have the same artifact ID and version as this module! This module: "
					+ moduleOrProduct.getName() + ", dependencies.txt:" + thisModule.artifactId + ":" + thisModule.version);
		}
		
		// Analyze the remaining dependencies
		int currentLine = 1;
		int currentIndent = 1;
		// The parent module to add the next dependency to.
		Stack<Identifiable> currentParents = new Stack<>();
		currentParents.push(moduleOrProduct);
		// The module related to the next parsed dependency.
		Module currentModule = null;
		for (currentLine = 1; currentLine < dependencyStrings.length; ++currentLine) {
			// There are further dependencies to analyze, so do so!
			int lineIndent = lineIndent(dependencyStrings[currentLine]);			
			Dependency currentDependency = checkDependency(dependencyStrings[currentLine].substring(3 * lineIndent));
			
			if (lineIndent == currentIndent + 1) {
				// We start a new hierarchy level in the dependency
				currentIndent = lineIndent;
				currentParents.push(currentModule);
			} else if (lineIndent < currentIndent) {
				// We go back in the hierarchy, pop the relevant parent entries for each level we go back.
				for (int i = currentIndent; i > lineIndent; --i) {
					currentParents.pop();
				}
				currentIndent = lineIndent;
			} else if (lineIndent > currentIndent + 1) {
				LOGGER.log(Level.ERROR, "dependencies.txt cannot be parsed: a single line jumps down more than one level of hierarchy.");
			}
			// else if the line indent stays equal, there is nothing to do.
			
			// Create/update the module
			currentModule = createOrFindModule(currentDependency.artifactId + ":" + currentDependency.version);
			
			Identifiable currentParent = currentParents.lastElement();
			if (currentParent instanceof Module && !((Module) currentParent).getConnectedDependencyModules().contains(currentModule)) {
				((Module) currentParent).getConnectedDependencyModules().add(currentModule);
			} else if (currentParent instanceof Product && !((Product) currentParent).getModules().contains(currentModule)) {
				((Product) currentParent).getModules().add(currentModule);
			}
		}
	}

	/**
	 * Calculates the line indent of the given line. Each indent level is either "+- ", "\- ", "|  ", or "   " in the beginning of the String.
	 * 
	 * @param theLine The line to analyze
	 * @return The indent level.
	 */
	private int lineIndent(String theLine) {
		int indent = 0;
		while (theLine.startsWith("+- ",  3 * indent) ||
			   theLine.startsWith("\\- ", 3 * indent) ||
			   theLine.startsWith("|  ",  3 * indent) ||
			   theLine.startsWith("   ",  3 * indent)) {
			++indent;
		}
		return indent;
	}

	/**
	 * Extract the dependency from the current file in the line starting at the offset with the given indent.
	 *
	 * @param dependencyString The String representation the current dependency to analyze, generated by the maven-dependency-plugin
	 * @return The dependency from the current line, or null on error.
	 */
	private Dependency checkDependency(String dependencyString) {
		// Maven dependencies are of the format <groupId>:<artifactId>:<packaging>:<version>:<something> [opt. version constraint selection]
		// P2 dependencies are of the format p2.eclipse.plugin:<artifactId>:eclipse-plugin:<version>:<something>
		String[] formattedDependencyString = dependencyString.split(":");
		if (formattedDependencyString.length < 4) {
			// The dependency does not have the correct format
			LOGGER.log(Level.ERROR, "A dependency cannot be correctly read: " + dependencyString);
			return null;
		}
		
		Dependency theDependency = new Dependency();
		theDependency.artifactId = formattedDependencyString[1];
		theDependency.version = formattedDependencyString[3];
				
		return theDependency;
	}

	/**
     * Return the product with the given name. If it was generated before, return that, or else return a new one.
     * The {@code name} and {@code ecoreId} of the module are pre-set.
     * 
     * @param name The identifying name of the product.
     * @return The (possibly newly created) product.
     */
    private Product createOrFindProduct(String name) {
        if (products.containsKey(name)) {
            return products.get(name);
        } else {
            // Create a new product and set it up
            final Product theProduct= SemanticsFactory.eINSTANCE.createProduct();
            theProduct.setName(name);
            theProduct.setEcoreId("PRODUCT_" + toAscii(name));
            // There are no external products.
            theProduct.setExternal(false);
            products.put(name, theProduct);
            project.getProducts().add(theProduct);
            return theProduct;
        }
    }
    
    /**
     * Return the module with the given name. If it was generated before, return that, or else return a new one.
     * The {@code name} and {@code ecoreId} of the module are pre-set.
     * 
     * @param name The identifying name of the module.
     * @return The (possibly newly created) module.
     */
    private Module createOrFindModule(String name) {
        if (modules.containsKey(name)) {
            return modules.get(name);
        } else {
            // Create a new module and set it up
            final Module theModule= SemanticsFactory.eINSTANCE.createModule();
            theModule.setName(name);
            theModule.setEcoreId("MODULE_" + toAscii(name));
            theModule.setExternal(true);
            modules.put(name, theModule);
            project.getModules().add(theModule);
            return theModule;
        }
    }
    
    /**
     * Return the componentInterface with the given name. If it was generated before, return that, or else return a new one.
     * The {@code name} and {@code ecoreId} of the module are pre-set.
     * 
     * @param name The identifying name of the componentInterface.
     * @return The (possibly newly created) componentInterface.
     */
    private ComponentInterface createOrFindComponentInterface(String name) {
        if (componentInterfaces.containsKey(name)) {
            return componentInterfaces.get(name);
        } else {
            // Create a new componentInterface and set it up
            final ComponentInterface theComponentInterface= SemanticsFactory.eINSTANCE.createComponentInterface();
            theComponentInterface.setName(name);
            theComponentInterface.setEcoreId("COMPONENTINTERFACE_" + toAscii(name));
            theComponentInterface.setExternal(true);
            componentInterfaces.put(name, theComponentInterface);
            project.getComponentInterfaces().add(theComponentInterface);
            return theComponentInterface;
        }
    }
    
    /**
     * Return the componentImplementation with the given name. If it was generated before, return that, or else return a new one.
     * The {@code name} and {@code ecoreId} of the module are pre-set.
     * 
     * @param name The identifying name of the componentImplementation.
     * @return The (possibly newly created) componentImplementation.
     */
    private ComponentImplementation createOrFindComponentImplementation(String name) {
        if (componentImplementations.containsKey(name)) {
            return componentImplementations.get(name);
        } else {
            // Create a new componentImplementation and set it up
            final ComponentImplementation theComponentImplementation= SemanticsFactory.eINSTANCE.createComponentImplementation();
            theComponentImplementation.setName(name);
            theComponentImplementation.setEcoreId("COMPONENTIMPLEMENTATION_" + toAscii(name));
            // Component implementations cannot be external.
            theComponentImplementation.setExternal(false);
            componentImplementations.put(name, theComponentImplementation);
            project.getComponentImplementations().add(theComponentImplementation);
            return theComponentImplementation;
        }
    }

	/**
	 * Returns the value of the attribute "key" out of an attribute list and removes
	 * it from the list.
	 *
	 * @param attributes the list of attributes
	 * @param key        the attribute asked for
	 * @return the value of the attribute, if the attribute does not exist, return
	 *         is null.
	 */
	private static String getAndRemove(final Attributes attributes, final String key) {
		final String value = attributes.getValue(key);
		attributes.remove(new Attributes.Name(key));
		if (null == value) {
			return StaticVariables.NOT_SET;
		}

		return value.split(";")[0];
	}

	/**
	 * Returns the child {@code Element} of the given node list with the given tag name.
	 * 
	 * @param nodes The node list to search the element in.
	 * @param tagName The tag name searched for.
	 * @return The node with the given tag name, or {@code null}.
	 */
	private Node findChildByTagName(NodeList nodes, String tagName) {
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			if (node instanceof Element && ((Element) node).getTagName() == tagName) {
				return node;
			}
		}
		return null;
	}
    

    /**
     * Finds all files in a directory and all its sub-directories. Adds the path to
     * the files to the <em>accumulator</em>
     *
     * @param name Filename extension/ending to search for
     * @param file Path to search in
     * @param accumulator The list to accumulate the found paths in.
     */
    private void findFiles(final String name, final File file, final List<Path> accumulator) {
        final File[] list = file.listFiles();

        if (list != null) {
            for (final File fil : list) {
                if (fil.isDirectory() && fil.getName() != "target") {
                    findFiles(name, fil, accumulator);
                } else if (fil.getName().endsWith(name)) {
                    Path filePath = Paths.get(fil.getPath());
                    accumulator.add(filePath);
                }
            }
        }
    }

    /**
     * Converts the given name to an ACII string save for using in an Ecore ID.
     * German umlauts are converted to their long form counterparts (e.g., ä->ae)
     * and special characters not in the alphabet are replaced by underscores (_).
     * 
     * @param name The name to convert to an ASCII string
     * @return An ASCII-only version of the string.
     */
    private String toAscii(String name) {
        Map<Character, String> mappings = new HashMap<>();
        mappings.put('Ä', "Ae");
        mappings.put('ä', "ae");
        mappings.put('Ö', "Oe");
        mappings.put('ö', "oe");
        mappings.put('Ü', "Ue");
        mappings.put('ü', "ue");
        mappings.put('ẞ', "Ss");
        mappings.put('ß', "ss");
        
        StringBuilder sb = new StringBuilder();
        name.chars().forEachOrdered((int character) -> {
            // Replace all known mappings to readable allowable ID substrings
            if (mappings.containsKey((char) character)) {
                sb.append(mappings.get((char) character));
            // Keep all A-Z,a-z,0-9 and .- the same.
            } else if (character >= 'A' && character <= 'Z' || 
                       character >= 'a' && character <= 'z' ||
                       character == '.' ||
                       character == '-' ||
                       character == '0' ||
                       character >= '1' && character <= '9') {
                sb.append((char) character);
            // Replace all other characters by _
            } else {
                sb.append('_');
            }
        });
        
        return sb.toString();
    }

}

