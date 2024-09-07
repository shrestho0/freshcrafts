package fresh.crafts.engine.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.models.WatchDogServicesStatus;
import fresh.crafts.engine.v1.repositories.WatchDogServicesStatusRepository;
import java.util.List;

@Service
public class WatchDogServicesStatusService {
    @Autowired
    private WatchDogServicesStatusRepository watchDogServicesStatusRepository;

    public List<WatchDogServicesStatus> getWatchDogServicesStatus() {
        return watchDogServicesStatusRepository.findAll();
    }

}
