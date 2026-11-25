package com.example.helloworld.controller;

import com.example.helloworld.exception.InvalidInputException;
import com.example.helloworld.service.GreetingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Controller tests with a mocked service
@WebMvcTest(HelloWorldController.class)
class HelloWorldControllerTest {

    private static final String REJECTION_REASON = "rejected by a service rule";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GreetingService greetingService;

    @Test
    void returnsServiceGreetingAsJson() throws Exception {
        scenario()
                .givenServiceGreets("alice", "Hello Alice")
                .withName("alice")
                .whenGet()
                .thenRespondsWithGreeting("Hello Alice")
                .andServiceWasCalledWith("alice");
    }

    @ParameterizedTest
    @ValueSource(strings = {"  alice  ", "", "zoe"})
    void passesNameToServiceUnchanged(String name) throws Exception {
        scenario()
                .givenServiceGreets(name, "Hello")
                .withName(name)
                .whenGet()
                .thenRespondsWithGreeting("Hello")
                .andServiceWasCalledWith(name);
    }

    @Test
    void mapsServiceRejectionToInvalidInput() throws Exception {
        scenario()
                .givenServiceRejects("zoe")
                .withName("zoe")
                .whenGet()
                .thenRespondsInvalidInputAsJson()
                .andServiceWasCalledWith("zoe");
    }

    @Test
    void rejectsMissingNameWithoutCallingService() throws Exception {
        scenario()
                .whenGet()
                .thenRespondsInvalidInputAsJson()
                .andServiceWasNeverCalled();
    }

    @Test
    void rejectsRepeatedNameWithoutCallingService() throws Exception {
        scenario()
                .withName("alice", "zoe")
                .whenGet()
                .thenRespondsInvalidInputAsJson()
                .andServiceWasNeverCalled();
    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_XML_VALUE})
    void returnsInvalidInputAsJsonEvenWhenClientDoesNotAcceptJson(String accept) throws Exception {
        scenario()
                .givenServiceRejects("zoe")
                .withName("zoe")
                .accepting(accept)
                .whenGet()
                .thenRespondsInvalidInputAsJson();
    }

    @Test
    void returns405ForUnsupportedMethodWithoutCallingService() throws Exception {
        scenario()
                .withName("alice")
                .whenPost()
                .thenRespondsMethodNotAllowed()
                .andServiceWasNeverCalled();
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void logsWhyRequestWasRejectedWithoutTheName(CapturedOutput output) throws Exception {
        scenario().givenServiceRejects("zoe").withName("zoe").whenGet().thenRespondsInvalidInputAsJson();
        scenario().whenGet().thenRespondsInvalidInputAsJson();

        assertThat(output)
                .contains("Rejected GET /hello-world: \"" + REJECTION_REASON + "\"")
                .contains("Rejected GET /hello-world: \"Required request parameter 'name'")
                .doesNotContain("zoe");
    }

    private Scenario scenario() {
        return new Scenario();
    }

    // Helper for building test requests
    private final class Scenario {

        private String[] names;
        private String accept;
        private ResultActions result;

        // given

        Scenario givenServiceGreets(String name, String greeting) {
            when(greetingService.greet(name)).thenReturn(greeting);
            return this;
        }

        Scenario givenServiceRejects(String name) {
            when(greetingService.greet(name)).thenThrow(new InvalidInputException(REJECTION_REASON));
            return this;
        }

        // request

        Scenario withName(String... values) {
            this.names = values;
            return this;
        }

        Scenario accepting(String mediaType) {
            this.accept = mediaType;
            return this;
        }

        // when

        Scenario whenGet() throws Exception {
            return perform(get("/hello-world"));
        }

        Scenario whenPost() throws Exception {
            return perform(post("/hello-world"));
        }

        private Scenario perform(MockHttpServletRequestBuilder request) throws Exception {
            if (names != null) {
                request.param("name", names);
            }
            if (accept != null) {
                request.accept(accept);
            }
            result = mockMvc.perform(request);
            return this;
        }

        // then

        Scenario thenRespondsWithGreeting(String message) throws Exception {
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json("{\"message\":\"" + message + "\"}", JsonCompareMode.STRICT));
            return this;
        }

        Scenario thenRespondsInvalidInputAsJson() throws Exception {
            result.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().json("{\"error\":\"Invalid Input\"}", JsonCompareMode.STRICT));
            return this;
        }

        Scenario thenRespondsMethodNotAllowed() throws Exception {
            result.andExpect(status().isMethodNotAllowed());
            return this;
        }

        // and

        Scenario andServiceWasCalledWith(String name) {
            verify(greetingService).greet(name);
            verifyNoMoreInteractions(greetingService);
            return this;
        }

        Scenario andServiceWasNeverCalled() {
            verifyNoInteractions(greetingService);
            return this;
        }
    }
}
