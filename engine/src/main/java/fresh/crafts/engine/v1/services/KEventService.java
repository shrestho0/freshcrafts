package fresh.crafts.engine.v1.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.repositories.KEventRepository;

@Service
public class KEventService {

    @Autowired
    private KEventRepository kEventRepository;

    public KEvent getById(String kEventId) {

        System.out.println("[TEMP_DEBUG]: KEventService" + kEventId);

        if (kEventId == null)
            return null;

        Optional<KEvent> x = kEventRepository.findById(kEventId);

        if (x.isEmpty())
            return null;

        KEvent y = x.get();
        System.out.println("[TEMP_DEBUG]: Found" + y);

        return y;
    }

    public KEvent createOrUpdate(KEvent kEvent) {
        return kEvent != null ? kEventRepository.save(kEvent) : kEvent;
    }
}
