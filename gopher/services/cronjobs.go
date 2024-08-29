package services

import (
	"encoding/json"
	"log"
	"time"

	"github.com/go-co-op/gocron"
	"thisthing.works/watchdog/models"
	"thisthing.works/watchdog/utils"
)

func StartCrons() {
	scheduler := gocron.NewScheduler(time.UTC)
	sitesUrl := utils.GetEnv("ENGINE_BASE_URL")
	scheduler.Every(5).Minute().Do(func() {
		log.Println("Cron started")
		resBody, _, err := utils.RequestGet(sitesUrl + "gimme-links")
		if err != nil {
			return
		}
		var sites []models.Site
		err = json.Unmarshal(resBody, &sites)
		if err != nil {
			log.Println("json decode error:", err.Error())
			return
		}
		var sendSites []models.SiteTestResult
		for _, site := range sites {
			_, statusCode, err := utils.RequestGet(site.Domain)
			siteResult := models.SiteTestResult{
				Domain:     site.Domain,
				StatusCode: statusCode,
				Timestamp:  utils.GetUlid(),
				Working:    err != nil && statusCode < 400,
			}
			sendSites = append(sendSites, siteResult)
		}
		jsonData, err := json.Marshal(sendSites)
		if err != nil {
			log.Println("Failed to create result json", err.Error())
			return
		}
		resBody, _, err = utils.RequestPost(sitesUrl+"gimme-feedback", jsonData)
		if err != nil {
			log.Println("send data failed,error:", err.Error())
			return
		}
		var response models.SitePostResponse
		err = json.Unmarshal(resBody, &response)
		if err != nil {
			log.Println("send data json decode error:", err.Error())
			return
		}
		if response.Status == 200 {
			log.Println("Result send to server and saved")
		} else {
			log.Println(response.Msg)
		}

		log.Println("cron ended successful")
	})

	// Start the scheduler in a non-blocking way
	scheduler.StartAsync()
}
