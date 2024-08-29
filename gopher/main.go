package main

import (
	"log"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/logger"
	"thisthing.works/watchdog/routes"
	"thisthing.works/watchdog/services"
	"thisthing.works/watchdog/utils"
)

func main() {

	app := fiber.New()

	app.Use(logger.New())

	// routes
	sites := routes.GetSiteRoutes()
	app.Mount("/", sites) // subroute

	// run while application runs
	go services.StartCrons()

	port := utils.GetEnv("PORT")
	log.Fatal(app.Listen(":" + port))

}
