package utils

import (
	"context"
	"database/sql"
	"log"
	"time"

	"github.com/redis/go-redis/v9"
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

func IsRedisConnectionOk(port string, password string) bool {

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel() // Ensure the context is cancelled when done

	// Create a Redis client
	rdb := redis.NewClient(&redis.Options{
		Addr:     "localhost:" + port,
		Password: password,
		DB:       0, // use default DB
	})

	// Check connection by pinging Redis
	err := rdb.Ping(ctx).Err()
	if err != nil {
		log.Printf("Could not connect to Redis: %v\n", err)
	}

	// log.Println("Connected to Redis successfully!")

	// Close the Redis client connection
	defer func() {
		if err := rdb.Close(); err != nil {
			log.Printf("Could not close Redis connection: %v\n", err)
		}
		log.Println("Redis connection closed successfully!")
	}()

	return err == nil
}
