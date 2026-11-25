package com.example.helloworld;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

// Full app tests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloWorldApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void greetsValidName() {
        ResponseEntity<String> response = restTemplate.getForEntity("/hello-world?name=alice", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"message\":\"Hello Alice\"}");
    }

    @ParameterizedTest
    // U+00A0, U+2007, U+202F, U+3000 as URL-encoded UTF-8
    @ValueSource(strings = {"%C2%A0alice", "%E2%80%87alice", "%E2%80%AFalice", "%E3%80%80alice", "alice%E3%80%80"})
    void greetsNameWithEncodedUnicodeSpaces(String encodedName) {
        ResponseEntity<String> response =
                restTemplate.getForEntity(URI.create("/hello-world?name=" + encodedName), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"message\":\"Hello Alice\"}");
    }

    @ParameterizedTest
    @ValueSource(strings = {"^alice", "{alice", "|alice", "`alice", "[alice", "\\alice"})
    void rejectsUnencodedSpecialCharactersWithJson(String name) throws IOException {
        // Raw socket so the character is sent unencoded, exactly as a browser or curl may send it.
        try (Socket socket = new Socket("localhost", port)) {
            OutputStream out = socket.getOutputStream();
            out.write(("GET /hello-world?name=" + name + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
            String response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            assertThat(response).startsWith("HTTP/1.1 400");
            assertThat(response).contains("Content-Type: application/json");
            assertThat(response).contains("{\"error\":\"Invalid Input\"}");
        }
    }

    @Test
    void rejectsInvalidName() {
        ResponseEntity<String> response = restTemplate.getForEntity("/hello-world?name=zoe", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("{\"error\":\"Invalid Input\"}");
    }
}
