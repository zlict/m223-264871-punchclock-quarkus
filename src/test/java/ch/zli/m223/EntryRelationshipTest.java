package ch.zli.m223;

import ch.zli.m223.model.Category;
import ch.zli.m223.model.Entry;
import ch.zli.m223.model.Tag;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class EntryRelationshipTest {
    @Inject
    EntityManager entityManager;

    @Test
    @TestTransaction
    public void loadsEntriesFromCategoryAndTag() {
        Category category = new Category();
        category.setTitle("Projektarbeit");
        entityManager.persist(category);
        Tag tag = new Tag();
        tag.setTitle("Java");
        entityManager.persist(tag);

        Entry entry = new Entry();
        entry.setCheckIn(LocalDateTime.of(2026, 10, 14, 8, 0));
        entry.setCheckOut(LocalDateTime.of(2026, 10, 14, 17, 0));
        entry.setCategory(category);
        entry.getTags().add(tag);
        entityManager.persist(entry);
        entityManager.flush();
        Long entryId = entry.getId();
        Long categoryId = category.getId();
        Long tagId = tag.getId();
        entityManager.clear();

        Category loadedCategory = entityManager.find(Category.class, categoryId);
        Tag loadedTag = entityManager.find(Tag.class, tagId);
        assertEquals(1, loadedCategory.getEntries().size());
        assertEquals(entryId, loadedCategory.getEntries().iterator().next().getId());
        assertEquals(1, loadedTag.getEntries().size());
        assertEquals(entryId, loadedTag.getEntries().iterator().next().getId());

        Entry loadedEntry = entityManager.find(Entry.class, entryId);
        loadedEntry.setCategory(null);
        loadedEntry.getTags().clear();
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, entityManager.find(Category.class, categoryId).getEntries().size());
        assertEquals(0, entityManager.find(Tag.class, tagId).getEntries().size());
    }
}
