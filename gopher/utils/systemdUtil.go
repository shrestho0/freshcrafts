package utils

import (
	"log"
	"os/exec"
)

func CheckSystemdServiceRunning(service string) bool {
	// returns 3 if the thing is not active
	// manual test systemctl is-active fc_cockpit fc_engine fc_depwiz fc_wiz_postgres fc_wiz_mongo fc_wiz_mysql
	cmd := exec.Command("systemctl", "is-active", service)
	out, err := cmd.CombinedOutput()
	log.Println("out", string(out))
	if err != nil {
		if exitError, ok := err.(*exec.ExitError); ok {
			log.Printf("Command exited with status: %d\n", exitError.ExitCode())
		} else {
			log.Println("Error executing command:", err)
		}
		log.Printf("[Error] Failed to run `systemctl is-active` for %v\n%v", service, err)

		return false
	}

	// get exit code

	return true
}
