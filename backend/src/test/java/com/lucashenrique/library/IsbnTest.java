package com.lucashenrique.library;
import com.lucashenrique.library.catalog.domain.Isbn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
class IsbnTest {
 @Test void convertsEquivalentFormats(){
  assertEquals("9780306406157",Isbn.canonicalize("0-306-40615-2"));
  assertEquals("9780306406157",Isbn.canonicalize("978 0 306 40615 7"));
 }
 @ParameterizedTest
 @ValueSource(strings={"978\t0\t306\t40615\t7", "978\u00a00\u00a0306\u00a040615\u00a07", "978\u202f0\u202f306\u202f40615\u202f7"})
 void normalizesWhitespaceFromPastedIsbn(String value){
  assertEquals("9780306406157",Isbn.canonicalize(value));
 }
 @ParameterizedTest
 @ValueSource(strings={"978\t0\t306\t40615\t8", "978\u00a00\u00a0306\u00a040615\u00a08", "978\u202f0\u202f306\u202f40615\u202f8"})
 void whitespaceDoesNotBypassChecksum(String value){
  assertThrows(IllegalArgumentException.class,()->Isbn.canonicalize(value));
 }
 @Test void convertsIsbn10WithX(){assertEquals("9780804429573",Isbn.canonicalize("080442957X"));}
 @Test void rejectsInvalidChecksumsAndFormats(){
  for(String value:new String[]{"0306406153","9780306406158","1234567890128","not-isbn",""})
   assertThrows(IllegalArgumentException.class,()->Isbn.canonicalize(value),value);
  assertThrows(IllegalArgumentException.class,()->Isbn.canonicalize(null));
 }
}
