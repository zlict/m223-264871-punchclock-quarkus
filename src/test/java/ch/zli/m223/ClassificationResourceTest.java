package ch.zli.m223;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasItems;

@QuarkusTest
public class ClassificationResourceTest {
    private long create(String path, String body) {
        return given().contentType("application/json").body(body)
            .when().post(path).then().statusCode(200).extract().jsonPath().getLong("id");
    }

    @Test
    public void testCategoriesTagsAndRelationships() {
        long category = create("/categories", "{\"title\":\"Projektarbeit\"}");
        long firstTag = create("/tags", "{\"title\":\"Java\"}");
        long secondTag = create("/tags", "{\"title\":\"Kurs\"}");

        for (String path : new String[]{"/categories", "/tags"}) {
            long id = path.equals("/categories") ? category : firstTag;
            given().when().get(path).then().statusCode(200).body("id", hasItems((int) id));
            given().when().get(path + "/" + id).then().statusCode(200).body("id", is((int) id));
            given().contentType("application/json").body("{\"title\":\"Neu\"}")
                .when().put(path + "/" + id).then().statusCode(200).body("title", is("Neu"));
            given().when().get(path + "/" + id).then().body("title", is("Neu"));
            given().contentType("application/json").body("{\"title\":\" \"}")
                .when().post(path).then().statusCode(400);
            given().contentType("application/json").body("{}")
                .when().put(path + "/" + id).then().statusCode(400);
        }

        String entry = """
            {"checkIn":"2026-09-29T08:00:00","checkOut":"2026-09-29T17:00:00",
             "categoryId":%d,"tagIds":[%d,%d,%d]}
            """.formatted(category, firstTag, secondTag, firstTag);
        long firstEntry = create("/entries", entry);
        long secondEntry = create("/entries", entry);
        given().when().get("/entries").then().statusCode(200)
            .body("find { it.id == " + firstEntry + " }.categoryId", is((int) category))
            .body("find { it.id == " + firstEntry + " }.tagIds.size()", is(2))
            .body("find { it.id == " + firstEntry + " }.tagIds", hasItems((int) firstTag, (int) secondTag));
        given().when().get("/entry-details/{id}", firstEntry).then().statusCode(200)
            .body("id", is((int) firstEntry))
            .body("checkIn", is("2026-09-29T08:00:00"))
            .body("checkOut", is("2026-09-29T17:00:00"))
            .body("category.id", is((int) category))
            .body("category.title", is("Neu"))
            .body("tags.size()", is(2))
            .body("tags.id", hasItems((int) firstTag, (int) secondTag))
            .body("tags.title", hasItems("Neu", "Kurs"));
        String detail = "find { it.id == " + firstEntry + " }";
        given().when().get("/entry-details").then().statusCode(200)
            .body("size()", is(2))
            .body(detail + ".checkIn", is("2026-09-29T08:00:00"))
            .body(detail + ".checkOut", is("2026-09-29T17:00:00"))
            .body(detail + ".category.id", is((int) category))
            .body(detail + ".category.title", is("Neu"))
            .body(detail + ".category.size()", is(2))
            .body(detail + ".tags.size()", is(2))
            .body(detail + ".tags.id", hasItems((int) firstTag, (int) secondTag))
            .body(detail + ".tags.title", hasItems("Neu", "Kurs"))
            .body(detail + ".tags[0].size()", is(2))
            .body(detail + ".containsKey('categoryId')", is(false))
            .body(detail + ".containsKey('tagIds')", is(false));
        given().when().delete("/categories/" + category).then().statusCode(409);
        given().when().delete("/tags/" + firstTag).then().statusCode(409);

        given().contentType("application/json").body(entry.replace("\"categoryId\":" + category, "\"categoryId\":999999"))
            .when().post("/entries").then().statusCode(404);
        given().contentType("application/json").body(entry.replace("\"tagIds\":[" + firstTag, "\"tagIds\":[999999"))
            .when().put("/entries/" + firstEntry).then().statusCode(404);
        given().when().get("/entries").then().body("find { it.id == " + firstEntry + " }.tagIds.size()", is(2));

        String cleared = """
            {"checkIn":"2026-09-29T08:00:00","checkOut":"2026-09-29T17:00:00",
             "categoryId":null,"tagIds":[]}
            """;
        given().contentType("application/json").body(cleared).when().put("/entries/" + firstEntry)
            .then().statusCode(200).body("tagIds.size()", is(0));
        given().when().get("/entry-details").then().statusCode(200)
            .body(detail + ".category", nullValue())
            .body(detail + ".tags.size()", is(0));
        given().when().get("/entry-details/{id}", firstEntry).then().statusCode(200)
            .body("category", nullValue())
            .body("tags.size()", is(0));
        given().when().delete("/entries/" + secondEntry).then().statusCode(204);
        given().when().delete("/tags/" + firstTag).then().statusCode(204);
        given().when().delete("/tags/" + secondTag).then().statusCode(204);
        given().when().delete("/categories/" + category).then().statusCode(204);
        given().when().delete("/entries/" + firstEntry).then().statusCode(204);

        given().when().get("/entry-details").then().statusCode(200).body("size()", is(0));
        given().when().get("/entry-details/{id}", firstEntry).then().statusCode(404);

        for (String path : new String[]{"/categories/" + category, "/tags/" + firstTag}) {
            given().when().get(path).then().statusCode(404);
            given().when().delete(path).then().statusCode(404);
            given().contentType("application/json").body("{\"title\":\"Neu\"}")
                .when().put(path).then().statusCode(404);
        }
    }
}
