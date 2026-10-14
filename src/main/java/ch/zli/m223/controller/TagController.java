package ch.zli.m223.controller;

import java.util.List;
import ch.zli.m223.dto.TagDto;
import ch.zli.m223.model.Tag;
import ch.zli.m223.service.TagService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@org.eclipse.microprofile.openapi.annotations.tags.Tag(name = "Tags")
public class TagController {
    @Inject
    TagService service;

    @GET
    public List<TagDto> index() {
        return service.findAll().stream().map(TagController::toDto).toList();
    }

    @GET
    @Path("/{id}")
    public TagDto get(@PathParam("id") Long id) { return toDto(service.findById(id)); }

    @POST
    public TagDto create(TagDto dto) { return toDto(service.create(toEntity(dto))); }

    @PUT
    @Path("/{id}")
    public TagDto update(@PathParam("id") Long id, TagDto dto) {
        return toDto(service.update(id, toEntity(dto)));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    private static Tag toEntity(TagDto dto) {
        if (dto == null) { return null; }
        Tag entity = new Tag();
        entity.setTitle(dto.getTitle());
        return entity;
    }

    private static TagDto toDto(Tag entity) {
        TagDto dto = new TagDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        return dto;
    }
}
