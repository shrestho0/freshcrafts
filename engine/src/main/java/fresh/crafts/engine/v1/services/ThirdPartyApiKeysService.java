package fresh.crafts.engine.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.models.ThirdPartyApiKeys;
import fresh.crafts.engine.v1.repositories.ThirdPartyApiKeysRepository;

@Service
public class ThirdPartyApiKeysService {

    @Autowired
    private ThirdPartyApiKeysRepository apiRepo;

    String onlyModelId = "API_KEYS";

    ThirdPartyApiKeys getOnly() {
        // if exists returns, if not create then return
        return apiRepo.findById(onlyModelId).orElseGet(() -> createDefault());
    }

    ThirdPartyApiKeys createDefault() {
        ThirdPartyApiKeys apiKeys = new ThirdPartyApiKeys();
        apiKeys.setId(onlyModelId);
        apiKeys.setIsAzureChatApiSet(false);
        apiKeys.setAzureChatApiEndpoint(null);
        apiKeys.setAzureChatApiKey(null);
        return apiRepo.save(apiKeys);
    }

    ThirdPartyApiKeys updatePartial(ThirdPartyApiKeys apiKeys) {
        ThirdPartyApiKeys existing = getOnly();

        // if apikey and endpoint exists for azure chat, isAzureChatApiSet = true
        // ekta dile, ekta update hobe, duita dile duita, ultimately, jodi duitai exists
        // kore, then it's ready to go
        if (apiKeys.getAzureChatApiKey() != null) {
            existing.setAzureChatApiKey(apiKeys.getAzureChatApiKey());
        }
        if (apiKeys.getAzureChatApiEndpoint() != null) {
            existing.setAzureChatApiEndpoint(apiKeys.getAzureChatApiEndpoint());
        }

        if (existing.getAzureChatApiKey() != null && existing.getAzureChatApiEndpoint() != null) {
            existing.setIsAzureChatApiSet(true);
        }
        // more will be added later

        return apiRepo.save(existing);

    }

}
