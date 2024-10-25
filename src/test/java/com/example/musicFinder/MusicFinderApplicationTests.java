package com.example.musicFinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class MusicFinderApplicationTests {

	@InjectMocks
	private MusicFinderController musicFinderController;

	@Mock
	private RestTemplate restTemplate;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		objectMapper = new ObjectMapper();
	}

	@Test
	void testUnitCorrectLyrics() throws RestClientException, IOException {
		String artist = "Taylor Swift";
		String song = "Love Story";

		// Read expected lyrics from the file
		Path filePath = Path.of("src/test/resources/data/lyrics.txt");
		String expectedLyrics = Files.readString(filePath).replaceAll("\\r", "").replaceAll("\\n+", "<br>");

		assertNotNull(expectedLyrics, "Lyrics file not found");

		// Simulate API JSON response with the lyrics field
		String mockApiResponse = objectMapper
				.writeValueAsString(objectMapper.createObjectNode().put("lyrics", expectedLyrics));

		// Mocking the RestTemplate call to return the expected JSON response
		when(restTemplate.getForObject("https://api.lyrics.ovh/v1/" + artist + "/" + song, String.class))
				.thenReturn(mockApiResponse);

		// Act
		ResponseEntity<ObjectNode> responseEntity = musicFinderController.getSongDetails(artist, song);
		ObjectNode response = responseEntity.getBody();

		// Assert
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(artist, response.get("artist").asText());
		assertEquals(song, response.get("song").asText());
		assertEquals(expectedLyrics, response.get("lyrics").asText());
		assertTrue(response.get("youtubeSearch").asText()
				.contains("https://www.youtube.com/results?search_query=Taylor+Swift+Love+Story"));
	}

	@Test
	void testUnitIncorrectInputs() throws RestClientException, IOException {
		String invalidArtist = "Unknown Artist";
		String song = "Love Story";
		String artist = "Taylor Swift";
		String invalidSong = "Unknown Song";
		String expectedError = "Artist or song not found";

		// Act - Call the controller method with an invalid artist
		ResponseEntity<ObjectNode> responseForInvalidArtist = musicFinderController.getSongDetails(invalidArtist, song);
		ObjectNode response = responseForInvalidArtist.getBody();

		// Assert for invalid artist
		assertEquals(HttpStatus.NOT_FOUND, responseForInvalidArtist.getStatusCode());
		assertEquals(expectedError, response.get("error").asText());

		// Act - Call the controller method with an invalid song
		ResponseEntity<ObjectNode> responseForInvalidSong = musicFinderController.getSongDetails(artist, invalidSong);
		ObjectNode responseForInvalidSongBody = responseForInvalidSong.getBody();

		// Assert - Check error response for song not found
		assertEquals(HttpStatus.NOT_FOUND, responseForInvalidSong.getStatusCode());
		assertEquals(expectedError, responseForInvalidSongBody.get("error").asText());
	}
}
