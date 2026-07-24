package com.example.digitalocean.service;

import com.example.digitalocean.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;


@Slf4j
@Service
public class UrlService {

    public ResponseEntity<?> audit(String url) {

        // Validate URL
        try {
            URI.create(url).toURL();
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid URL. Example: https://www.example.com", HttpStatus.BAD_REQUEST);
        }

        try {
            long start = System.currentTimeMillis();

            Connection.Response response = Jsoup.connect(url)
                    .timeout(10_0000)
                    .followRedirects(true)
                    .execute();

            long responseTime = System.currentTimeMillis() - start;

            Document doc = response.parse();

            Response result = new Response();
            result.setStatus(response.statusCode());
            result.setResponseTime(responseTime);
            result.setMetaDescription(getMetaDescription(doc));
            result.setH1Count(doc.select("h1").size());
            result.setMissingAltImages(
                    doc.select("img:not([alt]), img[alt='']").size()
            );
            result.setWordCount(getWordCount(doc));
            result.setPageTitle(doc.title());

            log.info("{} - {} - {} ms", url, response.statusCode(), responseTime);

            return new ResponseEntity<>(result , HttpStatus.OK);

        } catch (HttpStatusException e) {
            Response r = new  Response();
            r.setStatus(e.getStatusCode());
            return new ResponseEntity<>(r , HttpStatus.OK);
        }catch (MalformedURLException e) {

            log.error("Malformed URL: {}", url, e);
            return new ResponseEntity<>("Malformed URL. try something like : https://www.example.com",HttpStatus.BAD_REQUEST);

        } catch (UnknownHostException e) {

            log.error("Unknown host: {}", url, e);
            return new ResponseEntity<>("Website not found.",HttpStatus.BAD_REQUEST);

        } catch (SocketTimeoutException e) {

            log.error("Timeout: {}", url, e);
            return new ResponseEntity<>("Website took too long to respond.",HttpStatus.REQUEST_TIMEOUT);

        } catch (IOException e) {

            log.error("IO Error while auditing {}", url, e);
            return new ResponseEntity<>("unable to parse the webpage",HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {

            log.error("Unexpected error", e);
            return new ResponseEntity<>("something is wrong with the given url",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getMetaDescription(Document doc) {
        Element meta = doc.selectFirst("meta[name=description]");
        return meta != null ? meta.attr("content") : "";
    }

    private int getWordCount(Document doc) {
        doc.body();
        String text = doc.body().text().trim();
        return text.isEmpty() ? 0 : text.split("\\s+").length;
    }
}
