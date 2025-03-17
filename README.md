# Description

Our Recipe Sharing Platform is a Java Spring Boot web service for discovering, storing, and sharing recipes. It features user authentication, recipe management and social engagement like adding to favorites and commenting.

## User Authentication & Profile & Contributors

- Register a new user/chef (unique email, chef must be 18+)
- Login a user
- Change password (must have at least one uppercase letter, one number and a minimum length of 8 characters)
- Get user profile
- Update user profile
- Delete user profile
- Follow user (users can follow chefs and only chefs can be followed)
- Add contributor to recipe (to be a contributor you need to be an authorized chef with at least 3 recipes posted and a following of 2 and 18+)
- Remove contributor
- Get recipes from chefs you follow

## Recipe Management & Favorites

- Add a new recipe (only a chef can add a recipe)
- Get all recipes (filtered by popularity and age restriction and vegetarian preferences)
- Get a recipe by ID
- Update a recipe (only the author or contributors can update a recipe)
- Delete a recipe
- Change frozen status (only the author can change it)
- Featured recipe for each chef account (must be authorized, with at least 3 recipes and at least 4 likes in total)
- Add recipe to favorites (only users)
- Remove recipe from favorites

## Comments & Additional Features

- Leave a comment on a recipe if not frozen
- Get comments for a recipe filtered by popularity
- Update a comment
- Delete a comment
- Filter recipes by category and sort by popularity
- Like comment
- Unlike comment
- Report comment

# Prerequisites

For using Github Codespaces, no prerequisites are mandatory.
Follow the [./PREREQUISITES.md](./PREREQUISITES.md) instructions to configure a local virtual machine with Ubuntu, Docker, IntelliJ.

# Access the code

* Fork the code GitHub repository under your Organization
  * https://github.com/UNIBUC-PROD-ENGINEERING/service
* Clone the code repository:
  * git@github.com:YOUR_ORG_NAME/service.git

# Run code in Github Codespaces

* Make sure that the Github repository is forked under your account / Organization
* Create a new Codespace from your forked repository
* Wait for the Codespace to be up and running
* Make sure that Docker service has been started
    * ```docker ps``` should return no error
* For running all services in docker:
    * Build the docker image of the hello world service
        * ```make build```
    * Start all the service containers
        * ```./start.sh```
* For running / debugging directly in Visual Studio Code
    * Start the MongoDB related services
        * ```./start_mongo_only.sh```
    * Start the Spring Boot service by clicking `Run` button inside Visual Studio Code
* Use [requests.http](requests.http) to test API endpoints

NOTE: for a live demo, please check out [this youtube video](https://youtu.be/-9ePlxz03kg)

# Run/debug code in IntelliJ
* Build the code
    * IntelliJ will build it automatically
    * If you want to build it from command line and also run unit tests, run: ```./gradlew build```
* Create an IntelliJ run configuration for a Jar application
    * Add in the configuration the JAR path to the build folder `./build/libs/hello-0.0.1-SNAPSHOT.jar`
* Start the MongoDB container using docker compose
    * ```docker-compose up -d mongo```
* Run/debug your IntelliJ run configuration
* Open in your browser:
    * http://localhost:8080/hello-world
    * http://localhost:8080/info

# Deploy and run the code locally as docker instance

* Build the docker image of the hello world service
    * ```make build```
* Start all the containers
    * ```./start.sh```

* Verify that all containers started, by running
  ```
  service git:(master) ✗  $ docker ps
  CONTAINER ID   IMAGE           COMMAND                  CREATED         STATUS         PORTS                      NAMES
  c1d05dddd3fe   mongo:5.0.2     "docker-entrypoint.s…"   6 seconds ago   Up 5 seconds   0.0.0.0:27017->27017/tcp   service_mongo_1
  e90bb406c139   hello-img       "java -jar /hello/li…"   6 seconds ago   Up 5 seconds   0.0.0.0:8080->8080/tcp     service_hello_1
  411475a7b596   mongo-express   "tini -- /docker-ent…"   6 seconds ago   Up 2 seconds   0.0.0.0:8090->8081/tcp     service_mongo-admin-ui_1
  ```
* Open in your browser:
    * http://localhost:8080/hello-world
    * http://localhost:8080/info
* You can test other API endpoints using [requests.http](requests.http)
* You can access the MongoDB Admin UI at:
  * http://localhost:8090 
