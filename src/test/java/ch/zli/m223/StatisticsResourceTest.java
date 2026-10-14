package ch.zli.m223;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class StatisticsResourceTest {
    @Test
    void returnsDailyDurationsAsJsonObject() {
        assertEquals(Map.of(), given().when().get("/statistics/time-summaries")
                .then().statusCode(200).extract().as(Map.class));
        long first = createEntry("2024-08-20T08:00:00", "2024-08-20T12:00:00");
        long second = createEntry("2024-08-20T13:00:00", "2024-08-20T17:30:00");
        long third = createEntry("2024-08-21T23:00:00", "2024-08-22T00:45:15");
        long fourth = createEntry("2024-08-20T00:00:00", "2024-08-21T00:00:00");
        try {
            assertEquals(Map.of("2024-08-20", "32:30:00", "2024-08-21", "01:00:00",
                            "2024-08-22", "00:45:15"),
                    given().when().get("/statistics/time-summaries")
                            .then().statusCode(200).contentType("application/json").extract().as(Map.class));
        } finally {
            for (long id : new long[] {first, second, third, fourth}) {
                given().when().delete("/entries/{id}", id).then().statusCode(204);
            }
        }
    }

    private long createEntry(String start, String end) {
        return given().contentType("application/json")
                .body(Map.of("checkIn", start, "checkOut", end))
                .when().post("/entries").then().statusCode(200).extract().jsonPath().getLong("id");
    }
}
