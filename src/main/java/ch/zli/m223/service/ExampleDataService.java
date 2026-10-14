package ch.zli.m223.service;

import ch.zli.m223.model.Category;
import ch.zli.m223.model.Entry;
import ch.zli.m223.model.Tag;
import ch.zli.m223.repository.CategoryRepository;
import ch.zli.m223.repository.EntryRepository;
import ch.zli.m223.repository.TagRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ExampleDataService {
    @Inject
    CategoryRepository categoryRepository;
    @Inject
    TagRepository tagRepository;
    @Inject
    EntryRepository entryRepository;

    @ConfigProperty(name = "punchclock.example-data.enabled", defaultValue = "false")
    boolean enabled;

    void onStart(@Observes StartupEvent event) {
        if (enabled) {
            createExampleData();
        }
    }

    @Transactional
    public void createExampleData() {
        // Nur eine vollständig leere Datenbank mit Beispieldaten füllen.
        if (categoryRepository.count() > 0 || tagRepository.count() > 0
                || entryRepository.count() > 0) {
            return;
        }

        Category project = createCategory("Projektarbeit");
        Category lesson = createCategory("Unterricht");
        Category administration = createCategory("Administration");

        Tag java = createTag("Java");
        Tag backend = createTag("Backend");
        Tag documentation = createTag("Dokumentation");

        createEntry("2026-10-12T08:00:00", "2026-10-12T11:30:00", project, java, backend);
        createEntry("2026-10-12T13:00:00", "2026-10-12T16:00:00", lesson, java);
        createEntry("2026-10-13T08:30:00", "2026-10-13T09:30:00", administration, documentation);
        createEntry("2026-10-13T10:00:00", "2026-10-13T11:00:00", null);
    }

    private Category createCategory(String title) {
        Category category = new Category();
        category.setTitle(title);
        categoryRepository.persist(category);
        return category;
    }

    private Tag createTag(String title) {
        Tag tag = new Tag();
        tag.setTitle(title);
        tagRepository.persist(tag);
        return tag;
    }

    private void createEntry(String checkIn, String checkOut, Category category, Tag... tags) {
        Entry entry = new Entry();
        entry.setCheckIn(LocalDateTime.parse(checkIn));
        entry.setCheckOut(LocalDateTime.parse(checkOut));
        entry.setCategory(category);
        for (Tag tag : tags) {
            entry.getTags().add(tag);
        }
        // Entry besitzt die Beziehungen; JPA schreibt auch die Zwischentabelle.
        entryRepository.persist(entry);
    }
}
