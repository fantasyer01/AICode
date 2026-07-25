package com.commandhub.model;

import java.util.List;

public class PageResult<T> {

    private List<T> content;
    private int page;
    private int size;
    private long total;
    private int totalPages;

    public PageResult(List<T> content, int page, int size, long total) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
    public int getTotalPages() { return totalPages; }
}
