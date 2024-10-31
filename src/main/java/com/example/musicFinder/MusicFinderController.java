package com.example.musicfinder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.exception.ArtistOrSongNotFoundException;

@RestController
public class MusicFinderController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_ENDPOINT = "https://api.lyrics.ovh/v1/";
    private static final String YOUTUBE_SEARCH_URL = "https://www.youtube.com/results?search_query=";

    @GetMapping("/status")
    public String getStatus() {
        return "{\"status\":\"Application is running\"}";
    }

    private String getFormattedLyrics(String artist, String song) throws ArtistOrSongNotFoundException {
        try {

            String userInput = artist + "/" + song;

            if(!isValidInput(userInput)) {
                throw new IllegalArgumentException("Invalid input");
            }

            String apiUrl = UriComponentsBuilder.fromHttpUrl(API_ENDPOINT)
                    .pathSegment(artist, song)
                    .build()
                    .toUriString();

            String rawJson = restTemplate.getForObject(apiUrl, String.class);
            JsonNode jsonNode = objectMapper.readTree(rawJson);
            String rawLyrics = jsonNode.get("lyrics").asText();
            
            String formattedLyrics = rawLyrics.replaceAll("\\r", "");
            formattedLyrics = formattedLyrics.replaceAll("\\n+", "<br>");

            return formattedLyrics.trim();

        } catch (Exception e) {
            throw new ArtistOrSongNotFoundException("Artist or song not found");
        }
    }

    private String getYouTubeSearchUrl(String artist, String song) {
        String searchQuery = artist.replace(" ", "+") + "+" + song.replace(" ", "+");
        return YOUTUBE_SEARCH_URL + searchQuery;
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
        } catch (ArtistOrSongNotFoundException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // Return 404 Not Found
        }
    }


    private boolean isValidInput(String input) {
        // Check if the input contains only alphanumeric characters and spaces
        return input.matches("^[a-zA-Z0-9\\s]+$");
    }
}
