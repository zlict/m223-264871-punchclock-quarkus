package ch.zli.m223.service;

import java.util.List;

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

    @Transactional
    public Entry createEntry(Entry entry) {
        entryRepository.persist(entry);
        return entry;
    }

    public List<Entry> findAll() {
        return entryRepository.listAll();
    }
    @Transactional
    public Entry updateEntry(Long id, Entry entry) {
        Entry existingEntry = entryRepository.findById(id);
        if (existingEntry == null) {
            throw new NotFoundException("Entry not found");
        }
        if (entry == null || entry.getCheckIn() == null || entry.getCheckOut() == null) {
            throw new BadRequestException("Check-in and check-out are required");
        }
        existingEntry.setCheckIn(entry.getCheckIn());
        existingEntry.setCheckOut(entry.getCheckOut());
        return existingEntry;
    }

    @Transactional
    public void deleteEntry(Long id) {
        if (!entryRepository.deleteById(id)) {
            throw new NotFoundException("Entry not found");
        }
    }
}
