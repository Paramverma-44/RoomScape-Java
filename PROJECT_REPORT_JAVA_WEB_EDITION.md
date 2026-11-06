# Virtual Interior Room Designer — Java Web Edition

## Objective
Convert the submitted Java Swing room-planning project into a browser-accessible application while keeping application behavior in Java.

## Technology stack
- Java 17
- Spring Boot / Spring MVC
- Thymeleaf server-side templates
- SVG markup rendered by the server for the top-down room plan
- Maven for dependency management and packaging
- Docker for Render deployment

## Main features
- Add furniture and room objects to a 2D plan
- Select objects from a list
- Set object position, dimensions, orientation, and colour
- Change room name, wall colour, and floor colour
- Reset the sample layout
- Export the current layout as a `.room` text file

## How it works
A browser submits standard HTML forms to Spring MVC endpoints. The controller updates a room model held in the user's HTTP session. Thymeleaf then renders the updated room and furniture as SVG. No client-side JavaScript is included; interaction is request/response based, so the page reloads after each change.

## Run locally
Install JDK 17+ and Maven 3.9+, then run `mvn spring-boot:run`. Open `http://localhost:8080`.

## Deployment
The included Dockerfile builds the Maven project and runs it on the port supplied by Render. `render.yaml` defines a Render web service.

## Relationship to the original submission
The original Java Swing source, JAR, sample design, and report are retained under `reference-java-project/`. The browser edition is a Java web adaptation: Swing itself cannot run directly inside a browser, so the web interface is rendered by Thymeleaf and SVG.
