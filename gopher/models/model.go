package models

type (
	Site struct {
		Domain string `bson:"domain"`
		Port   int    `bson:"portAssigned"`
		SSL    bool   `bson:"ssl"`
	}

	SiteTestResult struct {
		Domain           string `bson:"domain"`
		Port             int    `bson:"port"`
		Working          bool   `bson:"working" `
		PartiallyWorking bool   `bson:"partiallyWorking"`
		DomainStatusCode int    `bson:"domainStatusCode"`
		PortStatusCode   int    `bson:"portStatusCode"`
		Timestamp        string `bson:"timestamp"`
	}

	SitePostResponse struct {
		Status int    `bson:"status"`
		Msg    string `bson:"msg"`
	}

	DependencyTestResult struct {
		Timestamp         string `bson:"timestamp"`
		PrimaryKafka      bool   `bson:"primary_kafka"`
		PrimaryMongo      bool   `bson:"primary_mongo"`
		SecondaryMongo    bool   `bson:"secondary_mongo"`
		SecondaryMysql    bool   `bson:"secondary_mysql"`
		SecondaryPostgres bool   `bson:"secondary_postgres"`
	}

	ServiceTestResult struct {
		Timestamp      string `bson:"timestamp"`
		Engine         bool   `bson:"engine"`
		Cockpit        bool   `bson:"cockpit"`
		Depwiz         bool   `bson:"depwiz"`
		WizardMongo    bool   `bson:"wizard_mongo"`
		WizardPostgres bool   `bson:"wizard_postgres"`
		WizardMysql    bool   `bson:"wizard_mysql"`
	}
)
