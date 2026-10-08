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
class ReaderAndCopyIntegrationTest {
 @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired JdbcTemplate jdbc;
 final List<Long> readers=new ArrayList<>();final List<Long> copies=new ArrayList<>();
 Long category,author,book;
 @AfterEach void cleanup(){
  for(long id:copies)jdbc.update("DELETE FROM book_copies WHERE id=?",id);
  for(long id:readers)jdbc.update("DELETE FROM readers WHERE id=?",id);
  if(book!=null){jdbc.update("DELETE FROM book_authors WHERE book_id=?",book);jdbc.update("DELETE FROM books WHERE id=?",book);}
  if(author!=null)jdbc.update("DELETE FROM authors WHERE id=?",author);
  if(category!=null)jdbc.update("DELETE FROM categories WHERE id=?",category);
 }
 long create(String path,Object body) throws Exception {
  String response=mvc.perform(post("/api/"+path).contentType("application/json").content(json.writeValueAsString(body)))
   .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
  long id=json.readTree(response).get("id").asLong();
  if(path.equals("readers"))readers.add(id);if(path.equals("copies"))copies.add(id);return id;
 }
 long book() throws Exception {
  category=create("categories",Map.of("name","Copies-"+UUID.randomUUID()));author=create("authors",Map.of("name","Autor de exemplares"));
  book=create("books",Map.of("isbn","9780306406157","title","Livro","publicationYear",2000,"categoryId",category,"authorIds",List.of(author)));return book;
 }
 @Test void readerCrudAndStatus() throws Exception {
  long id=create("readers",Map.of("registrationNumber","  ab-01  ","name","  Leitor  ","email",""));
  mvc.perform(get("/api/readers/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.registrationNumber").value("AB-01"))
   .andExpect(jsonPath("$.name").value("Leitor")).andExpect(jsonPath("$.active").value(true));
  mvc.perform(put("/api/readers/"+id).contentType("application/json").content("{\"registrationNumber\":\"ab-02\",\"name\":\"Outro leitor\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.registrationNumber").value("AB-02"));
  mvc.perform(patch("/api/readers/"+id+"/status").contentType("application/json").content("{\"active\":false}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
  mvc.perform(patch("/api/readers/"+id+"/status").contentType("application/json").content("{\"active\":true}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
  mvc.perform(delete("/api/readers/"+id)).andExpect(status().isNoContent());
  mvc.perform(get("/api/readers/"+id)).andExpect(status().isNotFound());
 }
 @Test void duplicateRegistrationRollsBackAndEmailsCanBeShared() throws Exception {
  create("readers",Map.of("registrationNumber","ABC","name","Primeiro","email","family@example.org"));
  create("readers",Map.of("registrationNumber","DEF","name","Segundo","email","family@example.org"));
  mvc.perform(post("/api/readers").contentType("application/json").content("{\"registrationNumber\":\" abc \",\"name\":\"Duplicado\"}"))
   .andExpect(status().isConflict());
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM readers WHERE registration_number='ABC'",Integer.class));
 }
 @Test void invalidReaderInputsAndPagination() throws Exception {
  for(String input:List.of("{\"registrationNumber\":\"á\",\"name\":\"Leitor\"}","{\"registrationNumber\":\"AB\",\"name\":\" \",\"email\":\"x\"}","{\"registrationNumber\":\"AB\",\"name\":\"Leitor\",\"email\":\"bad\"}"))
   mvc.perform(post("/api/readers").contentType("application/json").content(input)).andExpect(status().isBadRequest());
  mvc.perform(get("/api/readers?size=101")).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/readers/999999/status").contentType("application/json").content("{}" )).andExpect(status().isBadRequest());
 }
 @Test void copyCrudAndWithdrawalPreserveBook() throws Exception {
  long bookId=book();long id=create("copies",Map.of("inventoryCode","  cp-01  ","bookId",bookId));
  mvc.perform(get("/api/copies/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.inventoryCode").value("CP-01"))
   .andExpect(jsonPath("$.available").value(true)).andExpect(jsonPath("$.bookId").value(bookId));
  mvc.perform(put("/api/copies/"+id).contentType("application/json").content(json.writeValueAsString(Map.of("inventoryCode","CP-02","bookId",bookId))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.inventoryCode").value("CP-02"));
  mvc.perform(post("/api/copies/"+id+"/withdrawal")).andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false)).andExpect(jsonPath("$.circulationStatus").value("WITHDRAWN"));
  mvc.perform(delete("/api/copies/"+id)).andExpect(status().isNoContent());
  mvc.perform(get("/api/copies/"+id)).andExpect(status().isNotFound());
  mvc.perform(get("/api/books/"+bookId)).andExpect(status().isOk());
 }
 @Test void duplicateInventoryAndImmutableBook() throws Exception {
  long bookId=book();long id=create("copies",Map.of("inventoryCode","ABC","bookId",bookId));
  mvc.perform(post("/api/copies").contentType("application/json").content(json.writeValueAsString(Map.of("inventoryCode"," abc ","bookId",bookId))))
   .andExpect(status().isConflict());
  mvc.perform(put("/api/copies/"+id).contentType("application/json").content(json.writeValueAsString(Map.of("inventoryCode","OTHER","bookId",Long.MAX_VALUE))))
   .andExpect(status().isConflict());
  mvc.perform(get("/api/copies/"+id)).andExpect(jsonPath("$.inventoryCode").value("ABC")).andExpect(jsonPath("$.bookId").value(bookId));
 }
 @Test void bookWithCopiesCannotBeDeletedAndAssociationsRollback() throws Exception {
  long bookId=book();create("copies",Map.of("inventoryCode","CP-BOOK","bookId",bookId));
  mvc.perform(delete("/api/books/"+bookId)).andExpect(status().isConflict());
  mvc.perform(get("/api/books/"+bookId)).andExpect(status().isOk()).andExpect(jsonPath("$.authors[0].id").value(author));
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM book_authors WHERE book_id=?",Integer.class,bookId));
 }
 @Test void missingBookAndInvalidCopyCodeAreRejected() throws Exception {
  mvc.perform(post("/api/copies").contentType("application/json").content("{\"inventoryCode\":\"CP\",\"bookId\":9223372036854775807}"))
   .andExpect(status().isNotFound());
  mvc.perform(post("/api/copies").contentType("application/json").content("{\"inventoryCode\":\"a b\",\"bookId\":1}"))
   .andExpect(status().isBadRequest());
  mvc.perform(get("/api/copies?size=0")).andExpect(status().isBadRequest());
 }
 @Test void twoCopiesReferenceOneBook() throws Exception {
  long bookId=book();create("copies",Map.of("inventoryCode","ONE","bookId",bookId));create("copies",Map.of("inventoryCode","TWO","bookId",bookId));
  assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM book_copies WHERE book_id=?",Integer.class,bookId));
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM books WHERE id=?",Integer.class,bookId));
  mvc.perform(get("/api/copies")).andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements").value(2));
 }
}
