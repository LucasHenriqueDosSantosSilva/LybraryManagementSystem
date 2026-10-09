package com.lucashenrique.library;

import com.lucashenrique.library.exception.ResourceNotFoundException;
import com.lucashenrique.library.repository.CategoryRepository;
import com.lucashenrique.library.service.CategoryService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceFailureTest {
    @Test void reportsMissingCategoryWithoutHttpDependency() {
        var repository = mock(CategoryRepository.class);
        when(repository.findById(42L)).thenReturn(Optional.empty());
        var exception = assertThrows(ResourceNotFoundException.class, () -> new CategoryService(repository).get(42L));
        assertEquals("Categoria não encontrada.", exception.getMessage());
        verify(repository, never()).saveAndFlush(any());
    }
}
