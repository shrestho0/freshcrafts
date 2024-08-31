package services

import (
	"time"

	"github.com/go-co-op/gocron"
)

func StartCrons() {
	scheduler := gocron.NewScheduler(time.UTC)

	// Task 0: Ensure Database Connection
	scheduler.Every(1).Minute().Do(ensureDatabaseConnection)

	/// Task 1: Check For Domains from Projects and save to WatchDogProjectsDomainStatus Collection
	scheduler.Every(2).Minute().Do(checkProjectDomains)

	/// Task 2: Check Services Status and save to WatchDogServicesStatus Collection
	scheduler.Every(10).Second().Do(checkServices)

	// Task 3: Check and remove orphaned data everyday at 12:00 AM
	scheduler.Every(6).Hour().Do(removeOrphanedData)

	// Start the scheduler in a non-blocking way
	scheduler.StartAsync()
}
