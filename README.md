# Description

Our Recipe Sharing Platform is a Java Spring Boot web service for discovering, storing, and sharing recipes. It features user authentication, recipe management and social engagement like adding to favorites and commenting.

## User Authentication & Profile

- Register a new user: POST /api/auth/register
- Login a user: POST /api/auth/login
- Change password: PUT /api/auth/change-password
- Get user profile: GET /api/users/get-profile
- Update user profile: PUT /api/users/edit
- Delete user profile: DELETE /api/users/delete
- Follow user: POST /api/users/follow/{id-user}
- Solicit edit access to recipe (to be a contributor you need to be an authorized chef with at least 5 recipes posted and a following of 100 and 18+): POST /api/users/solicit-access/{id-post}
- Get recipes from chefs you follow: GET /api/users/get-feed

## Recipe Management & Favorites

- Add a new recipe: POST /api/recipes/add
- Get all recipes filtered by popularity and age restriction and vegetarian preferences: GET /api/recipes/get-recipes
- Get a recipe by ID: GET /api/recipes/get-recipe/{id-recipe}
- Update a recipe: PUT /api/recipes/update/{id-recipe}
- Delete a recipe: DELETE /api/recipes/delete/{id-recipe}
- Change frozen status: POST /api/recipes/change-status/{id-recipe}
- Weekly recipe (from an authorized chef with at least 5 recipes, at least 100 likes): GET api/recipes/get-weekly-recipe
- Add recipe to favorites: POST /api/favorites/add/{id-recipe}
- Remove recipe from favorites: POST /api/favorites/remove/{id-recipe}
- Get favorite recipes of a user: GET /api/favorites/get-favorites

## Additional Features

- Leave a comment on a recipe if not frozen: POST /api/comments/{id-recipe}
- Get comments for a recipe filtered by popularity: GET /api/comments/get-comments/{id-recipe}
- Update a comment: PUT /api/comments/edit/{id-comment}
- Delete a comment: DELETE /api/comments/delete/{id-comment}
- Filter recipes by category and sort by popularity: GET /api/comments/filter?category={category}
- Like comment: POST /api/comments/like/{id-recipe}
- Unlike comment: POST /api/comments/unlike/{id-recipe}
- Report comment: POST /api/comment/report/(id-comment)

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
