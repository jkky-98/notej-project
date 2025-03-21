package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.web.controller.dto.XtermsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface XtermsMapper {
    // String을 XtermsResponseDto로 매핑
    // 기본적으로 필드명이 같으면 자동 매핑됩니다.
    XtermsResponseDto fromResult(String result);
}
