package com.example.library;

public class Book {
    private String id;
    private String title;
    private String author;
    private int totalPages;
    private int currentPage;
    private String imageUrl;

    // בנאי ריק (חובה עבור Firestore!)
    public Book() {}

    public Book(String id, String title, String author, int totalPages, int currentPage, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}