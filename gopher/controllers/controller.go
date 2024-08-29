package controllers

import (
	"context"
	"fmt"
	"log"

	"github.com/go-playground/validator/v10"
	"github.com/gofiber/fiber/v2"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"thisthing.works/watchdog/config"
	"thisthing.works/watchdog/models"
	"thisthing.works/watchdog/utils"
)

var sitesDatabase *mongo.Database = config.DB.Database(utils.GetEnv("MONGO_DBNAME"))
var validate *validator.Validate = validator.New()

func GetSites(c *fiber.Ctx) error {
	cursor, err := sitesDatabase.Collection("sites").Find(context.TODO(), bson.D{{}})
	if err != nil {
		return c.SendStatus(fiber.StatusInternalServerError)
	}
	defer cursor.Close(context.TODO())

	var sites []models.Site
	for cursor.Next(context.TODO()) {
		var site models.Site
		if err := cursor.Decode(&site); err != nil {
			log.Println("unable to convert to site")
		}
		sites = append(sites, site)
	}

	return c.JSON(sites)
	// return c.SendStatus(400)
}

func PostSiteResults(c *fiber.Ctx) error {
	var siteTestResult []models.SiteTestResult
	if err := c.BodyParser(&siteTestResult); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(models.SitePostResponse{Status: fiber.StatusBadRequest, Msg: "uable to parse data"})
	}
	for index, result := range siteTestResult {
		err := validate.Struct(result)
		if err != nil {
			return c.Status(fiber.StatusBadRequest).JSON(models.SitePostResponse{Status: fiber.StatusBadRequest,
				Msg: fmt.Sprintf("validation error at index: %d, error: %s", index, err.Error())})
		}
	}

	_, err := sitesDatabase.Collection("sitesResults").InsertMany(context.TODO(), utils.ToBsonSlice(siteTestResult))
	if err != nil {
		return c.SendStatus(fiber.StatusInternalServerError)
	}
	return c.JSON(models.SitePostResponse{Status: fiber.StatusOK, Msg: "done"})
}
