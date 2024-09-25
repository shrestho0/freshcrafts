package fresh.crafts.engine.v1.controllers;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.models.AIChatHistory;
import fresh.crafts.engine.v1.models.AICodeDoc;
import fresh.crafts.engine.v1.services.AIService;
import fresh.crafts.engine.v1.utils.enums.CommonSortField;

@RestController
@RequestMapping(value = "/api/v1/ai", consumes = "application/json", produces = "application/json")
public class AIController {

    @Autowired
    private AIService aiService;

    // get api keys
    @GetMapping("/chat/api-keys")
    public ResponseEntity<CommonResponseDto> getApiKeys() {
        CommonResponseDto res = aiService.getAzureChatApiKeys();

        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PostMapping("/chat/api-keys")
    public ResponseEntity<CommonResponseDto> setApiKeys(
            @RequestBody HashMap<String, String> keys) {
        CommonResponseDto res = aiService.setAzureChatApiKeys(keys);
        // System.out.println("res: " + res);

        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    // create chat history

    @PostMapping("/chat/history")
    public ResponseEntity<CommonResponseDto> saveChat(
            @RequestBody AIChatHistory chat) {
        CommonResponseDto res = aiService.saveChat(chat);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    // get chat history
    @GetMapping("/chat/history")
    public Page<AIChatHistory> getChatHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") CommonSortField orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sort) {

        // no search on notifications, cause, that's the plan

        Pageable pageable = PageRequest.of(page, pageSize, sort, orderBy.getFieldName());

        return aiService.getChatHistoryPaginated(pageable);

    }

    // update chat history, if needed
    // delete chat history
    @DeleteMapping("/chat/history/{id}")
    public ResponseEntity<CommonResponseDto> deleteChatHistory(
            @PathVariable String id) {
        CommonResponseDto res = aiService.deleteChatHistory(id);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    // create vulnaribility report (another type chat history)
    // get vulnaribility report

    // Code Doc Stuff
    // @GetMapping("/code-doc/:id")
    // // return by project id

    @PostMapping("/code-doc")
    public ResponseEntity<CommonResponseDto> createCodeDoc(
            @RequestBody AICodeDoc codeDoc) {
        CommonResponseDto res = aiService.createCodeDoc(codeDoc);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PatchMapping("/code-doc/{id}")
    public ResponseEntity<CommonResponseDto> updateCodeDoc(
            @PathVariable String id,
            @RequestBody AICodeDoc codeDoc) {
        codeDoc.setId(id);
        CommonResponseDto res = aiService.updateCodeDoc(codeDoc);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/code-doc/by-project/{projectId}")
    public List<AICodeDoc> getProjectCodeDocs(
            @PathVariable String projectId) {

        return aiService.getProjectCodeDocs(projectId);

    }

    @DeleteMapping("/code-doc/{id}")
    public ResponseEntity<CommonResponseDto> deleteCodeDoc(
            @PathVariable String id) {
        CommonResponseDto res = aiService.deleteCodeDoc(id);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

}
