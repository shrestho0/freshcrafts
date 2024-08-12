

# EXECUTABLE_RELATIVE_LOCATIONS = {
#     "engine": "../engine/target/engine-0.0.1-SNAPSHOT.jar",
#     "cockpit": "../cockpit/build", # run 
# }

# Note: Ports must be same as in the environment vars / properties files
REQUIRED_OPEN_PORTS=[80,443, 9092,9093, 27017, 17017, 15432, 13306, 10000, 10001,]
SYSTEMD_SERVICE_DIRECTORY = "/lib/systemd/system/" # for ubuntu based systems
NGINX_DIRECTORY = "/etc/nginx"
DATA_FILE_LOCATION="./installation-data.json"
LOG_DIR = "../fc_logs/"
LOG_FILE_NAME = "installer-log"

TEMP_DIR = "./fc_temp/"

FC_SERVICES_INFO = {
    "engine": {
        "ENV_FILE": "../engine/src/main/resources/application.properties",
        "SYSTEMD_SERVICE_NAME": "fc_engine",
        "SYSTEMD_SERVICE_TEMPLATE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE": "../engine/target/engine-0.0.1-SNAPSHOT.jar",
        "SYSTEMD_EXEC_COMMAND": "java -jar ",
    },
    "wiz_mysql": {
        "ENV_FILE": "../wiz_mysql/src/main/resources/application.properties",
        "SYSTEMD_SERVICE_NAME": "fc_wiz_mysql",
        "SYSTEMD_SERVICE_TEMPLATE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE": "../wiz_mysql/target/wiz_mysql-0.0.1-SNAPSHOT.jar",
        "SYSTEMD_EXEC_COMMAND": "java -jar ",
    },

    "wiz_postgres": {
        "ENV_FILE": "../wiz_postgres/src/main/resources/application.properties",
        "SYSTEMD_SERVICE_NAME": "fc_wiz_postgres",
        "SYSTEMD_SERVICE_TEMPLATE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE": "../wiz_postgres/target/wiz_postgres-0.0.1-SNAPSHOT.jar",
        "SYSTEMD_EXEC_COMMAND": "java -jar ",
    },
    "wiz_mongo": {
        "ENV_FILE": "../wiz_mongo/src/main/resources/application.properties",
        "SYSTEMD_SERVICE_NAME": "fc_wiz_mongo",
        "SYSTEMD_SERVICE_TEMPLATE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE": "../wiz_mongo/target/wiz_mongo-0.0.1-SNAPSHOT.jar",
        "SYSTEMD_EXEC_COMMAND": "java -jar ",
    },


    "cockpit": {
        "COCKPIT": True,
        "ENV_FILE": "../cockpit/.env",
        "SYSTEMD_SERVICE_NAME": "fc_cockpit",
        "SYSTEMD_SERVICE_TEMPLATE": "./templates/fc_common.service.template",
        "SYSTEMD_BUILD_DIRECTORY": "../cockpit/build",
        "SYSTEMD_EXEC_COMMAND": "/usr/bin/node --env-file=\"{ENV}\" \"{BUILD_DIR}\"", 
        # PROTOCOL_HEADER=x-forwarded-proto HOST_HEADER=x-forwarded-host node --env-file="/mnt/github/tjf/freshcrafts/cockpit/.env" "/mnt/github/tjf/freshcrafts/cockpit/build"
    },


    }



# FC_SERVICES_ENV_LOCATIONS = {
#     "engine": {       
#         "env": "../engine/src/main/resources/application.properties",
#     },
#     "cockpit": {
#         "env": "../cockpit/.env",
#     },
#     "wiz_mysql": {
#         "env": "../wiz_mysql/src/main/resources/application.properties",
#     },
#     "wiz_postgres": {
#         "env": "../wiz_postgres/src/main/resources/application.properties",
#     },
#     "wiz_mongo": {
#         "env": "../wiz_mongo/src/main/resources/application.properties",
#     },
# }
