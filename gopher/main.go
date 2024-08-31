package main

import (
	"log"

	"thisthing.works/watchdog/services"
)

func main() {

	// run while application runs

	log.Println("Gopher has started")
	go services.StartCrons()

	// keeping the program running
	select {}

}
