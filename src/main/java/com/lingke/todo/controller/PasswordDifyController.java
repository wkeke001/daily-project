package com.lingke.todo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;

@RestController
@RequestMapping("/passwords/dify")
public class PasswordDifyController {

    private static final String DIFY_BASE_URL = "http://175.178.44.121";
    private static final String WORKFLOW_URL = DIFY_BASE_URL + "/v1/chat-messages";
    private static final String UPLOAD_URL = DIFY_BASE_URL + "/v1/files/upload";
    private static final String API_KEY = "Bearer app-aXefakl2oTxFz7FoYXIJa0ZZ";

    @PostMapping("/query")
    public ResponseEntity<String> query(@RequestBody String body) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> resp = rest.exchange(WORKFLOW_URL, HttpMethod.POST, entity, String.class);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resp.getBody());
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ") : "unknown error";
            return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"" + msg + "\"}");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "user", defaultValue = "default") String user) {
        try {
            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", API_KEY);

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            formData.add("file", resource);
            formData.add("user", user);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(formData, headers);
            ResponseEntity<String> resp = rest.exchange(UPLOAD_URL, HttpMethod.POST, entity, String.class);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resp.getBody());
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ") : "unknown error";
            return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"" + msg + "\"}");
        }
    }
}
