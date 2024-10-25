package com.example.musicFinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
		Path filePath = Path.of("src\\test\\java\\com\\testData\\LoveStory.txt");
		String expectedLyrics = Files.readString(filePath).replaceAll("\\r", "").replaceAll("\\n+", "<br>");

		// Simulate API JSON response with the lyrics field
		String mockApiResponse = objectMapper
				.writeValueAsString(objectMapper.createObjectNode().put("lyrics", expectedLyrics));

		// Mocking the RestTemplate call to return the expected JSON response
		when(restTemplate.getForObject("https://api.lyrics.ovh/v1/Taylor Swift/Love Story", String.class))
				.thenReturn(mockApiResponse);

		// Act
		ObjectNode response = musicFinderController.getSongDetails(artist, song);

		// Assert
		assertEquals(artist, response.get("artist").asText());
		assertEquals(song, response.get("song").asText());
		assertEquals(expectedLyrics, response.get("lyrics").asText());
		assertTrue(response.get("youtubeSearch").asText()
				.contains("https://www.youtube.com/results?search_query=Taylor+Swift+Love+Story"));
	}
}
