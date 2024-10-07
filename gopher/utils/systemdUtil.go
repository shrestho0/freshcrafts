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
	log.Println("err", err)

	exitCode := cmd.ProcessState.ExitCode()
	log.Println("exitCode", exitCode)

	switch exitCode {
	case 0:
		fallthrough
	// case 3:
	// return true
	// fallthrough
	default:
		return err == nil // error nil so no error thus success
	}

}
