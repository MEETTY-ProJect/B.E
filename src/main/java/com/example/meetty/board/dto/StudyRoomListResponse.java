package com.example.meetty.board.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@AllArgsConstructor
@Data
public class StudyRoomListResponse<T> {
    private List<T> studyGroups;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;

    public StudyRoomListResponse(Page<T> page) {
        this.studyGroups = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
    }
}
