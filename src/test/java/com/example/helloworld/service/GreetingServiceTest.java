package com.example.helloworld.service;

import com.example.helloworld.exception.InvalidInputException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GreetingServiceTest {

    private final GreetingService service = new GreetingService();

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            "alice    | Hello Alice",
            "Alice    | Hello Alice",
            "ALICE    | Hello ALICE",
            "a        | Hello A",
            "m        | Hello M",
            "M        | Hello M",
            "mcDonald | Hello McDonald",
            "bob-123  | Hello Bob-123",
            "'  alice  ' | Hello Alice",
            "anna maria  | Hello Anna maria",
            "'\u00A0alice'       | Hello Alice", // non-breaking space
            "'alice\u00A0'       | Hello Alice",
            "'\u2007\u202Falice' | Hello Alice",
            "'\u3000alice'       | Hello Alice",
            "'anna\u00A0maria'   | Hello Anna\u00A0maria",
    })
    void greetsNamesStartingWithAToM(String name, String expected) {
        assertThat(service.greet(name)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"n", "N", "nancy", "zoe", "Zoe", "Z"})
    void rejectsNamesStartingWithNToZ(String name) {
        assertInvalid(name);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", "\u00A0", "\u00A0 \u202F"})
    void rejectsMissingOrBlankNames(String name) {
        assertInvalid(name);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1alice", "_alice", "-bob", "@carl", "^alice", "{alice", "|alice","Élodie", "ıvan", "Ádám", "日本"})
    void rejectsNamesNotStartingWithAnEnglishLetter(String name) {
        assertInvalid(name);
    }

    private void assertInvalid(String name) {
        assertThatThrownBy(() -> service.greet(name)).isInstanceOf(InvalidInputException.class);
    }
}
