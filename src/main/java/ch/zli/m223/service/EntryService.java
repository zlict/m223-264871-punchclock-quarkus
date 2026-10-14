package ch.zli.m223.service;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import ch.zli.m223.model.Tag;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import ch.zli.m223.model.Entry;
import ch.zli.m223.repository.EntryRepository;

@ApplicationScoped
public class EntryService {
    @Inject
    private EntryRepository entryRepository;

    @Inject
    CategoryService categoryService;
    @Inject
    TagService tagService;

    @Transactional
    public Entry createEntry(Entry entry) {
        validateEntry(entry);
        resolveReferences(entry);
        entryRepository.persist(entry);
        return entry;
    }

    public List<Entry> findAll() {
        return entryRepository.listAll();
    }
    
    public Entry findById(Long id) {
        Entry entry = entryRepository.findById(id);
        if (entry == null) {
            throw new NotFoundException("Entry not found");
        }
        return entry;
    }

    @Transactional
    public Entry updateEntry(Long id, Entry entry) {
        Entry existingEntry = entryRepository.findById(id);
        if (existingEntry == null) {
            throw new NotFoundException("Entry not found");
        }
        validateEntry(entry);
        resolveReferences(entry);
        existingEntry.setCheckIn(entry.getCheckIn());
        existingEntry.setCheckOut(entry.getCheckOut());
        existingEntry.setCategory(entry.getCategory());
        existingEntry.setTags(entry.getTags());
        return existingEntry;
    }

    private static void validateEntry(Entry entry) {
        if (entry == null || entry.getCheckIn() == null || entry.getCheckOut() == null) {
            throw new BadRequestException("Check-in and check-out are required");
        }
        if (!entry.getCheckIn().isBefore(entry.getCheckOut())) {
            throw new BadRequestException("Check-in must be before check-out");
        }
    }

    @Transactional
    public void deleteEntry(Long id) {
        if (!entryRepository.deleteById(id)) {
            throw new NotFoundException("Entry not found");
        }
    }
    private void resolveReferences(Entry entry) {
        if (entry.getCategory() != null) {
            entry.setCategory(categoryService.findById(entry.getCategory().getId()));
        }
        Set<Tag> tags = new LinkedHashSet<>();
        for (Tag tag : entry.getTags()) {
            tags.add(tagService.findById(tag.getId()));
        }
        entry.setTags(tags);
    }
}
