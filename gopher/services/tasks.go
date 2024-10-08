package services

import (
	"context"
	"log"
	"strconv"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"thisthing.works/watchdog/config"
	"thisthing.works/watchdog/models"
	"thisthing.works/watchdog/utils"
)

var sitesDatabase *mongo.Database

func ensureDatabaseConnection() {
	x, err := config.ConnectDB()
	log.Println("DB", x, "err", err)
	if err != nil {
		log.Println("Database connection not established")
		return
	}

	sitesDatabase = x.Client().Database(utils.GetEnv("MONGO_DBNAME"))
}

func checkProjectDomains() {

	if sitesDatabase == nil {
		log.Println("Database connection not established")
		return
	}

	log.Println("Started checking project domains")

	// Find for domains from Projects
	cursor, err := sitesDatabase.Collection("Projects").Find(context.TODO(), bson.D{{}})

	if err != nil {
		// return c.SendStatus(fiber.StatusInternalServerError)
		log.Println("Failed to get data from projects collection")
		return
	}
	defer cursor.Close(context.TODO())

	var sites []models.Site
	for cursor.Next(context.TODO()) {
		var site models.Site
		if err := cursor.Decode(&site); err != nil {
			log.Println("unable to convert to site")
		}
		if site.Domain != "" {
			sites = append(sites, site)
		}
	}

	// sites = append(sites, []models.Site{
	// 	{Domain: "google.com"},
	// 	{Domain: "facebook.com"},
	// 	{Domain: "twitter.com"},
	// }...)

	// log the sites
	// for _, site := range sites {
	// 	log.Println(site.Domain)
	// }

	// mo

	var checkedSites []models.SiteTestResult

	for _, site := range sites {
		protocol := "http://"

		if site.SSL {
			protocol = "https://"
		}

		_, domainStatusCode, err := utils.RequestGet(protocol + site.Domain) // FIXME: check ssl=true in projects, and also check for port
		if err != nil {
			log.Println("Error: DomainRequest", err)
		}

		_, portStatusCode, err := utils.RequestGet("http://localhost:" + strconv.Itoa(site.Port))

		if err != nil {
			log.Println("Error: PortRequest", err)
		}

		siteResult := models.SiteTestResult{
			Domain: site.Domain,
			Port:   site.Port,

			DomainStatusCode: domainStatusCode,
			PortStatusCode:   portStatusCode,

			Timestamp:        utils.GetUlid(),
			Working:          (err == nil) && (domainStatusCode >= 200 && domainStatusCode < 500) && (portStatusCode >= 200 && portStatusCode < 500),
			PartiallyWorking: (domainStatusCode >= 200 && domainStatusCode < 500) || (portStatusCode >= 200 && portStatusCode < 500),
		}

		checkedSites = append(checkedSites, siteResult)
		// log.Println("gg", siteResult)
	}

	// log the checked sites
	// for _, site := range checkedSites {
	// 	log.Println(site)
	// }

	// save the checked sites to the database collection WatchDogProjectsDomainStatus
	// _, err = sitesDatabase.Collection("WatchDogProjectsDomainStatus").InsertMany(context.TODO(), utils.ToBsonSlice(checkedSites))
	// update many by domain name
	for _, site := range checkedSites {
		_, err = sitesDatabase.Collection("WatchDogProjectsDomainStatus").UpdateOne(context.TODO(), bson.D{{Key: "domain", Value: site.Domain}}, bson.D{{Key: "$set", Value: site}}, options.Update().SetUpsert(true))
		if err != nil {
			log.Println(err)
			log.Println("Failed to save data to WatchDogProjectsDomainStatus collection")
			return
		}
	}

	log.Println("Ended checking project domains")
}

func checkServices() {

	if sitesDatabase == nil {
		log.Println("Database connection not established")
		return
	}

	log.Println("Started checking services")
	// Service Type 1: Kafka, Primary Mongo, Secondary Mongo, Mysql, Postgres

	// Service Type 2: Engine, Cockpit, Depwiz, Wiz_Mysql, Wiz_Postgres, Wiz_Mongo

	colName := "WatchDogServicesStatus"
	depId := "dependencies"
	serId := "services"

	timeStamp := utils.GetUlid()

	dependencyTestResult := models.DependencyTestResult{
		Timestamp:         timeStamp,
		PrimaryKafka:      utils.CheckKafkaRunning(),
		PrimaryMongo:      utils.CheckPrimaryMongoRunning(),
		SecondaryMongo:    utils.CheckSecondaryMongoRunning(),
		SecondaryMysql:    utils.CheckSecondaryMysqlRunning(),
		SecondaryPostgres: utils.CheckSecondaryPostgresRunning(),
		SecondaryRedis:    utils.CheckSecondaryRedisRunning(),
	}

	// check systemd service status for these
	serviceTestResult := models.ServiceTestResult{
		Timestamp:      timeStamp,
		Engine:         utils.CheckEngineRunning(),
		Cockpit:        utils.CheckCockpitRunning(),
		Depwiz:         utils.CheckDepwizRunning(),
		WizardMongo:    utils.CheckWizardMongoRunning(),
		WizardPostgres: utils.CheckWizardPostgresRunning(),
		WizardMysql:    utils.CheckWizardMysqlRunning(),
		Redwiz:         utils.CheckRedwizRunning(),
	}

	// update
	_, err := sitesDatabase.Collection(colName).UpdateOne(context.TODO(), bson.D{{Key: "ID", Value: depId}}, bson.D{{Key: "$set", Value: dependencyTestResult}}, options.Update().SetUpsert(true))

	if err != nil {
		log.Println("Failed to update WatchDogServicesStatus results")
	}

	_, err = sitesDatabase.Collection(colName).UpdateOne(context.TODO(), bson.D{{Key: "ID", Value: serId}}, bson.D{{Key: "$set", Value: serviceTestResult}}, options.Update().SetUpsert(true))

	if err != nil {
		log.Println("Failed to update WatchDogServicesStatus results")
	}

	// _, err := sitesDatabase.Collection("Watch")
	// save testRes to WatchDogServiceStatus

	log.Println("Ended checking services")
}

func removeOrphanedData() {
	if sitesDatabase == nil {
		log.Println("Database connection not established")
		return
	}
	log.Println("Started removing orphaned data")
	// remove orphaned data
	// orphaned data are those who can not be found in the projects collection
	// remove data from WatchDogProjectsDomainStatus

	// Find for domains from WatchDogProjectsDomainStatus
	cursor, err := sitesDatabase.Collection("WatchDogProjectsDomainStatus").Find(context.TODO(), bson.D{{}})
	if err != nil {
		// return c.SendStatus(fiber.StatusInternalServerError)
		log.Println("Failed to get data from projects collection")
		return
	}
	defer cursor.Close(context.TODO())

	var sites []models.Site
	for cursor.Next(context.TODO()) {
		var site models.Site
		if err := cursor.Decode(&site); err != nil {
			log.Println("unable to convert to site")
		}
		if site.Domain != "" {
			sites = append(sites, site)
		}
	}

	cursor2, err := sitesDatabase.Collection("Projects").Find(context.TODO(), bson.D{{}})
	if err != nil {
		// return c.SendStatus(fiber.StatusInternalServerError)
		log.Println("Failed to get data from projects collection")
		return
	}
	defer cursor2.Close(context.TODO())

	var projectsDomains []models.Site
	for cursor2.Next(context.TODO()) {
		var project models.Site
		if err := cursor2.Decode(&project); err != nil {
			log.Println("unable to convert to site")
		}
		if project.Domain != "" {
			projectsDomains = append(projectsDomains, project)
		}
	}

	// log the sites
	var orphanedDomains []models.Site
	for _, site := range sites {
		found := false
		for _, project := range projectsDomains {
			if site.Domain == project.Domain {
				found = true
				break
			}
		}
		if !found {
			orphanedDomains = append(orphanedDomains, site)
		}
	}

	// log the orphaned domains
	for _, site := range orphanedDomains {
		// log.Println("orphaned", site.Domain)

		// delete orphaned data
		_, err = sitesDatabase.Collection("WatchDogProjectsDomainStatus").DeleteOne(context.TODO(), bson.D{{Key: "domain", Value: site.Domain}})
		if err != nil {
			log.Println("Failed to delete data from WatchDogProjectsDomainStatus collection")
			return
		}

	}

	log.Println("Ended removing orphaned data")

}
