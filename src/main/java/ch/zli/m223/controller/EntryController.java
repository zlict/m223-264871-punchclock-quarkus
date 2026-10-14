package ch.zli.m223.controller;

import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

import ch.zli.m223.dto.EntryDto;
import ch.zli.m223.model.Entry;
import ch.zli.m223.model.Category;
import ch.zli.m223.model.Tag;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import ch.zli.m223.service.EntryService;

@Transactional
@Path("/entries")
@org.eclipse.microprofile.openapi.annotations.tags.Tag(name = "Entries", description = "Handling of entries")
public class EntryController {

    @Inject
    EntryService entryService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Index all Entries.", description = "Returns a list of all entries.")
    public List<EntryDto> index() {
        return entryService.findAll().stream().map(EntryController::toDto).toList();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates a new entry.", description = "Creates a new entry and returns the newly added entry.")
    public EntryDto create(@NotNull @Valid EntryDto entry) {
        return toDto(entryService.createEntry(toEntity(entry)));
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates an entry.", description = "Replaces check-in and check-out of an existing entry.")
    public EntryDto update(@PathParam("id") Long id, @NotNull @Valid EntryDto entry) {
        return toDto(entryService.updateEntry(id, toEntity(entry)));
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deletes an entry.", description = "Deletes an existing entry by its ID.")
    public Response delete(@PathParam("id") Long id) {
        entryService.deleteEntry(id);
        return Response.noContent().build();
    }

    private static Entry toEntity(EntryDto dto) {
        if (dto == null) {
            return null;
        }
        Entry entry = new Entry();
        entry.setCheckIn(dto.getCheckIn());
        entry.setCheckOut(dto.getCheckOut());
        if (dto.getCategoryId() != null) {
            Category category = new Category();
            category.setId(dto.getCategoryId());
            entry.setCategory(category);
        }
        if (dto.getTagIds() != null) {
            for (Long tagId : dto.getTagIds()) {
                if (tagId == null) { throw new BadRequestException("Tag ID must not be null"); }
                Tag tag = new Tag();
                tag.setId(tagId);
                entry.getTags().add(tag);
            }
        }
        return entry;
    }

    private static EntryDto toDto(Entry entry) {
        EntryDto dto = new EntryDto();
        dto.setId(entry.getId());
        dto.setCheckIn(entry.getCheckIn());
        dto.setCheckOut(entry.getCheckOut());
        dto.setCategoryId(entry.getCategory() == null ? null : entry.getCategory().getId());
        Set<Long> tagIds = new LinkedHashSet<>();
        for (Tag tag : entry.getTags()) {
            tagIds.add(tag.getId());
        }
        dto.setTagIds(tagIds);
        return dto;
    }
}
