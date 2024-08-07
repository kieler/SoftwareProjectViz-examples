FROM eclipse-temurin:17

WORKDIR /app
RUN wget https://github.com/kieler/klighd-vscode/releases/download/v0.5.0/klighd-linux
RUN chmod u+x klighd-linux
COPY de.cau.cs.kieler.spviz.osgiviz.language.server-0.1.0-SNAPSHOT-shaded.jar de.cau.cs.kieler.spviz.osgiviz.language.server-0.1.0-SNAPSHOT-shaded.jar
COPY KLighD.osgi KLighD.osgi
COPY klighd1.osgiviz klighd1.osgiviz
COPY klighd2.osgiviz klighd2.osgiviz

EXPOSE 7001

CMD ["./klighd-linux", "--ls_path", "de.cau.cs.kieler.spviz.osgiviz.language.server-0.1.0-SNAPSHOT-shaded.jar", "-p", "7001", "serve"]
