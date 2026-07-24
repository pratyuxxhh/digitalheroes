package com.example.digitalocean;

import com.example.digitalocean.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.digitalocean.entity.Response;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DigitaloceanApplicationTests {

	@Test
	void shouldReturnBadRequestForInvalidUrl() {

		UrlService service = new UrlService();

		ResponseEntity<?> response = service.audit("abc");

		assertEquals(400, response.getStatusCode().value());
		assertTrue(response.getBody().toString().contains("Invalid URL"));
	}

	@Test
	void shouldReturnStatus403WhenWebsiteReturns403() {

		UrlService service = new UrlService();

		ResponseEntity<?> response =
				service.audit("https://httpstat.us/403");

		assertEquals(200, response.getStatusCode().value());

		Response body = (Response) response.getBody();

		assertEquals(403, body.getStatus());
	}

	@Test
	void shouldAuditGoogleSuccessfully() {

		UrlService service = new UrlService();

		ResponseEntity<?> response =
				service.audit("https://example.com");

		assertEquals(200, response.getStatusCode().value());

		Response body = (Response) response.getBody();

		assertEquals(200, body.getStatus());
		assertTrue(body.getWordCount() > 0);
	}

}
