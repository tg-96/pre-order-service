package com.preOrderService.newsFeed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommentsResponseDto {
    private Long commentsId;
    private String writerEmail;
    private String name;
    private String text;
}
