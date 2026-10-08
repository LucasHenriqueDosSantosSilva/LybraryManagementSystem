package com.lucashenrique.library.entity;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;
@Entity @Table(name="books")
public class Book {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=13, columnDefinition="char(13)") private String isbn;
    @Column(nullable=false, length=250) private String title;
    @Column(columnDefinition="text") private String description;
    @Column(nullable=false) private short publicationYear;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="category_id") private Category category;
    @ManyToMany
    @JoinTable(name="book_authors", joinColumns=@JoinColumn(name="book_id"), inverseJoinColumns=@JoinColumn(name="author_id"))
    private Set<Author> authors=new LinkedHashSet<>();
    public Long getId(){return id;}
    public String getIsbn(){return isbn;}
    public String getTitle(){return title;}
    public String getDescription(){return description;}
    public short getPublicationYear(){return publicationYear;}
    public Category getCategory(){return category;}
    public Set<Author> getAuthors(){return authors;}
    public void setDetails(String isbn, String title, String description, short year, Category category, Set<Author> authors){
        this.isbn=isbn;this.title=title;this.description=description;this.publicationYear=year;this.category=category;
        this.authors.clear();this.authors.addAll(authors);
    }
}
