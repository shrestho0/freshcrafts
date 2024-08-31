package utils

import (
	"log"
	"time"

	"github.com/IBM/sarama"
)

func CheckKafkaRunning() bool {
	broker := "localhost:9092"

	// Create a new Sarama configuration
	config := sarama.NewConfig()
	config.Producer.RequiredAcks = sarama.WaitForAll
	config.Producer.Retry.Max = 5
	config.Producer.Return.Successes = true
	config.Net.DialTimeout = 5 * time.Second

	// Create a new broker connection
	brokerConnection := sarama.NewBroker(broker)

	// Open the connection
	err := brokerConnection.Open(config)
	if err != nil {
		log.Printf("Failed to connect to Kafka broker: %v", err)
	}

	// Check if the broker is connected
	connected, err := brokerConnection.Connected()
	if err != nil {
		log.Printf("Failed to check if Kafka broker is connected: %v", err)
	}

	if connected {
		log.Println("Kafka is running")

	} else {
		log.Println("Kafka is not running")
	}

	// Close the connection
	brokerConnection.Close()

	return connected
}
