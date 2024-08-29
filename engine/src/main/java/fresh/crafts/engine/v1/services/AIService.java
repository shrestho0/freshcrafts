package fresh.crafts.engine.v1.services;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
// import fresh.crafts.engine.v1.entities.AIChatMessage;
import fresh.crafts.engine.v1.models.AIChatHistory;
import fresh.crafts.engine.v1.models.ThirdPartyApiKeys;
import fresh.crafts.engine.v1.repositories.AIChatHistoryRepository;

@Service
public class AIService {

    @Autowired
    private ThirdPartyApiKeysService apiKeysService;

    @Autowired
    private AIChatHistoryRepository chatHistoryRepository;

    public CommonResponseDto getAzureChatApiKeys() {
        CommonResponseDto res = new CommonResponseDto();
        ThirdPartyApiKeys apiKeys = apiKeysService.getOnly();

        if (apiKeys.getIsAzureChatApiSet()) {

            HashMap<String, String> keys = new HashMap<>();
            keys.put("azureChatApiEndpoint", apiKeys.getAzureChatApiEndpoint());
            keys.put("azureChatApiKey", apiKeys.getAzureChatApiKey());

            res.setPayload(keys);
            res.setSuccess(true);
            res.setStatusCode(200);
        } else {
            res.setSuccess(false);
            res.setStatusCode(404);
            res.setMessage("Azure Chat API keys not set");
        }

        return res;
    }

    public CommonResponseDto setAzureChatApiKeys(HashMap<String, String> keys) {
        CommonResponseDto res = new CommonResponseDto();
        ThirdPartyApiKeys apiKeys = apiKeysService.getOnly();
        System.out.println("req: " + keys);
        if (keys.containsKey("azureChatApiEndpoint") && keys.containsKey("azureChatApiKey")) {
            apiKeys.setAzureChatApiEndpoint(keys.get("azureChatApiEndpoint"));
            apiKeys.setAzureChatApiKey(keys.get("azureChatApiKey"));
            // apiKeys.setIsAzureChatApiSet(true); // will be done by update partial
            apiKeysService.updatePartial(apiKeys);
            res.setSuccess(true);
            res.setStatusCode(201);
            res.setMessage("Azure Chat API keys set successfully");
        } else {
            res.setSuccess(false);
            res.setStatusCode(400);
            res.setMessage("Invalid request. Valid keys are: azureChatApiEndpoint, azureChatApiKey");
        }

        return res;
    }

    public CommonResponseDto saveChat(AIChatHistory chat) {
        CommonResponseDto res = new CommonResponseDto();

        try {
            AIChatHistory savedChatHistory = chatHistoryRepository.save(chat);
            // show as json
            // Gson gson = new Gson();
            // String json = gson.toJson(savedChatHistory);
            // System.out.println("savedChatHistory: " + json);
            res.setSuccess(true);
            res.setStatusCode(201);
            res.setMessage("Chat history saved successfully");
            // res.setPayload(savedChatHistory); // not needed

        } catch (Exception e) {
            res.setSuccess(false);
            res.setStatusCode(400);
            res.setMessage("Failed to save chat history");
            return res;
        }

        return res;

    }

    public Page<AIChatHistory> getChatHistoryPaginated(Pageable pageable) {
        return chatHistoryRepository.findAll(pageable);
    }

    public CommonResponseDto deleteChatHistory(String id) {
        CommonResponseDto res = new CommonResponseDto();
        try {
            chatHistoryRepository.deleteById(id);
            res.setSuccess(true);
            res.setStatusCode(200);
            res.setMessage("Chat history deleted successfully");
        } catch (Exception e) {
            res.setSuccess(false);
            res.setStatusCode(400);
            res.setMessage("Failed to delete chat history");
        }
        return res;
    }

}