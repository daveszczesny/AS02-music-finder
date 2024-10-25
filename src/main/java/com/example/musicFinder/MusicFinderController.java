package com.example.musicFinder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MusicFinderController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/status")
    public String getStatus() {
        return "{\"status\":\"Application is running\"}";
    }

    private String getFormattedLyrics(String artist, String song) {

        String apiUrl = "https://api.lyrics.ovh/v1/" + artist + "/" + song;
        try {

            String rawJson = restTemplate.getForObject(apiUrl, String.class);
            JsonNode jsonNode = objectMapper.readTree(rawJson);
            String rawLyrics = jsonNode.get("lyrics").asText();
            return rawLyrics.replaceAll("\\r", "").replaceAll("\\n+", "<br>").trim();
        } catch (Exception e) {
            throw new RuntimeException("Artist or song not found");
        }
    }

    private String getYouTubeSearchUrl(String artist, String song) {
        String searchQuery = artist.replace(" ", "+") + "+" + song.replace(" ", "+");
        return "https://www.youtube.com/results?search_query=" + searchQuery;
    }

    @GetMapping("/song/{artist}/{name}")
    public ResponseEntity<ObjectNode> getSongDetails(@PathVariable String artist, @PathVariable String name) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("artist", artist);
        response.put("song", name);
        response.put("youtubeSearch", getYouTubeSearchUrl(artist, name));

        try {
            String lyrics = getFormattedLyrics(artist, name);
            response.put("lyrics", lyrics);
            return ResponseEntity.ok(response); // Return 200 OK
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // Return 404 Not Found
        }
    }
}
