package ch.zli.m223;

import ch.zli.m223.model.Category;
import ch.zli.m223.model.Entry;
import ch.zli.m223.repository.CategoryRepository;
import ch.zli.m223.repository.EntryRepository;
import ch.zli.m223.repository.TagRepository;
import ch.zli.m223.service.ExampleDataService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ExampleDataServiceTest {
    @Inject
    ExampleDataService service;
    @Inject
    CategoryRepository categories;
    @Inject
    TagRepository tags;
    @Inject
    EntryRepository entries;
    @Inject
    EntityManager entityManager;

    @Test
    @TestTransaction
    void persistsRelationshipsAndDoesNotDuplicateData() {
        clearData();
        service.createExampleData();
        entityManager.flush();
        entityManager.clear();

        assertEquals(3, categories.count());
        assertEquals(3, tags.count());
        assertEquals(4, entries.count());
        Entry project = entries.find("checkIn", LocalDateTime.parse("2026-10-12T08:00:00")).singleResult();
        assertEquals("Projektarbeit", project.getCategory().getTitle());
        assertEquals(Set.of("Java", "Backend"), project.getTags().stream()
                .map(tag -> tag.getTitle()).collect(Collectors.toSet()));
        assertEquals(2, tags.find("title", "Java").singleResult().getEntries().size());
        Entry unassigned = entries.find("checkIn", LocalDateTime.parse("2026-10-13T10:00:00")).singleResult();
        assertNull(unassigned.getCategory());
        assertTrue(unassigned.getTags().isEmpty());
        for (Entry entry : entries.listAll()) {
            assertTrue(entry.getCheckOut().isAfter(entry.getCheckIn()));
        }

        service.createExampleData();
        assertEquals(3, categories.count());
        assertEquals(3, tags.count());
        assertEquals(4, entries.count());
    }

    @Test
    @TestTransaction
    void skipsDatabaseContainingOnlyACategory() {
        clearData();
        Category existing = new Category();
        existing.setTitle("Eigene Kategorie");
        categories.persist(existing);

        service.createExampleData();

        assertEquals(1, categories.count());
        assertEquals("Eigene Kategorie", categories.listAll().getFirst().getTitle());
        assertEquals(0, tags.count());
        assertEquals(0, entries.count());
    }

    private void clearData() {
        // Nur Testdaten; @TestTransaction rollt alle Änderungen nach dem Test zurück.
        entityManager.createNativeQuery("DELETE FROM entry_tag").executeUpdate();
        entries.deleteAll();
        tags.deleteAll();
        categories.deleteAll();
        entityManager.clear();
    }
}
