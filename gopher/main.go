package main

import (
	"thisthing.works/watchdog/services"
)

func main() {

	// run while application runs
	go services.StartCrons()

	// keeping the program running
	select {}

}
