package com.lucashenrique.library;
import com.lucashenrique.library.dto.CategoryRequest;
import com.lucashenrique.library.entity.Category;
import com.lucashenrique.library.repository.CategoryRepository;
import com.lucashenrique.library.service.CategoryService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CategoryServiceTest {
    @Test void stripsWhitespaceBeforePersistence() {
        CategoryRepository repository = mock(CategoryRepository.class);
        when(repository.saveAndFlush(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = new CategoryService(repository).create(new CategoryRequest("  História  "));
        assertEquals("História", result.name());
        verify(repository).saveAndFlush(argThat(category -> category.getName().equals("História")));
    }
}
