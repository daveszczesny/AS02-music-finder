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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.musicFinder.MusicFinderController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class MusicFinderApplicationTests {

	
	@Mock
	private RestTemplate restTemplate;

	private MusicFinderController musicFinderController;

	private ObjectMapper objectMapper;

	private static final String API_ENDPOINT = "https://api.lyrics.ovh/v1/";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		musicFinderController = new MusicFinderController();
		musicFinderController.setRestTemplate(restTemplate);

		objectMapper = new ObjectMapper();
	}

	@Test
	void testUnitCorrectLyrics() throws RestClientException, IOException {
		String artist = "Taylor Swift";
		String song = "Love Story";
		String youtubeSearch = "https://www.youtube.com/results?search_query=Taylor+Swift+Love+Story";

		// Read expected lyrics from the file
		Path expectedLyricsFilePath = Path.of("src/test/resources/data/expectedLyrics.txt");

		String expectedLyrics = Files.readString(expectedLyricsFilePath);
		assertNotNull(expectedLyrics, "Expected lyrics file not found");

		String lyrics = "We were both young, when I first saw you.\r\nI close my eyes and the flashback starts-\r\nI'm standing there, on a balcony in summer air.\r\nI see the lights; see the party, the ball gowns.\r\nI see you make your way through the crowd-\n\nYou say hello, little did I know...\n\n\n\nThat you were Romeo, you were throwing pebbles-\n\nAnd my daddy said \"stay away from Juliet\"-\n\nAnd I was crying on the staircase-\n\nbegging you, \"Please don't go...\"\n\nAnd I said...\n\n\n\nRomeo take me somewhere, we can be alone.\n\nI'll be waiting; all there's left to do is run.\n\nYou'll be the prince and I'll be the princess,\n\nIt's a love story, baby, just say yes.\n\n\n\nSo I sneak out to the garden to see you.\n\nWe keep quiet, because we're dead if they knew-\n\nSo close your eyes... escape this town for a little while.\n\nOh, Oh.\n\n\n\nCause you were Romeo - I was a scarlet letter,\n\nAnd my daddy said \"stay away from Juliet\" -\n\nbut you were everything to me-\n\nI was begging you, \"Please don't go\"\n\nAnd I said...\n\n\n\nRomeo take me somewhere, we can be alone.\n\nI'll be waiting; all there's left to do is run.\n\nYou'll be the prince and I'll be the princess.\n\nIt's a love story, baby, just say yes-\n\n\n\nRomeo save me, they're trying to tell me how to feel.\n\nThis love is difficult, but it's real.\n\nDon't be afraid, we'll make it out of this mess.\n\nIt's a love story, baby, just say yes.\n\nOh, Oh.\n\n\n\nI got tired of waiting.\n\nWondering if you were ever coming around.\n\nMy faith in you was fading-\n\nWhen I met you on the outskirts of town.\n\nAnd I said...\n\n\n\nRomeo save me, I've been feeling so alone.\n\nI keep waiting, for you but you never come.\n\nIs this in my head, I don't know what to think-\n\nHe knelt to the ground and pulled out a ring and said...\n\n\n\nMarry me Juliet, you'll never have to be alone.\n\nI love you, and that's all I really know.\n\nI talked to your dad -- go pick out a white dress\n\nIt's a love story, baby just say... yes.\n\nOh, Oh, Oh, Oh, Oh.\n\n\n\n'cause we were both young when I first saw you`";

		String formatLyrics = musicFinderController.formatLyrics(lyrics);

		// Mocking the API to return our lyrics
		String apiUrl = UriComponentsBuilder.fromHttpUrl(API_ENDPOINT)
			.pathSegment(artist, song)
			.toUriString();

		String mockApiResponse = objectMapper.writeValueAsString(
			objectMapper.createObjectNode().put("lyrics", formatLyrics)
		);

		when(restTemplate.getForObject(apiUrl, String.class)).thenReturn(mockApiResponse);

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
