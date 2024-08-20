FROM maven:3.9.8-eclipse-temurin-17

WORKDIR /app
# Get the KLighD CLI
ADD https://github.com/kieler/klighd-vscode/releases/download/v0.5.0/klighd-linux .
RUN chmod u+x klighd-linux
# Get the SPViz CLI
ADD https://github.com/kieler/SoftwareProjectViz/releases/download/v0.4.0/spviz-cli.jar .

# Copy the SPViz model files and generate and build their projects
COPY osgi.spvizmodel osgi.spviz .
RUN java -jar spviz-cli.jar osgi.spvizmodel osgi.spviz -o generated/ --build \
 && rm -rf generated/de.cau.cs.kieler.spviz.osgi.generate/

# Copy and build the modified generator
COPY de.cau.cs.kieler.spviz.osgi.generate/ generated/de.cau.cs.kieler.spviz.osgi.generate/
RUN mvn clean package -f generated/de.cau.cs.kieler.spviz.osgiviz.build/pom.xml -P generator

# Copy the pre-generated OSGiViz model files for the example visualizations (possibly replace "KLighD.osgi" with a model generated from last KLighD release in this dockerfile)

COPY documentation-example/KLighD.osgi KLighD.osgi
COPY documentation-example/klighd1.osgiviz klighd1.osgiviz
COPY documentation-example/klighd2.osgiviz klighd2.osgiviz

# Start the server
EXPOSE 7001
CMD ["./klighd-linux", "--ls_path", "generated/de.cau.cs.kieler.spviz.osgiviz.language.server/target/de.cau.cs.kieler.spviz.osgiviz.language.server-0.1.0-SNAPSHOT-shaded.jar", "-p", "7001", "serve"]
