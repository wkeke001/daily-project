package com.lingke.todo.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/ai")
public class AiProxyController {

    private static final String API_URL = "http://127.0.0.1:8080/v1/workflows/run";
    private static final String API_KEY = "Bearer app-jUCEC5JnAZuyB1Yy0KFnCGSG";

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String body) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> resp = rest.exchange(API_URL, HttpMethod.POST, entity, String.class);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resp.getBody());
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ") : "unknown error";
            return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"" + msg + "\"}");
        }
    }

    @PostMapping("/chat/stream")
    public void chatStream(@RequestBody String body, HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = response.getWriter()) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
                writer.flush();
            }
        } catch (Exception e) {
            response.getWriter().write("data: {\"error\":\"" + e.getMessage() + "\"}\n\n");
        } finally {
            conn.disconnect();
        }
    }
}
