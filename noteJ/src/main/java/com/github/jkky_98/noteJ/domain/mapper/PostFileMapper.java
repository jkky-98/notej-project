package com.github.jkky_98.noteJ.domain.mapper;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PostFileMapper {

    @Mapping(target = "post", source = "post")
    @Mapping(target = "url", source = "urlImage")
    PostFile toPostFile(Post post, String urlImage);
}
