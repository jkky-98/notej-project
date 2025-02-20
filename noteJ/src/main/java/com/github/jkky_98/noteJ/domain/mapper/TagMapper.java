package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagMapper {

    // String(tagName) -> Tag 변환
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "tagName", target = "name")
    Tag toTag(String tagName);

    List<Tag> toTagList(List<String> tagNames);

    // Tag -> PostTag 변환 (List<Tag> -> List<PostTag>)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "post", target = "post")
    @Mapping(source = "tag", target = "tag")
    PostTag toPostTag(Post post, Tag tag);
}
