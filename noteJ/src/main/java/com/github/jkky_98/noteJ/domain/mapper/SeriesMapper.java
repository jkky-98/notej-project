package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.web.controller.dto.SeriesViewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface SeriesMapper {

    @Mapping(source = "series.seriesName", target = "seriesName")
    @Mapping(expression = "java(series.getPosts().size())", target = "count")
    @Mapping(expression = "java(series.getPosts().isEmpty() ? null : series.getPosts().get(0).getLastModifiedDt())", target = "lastUpdateDt")
    SeriesViewDto toSeriesViewDto(Series series);

    List<SeriesViewDto> toSeriesViewDtoList(List<Series> seriesList);

    @Mapping(source = "seriesName", target = "seriesName")
    Series toSeries(String seriesName);
}
