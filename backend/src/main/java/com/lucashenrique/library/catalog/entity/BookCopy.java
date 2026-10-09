package com.lucashenrique.library.catalog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_copies")
public class BookCopy {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 40)
  private String inventoryCode;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id", updatable = false)
  private Book book;

  @Column(nullable = false, length = 20)
  private String circulationStatus = "IN_CIRCULATION";

  protected BookCopy() {}

  public BookCopy(String code, Book book) {
    this.inventoryCode = code;
    this.book = book;
  }

  public Long getId() {
    return id;
  }

  public String getInventoryCode() {
    return inventoryCode;
  }

  public Book getBook() {
    return book;
  }

  public String getCirculationStatus() {
    return circulationStatus;
  }

  public void setInventoryCode(String code) {
    this.inventoryCode = code;
  }

  public void withdraw() {
    this.circulationStatus = "WITHDRAWN";
  }
}
