package ch.zli.m223.service;

import java.util.List;
import ch.zli.m223.model.Category;
import ch.zli.m223.repository.CategoryRepository;
import ch.zli.m223.repository.EntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class CategoryService {
    @Inject
    CategoryRepository repository;
    @Inject
    EntryRepository entryRepository;

    public List<Category> findAll() { return repository.listAll(); }

    public Category findById(Long id) {
        Category entity = repository.findById(id);
        if (entity == null) { throw new NotFoundException("Category not found"); }
        return entity;
    }

    @Transactional
    public Category create(Category entity) {
        validate(entity);
        repository.persist(entity);
        return entity;
    }

    @Transactional
    public Category update(Long id, Category entity) {
        Category existing = findById(id);
        validate(entity);
        existing.setTitle(entity.getTitle());
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        Category existing = findById(id);
        if (entryRepository.count("category.id = ?1", id) > 0) {
            throw new WebApplicationException("Category is still referenced by entries", 409);
        }
        repository.delete(existing);
    }

    private void validate(Category entity) {
        if (entity == null || entity.getTitle() == null || entity.getTitle().isBlank()) {
            throw new BadRequestException("Title is required");
        }
    }
}
