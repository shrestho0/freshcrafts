package utils

import (
	"context"
	"database/sql"
	"time"

	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func IsMongoDBConnectionOk(uri string) bool {

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)

	// no idea what it is, intellience suggested
	defer cancel()

	client, err := mongo.Connect(ctx, options.Client().ApplyURI(uri))

	if err != nil {
		// log.Println("Failed to connect database")
		return false
	}
	//ping the database
	err = client.Ping(ctx, nil)

	// no error means okay
	client.Disconnect(ctx)
	return err == nil

}

func IsMysqlDBConnectionOk(dsn string) bool {

	// opening mysql db connection
	db, err := sql.Open("mysql", dsn)
	if err != nil {
		return false
	}

	// Check if the connection is alive by pinging the database
	err = db.Ping()

	return err == nil
}

func IsPostgresDBConnectionOk(dsn string) bool {
	db, err := sql.Open("postgres", dsn)
	// log.Println("db", db)

	if err != nil {
		// log.Printf("failed to open connection: %v", err)
		return false
	}

	// Check if the connection is alive by pinging the database
	err = db.Ping()

	db.Close()
	// log.Println("err", err, "db", db, "errNil", err == nil)

	return err == nil
}
