package com.lucashenrique.library;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
class CatalogIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired JdbcTemplate jdbc;
 @Autowired ObjectMapper json;
 long category,author;
 final List<Long> createdAuthors=new ArrayList<>();
 final List<Long> createdBooks=new ArrayList<>();
 @BeforeEach void setup() throws Exception {
  category=create("categories",Map.of("name","Test-"+UUID.randomUUID()));
  author=create("authors",Map.of("name","Autor de teste"));createdAuthors.add(author);
 }
 @AfterEach void cleanup(){
  for(long id:createdBooks){jdbc.update("DELETE FROM book_authors WHERE book_id=?",id);jdbc.update("DELETE FROM books WHERE id=?",id);}
  for(long id:createdAuthors)jdbc.update("DELETE FROM authors WHERE id=?",id);
  jdbc.update("DELETE FROM categories WHERE id=?",category);
 }
 long create(String resource,Object body) throws Exception {
  String response=mvc.perform(post("/api/"+resource).contentType("application/json").content(json.writeValueAsString(body)))
   .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
  return json.readTree(response).get("id").asLong();
 }
 Map<String,Object> book(String isbn,List<Long> authors){
  return new HashMap<>(Map.of("isbn",isbn,"title","  Livro de teste  ","publicationYear",2000,"categoryId",category,"authorIds",authors));
 }
 long createBook() throws Exception {long id=create("books",book("0306406152",List.of(author)));createdBooks.add(id);return id;}
 @Test void createsCanonicalBookWithAuthorsAndListsIt() throws Exception {
  long id=createBook();
  mvc.perform(get("/api/books/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.isbn").value("9780306406157"))
   .andExpect(jsonPath("$.title").value("Livro de teste")).andExpect(jsonPath("$.authors[0].id").value(author));
  mvc.perform(get("/api/books")).andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements").value(1));
 }
 @Test void equivalentIsbnIsRejectedAndTransactionRollsBack() throws Exception {
  createBook();
  mvc.perform(post("/api/books").contentType("application/json").content(json.writeValueAsString(book("9780306406157",List.of(author)))))
   .andExpect(status().isConflict());
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM books WHERE isbn='9780306406157'",Integer.class));
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM book_authors WHERE author_id=?",Integer.class,author));
 }
 @Test void missingAuthorDoesNotLeavePartialBook() throws Exception {
  mvc.perform(post("/api/books").contentType("application/json").content(json.writeValueAsString(book("0306406152",List.of(author,Long.MAX_VALUE)))))
   .andExpect(status().isNotFound());
  assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM books WHERE category_id=?",Integer.class,category));
 }
 @Test void rejectsEmptyAuthorsAndInvalidIsbn() throws Exception {
  mvc.perform(post("/api/books").contentType("application/json").content(json.writeValueAsString(book("0306406152",List.of())))).andExpect(status().isBadRequest());
  mvc.perform(post("/api/books").contentType("application/json").content(json.writeValueAsString(book("9780306406158",List.of(author))))).andExpect(status().isBadRequest());
 }
 @Test void linkedCategoryCannotBeDeleted() throws Exception {
  createBook();mvc.perform(delete("/api/categories/"+category)).andExpect(status().isConflict());
  mvc.perform(get("/api/categories/"+category)).andExpect(status().isOk());
 }
 @Test void linkedAuthorCannotBeDeleted() throws Exception {
  createBook();mvc.perform(delete("/api/authors/"+author)).andExpect(status().isConflict());
  mvc.perform(get("/api/authors/"+author)).andExpect(status().isOk());
 }
 @Test void updatesAssociationsAndDeletesBookWithoutDeletingAuthors() throws Exception {
  long id=createBook();long other=create("authors",Map.of("name","Segundo autor"));createdAuthors.add(other);
  var updated=book("0306406152",List.of(other));updated.put("title","Outro título");
  mvc.perform(put("/api/books/"+id).contentType("application/json").content(json.writeValueAsString(updated)))
   .andExpect(status().isOk()).andExpect(jsonPath("$.authors[0].id").value(other));
  assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM book_authors WHERE author_id=?",Integer.class,author));
  mvc.perform(delete("/api/books/"+id)).andExpect(status().isNoContent());
  mvc.perform(get("/api/books/"+id)).andExpect(status().isNotFound());
  mvc.perform(get("/api/authors/"+other)).andExpect(status().isOk());
 }
 @Test void authorsAllowHomonymsAndCanBeUpdatedAndDeleted() throws Exception {
  long other=create("authors",Map.of("name","Autor de teste"));createdAuthors.add(other);
  mvc.perform(put("/api/authors/"+other).contentType("application/json").content("{\"name\":\"  Novo nome  \"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Novo nome"));
  mvc.perform(get("/api/authors")).andExpect(status().isOk());
  mvc.perform(delete("/api/authors/"+other)).andExpect(status().isNoContent());
  mvc.perform(get("/api/authors/"+other)).andExpect(status().isNotFound());
 }
 @Test void missingCategoryAndInvalidYearAreRejected() throws Exception {
  var input=book("0306406152",List.of(author));input.put("categoryId",Long.MAX_VALUE);
  mvc.perform(post("/api/books").contentType("application/json").content(json.writeValueAsString(input))).andExpect(status().isNotFound());
  input.put("categoryId",category);input.put("publicationYear",0);
  mvc.perform(post("/api/books").contentType("application/json").content(json.writeValueAsString(input))).andExpect(status().isBadRequest());
 }

 @Test void paginationKeepsOrderTotalsAndAllAuthors() throws Exception {
  long first=createBook();
  long extra=create("authors",Map.of("name","Outro autor"));createdAuthors.add(extra);
  jdbc.update("INSERT INTO book_authors(book_id,author_id) VALUES(?,?)",first,extra);
  long second=create("books",book("9780804429573",List.of(author)));createdBooks.add(second);
  long third=create("books",book("9780134685991",List.of(author)));createdBooks.add(third);
  mvc.perform(get("/api/books").param("categoryId",Long.toString(category)).param("size","2"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements").value(3))
   .andExpect(jsonPath("$.page.totalPages").value(2)).andExpect(jsonPath("$.content.length()").value(2))
   .andExpect(jsonPath("$.content[0].id").value(first)).andExpect(jsonPath("$.content[1].id").value(second))
   .andExpect(jsonPath("$.content[0].authors.length()").value(2));
  mvc.perform(get("/api/books").param("categoryId",Long.toString(category)).param("size","2").param("page","1"))
   .andExpect(jsonPath("$.page.totalElements").value(3)).andExpect(jsonPath("$.content.length()").value(1))
   .andExpect(jsonPath("$.content[0].id").value(third));
  mvc.perform(get("/api/books").param("categoryId",Long.toString(category)).param("author","Outro"))
   .andExpect(jsonPath("$.page.totalElements").value(1)).andExpect(jsonPath("$.content[0].authors.length()").value(2));
 }
}
