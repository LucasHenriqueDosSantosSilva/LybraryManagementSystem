package com.lucashenrique.library;
import com.lucashenrique.library.catalog.domain.Isbn;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class IsbnTest {
 @Test void convertsEquivalentFormats(){
  assertEquals("9780306406157",Isbn.canonicalize("0-306-40615-2"));
  assertEquals("9780306406157",Isbn.canonicalize("978 0 306 40615 7"));
 }
 @Test void convertsIsbn10WithX(){assertEquals("9780804429573",Isbn.canonicalize("080442957X"));}
 @Test void rejectsInvalidChecksumsAndFormats(){
  for(String value:new String[]{"0306406153","9780306406158","1234567890128","not-isbn",""})
   assertThrows(IllegalArgumentException.class,()->Isbn.canonicalize(value),value);
  assertThrows(IllegalArgumentException.class,()->Isbn.canonicalize(null));
 }
}
