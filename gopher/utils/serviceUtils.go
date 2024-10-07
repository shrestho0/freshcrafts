package utils

import (
	"fmt"

	_ "github.com/go-sql-driver/mysql"
	_ "github.com/lib/pq"
)

func CheckKafkaRunning() bool {
	// broker := "localhost:9092"
	return IsKafkaConnectionOk(GetEnv("PRIMARY_KAFKA_BROKER"))
}

func CheckPrimaryMongoRunning() bool {
	return IsMongoDBConnectionOk(GetEnv("PRIMARY_MONGODB_URI"))
}

func CheckSecondaryMongoRunning() bool {
	return IsMongoDBConnectionOk(GetEnv("SECONDARY_MONGODB_URI"))
}

func CheckSecondaryMysqlRunning() bool {
	// dsn := "root:password@tcp(127.0.0.1:13306)/"
	return IsMysqlDBConnectionOk(GetEnv("SECONDARY_MYSQL_DSN"))
}

func CheckSecondaryPostgresRunning() bool {
	// log.Println("SECONDARY_POSTGRES_DSN", GetEnv("SECONDARY_POSTGRES_DSN"))
	dsn := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=disable", GetEnv("SECONDARY_POSTGRES_HOST"), GetEnv("SECONDARY_POSTGRES_PORT"), GetEnv("SECONDARY_POSTGRES_USER"), GetEnv("SECONDARY_POSTGRES_PASSWORD"), GetEnv("SECONDARY_POSTGRES_DBNAME"))
	// dsn := "host=localhost port=15432 user=root password=password dbname=postgres sslmode=disable"
	return IsPostgresDBConnectionOk(dsn)
}

func CheckSecondaryRedisRunning() bool {
	return IsRedisConnectionOk(
		GetEnv("SECONDARY_REDIS_PORT"),
		GetEnv("SECONDARY_REDIS_PASSWORD"))
}

func CheckEngineRunning() bool {
	return CheckSystemdServiceRunning("fc_engine")
}
func CheckCockpitRunning() bool {
	return CheckSystemdServiceRunning("fc_cockpit.service")
}
func CheckDepwizRunning() bool {
	return CheckSystemdServiceRunning("fc_depwiz")
}
func CheckWizardMongoRunning() bool {
	return CheckSystemdServiceRunning("fc_wiz_mongo")
}
func CheckWizardPostgresRunning() bool {
	return CheckSystemdServiceRunning("fc_wiz_postgres")
}

func CheckWizardMysqlRunning() bool {
	return CheckSystemdServiceRunning("fc_wiz_mysql")
}
func CheckRedwizRunning() bool {
	return CheckSystemdServiceRunning("fc_redwiz")
}
