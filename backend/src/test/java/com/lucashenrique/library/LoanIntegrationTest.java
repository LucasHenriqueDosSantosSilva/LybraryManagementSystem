package com.lucashenrique.library;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @Import(LoanIntegrationTest.TimeConfig.class)
class LoanIntegrationTest {
 static class TestClock extends Clock {
  final AtomicReference<Instant> instant=new AtomicReference<>();
  public ZoneId getZone(){return ZoneId.of("America/Sao_Paulo");}
  public Clock withZone(ZoneId zone){return Clock.fixed(instant(),zone);}
  public Instant instant(){return instant.get();}
  void date(String date){instant.set(LocalDate.parse(date).atTime(12,0).atZone(getZone()).toInstant());}
 }
 @TestConfiguration static class TimeConfig {@Bean @Primary TestClock testClock(){return new TestClock();}}
 @Autowired TestClock clock;@Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired JdbcTemplate jdbc;
 long category,author,book,reader,copy;
 @BeforeEach void setup() throws Exception {
  clock.date("2026-10-08");
  category=create("categories",Map.of("name","Loans-"+UUID.randomUUID()));
  author=create("authors",Map.of("name","Autor de empréstimo"));
  book=create("books",Map.of("isbn","0306406152","title","Livro","publicationYear",2000,"categoryId",category,"authorIds",List.of(author)));
  reader=create("readers",Map.of("registrationNumber","L-"+UUID.randomUUID().toString().substring(0,12),"name","Leitor"));
  copy=create("copies",Map.of("inventoryCode","C-"+UUID.randomUUID(),"bookId",book));
 }
 @AfterEach void cleanup(){
  jdbc.update("DELETE FROM loans WHERE copy_id=?",copy);jdbc.update("DELETE FROM book_copies WHERE id=?",copy);
  jdbc.update("DELETE FROM readers WHERE id=?",reader);jdbc.update("DELETE FROM book_authors WHERE book_id=?",book);
  jdbc.update("DELETE FROM books WHERE id=?",book);jdbc.update("DELETE FROM authors WHERE id=?",author);jdbc.update("DELETE FROM categories WHERE id=?",category);
 }
 long create(String path,Object payload) throws Exception {
  String response=mvc.perform(post("/api/"+path).contentType("application/json").content(json.writeValueAsString(payload)))
   .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();return json.readTree(response).get("id").asLong();
 }
 String payload() throws Exception{return json.writeValueAsString(Map.of("readerId",reader,"copyId",copy));}
 long loan() throws Exception{return create("loans",Map.of("readerId",reader,"copyId",copy));}
 @Test void loanAndReturnRestoreAvailabilityAndPreserveHistory() throws Exception {
  long id=loan();
  mvc.perform(get("/api/loans/"+id)).andExpect(jsonPath("$.loanDate").value("2026-10-08")).andExpect(jsonPath("$.dueDate").value("2026-10-22")).andExpect(jsonPath("$.status").value("ACTIVE"));
  mvc.perform(get("/api/copies/"+copy)).andExpect(jsonPath("$.available").value(false));
  mvc.perform(post("/api/loans/"+id+"/return")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RETURNED"));
  mvc.perform(get("/api/copies/"+copy)).andExpect(jsonPath("$.available").value(true));
  mvc.perform(get("/api/loans?readerId="+reader+"&status=RETURNED")).andExpect(jsonPath("$.page.totalElements").value(1));
  loan();assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM loans WHERE copy_id=?",Integer.class,copy));
 }
 @Test void inactiveReaderAndWithdrawnCopyCannotBorrow() throws Exception {
  mvc.perform(patch("/api/readers/"+reader+"/status").contentType("application/json").content("{\"active\":false}")).andExpect(status().isOk());
  mvc.perform(post("/api/loans").contentType("application/json").content(payload())).andExpect(status().isConflict());
  mvc.perform(patch("/api/readers/"+reader+"/status").contentType("application/json").content("{\"active\":true}")).andExpect(status().isOk());
  mvc.perform(post("/api/copies/"+copy+"/withdrawal")).andExpect(status().isOk());
  mvc.perform(post("/api/loans").contentType("application/json").content(payload())).andExpect(status().isConflict());
 }
 @Test void activeLoanBlocksWithdrawalAndDuplicateLoan() throws Exception {
  loan();mvc.perform(post("/api/copies/"+copy+"/withdrawal")).andExpect(status().isConflict());
  mvc.perform(post("/api/loans").contentType("application/json").content(payload())).andExpect(status().isConflict());
 }
 @Test void inactiveReaderCanReturnButCannotEraseHistory() throws Exception {
  long id=loan();mvc.perform(patch("/api/readers/"+reader+"/status").contentType("application/json").content("{\"active\":false}"));
  mvc.perform(post("/api/loans/"+id+"/return")).andExpect(status().isOk());
  mvc.perform(delete("/api/readers/"+reader)).andExpect(status().isConflict());
  mvc.perform(delete("/api/copies/"+copy)).andExpect(status().isConflict());
  var changed=Map.of("isbn","9780804429573","title","Livro","publicationYear",2000,"categoryId",category,"authorIds",List.of(author));
  mvc.perform(put("/api/books/"+book).contentType("application/json").content(json.writeValueAsString(changed))).andExpect(status().isConflict());
 }
 @Test void duplicateReturnAndEarlierDateAreRejected() throws Exception {
  long id=loan();clock.date("2026-10-07");mvc.perform(post("/api/loans/"+id+"/return")).andExpect(status().isConflict());
  clock.date("2026-10-09");mvc.perform(post("/api/loans/"+id+"/return")).andExpect(status().isOk());
  clock.date("2026-10-10");mvc.perform(post("/api/loans/"+id+"/return")).andExpect(status().isConflict());
  mvc.perform(get("/api/loans/"+id)).andExpect(jsonPath("$.returnedDate").value("2026-10-09"));
 }
 @Test void overdueStartsAfterDueDateAndStopsAfterReturn() throws Exception {
  long id=loan();clock.date("2026-10-22");mvc.perform(get("/api/loans/"+id)).andExpect(jsonPath("$.status").value("ACTIVE"));
  clock.date("2026-10-23");mvc.perform(get("/api/loans?status=OVERDUE")).andExpect(jsonPath("$.page.totalElements").value(1));
  mvc.perform(post("/api/loans/"+id+"/return")).andExpect(status().isOk());
  mvc.perform(get("/api/loans?status=OVERDUE")).andExpect(jsonPath("$.page.totalElements").value(0));
 }
 List<Integer> race(Callable<Integer> first,Callable<Integer> second) throws Exception {
  ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
  try{
   Callable<Integer> a=()->{ready.countDown();start.await();return first.call();};
   Callable<Integer> b=()->{ready.countDown();start.await();return second.call();};
   var fa=pool.submit(a);var fb=pool.submit(b);assertTrue(ready.await(5,TimeUnit.SECONDS));start.countDown();
   return List.of(fa.get(15,TimeUnit.SECONDS),fb.get(15,TimeUnit.SECONDS));
  }finally{start.countDown();pool.shutdownNow();}
 }
 @Test void concurrentLoansProduceOneSuccessAndOneConflict() throws Exception {
  String body=payload();Callable<Integer> call=()->mvc.perform(post("/api/loans").contentType("application/json").content(body)).andReturn().getResponse().getStatus();
  var statuses=race(call,call);assertTrue(statuses.contains(201));assertTrue(statuses.contains(409));
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM loans WHERE copy_id=? AND returned_date IS NULL",Integer.class,copy));
 }
 @Test void concurrentReturnsRecordDateOnce() throws Exception {
  long id=loan();Callable<Integer> call=()->mvc.perform(post("/api/loans/"+id+"/return")).andReturn().getResponse().getStatus();
  var statuses=race(call,call);assertTrue(statuses.contains(200));assertTrue(statuses.contains(409));
 }
 @Test void concurrentWithdrawalAndLoanKeepStateConsistent() throws Exception {
  String body=payload();var statuses=race(()->mvc.perform(post("/api/loans").contentType("application/json").content(body)).andReturn().getResponse().getStatus(),
   ()->mvc.perform(post("/api/copies/"+copy+"/withdrawal")).andReturn().getResponse().getStatus());
  assertTrue(statuses.equals(List.of(201,409))||statuses.equals(List.of(409,200)),statuses.toString());
  assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM loans l JOIN book_copies c ON c.id=l.copy_id WHERE c.id=? AND c.circulation_status='WITHDRAWN' AND l.returned_date IS NULL",Integer.class,copy));
 }
 @Test void deactivationHoldingReaderLockPreventsWaitingLoan() throws Exception {
  ExecutorService pool=Executors.newSingleThreadExecutor();
  var transaction=new org.springframework.transaction.support.TransactionTemplate(transactionManager);
  String body=payload();
  try {
   final java.util.concurrent.atomic.AtomicReference<Future<Integer>> pending=new java.util.concurrent.atomic.AtomicReference<>();
   transaction.executeWithoutResult(tx->{
    jdbc.queryForObject("SELECT id FROM readers WHERE id=? FOR UPDATE",Long.class,reader);
    jdbc.update("UPDATE readers SET active=0 WHERE id=?",reader);
    pending.set(pool.submit(()->mvc.perform(post("/api/loans").contentType("application/json").content(body)).andReturn().getResponse().getStatus()));
   });
   assertEquals(409,pending.get().get(15,TimeUnit.SECONDS));
   assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM loans WHERE reader_id=?",Integer.class,reader));
  }finally{pool.shutdownNow();}
 }
 @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;
 @Test void databaseProtectsActiveUniquenessAndDates() throws Exception {
  loan();assertThrows(DataIntegrityViolationException.class,()->jdbc.update("INSERT INTO loans(reader_id,copy_id,loan_date,due_date) VALUES(?,?,?,?)",reader,copy,"2026-10-08","2026-10-22"));
  var failure=assertThrows(org.springframework.jdbc.UncategorizedSQLException.class,()->jdbc.update("INSERT INTO loans(reader_id,copy_id,loan_date,due_date,returned_date) VALUES(?,?,?,?,?)",reader,copy,"2026-10-08","2026-10-07","2026-10-08"));
  assertEquals(3819,failure.getSQLException().getErrorCode());
  assertTrue(failure.getSQLException().getMessage().contains("ck_loans_due"));
 }
 @Test void missingResourcesAndFiltersAreReported() throws Exception {
  mvc.perform(post("/api/loans").contentType("application/json").content("{\"readerId\":9223372036854775807,\"copyId\":1}")).andExpect(status().isNotFound());
  mvc.perform(post("/api/loans/999999/return")).andExpect(status().isNotFound());
  mvc.perform(get("/api/loans?status=INVALID")).andExpect(status().isBadRequest());
  mvc.perform(get("/api/loans?readerId=9223372036854775807")).andExpect(status().isNotFound());
 }
}
