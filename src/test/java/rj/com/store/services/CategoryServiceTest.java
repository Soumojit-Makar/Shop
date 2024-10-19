package rj.com.store.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import rj.com.store.datatransferobjects.CategoryDTO;
import rj.com.store.enities.Category;
import rj.com.store.enities.Product;
import rj.com.store.exceptions.ResourceNotFoundException;
import rj.com.store.repositories.CategoryRepository;
import rj.com.store.services.ImageServiceInCloud;
import rj.com.store.services.servicesimp.CategoryServiceImp;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceImpTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ImageServiceInCloud imageServiceInCloud;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImp categoryServiceImp;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Sample data setup
        category = new Category();
        category.setCategoryId(UUID.randomUUID().toString());
        category.setTitle("Electronics");
        category.setDescription("Category for electronics products");
        category.setCoverImage("image_url");

        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(category.getCategoryId());
        categoryDTO.setTitle(category.getTitle());
        categoryDTO.setDescription(category.getDescription());
        categoryDTO.setCoverImage(category.getCoverImage());
    }

    @Test
    void createCategorySuccess() {
        when(modelMapper.map(any(CategoryDTO.class), eq(Category.class))).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);
        CategoryDTO createdCategory = categoryServiceImp.createCategory(categoryDTO);
        assertNotNull(createdCategory);
        assertEquals("Electronics", createdCategory.getTitle());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategorySuccess() {
        String categoryId = "some-category-id";

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setTitle("Old Title");
        category.setCoverImage("old-cover.jpg");
        category.setDescription("Old description");

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(categoryId);
        categoryDTO.setTitle("Updated Title");
        categoryDTO.setCoverImage("updated-cover.jpg");
        categoryDTO.setDescription("Updated description");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(modelMapper.map(any(CategoryDTO.class), eq(Category.class))).thenReturn(category);
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryDTO updatedCategory = categoryServiceImp.updateCategory(categoryDTO, categoryId);
        assertNotNull(updatedCategory);
        assertEquals("Updated Title", updatedCategory.getTitle());
        assertEquals("updated-cover.jpg", updatedCategory.getCoverImage());
        assertEquals("Updated description", updatedCategory.getDescription());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }




    @Test
    void updateCategoryNotFound() {
        String categoryId = category.getCategoryId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryServiceImp.updateCategory(categoryDTO, categoryId);
        });
        verify(categoryRepository, times(0)).save(any(Category.class));
    }

    @Test
    void deleteCategorySuccess() {
        String categoryId = category.getCategoryId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        categoryServiceImp.deleteCategory(categoryId);
        verify(categoryRepository, times(1)).delete(any(Category.class));
        verify(imageServiceInCloud, times(1)).deleteImage(anyString());
    }

    @Test
    void deleteCategoryNotFound() {
        String categoryId = category.getCategoryId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryServiceImp.deleteCategory(categoryId);
        });
        verify(categoryRepository, times(0)).delete(any(Category.class));
        verify(imageServiceInCloud, times(0)).deleteImage(anyString());
    }

    @Test
    void getCategoryByIdSuccess() {
        String categoryId = category.getCategoryId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);
        CategoryDTO foundCategory = categoryServiceImp.getCategoryById(categoryId);
        assertNotNull(foundCategory);
        assertEquals("Electronics", foundCategory.getTitle());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void getCategoryByIdNotFound() {
        String categoryId = category.getCategoryId();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryServiceImp.getCategoryById(categoryId);
        });
        verify(categoryRepository, times(1)).findById(categoryId);
    }
}
