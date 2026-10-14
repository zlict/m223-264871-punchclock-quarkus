package ch.zli.m223.controller;

import java.util.List;
import ch.zli.m223.dto.CategoryDto;
import ch.zli.m223.model.Category;
import ch.zli.m223.service.CategoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Category")
public class CategoryController {
    @Inject
    CategoryService service;

    @GET
    public List<CategoryDto> index() {
        return service.findAll().stream().map(CategoryController::toDto).toList();
    }

    @GET
    @Path("/{id}")
    public CategoryDto get(@PathParam("id") Long id) { return toDto(service.findById(id)); }

    @POST
    public CategoryDto create(CategoryDto dto) { return toDto(service.create(toEntity(dto))); }

    @PUT
    @Path("/{id}")
    public CategoryDto update(@PathParam("id") Long id, CategoryDto dto) {
        return toDto(service.update(id, toEntity(dto)));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    private static Category toEntity(CategoryDto dto) {
        if (dto == null) { return null; }
        Category entity = new Category();
        entity.setTitle(dto.getTitle());
        return entity;
    }

    private static CategoryDto toDto(Category entity) {
        CategoryDto dto = new CategoryDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        return dto;
    }
}
