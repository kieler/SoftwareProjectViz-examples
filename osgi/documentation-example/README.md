# Deploy a diagram service

This is an example on how to deploy a diagram service for some example visualizations for the [KLighD project](https://github.com/kieler/klighd) using this OSGi example configuration and extractor in combination with Docker.

To run this example, follow these steps:
1. Download the latest release of the KLighD CLI (the linux variant called `klighd-linux`) from the assets of the [release](https://github.com/kieler/klighd-vscode/releases) and put it into this folder.
1. Do the steps to build the OSGiViz project from the configuration from the parent folder and let Maven build the language server.
1. copy the language server jar into this folder.
1. execute `docker compose up --build -d` in this folder to create and run a new container
1. open the `index.html` file in your browser, it should now show a few examples of OSGi visualizations of the KLighD project using SPViz!

By default, this uses port 7001 and forwards that from the container to your host machine and uses the KLighD example.
To change the port, modify the `Dockerfile` and change the last two lines to both use your preferred port instead.
To change the shown example, load them into this folder, modify the `COPY` commands in the `Dockerfile` and refer to these files in the `index.html` file.