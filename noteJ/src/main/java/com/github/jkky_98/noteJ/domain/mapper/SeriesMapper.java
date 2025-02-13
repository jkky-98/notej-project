package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.web.controller.dto.SeriesViewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeriesMapper {

    @Mapping(source = "series.seriesName", target = "seriesName")
    @Mapping(expression = "java(series.getPosts().size())", target = "count")
    @Mapping(expression = "java(series.getPosts().isEmpty() ? null : series.getPosts().get(0).getLastModifiedDt())", target = "lastUpdateDt")
    SeriesViewDto toSeriesViewDto(Series series);

    List<SeriesViewDto> toSeriesViewDtoList(List<Series> seriesList);
}
