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
		Status int    `josn:"status"`
		Msg    string `json:"msg"`
	}
)
