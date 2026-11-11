package com.emotion.common.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private long total;
    private int page;
    private int size;
    private int pages;
    private List<T> records;

    public PageResult() {}

    public PageResult(long total, int page, int size, int pages, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = pages;
        this.records = records;
    }

    public static <T> PageResult<T> of(long total, int page, int size, List<T> records) {
        int pages = (int) Math.ceil((double) total / size);
        return new PageResult<>(total, page, size, pages, records);
    }
}
