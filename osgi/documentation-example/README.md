# Deploy a diagram service

This is an example on how to deploy a diagram service for some example visualizations for the [KLighD project](https://github.com/kieler/klighd) using this OSGi example configuration and extractor in combination with Docker.

To run this example, follow these steps:
1. execute `docker compose up --build -d` one folder up from this one to create and run a new container
1. open the `index.html` file in your browser, it should now show a few examples of OSGi visualizations of the KLighD project using SPViz!

By default, this uses port 7001 and forwards that from the container to your host machine and uses the KLighD example.
To change the port, modify the `Dockerfile` and the `docker-compose.yaml` and change the last two lines to both use your preferred port instead.
To change the shown example, load them into this folder, modify the `COPY` commands in the `Dockerfile` and refer to these files in the `index.html` file.