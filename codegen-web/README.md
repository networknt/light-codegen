# codegen-web

## Running codegen-web locally

#### Building & running the server
1. Build the artifact with maven:
    - `mvn clean package -Dmaven.test.skip=true`
2. Serve the application from the shaded jar:
    - `java -jar target/codegen-web-shaded.jar` 
    
#### Building the view
1. Install all dependencies: 
    - `cd view`
    - `yarn install`
2. Start the create-react-app development server:
    - `yarn start`
   

## Running codegen-web in docker

**Ensure the localnet network exists:** `docker network create localnet` 

#### Building the server
1. Build the artifact with maven:
    - `mvn clean package -Dmaven.test.skip=true`
2. Build and tag the docker image:
    - `docker build -t light/codegen-web-server:latest .`
3. Starting:
    - `docker run -d --name codegen-web-server --network localnet -p 8080:8080 light/codegen-web-server:latest`

#### Building the view
1. Build and tag the docker image:
    - `docker build -t light/codegen-web-view:latest -f view/Dockerfile view`
2. Starting:
    - `docker run -d --network localnet -p 3000:3000 light/codegen-web-view:latest`

### Starting with docker-compose:

#### Starting the application:

- `docker-compose up`

`-d` to run in background. 

#### Stopping the application in the background:

- `docker-compose down`

