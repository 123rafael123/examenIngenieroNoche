package com.company.inventory.services;

import com.company.inventory.dao.ICategoryDao;
import com.company.inventory.dao.IProductDao;
import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.util.Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del servicio de productos")
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl service;

    @Mock
    private ICategoryDao categoryDao;

    @Mock
    private IProductDao productDao;

    @Test
    @DisplayName("Guarda un producto cuando la categoría existe")
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
    @DisplayName("Retorna no encontrado al guardar con una categoría inexistente")
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
    @DisplayName("Busca un producto existente por identificador y descomprime su imagen")
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
    @DisplayName("Retorna no encontrado al buscar un producto inexistente")
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

    @Test
    @DisplayName("Retorna solicitud incorrecta cuando el DAO no guarda el producto")
    void testSaveProductDaoReturnsNull() {
        // Given
        Category category = createCategory();
        Product product = createProduct();
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.save(product)).thenReturn(null);

        // When
        ResponseEntity<ProductResponseRest> response = service.save(product, 1L);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Producto no guardado ", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(1L);
        verify(productDao).save(product);
    }

    @Test
    @DisplayName("Retorna error interno cuando falla el guardado del producto")
    void testSaveProductDaoException() {
        // Given
        Category category = createCategory();
        Product product = createProduct();
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.save(product)).thenThrow(new RuntimeException("Error del DAO"));

        // When
        ResponseEntity<ProductResponseRest> response = service.save(product, 1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al guardar producto", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(1L);
        verify(productDao).save(product);
    }

    @Test
    @DisplayName("Retorna error interno cuando falla la búsqueda de producto por identificador")
    void testSearchByIdDaoException() {
        // Given
        when(productDao.findById(1L)).thenThrow(new RuntimeException("Error del DAO"));

        // When
        ResponseEntity<ProductResponseRest> response = service.searchById(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("respuesta nok", response.getBody().getMetadata().get(0).get("type"));
        verify(productDao).findById(1L);
    }

    @Test
    @DisplayName("Busca productos por nombre y descomprime sus imágenes")
    void testSearchByNameSuccess() {
        // Given
        byte[] firstPicture = new byte[]{1, 2, 3, 4};
        byte[] secondPicture = new byte[]{5, 6, 7, 8};
        Product firstProduct = createStoredProduct(1L, "Arroz integral", firstPicture);
        Product secondProduct = createStoredProduct(2L, "Arroz blanco", secondPicture);
        when(productDao.findByNameContainingIgnoreCase("arroz"))
                .thenReturn(List.of(firstProduct, secondProduct));

        // When
        ResponseEntity<ProductResponseRest> response = service.searchByName("arroz");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Product> products = response.getBody().getProduct().getProducts();
        assertEquals(2, products.size());
        assertEquals("Arroz integral", products.get(0).getName());
        assertArrayEquals(firstPicture, products.get(0).getPicture());
        assertArrayEquals(secondPicture, products.get(1).getPicture());
        verify(productDao).findByNameContainingIgnoreCase("arroz");
    }

    @Test
    @DisplayName("Retorna no encontrado cuando ningún producto coincide con el nombre")
    void testSearchByNameNotFound() {
        // Given
        when(productDao.findByNameContainingIgnoreCase("inexistente")).thenReturn(List.of());

        // When
        ResponseEntity<ProductResponseRest> response = service.searchByName("inexistente");

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Productos no encontrados ", response.getBody().getMetadata().get(0).get("date"));
        verify(productDao).findByNameContainingIgnoreCase("inexistente");
    }

    @Test
    @DisplayName("Retorna error interno cuando falla la búsqueda de productos por nombre")
    void testSearchByNameDaoException() {
        // Given
        when(productDao.findByNameContainingIgnoreCase("arroz"))
                .thenThrow(new RuntimeException("Error del DAO"));

        // When
        ResponseEntity<ProductResponseRest> response = service.searchByName("arroz");

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al buscar producto por nombre",
                response.getBody().getMetadata().get(0).get("date"));
        verify(productDao).findByNameContainingIgnoreCase("arroz");
    }

    @Test
    @DisplayName("Lista los productos y descomprime sus imágenes")
    void testSearchProductsSuccess() {
        // Given
        byte[] firstPicture = new byte[]{1, 2, 3, 4};
        byte[] secondPicture = new byte[]{5, 6, 7, 8};
        Product firstProduct = createStoredProduct(1L, "Arroz", firstPicture);
        Product secondProduct = createStoredProduct(2L, "Leche", secondPicture);
        when(productDao.findAll()).thenReturn(List.of(firstProduct, secondProduct));

        // When
        ResponseEntity<ProductResponseRest> response = service.search();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Product> products = response.getBody().getProduct().getProducts();
        assertEquals(2, products.size());
        assertEquals("Arroz", products.get(0).getName());
        assertEquals("Leche", products.get(1).getName());
        assertArrayEquals(firstPicture, products.get(0).getPicture());
        assertArrayEquals(secondPicture, products.get(1).getPicture());
        verify(productDao).findAll();
    }

    @Test
    @DisplayName("Retorna no encontrado cuando la lista de productos está vacía")
    void testSearchProductsEmpty() {
        // Given
        when(productDao.findAll()).thenReturn(List.of());

        // When
        ResponseEntity<ProductResponseRest> response = service.search();

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Productos no encontrados ", response.getBody().getMetadata().get(0).get("date"));
        verify(productDao).findAll();
    }

    @Test
    @DisplayName("Retorna error interno cuando falla el listado de productos")
    void testSearchProductsDaoException() {
        // Given
        when(productDao.findAll()).thenThrow(new RuntimeException("Error del DAO"));

        // When
        ResponseEntity<ProductResponseRest> response = service.search();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al buscar productos", response.getBody().getMetadata().get(0).get("date"));
        verify(productDao).findAll();
    }

    @Test
    @DisplayName("Actualiza un producto correctamente")
    void testUpdateProductSuccess() {
        // Given
        Category category = createCategory();
        Product storedProduct = createProduct();
        Product newValues = createProduct();
        newValues.setName("Arroz premium");
        newValues.setPrice(18);
        newValues.setAccount(30);
        newValues.setPicture(new byte[]{9, 8, 7});
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.findById(1L)).thenReturn(Optional.of(storedProduct));
        when(productDao.save(storedProduct)).thenReturn(storedProduct);

        // When
        ResponseEntity<ProductResponseRest> response = service.update(newValues, 1L, 1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Product updatedProduct = response.getBody().getProduct().getProducts().get(0);
        assertEquals("Arroz premium", updatedProduct.getName());
        assertEquals(18, updatedProduct.getPrice());
        assertEquals(30, updatedProduct.getAccount());
        assertEquals(category, updatedProduct.getCategory());
        assertArrayEquals(newValues.getPicture(), updatedProduct.getPicture());
        verify(categoryDao).findById(1L);
        verify(productDao).findById(1L);
        verify(productDao).save(storedProduct);
    }

    @Test
    @DisplayName("Retorna no encontrado al actualizar con una categoría inexistente")
    void testUpdateProductWithMissingCategory() {
        // Given
        Product newValues = createProduct();
        when(categoryDao.findById(99L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ProductResponseRest> response = service.update(newValues, 99L, 1L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Categoria no encontrada asociada al producto ",
                response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(99L);
        verify(productDao, never()).findById(anyLong());
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Retorna no encontrado al actualizar un producto inexistente")
    void testUpdateMissingProduct() {
        // Given
        Category category = createCategory();
        Product newValues = createProduct();
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.findById(99L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ProductResponseRest> response = service.update(newValues, 1L, 99L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Producto no actualizado ", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(1L);
        verify(productDao).findById(99L);
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Retorna solicitud incorrecta cuando el DAO no actualiza el producto")
    void testUpdateProductDaoReturnsNull() {
        // Given
        Category category = createCategory();
        Product storedProduct = createProduct();
        Product newValues = createProduct();
        newValues.setName("Arroz premium");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.findById(1L)).thenReturn(Optional.of(storedProduct));
        when(productDao.save(storedProduct)).thenReturn(null);

        // When
        ResponseEntity<ProductResponseRest> response = service.update(newValues, 1L, 1L);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Producto no actualizado ", response.getBody().getMetadata().get(0).get("date"));
        verify(productDao).save(storedProduct);
    }

    @Test
    @DisplayName("Retorna error interno cuando falla el DAO de categorías durante la actualización")
    void testUpdateProductCategoryDaoException() {
        // Given
        Product newValues = createProduct();
        when(categoryDao.findById(1L)).thenThrow(new RuntimeException("Error del DAO"));

        // When
        ResponseEntity<ProductResponseRest> response = service.update(newValues, 1L, 1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al actualizar producto", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(1L);
        verifyNoInteractions(productDao);
    }

    @Test
    @DisplayName("Retorna error interno cuando falla la búsqueda del producto a actualizar")
    void testUpdateProductFindDaoException() {
        // Given
        Category category = createCategory();
        Product newValues = createProduct();
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.findById(1L)).thenThrow(new RuntimeException("Error del DAO"));

        // When
        ResponseEntity<ProductResponseRest> response = service.update(newValues, 1L, 1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al actualizar producto", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(1L);
        verify(productDao).findById(1L);
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Retorna error interno cuando falla el guardado del producto actualizado")
    void testUpdateProductSaveDaoException() {
        // Given
        Category category = createCategory();
        Product storedProduct = createProduct();
        Product newValues = createProduct();
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.findById(1L)).thenReturn(Optional.of(storedProduct));
        when(productDao.save(storedProduct)).thenThrow(new RuntimeException("Error del DAO"));

        // When
        ResponseEntity<ProductResponseRest> response = service.update(newValues, 1L, 1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al actualizar producto", response.getBody().getMetadata().get(0).get("date"));
        verify(productDao).save(storedProduct);
    }

    @Test
    @DisplayName("Elimina un producto correctamente")
    void testDeleteProductSuccess() {
        // Given
        doNothing().when(productDao).deleteById(1L);

        // When
        ResponseEntity<ProductResponseRest> response = service.deleteById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Producto eliminado", response.getBody().getMetadata().get(0).get("date"));
        verify(productDao).deleteById(1L);
    }

    @Test
    @DisplayName("Retorna error interno cuando falla la eliminación del producto")
    void testDeleteProductDaoException() {
        // Given
        doThrow(new RuntimeException("Error del DAO")).when(productDao).deleteById(1L);

        // When
        ResponseEntity<ProductResponseRest> response = service.deleteById(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al eliminar producto", response.getBody().getMetadata().get(0).get("date"));
        verify(productDao).deleteById(1L);
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

    private Product createStoredProduct(Long id, String name, byte[] originalPicture) {
        Product product = createProduct();
        product.setId(id);
        product.setName(name);
        product.setPicture(Util.compressZLib(originalPicture));
        return product;
    }
}
