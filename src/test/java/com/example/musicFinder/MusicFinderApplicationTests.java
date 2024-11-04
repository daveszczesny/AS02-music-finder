package com.example.musicfinder;

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
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.musicfinder.MusicFinderController;

@SpringBootTest
class MusicFinderApplicationTests {

	
	@Mock
	private RestTemplate restTemplate;

	private MusicFinderController musicFinderController;

	private static final String API_ENDPOINT = "https://api.lyrics.ovh/v1/";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		musicFinderController = new MusicFinderController();
		musicFinderController.setRestTemplate(restTemplate);
	}

	@Test
	void testUnitCorrectLyrics() throws RestClientException, IOException {
		String artist = "Taylor Swift";
		String song = "Love Story";
		String youtubeSearch = "https://www.youtube.com/results?search_query=Taylor+Swift+Love+Story";

		// Read expected lyrics from the file
		Path rawLyricsFilePath = Path.of("src/test/resources/data/lyrics.txt");
		Path expectedLyricsFilePath = Path.of("src/test/resources/data/expectedLyrics.txt");
		String rawLyrics = Files.readString(rawLyricsFilePath);
		String expectedLyrics = Files.readString(expectedLyricsFilePath);

		assertNotNull(expectedLyrics, "Lyrics file not found");

		// Mocking the API to return our lyrics

		String apiUrl = UriComponentsBuilder.fromHttpUrl(API_ENDPOINT)
			.pathSegment(artist, song)
			.toUriString();
		when(restTemplate.getForObject(apiUrl, String.class)).thenReturn(rawLyrics);

		// Call the controller method
		ResponseEntity<ObjectNode> responseEntity = musicFinderController.getSongDetails(artist, song);
		ObjectNode response = responseEntity.getBody();
		// Assert
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(artist, response.get("artist").asText());
		assertEquals(song, response.get("song").asText());
		assertEquals(expectedLyrics, response.get("lyrics").asText());
		assertTrue(response.get("youtubeSearch").asText().contains(youtubeSearch));
	
	}

	@Test
	void testUnitIncorrectInputs() throws RestClientException {
		String invalidArtist = "Unknown Artist";
		String song = "Love Story";
		String artist = "Taylor Swift";
		String invalidSong = "Unknown Song";
		String expectedError = "Artist or song not found";

		// Call the controller method with an invalid artist
		ResponseEntity<ObjectNode> responseForInvalidArtist = musicFinderController.getSongDetails(invalidArtist, song);
		ObjectNode response = responseForInvalidArtist.getBody();

		// Assert for invalid artist
		assertEquals(HttpStatus.NOT_FOUND, responseForInvalidArtist.getStatusCode());
		assertEquals(expectedError, response.get("error").asText());

		// Call the controller method with an invalid song
		ResponseEntity<ObjectNode> responseForInvalidSong = musicFinderController.getSongDetails(artist, invalidSong);
		ObjectNode responseForInvalidSongBody = responseForInvalidSong.getBody();

		// Assert - Check error response for song not found
		assertEquals(HttpStatus.NOT_FOUND, responseForInvalidSong.getStatusCode());
		assertEquals(expectedError, responseForInvalidSongBody.get("error").asText());
	}

}
