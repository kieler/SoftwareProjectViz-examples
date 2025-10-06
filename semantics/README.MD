# Example for Project-Spanning Analysis of the KIELER Project
This example is a combination of the OSGi example with a new categorization into the different projects and use of the built-in dependency analysis of Maven in its `maven-dependency-plugin`.

The generator provides a command line tool using [Picocli](https://picocli.info/) to generate a Semantics project model from a `KIELER` folder with multiple source projects checked out inside it.
In particular, this example has been designed and tested to analyze the KIELER Semantics, Pragmatics, KLighD, and ELK project all at once.
For this example, we expect the project to be structured into Maven modules or Eclipse plugins as described in the OSGi and Maven/SpringDI examples.
Furthermore, the projects should have been built on the system beforehand, configured to produce `dependencies.txt` files from the `maven-dependency-plugin` analyzer.
