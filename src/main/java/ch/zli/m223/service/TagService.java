package ch.zli.m223.service;

import java.util.List;
import ch.zli.m223.model.Tag;
import ch.zli.m223.repository.TagRepository;
import ch.zli.m223.repository.EntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class TagService {
    @Inject
    TagRepository repository;
    @Inject
    EntryRepository entryRepository;

    public List<Tag> findAll() { return repository.listAll(); }

    public Tag findById(Long id) {
        Tag entity = repository.findById(id);
        if (entity == null) { throw new NotFoundException("Tag not found"); }
        return entity;
    }

    @Transactional
    public Tag create(Tag entity) {
        validate(entity);
        repository.persist(entity);
        return entity;
    }

    @Transactional
    public Tag update(Long id, Tag entity) {
        Tag existing = findById(id);
        validate(entity);
        existing.setTitle(entity.getTitle());
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        Tag existing = findById(id);
        if (entryRepository.getEntityManager().createQuery("select count(e) from Entry e join e.tags t where t.id = :id", Long.class).setParameter("id", id).getSingleResult() > 0) {
            throw new WebApplicationException("Tag is still referenced by entries", 409);
        }
        repository.delete(existing);
    }

    private void validate(Tag entity) {
        if (entity == null || entity.getTitle() == null || entity.getTitle().isBlank()) {
            throw new BadRequestException("Title is required");
        }
    }
}
