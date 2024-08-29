from _utils.utils import find_java17

"""
System Setup Related Env Vars
"""

# Note: Ports must be same as in the environment vars / properties files
REQUIRED_OPEN_PORTS=[80,443, 9092,9093, 27017, 17017, 15432, 13306, 10000, 10001,]

ROOT_ENV_FILE = "./.env"


SYSTEMD_SERVICE_DIRECTORY = "/lib/systemd/system/" # for ubuntu based systems

NGINX_DIRECTORY = "/etc/nginx"
DATA_FILE_LOCATION="./installation-data.json"

DOCKER_COMPOSE_COMMAND="docker compose up -d"
DOCKER_COMPOSE_FILE="../docker-compose.yml"

LOG_DIR = "../fc_logs/"
LOG_FILE_NAME = "installer-log"
JAVA_17_AVAILABLE,JAVA_17_DIR = find_java17()
TEMP_DIR = "./fc_temp/"



FC_SERVICES_INFO = {
    "wiz_mysql": {
        "ENV_FILE": "../wiz_mysql/src/main/resources/application.properties",
        "SRC_DIR": "../wiz_mysql",
        "SYSTEMD_SERVICE_NAME": "fc_wiz_mysql",
        "SYSTEMD_SERVICE_TEMPLATE_FILE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE_FILE": "../wiz_mysql/target/wiz_mysql-0.0.1-SNAPSHOT.jar",
        "SYSTEMD_EXEC_COMMAND": "java -jar ",
    },
    "engine": {
        "ENV_FILE": "../engine/src/main/resources/application.properties",
        "SRC_DIR": "../engine",
        "SYSTEMD_SERVICE_NAME": "fc_engine",
        "SYSTEMD_SERVICE_TEMPLATE_FILE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE_FILE": "../engine/target/engine-0.0.1-SNAPSHOT.jar",
        "SYSTEMD_EXEC_COMMAND": "java -jar ",
    },

    "wiz_postgres": {
        "ENV_FILE": "../wiz_postgres/src/main/resources/application.properties",
        "SRC_DIR": "../wiz_postgres",
        "SYSTEMD_SERVICE_NAME": "fc_wiz_postgres",
        "SYSTEMD_SERVICE_TEMPLATE_FILE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE_FILE": "../wiz_postgres/target/wiz_postgres-0.0.1-SNAPSHOT.jar",
        "SYSTEMD_EXEC_COMMAND": "java -jar ",
    },
    "wiz_mongo": {
        "ENV_FILE": "../wiz_mongo/src/main/resources/application.properties",
        "SRC_DIR": "../wiz_mongo",
        "SYSTEMD_SERVICE_NAME": "fc_wiz_mongo",
        "SYSTEMD_SERVICE_TEMPLATE_FILE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE_FILE": "../wiz_mongo/target/wiz_mongo-0.0.1-SNAPSHOT.jar",
        "SYSTEMD_EXEC_COMMAND": "java -jar ",
    },
    
    "dep_wizard": {
        "ENV_FILE": "../depwiz/src/main/resources/application.properties",
        "SRC_DIR": "../depwiz",
        "SYSTEMD_SERVICE_NAME": "fc_depwiz",
        "SYSTEMD_SERVICE_TEMPLATE_FILE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE_FILE": "../depwiz/target/depwiz-0.0.1-SNAPSHOT.jar",
        "SYSTEMD_EXEC_COMMAND": "java -jar ",
    },
    
    "gopher": {
        "ENV_FILE": "../gopher/.env",
        "SRC_DIR": "../gopher",
        "SYSTEMD_SERVICE_NAME": "fc_gopher",
        "SYSTEMD_SERVICE_TEMPLATE_FILE": "./templates/fc_common.service.template",
        "SYSTEMD_EXECUTABLE_FILE": "../gopher/watchdog",
        "SYSTEMD_EXEC_COMMAND": " ",
    },


    "cockpit": {
        "COCKPIT": True,
        "ENV_FILE": "../cockpit/.env",
        "SRC_DIR": "../cockpit",
        "NGINX_CONFIG_TEMPLATE_FILE": "./templates/cocpit_nginx.conf.template",
        "SYSTEMD_SERVICE_NAME": "fc_cockpit",
        "SYSTEMD_SERVICE_TEMPLATE_FILE": "./templates/fc_common.service.template",
        "SYSTEMD_BUILD_DIR": "../cockpit/build",
        "SYSTEMD_EXEC_COMMAND": "/usr/bin/node --env-file=\"{ENV}\" \"{BUILD_DIR}\"", 
        # PROTOCOL_HEADER=x-forwarded-proto HOST_HEADER=x-forwarded-host node --env-file="/mnt/github/tjf/freshcrafts/cockpit/.env" "/mnt/github/tjf/freshcrafts/cockpit/build"
    },


    }

SYSTEM_DEPS = [
        {
            "package": "docker",
            "checker_command": "docker --version",
        },
        {
            "package":"docker-compose",
            "checker_command": "docker compose version",
        },
        {
            "package":"nginx",
            "checker_command": "nginx -v",
        },
        { "package": "java",
            "checker_command": "java -version",
            "required_version": "17",
        },
        {
            "package": "mvn",
            "checker_command": "mvn -v",
        },
        {
            "package": "node",
            "checker_command": "node -v",
            "required_version_min": "20.6",
        },
        {
            "package": "npm",
            "checker_command": "npm -v",
        },
]

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
