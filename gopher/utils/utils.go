package utils

import (
	"bytes"
	"io"
	"log"
	"math/rand"
	"net/http"
	"os"
	"time"

	"github.com/go-playground/validator"
	"github.com/joho/godotenv"
	"github.com/oklog/ulid/v2"
	"thisthing.works/watchdog/models"
)

func GetEnv(key string) string {
	err := godotenv.Load()
	if err != nil {
		log.Println("Can't get the env files")
	}
	return os.Getenv(key)
}

func GetUlid() string {
	entropy := rand.New(rand.NewSource(time.Now().UnixNano()))

	// Generate a ULID
	id, err := ulid.New(ulid.Timestamp(time.Now()), entropy)
	if err != nil {
		log.Println(err)
	}
	return id.String()
}

func FormatValidationError(err error) string {
	msg := "Validation error on "
	for _, err := range err.(validator.ValidationErrors) {
		msg += err.Field() + " " + err.Tag() + ", "
	}

	return msg
}

func ToBsonSlice(results []models.SiteTestResult) []interface{} {
	var bsonSlice []interface{}
	for _, result := range results {
		bsonSlice = append(bsonSlice, result)
	}
	return bsonSlice
}

func RequestGet(url string) ([]byte, int, error) {
	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
		// log.Printf("client: could not create request: %s\n", err)
		return nil, 690, err
	}
	res, err := http.DefaultClient.Do(req)
	if err != nil {
		// log.Printf("client: could not create request: %s\n", err)
		return nil, 691, err
	}
	defer res.Body.Close()
	resBody, err := io.ReadAll(res.Body)
	if err != nil {
		// log.Printf("client: could not create request: %s\n", err)
		return nil, 692, err
	}
	return resBody, res.StatusCode, nil
}

func RequestPost(url string, data []byte) ([]byte, int, error) {
	req, err := http.NewRequest(http.MethodPost, url, bytes.NewBuffer(data))
	if err != nil {
		log.Printf("client: could not create request: %s\n", err)
		return nil, 690, err
	}
	req.Header.Set("Content-Type", "application/json")

	res, err := http.DefaultClient.Do(req)
	if err != nil {
		log.Printf("client: could not create request: %s\n", err)
		return nil, 691, err
	}
	resBody, err := io.ReadAll(res.Body)
	if err != nil {
		log.Printf("client: could not create request: %s\n", err)
		return nil, 692, err
	}
	return resBody, res.StatusCode, nil
}
