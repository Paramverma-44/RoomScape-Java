# Virtual Interior Room Designer — Java Web Edition

A browser-based adaptation of the submitted Virtual Interior Room Designer project. The application logic is written in **Java 17 with Spring Boot**. The interface is server-rendered with Thymeleaf; there is **no JavaScript**. Standard HTML forms submit changes to Java endpoints, and Java generates the room plan as SVG markup on the server.

## Features

- Add 14 furniture and room objects
- Select objects from a furniture list
- Change position, width, height, rotation, and furniture colour
- Change wall and floor colours and room name
- Reset to the sample room
- Export a `.room` text file
- Responsive interface
- Render deployment files included
- Original Java Swing source retained in `reference-java-project/`

## Run locally

Requirements: JDK 17+ and Maven 3.9+.

```bash
mvn spring-boot:run
```

Open http://localhost:8080.

To build a runnable JAR:

```bash
mvn clean package
java -jar target/virtual-interior-room-designer-1.0.0.jar
```

## Deploy to Render

### Option 1: Render Blueprint

1. Push this whole folder to a GitHub repository.
2. In Render, choose **New + → Blueprint**.
3. Connect the repository and let Render read `render.yaml`.
4. Create the service. Render builds the included `Dockerfile` and exposes the app on the assigned `PORT`.

### Option 2: Create a Web Service manually

1. Push this folder to GitHub.
2. Render Dashboard → **New + → Web Service** → connect the repository.
3. Select **Docker** as the runtime.
4. Choose the free plan if it is available for your account and deploy.

The app listens on Render's `PORT` environment variable. No database or API key is required.

## Java-only interaction model

The room editor uses ordinary HTML forms. Each add/select/move/resize/rotate/recolour/delete action is handled by a Spring MVC controller in Java. Thymeleaf renders the current design and Java emits the SVG attributes. No client-side JavaScript is included.

## Important note

A browser cannot display a desktop Swing window directly. This edition recreates the room-designer workflow as a Java web application while preserving the original Java Swing project in `reference-java-project/` for reference. It is not the Swing `.jar` running in a browser.
