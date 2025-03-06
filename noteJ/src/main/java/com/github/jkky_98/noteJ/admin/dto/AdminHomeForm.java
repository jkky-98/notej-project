package com.github.jkky_98.noteJ.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminHomeForm {
    private Long usersSize;
    private Long postsSize;
    private Long totalViewCount;
}
