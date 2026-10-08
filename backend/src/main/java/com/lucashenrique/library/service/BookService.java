package com.lucashenrique.library.service;
import com.lucashenrique.library.dto.*;
import com.lucashenrique.library.entity.*;
import com.lucashenrique.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Service @Transactional
public class BookService {
    private final BookRepository books;
    private final AuthorRepository authors;
    private final CategoryRepository categories;
    public BookService(BookRepository books,AuthorRepository authors,CategoryRepository categories){this.books=books;this.authors=authors;this.categories=categories;}
    public BookResponse create(BookRequest request){
        Book book=new Book();apply(book,request);books.saveAndFlush(book);return response(book);
    }
    public BookResponse update(Long id,BookRequest request){
        Book book=books.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Livro não encontrado."));
        apply(book,request);books.flush();return response(book);
    }
    @Transactional(readOnly=true)
    public BookResponse get(Long id){return response(books.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Livro não encontrado.")));}
    @Transactional(readOnly=true)
    public Page<BookResponse> list(int page,int size){return books.findAll(PageRequest.of(page,size,Sort.by("id"))).map(this::response);}
    public void delete(Long id){
        Book book=books.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Livro não encontrado."));
        books.delete(book);books.flush();
    }
    private void apply(Book book,BookRequest request){
        String isbn;
        try{isbn=Isbn.canonicalize(request.isbn());}catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,ex.getMessage());}
        Category category=categories.findById(request.categoryId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoria não encontrada."));
        Set<Author> selected=new LinkedHashSet<>(authors.findAllById(request.authorIds()));
        if(selected.size()!=request.authorIds().size())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Autor não encontrado.");
        String description=request.description()==null?null:request.description().strip();
        if(description!=null && description.isEmpty())description=null;
        book.setDetails(isbn,request.title().strip(),description,request.publicationYear().shortValue(),category,selected);
    }
    private BookResponse response(Book book){
        return new BookResponse(book.getId(),book.getIsbn(),book.getTitle(),book.getDescription(),book.getPublicationYear(),book.getCategory().getId(),
            book.getAuthors().stream().sorted(Comparator.comparing(Author::getId)).map(a->new AuthorResponse(a.getId(),a.getName())).toList());
    }
}
