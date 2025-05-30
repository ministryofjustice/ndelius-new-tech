# 'NDelius New Technology' Web Application

[![CircleCI](https://circleci.com/gh/ministryofjustice/ndelius-new-tech.svg?style=svg)](https://circleci.com/gh/ministryofjustice/ndelius-new-tech)

A [Play Framework](https://www.playframework.com/) based website.

Fully asynchronous and non-blocking from the ground up, with the potential to serve 10,000 concurrent users from a single server.

---

## Development

Prerequisites:
- Java 21
- [sbt](http://www.scala-sbt.org/release/docs) (Scala Build Tool)
- Node 22
- [nDelius Wrapper](https://github.com/ministryofjustice/ndelius-wrapper)
- [PDF Generator](https://github.com/ministryofjustice/pdf-generator)
- Docker
- Chrome
- Postman

### Chrome SameSite update

Changes to Chrome mean we must disable SameSite by default cookies in order to run the project locally.

To do this; in Chrome visit:

[chrome://flags/](chrome://flags/)

Change **SameSite by default cookies** to *Disabled*

### Elasticsearch and MongoDB

The project requires MongoDB and Eleasticsearch v6.x, the easiest way is to use Docker Compose:

```
docker-compose up
```

### Populate Elasticsearch

Once Elasticsearch is running, you can populate the instance with a small number of dummy offender records with the included Postman collection:

> Elasticsearch.postman_collection.json

To run the collection with [Newman](https://github.com/postmanlabs/newman):

```
newman run Elasticsearch.postman_collection.json
```

### PDF Generator

Clone the [PDF Generator](https://github.com/ministryofjustice/pdf-generator) repository and run:

```
./gradlew clean run
```

### Run nDelius New Tech

#### Node dependencies

Ensure you are using Node [lts/dubnium](https://nodejs.org/download/release/latest-dubnium/)

```
npm i
```

Pass the required environment variables and start the application:

```
OFFENDER_API_PROVIDER=stub ELASTIC_SEARCH_SCHEME=http STORE_PROVIDER=mongo PARAMS_USER_TOKEN_VALID_DURATION=10000d sbt -Dlogback.application.level=DEBUG run
```

### nDelius Wrapper

Clone the [nDelius Wrapper](https://github.com/ministryofjustice/ndelius-wrapper) repository.

Install dependencies

```
npm i
``` 

Start the nDelius Wrapper

```
ELASTIC_SEARCH_URL=http://127.0.0.1:9200 NEW_TECH_BASE_URL=http://127.0.0.1:9000/ npm start
```

You can now use nDelius New Tech at:

http://127.0.0.1:3000 

---

## Building and running

Prerequisites:
- sbt (Scala Build Tool) http://www.scala-sbt.org/release/docs
- Chrome

Build command (includes running unit and integration tests):
- `sbt assembly`

Environment variables

`
PARAMS_USER_TOKEN_VALID_DURATION=2000d \
STORE_PROVIDER=mongo \
OFFENDER_API_PROVIDER=stub \
APPLICATION_SECRET=mySuperSecretKeyThing \
ELASTIC_SEARCH_HOST=<the hostname of your ES cluster> \
ELASTIC_SEARCH_PORT=443 \
ELASTIC_SEARCH_SCHEME=https \
NOMIS_API_BASE_URL=<the URL of the NOMIS system> \
NOMIS_PAYLOAD_TOKEN=<the NOMIS API payload token> \
NOMIS_PRIVATE_KEY=<the NOMIS API private key> \
ANALYTICS_MONGO_CONNECTION=<the URL of you MongoDb instance>/analytics \
sbt -Dlogback.application.level=DEBUG run`

Running deployable fat jar (after building):
- `APPLICATION_SECRET=abcdefghijk java -jar ndelius2.jar` (in the `target/scala-2.11` directory)

Configuration parameters can be supplied via environment variables. See `application.conf` for full list of variables. 

e.g.:
- `STORE_ALFRESCO_URL=http://alfresco/ sbt run`
- `STORE_ALFRESCO_URL=http://alfresco/ APPLICATION_SECRET=abcdefghijk java -jar ndelius2.jar`

The website endpoint defaults to local port 9000.

Run all tests:
- sbt clean test

Run frontend tests:
- npm test

### Circle CI build

#### Branch build
The build pipeline performs the following steps
- `sbt assembly`
- deploy to the new tech smoke test environment
- run smoke tests using the tests in  [Smoke Tests]

If changes are required to the Smoke Tests which would break a `master` build then just branch the [Smoke Tests] with a branch of the _exact_ name as this branch. CircleCI will attempt to use a matching branch name else will use `master`

#### Master build

The build pipeline performs the following steps
- sbt assembly
- deploy to the new tech dev environment
- deploy to the new tech smoke test environment
- run smoke tests using the tests in  [Smoke Tests]

### Development notes

The [Play Framework](https://www.playframework.com/) provides the [Google Guice](https://github.com/google/guice/wiki/Motivation) Dependency 
Injection framework as standard, and [MVC](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller) based 
Webpages are generated via Play Framework [Twirl](https://www.playframework.com/documentation/2.5.x/ScalaTemplates) templates.

### Building and running with Docker

- Build Docker Image `./buildDocker.sh`
- Run Docker Container `docker run -d -p 9000:9000 --name ndelius2 -e APPLICATION_SECRET=abcdef ndelius2`

### Dependencies
 - Alfresco
 - Delius Offender API 
 - Elastic Search
 - PDF Generator
 - MongoDb (for Analytics)
 - Custody API (NOMIS)

[Smoke Tests]: https://github.com/noms-digital-studio/ndelius-new-tech-smoke-test