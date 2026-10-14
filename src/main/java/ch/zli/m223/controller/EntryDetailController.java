package ch.zli.m223.controller;

import java.util.ArrayList;
import java.util.List;

import ch.zli.m223.dto.CategoryDto;
import ch.zli.m223.dto.EntryDetailDto;
import ch.zli.m223.dto.TagDto;
import ch.zli.m223.model.Entry;
import ch.zli.m223.model.Tag;
import ch.zli.m223.service.EntryService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/entry-details")
@Transactional
@org.eclipse.microprofile.openapi.annotations.tags.Tag(name = "Entry details")
public class EntryDetailController {
    @Inject
    EntryService entryService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Index all entry details.",
        description = "Returns entries with category and tags including their IDs and titles.")
    public List<EntryDetailDto> index() {
        List<EntryDetailDto> entries = new ArrayList<>();
        for (Entry entry : entryService.findAll()) {
            entries.add(toDto(entry));
        }
        return entries;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get entry details.",
        description = "Returns one entry with category and tags including their IDs and titles.")
    public EntryDetailDto get(@PathParam("id") Long id) {
        return toDto(entryService.findById(id));
    }

    private static EntryDetailDto toDto(Entry entry) {
        EntryDetailDto dto = new EntryDetailDto();
        dto.setId(entry.getId());
        dto.setCheckIn(entry.getCheckIn());
        dto.setCheckOut(entry.getCheckOut());

        if (entry.getCategory() != null) {
            CategoryDto category = new CategoryDto();
            category.setId(entry.getCategory().getId());
            category.setTitle(entry.getCategory().getTitle());
            dto.setCategory(category);
        }

        List<TagDto> tags = new ArrayList<>();
        for (Tag tag : entry.getTags()) {
            TagDto tagDto = new TagDto();
            tagDto.setId(tag.getId());
            tagDto.setTitle(tag.getTitle());
            tags.add(tagDto);
        }
        dto.setTags(tags);
        return dto;
    }
}
