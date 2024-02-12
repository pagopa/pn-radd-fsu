package it.pagopa.pn.radd.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class HtmlSanitizerTest {

    @Autowired
    ObjectMapper objectMapper;

    HtmlSanitizer htmlSanitizer;

    @BeforeEach
    public void init() {
        htmlSanitizer = new HtmlSanitizer(objectMapper, HtmlSanitizer.SanitizeMode.DELETE_HTML);
    }


    @Test
    void sanitizeNumberTest() {
        int number = 10;
        int sanitized = (int) htmlSanitizer.sanitize(number);

        assertThat(sanitized).isEqualTo(number);

    }

    @Test
    void sanitizeInstantTest() {
        Instant now = Instant.now();
        Instant sanitized = (Instant) htmlSanitizer.sanitize(now);

        assertThat(sanitized).isEqualTo(now);

    }

    @Test
    void sanitizeNullTest() {
        Object sanitized = htmlSanitizer.sanitize(null);

        assertThat(sanitized).isNull();

    }

    @Test
    void sanitizeEmptyMapTest() {
        Map<Integer, String> emptyMap = Map.of();
        Object sanitized = htmlSanitizer.sanitize(emptyMap);

        assertThat(sanitized)
                .isInstanceOf(Map.class)
                .isEqualTo(emptyMap);
    }

    @Test
    void sanitizeStringWithSpecialCharacterTest() {
        String aString = "via dell'Aquila";
        Object sanitized = htmlSanitizer.sanitize(aString);

        assertThat(sanitized).isEqualTo(aString);

    }

    @Test
    void sanitizeLinkedListTest() {
        LinkedList<String> list = new LinkedList<>(List.of("Prova", "test", "l'aquila"));
        Object sanitized = htmlSanitizer.sanitize(list);

        assertThat(sanitized)
                .isInstanceOf(LinkedList.class)
                .isEqualTo(list);

    }

    @Test
    void sanitizeArrayListListTest() {
        ArrayList<String> list = new ArrayList<>(List.of("Prova", "test", "l'aquila"));
        Object sanitized = htmlSanitizer.sanitize(list);

        assertThat(sanitized)
                .isInstanceOf(ArrayList.class)
                .isEqualTo(list);

    }

    @Test
    void sanitizeHahSetTest() {
        HashSet<String> list = new HashSet<>(Set.of("Prova", "test", "l'aquila"));
        Object sanitized = htmlSanitizer.sanitize(list);

        assertThat(sanitized)
                .isInstanceOf(HashSet.class)
                .isEqualTo(list);

    }

    @Test
    void sanitizeTreeSetTest() {
        TreeSet<String> list = new TreeSet<>(Set.of("Prova", "test", "l'aquila"));
        Object sanitized = htmlSanitizer.sanitize(list);

        assertThat(sanitized)
                .isInstanceOf(TreeSet.class)
                .isEqualTo(list);

    }

    @Test
    void sanitizeTreeMapTest() {
        Map<Integer, Integer> map = Map.of(1, 1, 2, 2);
        TreeMap<Integer, Integer> treeMapInput = new TreeMap<>(map);

        Object sanitized = htmlSanitizer.sanitize(treeMapInput);

        assertThat(sanitized)
                .isInstanceOf(TreeMap.class)
                .isEqualTo(treeMapInput);

    }

    @Test
    void sanitizeConcurrentHashMapTest() {
        Map<String, Integer> map = Map.of("key1", 1, "key2", 2);
        ConcurrentHashMap<String, Integer> concHashMapInput = new ConcurrentHashMap<>(map);

        Object sanitized = htmlSanitizer.sanitize(concHashMapInput);

        assertThat(sanitized)
                .isInstanceOf(ConcurrentHashMap.class)
                .isEqualTo(concHashMapInput);

    }


    @Test
    void sanitizeStringWithoutHTMLElementTest() {
        String actualHTML = "Stringa che non contiene elementi HTML";
        String sanitized = (String) htmlSanitizer.sanitize(actualHTML);

        assertThat(sanitized).isEqualTo(actualHTML);

    }


    @Test
    void sanitizeStringWithImgAndOtherHTMLElementsTest() {
        String actualHTML = "<html><h1>SSRF WITH IMAGE POC</h1> <img src='https://prova.it'></img></html>";
        String sanitized = (String) htmlSanitizer.sanitize(actualHTML);
        System.out.println(sanitized);
        assertThat(sanitized).doesNotContain("<img", "<h1>", "<html>");
    }
}
