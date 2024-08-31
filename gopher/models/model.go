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
		Timestamp string `json:"timestamp"`

		PrimaryKafka      bool `json:"primary_kafka"`
		PrimaryMongo      bool `json:"primary_mongo"`
		SecondaryMongo    bool `json:"secondary_mongo"`
		SecondaryMysql    bool `json:"secondary_mysql"`
		SecondaryPostgres bool `json:"secondary_postgres"`
	}

	ServiceTestResult struct {
		Timestamp string `json:"timestamp"`

		Engine         bool `json:"engine"`
		Cockpit        bool `json:"cockpit"`
		Depwiz         bool `json:"depwiz"`
		WizardMongo    bool `json:"wizard_mongo"`
		WizardPostgres bool `json:"wizard_postgres"`
		WizardMysql    bool `json:"wizard_mysql"`
	}
)
