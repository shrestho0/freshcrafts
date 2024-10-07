package main

import (
	"log"

	"thisthing.works/watchdog/services"
)

func main() {

	log.Println("Gopher has started")
	go services.StartCrons()

	// keeping the program running
	select {}

}
