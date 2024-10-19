package rj.com.store.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import rj.com.store.datatransferobjects.ProductDTO;
import rj.com.store.enities.Product;
import rj.com.store.repositories.CategoryRepository;
import rj.com.store.repositories.ProductRepository;
import rj.com.store.services.servicesimp.ProductServiceImp;

import java.util.Optional;
import java.util.UUID;
import java.util.Date;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ImageServiceInCloud imageServiceInCloud;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private ProductServiceImp productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setProductId(UUID.randomUUID().toString());
        product.setTitle("Test Product");
        product.setPrice(100.0);
        product.setAddedDate(new Date());
        product.setProductImageName("image.jpg");

        productDTO = new ProductDTO();
        productDTO.setProductId(product.getProductId());
        productDTO.setTitle("Test Product");
        productDTO.setPrice(100.0);
        productDTO.setProductImageName("image.jpg");
    }

    @Test
    void testCreateProduct() {
        when(mapper.map(any(ProductDTO.class), eq(Product.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(mapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);
        ProductDTO createdProduct = productService.createProduct(productDTO);
        assertNotNull(createdProduct);
        assertEquals(productDTO.getProductId(), createdProduct.getProductId());
        assertEquals(productDTO.getTitle(), createdProduct.getTitle());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.findById(anyString())).thenReturn(Optional.of(product));
        productService.DeleteProduct(product.getProductId());
        verify(productRepository, times(1)).delete(any(Product.class));
        verify(imageServiceInCloud, times(1)).deleteImage(anyString());
    }

    @Test
    void testGetSingleProduct() {
        when(productRepository.findById(anyString())).thenReturn(Optional.of(product));
        when(mapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);
        ProductDTO result = productService.getSingleProduct(product.getProductId());
        assertNotNull(result);
        assertEquals(productDTO.getProductId(), result.getProductId());
        verify(productRepository, times(1)).findById(anyString());
    }

    @Test
    void testUpdateProduct() {
        when(productRepository.findById(anyString())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(mapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);
        ProductDTO updatedProduct = productService.updateProduct(productDTO, product.getProductId());
        assertNotNull(updatedProduct);
        assertEquals(productDTO.getTitle(), updatedProduct.getTitle());
        verify(productRepository, times(1)).findById(anyString());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
