[![SOFT](https://github.com/BananazTechnology/sale-bot-consumer/actions/workflows/SOFT.yml/badge.svg?branch=develop)](https://github.com/BananazTechnology/sale-bot-consumer/actions/workflows/SOFT.yml) [![RELEASE](https://github.com/BananazTechnology/sale-bot-consumer/actions/workflows/RELEASE.yml/badge.svg)](https://github.com/BananazTechnology/sale-bot-consumer/actions/workflows/RELEASE.yml)

# Sale Bot Consumer
* Description: A @spring-projects framework project which loads items into @mysql
* Version: (Check main for release or develop for dev)
* Creator: Aaron Renner
* THIS REPO IS A SALE EVENT CONSUMER

### Table of Contents
* [Introduction](#introduction)
* Setup *"How to"*
  * [Run Spring-Boot](#running-the-project)
* Help
  * [Setup Libraries and Examples](#libraries)
  
## Introduction

This Java application is built on the Spring-Boot framework! This consumer bot utilizes our [Sale Producer](https://github.com/BananazTechnology/sale-bot-producer) and listens for new events in our MySQL database then processes them based on the `contract_id` column value by matching them to the config in memory from [Market Config API](https://github.com/BananazTechnology/market-bot-config-api-sb). Unlike our original sale bot [discord-nft-sale-bot](https://github.com/Aman7123/discord-nft-sale-bot) which was configured on a per-instance basis with individual tokens, this revised setup requires only configuration to the organization DB which contains all configurations and are load balanced to these consumer bots. This bot includes a small internal API for obtaining the state configuration of each contract.

THIS REPO IS A SALE EVENT CONSUMER

## Setup
### Properties

Through environment variables or by modifying the application.properties the following values need to be set:

``` yaml
# API Config
server:
  # Listening port
  port: 8080
  # Open address
  address: 0.0.0.0
  servlet:
    # The base path after http://host:port
    context-path: /api
# MySQL Config
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://host:port/DB-Name?createDatabaseIfNotExist=true
    username: username
    password: password
  jpa:
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
```

### Running the Project

Executing the project can be done in two ways, the first is by initializing using Maven and the second produces a traditional Jar file. Before attempting to run the program some setup must be done inside of the [src/main/resources/application.properties](src/main/resources/application.yml), you can follow the guides above for setting up the properties.

### Accessing internal debug API
Assuming the `server` properties are configured as shown above.

* `https://localhost:8080/api/contracts` - This shows the current internal configuration of all contracts.
* `https://localhost:8080/api/actuator` - Different endpoints to view the status of Spring.

In a production environment, these debug ports and routes should be hardened, this [Production Monitoring](https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/html/production-ready-monitoring.html) guide describes the settings for changing the actuator port. The `/contracts` endpoint is read-only and contains no sensitive data.

### Build with Maven

If you have Maven installed on your machine you can navigate to the root project directory with this README file and execute the following. Remember to follow the above Database setup procedures first.
```sh
mvn -B -DskipTests clean package
```
You can also use the built-in Maven wrapper and execute the project by following this command.
```sh
./mvnw -B -DskipTests clean package
```
### Setting up in IDE

Download Lombok to your IDE or VS Code Extension!
Use the IDE "Run Configuration" to set up the `-Dspring.datasource.username` (eclipse example) in the Environment Properties.

### Creating a Docker Image

To build a container that can execute the application from a safe location you can use my supplied [Dockerfile](Dockerfile) to do so. You should follow the guides first to better understand some of these arguments.

```Dockerfile
CMD [ "java", \
        "-jar", \
        "Bot.jar"]
```

## Libraries

### Jump To
* [Required Dependencies](#required)
* [Included Dependencies](#included)
  * [JSON Smart](#json-smart)
  * [Javacord](#javacord)
  * [Twittered](#twittered)
  * [MySQL & JPA](#mysql-and-jpa)

### Required
You are required to install this into IDE
* [Lombok - Automated Class Method Generation](https://projectlombok.org/features/all)
```pom
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### Included
<details><summary>JSON-Smart</summary>
* [JSON Parser JAVADOC](https://javadoc.io/doc/net.minidev/json-smart/latest/index.html)
```pom
<dependency>
    <groupId>net.minidev</groupId>
    <artifactId>json-smart</artifactId>
</dependency>
```
</details>
<details><summary>Javacord</summary>
* [Javacord](https://github.com/Javacord/Javacord)
```pom
<dependency>
    <groupId>org.javacord</groupId>
    <artifactId>javacord</artifactId>
    <version>3.3.2</version>
    <type>pom</type>
</dependency>
```
</details>
<details><summary>Twittered</summary>
* [Twittered](https://github.com/redouane59/twittered)
```pom
<dependency>
    <groupId>com.github.redouane59.twitter</groupId>
    <artifactId>twittered</artifactId>
    <version>1.26</version>
</dependency>
```
</details>
<details><summary>MySQL and JPA</summary>
* [MySQL](https://mvnrepository.com/artifact/mysql/mysql-connector-java)
* [JPA](https://spring.io/guides/gs/accessing-data-jpa/)
```pom
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
</details>