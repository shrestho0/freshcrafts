package models

type (
	Site struct {
		Domain string `json:"domain"`
	}

	SiteTestResult struct {
		Domain     string `json:"domain" validate:"required"`
		Working    bool   `json:"working" `
		StatusCode int    `json:"statusCode" validate:"required"`
		Timestamp  string `json:"timestamp" validate:"required"`
	}

	SitePostResponse struct {
		Status int    `json:"status"`
		Msg    string `json:"msg"`
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
