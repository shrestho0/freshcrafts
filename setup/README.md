

### Begin setup

- Copy `.env` file from `.env.example`
- `.env` and `setup-env.py` must be in the same directory
- [Checkout all envs](#environment-variables)

**How common .env file is used?**


Each key:value pair is added to service specific env or application.properties files.
eg: `ENGINE_KEY=VALUE` goes to `engine/.../application.properties` as `KEY=VALUE`

Run `setup_env.py` to setup service specific environments

### Deploy Deployment

Run: `deploy.py`

fc_cockpit

### Environment Variables

COCKPIT_AUTHORIZATION_TOKEN

COCKPIT_SSE_TOKEN

https://github.com/settings/apps/freshcrafts
we'll allow multiple github accounts later


TODO: Details will be added later


#### Cockpit Envs

COCKPIT_ORIGIN=http://freshcrafts.local
COCKPIT_HOST=127.0.0.1
COCKPIT_PORT=10001

COCKPIT_GITHUB_CLIENT_ID=
COCKPIT_GITHUB_CLIENT_SECRET=

COCKPIT_GOOGLE_AUTH_URL=https://accounts.google.com/o/oauth2/auth
COCKPIT_GOOGLE_AUTH_PROVIDER_x509_CERT_URL=https://www.googleapis.com/oauth2/v1/certs
COCKPIT_GOOGLE_CLIENT_ID=
COCKPIT_GOOGLE_CLIENT_SECRET=

#### Cockpit Engine Communication
COCKPIT_ENGINE_BASE_URL=http://127.0.0.1:10000/api/v1
ENGINE_COCKPIT_URL_LOCAL=http://127.0.0.1:10001
GROUPG_COCKPIT_SSE_TOKEN=

COCKPIT_AUTH_COOKIE_NAME=_fc_auth
COCKPIT_AUTH_COOKIE_EXPIRES_IN=604800
COCKPIT_OAUTH_STATE_COOKIE_NAME=oauth_state

#### JWT Stuff
GROUPG_JWT_ACCESS_SECRET=
GROUPG_JWT_REFRESH_SECRET=
GROUPG_JWT_ISSUER=
GROUPG_JWT_ACCESS_EXPIRES_IN=10
GROUPG_JWT_REFRESH_EXPIRES_IN=604800

#### relative to this project or absolute path
COCKPIT_FILE_UPLOAD_DIR=../data/uploads

COCKPIT_GOOGLE_OAUTH_CALLBACK_URL=/_/oauth-callback/google
COCKPIT_GITHUB_OAUTH_CALLBACK_URL=/_/oauth-callback/github
COCKPIT_GITHUB_WEBHOOK_URL=/_/github_webhook/
COCKPIT_GITHUB_WEBHOOK_SECRET=


#### Internal Stuff
COCKPIT_PUBLIC_MYSQL_CONNECTION_HOST_LOCAL=localhost
COCKPIT_PUBLIC_MYSQL_CONNECTION_HOST_REMOTE=thisthing.works
COCKPIT_PUBLIC_MYSQL_CONNECTION_PORT_LOCAL=13306
COCKPIT_PUBLIC_MYSQL_CONNECTION_PORT_REMOTE=13306

COCKPIT_PUBLIC_POSTGRES_CONNECTION_HOST_LOCAL=localhost
COCKPIT_PUBLIC_POSTGRES_CONNECTION_HOST_REMOTE=thisthing.works
COCKPIT_PUBLIC_POSTGRES_CONNECTION_PORT_LOCAL=15432
COCKPIT_PUBLIC_POSTGRES_CONNECTION_PORT_REMOTE=15432

COCKPIT_PUBLIC_MONGO_CONNECTION_HOST_LOCAL=localhost
COCKPIT_PUBLIC_MONGO_CONNECTION_HOST_REMOTE=thisthing.works
COCKPIT_PUBLIC_MONGO_CONNECTION_PORT_LOCAL=17017
COCKPIT_PUBLIC_MONGO_CONNECTION_PORT_REMOTE=17017
                                                             
 
#### Kafka Stuff
ALLSPRING_spring.kafka.consumer.group-id=freshCrafts
ALLSPRING_spring.kafka.bootstrap-servers=localhost:9092

#### Engine Envs

#### Do not change
ENGINE_spring.application.name=engine
ENGINE_server.port=10000
ENGINE_freshCrafts.MONGO_CONFIG_SYSTEM_CONFIG_COLLECTION_ID=TheConfig

#### Application Specific

#### Primary Mongo
ENGINE_spring.data.mongodb.host=localhost
ENGINE_spring.data.mongodb.port=27017
ENGINE_spring.data.mongodb.database=crafts_starter
ENGINE_spring.data.mongodb.username=root
ENGINE_spring.data.mongodb.password=password
ENGINE_spring.data.mongodb.authentication-database=admin
#### OpenAPI Doc Url
ENGINE_springdoc.api-docs.path=/api/v1/api-docs
ENGINE_springdoc.swagger-ui.path=/api/v1/swagger
ENGINE_springdoc.default-produces-media-type=application/json
ENGINE_springdoc.default-consumes-media-type=application/json


#### WIZMONGO Envs
WIZMONGO_spring.application.name=wiz_mongo
WIZMONGO_spring.data.mongodb.host=localhost
WIZMONGO_spring.data.mongodb.port=17017
WIZMONGO_spring.data.mongodb.username=root
WIZMONGO_spring.data.mongodb.password=password
WIZMONGO_spring.data.mongodb.authentication-database=admin

#### WIZMYSQL Envs
WIZMYSQL_spring.application.name=wiz_mysql
WIZMYSQL_freshCrafts.SPRING_DATASOURCE_URL=jdbc:mysql://localhost:13306/
WIZMYSQL_freshCrafts.SPRING_DATASOURCE_USERNAME=root
WIZMYSQL_freshCrafts.SPRING_DATASOURCE_PASSWORD=password

#### WIZPOSTGRES Envs
WIZPOSTGRES_spring.application.name=wiz_postgres
WIZPOSTGRES_freshCrafts.SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:15432/postgres
WIZPOSTGRES_freshCrafts.SPRING_DATASOURCE_USERNAME=root
WIZPOSTGRES_freshCrafts.SPRING_DATASOURCE_PASSWORD=password
