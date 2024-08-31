package config

import (
	"context"
	"log"
	"time"

	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"thisthing.works/watchdog/utils"
)

// func ConnectDB() *mongo.Client {
// 	uri := utils.GetEnv("MONGO_DB_URL")

// 	ctx, _ := context.WithTimeout(context.Background(), 10*time.Second)
// 	client, err := mongo.Connect(ctx, options.Client().ApplyURI(uri))
// 	if err != nil {
// 		log.Println("Failed to connect database")
// 	}
// 	//ping the database
// 	err = client.Ping(ctx, nil)
// 	if err != nil {
// 		// log.Println(err)
// 	}
// 	log.Println("Connected to mango!")

// 	return client

// }

func ConnectDB() (*mongo.Database, error) {
	uri := utils.GetEnv("MONGO_DB_URL")

	ctx, _ := context.WithTimeout(context.Background(), 10*time.Second)
	client, err := mongo.Connect(ctx, options.Client().ApplyURI(uri))

	if err != nil {
		return nil, err
		// log.Println("Failed to connect database")
	}

	// ping the database
	err = client.Ping(ctx, nil)
	if err != nil {
		// log.Println(err)
		return nil, err
	}
	log.Println("Connected to mango!")
	return client.Database(utils.GetEnv("MONGO_DBNAME")), nil
}

// var DB *mongo.Database = ConnectDB()
