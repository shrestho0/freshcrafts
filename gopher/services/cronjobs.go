package services

import (
	"time"

	"github.com/go-co-op/gocron"
)

func StartCrons() {
	scheduler := gocron.NewScheduler(time.UTC)

	// Task 0: Ensure Database Connection
	scheduler.Every(1).Minute().Do(ensureDatabaseConnection)

	/// Task 1: Check Services Status and save to WatchDogServicesStatus Collection
	scheduler.Every(1).Minute().Do(checkServices)

	/// Task 2: Check For Domains from Projects and save to WatchDogProjectsOnlineStatus Collection
	scheduler.Every(3).Minute().Do(checkProjectDomains)

	// Task 3: Check and remove orphaned data every 6 hours
	scheduler.Every(6).Hour().Do(removeOrphanedData)

	// Start the scheduler in a non-blocking way
	scheduler.StartAsync()
}
