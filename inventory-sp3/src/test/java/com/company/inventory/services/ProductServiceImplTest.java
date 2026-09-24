package com.company.inventory.services;

import com.company.inventory.dao.ICategoryDao;
import com.company.inventory.dao.IProductDao;
import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.util.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl service;

    @Mock
    private ICategoryDao categoryDao;

    @Mock
    private IProductDao productDao;

    @Test
    void testSaveProductWithExistingCategory() {
        // Given
        Category category = createCategory();
        Product product = createProduct();
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResponseEntity<ProductResponseRest> response = service.save(product, 1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getProduct().getProducts().size());
        assertEquals("Arroz", response.getBody().getProduct().getProducts().get(0).getName());
        assertEquals(category, response.getBody().getProduct().getProducts().get(0).getCategory());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(categoryDao).findById(1L);
        verify(productDao).save(productCaptor.capture());
        assertEquals(category, productCaptor.getValue().getCategory());
    }

    @Test
    void testSaveProductWithMissingCategory() {
        // Given
        Product product = createProduct();
        when(categoryDao.findById(99L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ProductResponseRest> response = service.save(product, 99L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("respuesta nok", response.getBody().getMetadata().get(0).get("type"));
        assertEquals("Categoria no encontrada asociada al producto ",
                response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(99L);
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    void testSearchByIdExistingProduct() {
        // Given
        Product product = createProduct();
        byte[] originalPicture = product.getPicture();
        product.setPicture(Util.compressZLib(originalPicture));
        when(productDao.findById(1L)).thenReturn(Optional.of(product));

        // When
        ResponseEntity<ProductResponseRest> response = service.searchById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Product foundProduct = response.getBody().getProduct().getProducts().get(0);
        assertEquals("Arroz", foundProduct.getName());
        assertArrayEquals(originalPicture, foundProduct.getPicture());
        assertEquals("Respuesta ok", response.getBody().getMetadata().get(0).get("type"));
        verify(productDao).findById(1L);
    }

    @Test
    void testSearchByIdMissingProduct() {
        // Given
        when(productDao.findById(99L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ProductResponseRest> response = service.searchById(99L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("respuesta nok", response.getBody().getMetadata().get(0).get("type"));
        assertEquals("Producto no encontrada ", response.getBody().getMetadata().get(0).get("date"));
        verify(productDao).findById(99L);
    }

    private Category createCategory() {
        return new Category(1L, "Abarrotes", "Productos de consumo diario");
    }

    private Product createProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Arroz");
        product.setPrice(12);
        product.setAccount(20);
        product.setPicture(new byte[]{1, 2, 3, 4});
        return product;
    }
}
