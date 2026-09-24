package com.company.inventory.controller;

import com.company.inventory.model.Product;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.services.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ExtendWith(MockitoExtension.class)
class ProductRestControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ProductRestController productRestController;

    @Mock
    private IProductService productService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productRestController)
                .build();
    }

    @Test
    void testSearchProducts() throws Exception {
        // Given
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(5000);
        product.setAccount(10);

        ProductResponseRest response = new ProductResponseRest();
        response.getProduct().setProducts(List.of(product));
        response.setMetadata(
                "Respuesta ok",
                "00",
                "Respuesta exitosa"
        );

        when(productService.search()).thenReturn(
                new ResponseEntity<>(response, HttpStatus.OK)
        );

        // When y Then
        mockMvc.perform(get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").exists())
                .andExpect(jsonPath("$.product.products[0].name")
                        .value("Laptop"));
    }
    @Test
    void testSearchProductsError() throws Exception {
        // Given
        ProductResponseRest response = new ProductResponseRest();
        response.setMetadata(
                "Error",
                "01",
                "Error al consultar productos"
        );

        when(productService.search()).thenReturn(
                new ResponseEntity<>(
                        response,
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
        );

        // When y Then
        mockMvc.perform(get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.metadata").exists());
    }
    @Test
    void testSearchProductById() throws Exception {
        // Given
        Long id = 1L;

        Product product = new Product();
        product.setId(id);
        product.setName("Laptop");
        product.setPrice(5000);
        product.setAccount(10);

        ProductResponseRest response = new ProductResponseRest();
        response.getProduct().setProducts(List.of(product));
        response.setMetadata(
                "Respuesta ok",
                "00",
                "Respuesta exitosa"
        );

        when(productService.searchById(id)).thenReturn(
                new ResponseEntity<>(response, HttpStatus.OK)
        );

        // When y Then
        mockMvc.perform(get("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.products[0].id").value(1))
                .andExpect(jsonPath("$.product.products[0].name")
                        .value("Laptop"));
    }
    @Test
    void testSearchProductByIdNotFound() throws Exception {
        // Given
        Long id = 99L;

        ProductResponseRest response = new ProductResponseRest();
        response.setMetadata(
                "respuesta nok",
                "-1",
                "Producto no encontrado"
        );

        when(productService.searchById(id)).thenReturn(
                new ResponseEntity<>(response, HttpStatus.NOT_FOUND)
        );

        // When y Then
        mockMvc.perform(get("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.metadata").exists());
    }
    @Test
    void testSearchProductsByName() throws Exception {
        // Given
        String name = "lap";

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(5000);
        product.setAccount(10);

        ProductResponseRest response = new ProductResponseRest();
        response.getProduct().setProducts(List.of(product));
        response.setMetadata(
                "Respuesta ok",
                "00",
                "Productos encontrados"
        );

        when(productService.searchByName(name)).thenReturn(
                new ResponseEntity<>(response, HttpStatus.OK)
        );

        // When y Then
        mockMvc.perform(get("/api/v1/products/filter/{name}", name)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.products[0].name")
                        .value("Laptop"));
    }

    @Test
    void testSearchProductsByNameNotFound() throws Exception {
        // Given
        String name = "inexistente";

        ProductResponseRest response = new ProductResponseRest();
        response.setMetadata(
                "respuesta nok",
                "-1",
                "Productos no encontrados"
        );

        when(productService.searchByName(name)).thenReturn(
                new ResponseEntity<>(response, HttpStatus.NOT_FOUND)
        );

        // When y Then
        mockMvc.perform(get("/api/v1/products/filter/{name}", name)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void testSaveProduct() throws Exception {
        // Given: imagen falsa para la prueba
        MockMultipartFile picture = new MockMultipartFile(
                "picture",
                "laptop.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "imagen de prueba".getBytes()
        );

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Laptop");
        savedProduct.setPrice(5000);
        savedProduct.setAccount(10);

        ProductResponseRest response = new ProductResponseRest();
        response.getProduct().setProducts(List.of(savedProduct));
        response.setMetadata(
                "respuesta ok",
                "00",
                "Producto guardado"
        );

        when(productService.save(any(Product.class), eq(1L)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // When y Then
        mockMvc.perform(multipart("/api/v1/products")
                        .file(picture)
                        .param("name", "Laptop")
                        .param("price", "5000")
                        .param("account", "10")
                        .param("categoryId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.products[0].name")
                        .value("Laptop"));
    }
    @Test
    void testSaveProductCategoryNotFound() throws Exception {
        // Given
        MockMultipartFile picture = new MockMultipartFile(
                "picture",
                "laptop.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "imagen de prueba".getBytes()
        );

        ProductResponseRest response = new ProductResponseRest();
        response.setMetadata(
                "respuesta nok",
                "-1",
                "Categoria no encontrada asociada al producto"
        );

        when(productService.save(any(Product.class), eq(99L)))
                .thenReturn(
                        new ResponseEntity<>(
                                response,
                                HttpStatus.NOT_FOUND
                        )
                );

        // When y Then
        mockMvc.perform(multipart("/api/v1/products")
                        .file(picture)
                        .param("name", "Laptop")
                        .param("price", "5000")
                        .param("account", "10")
                        .param("categoryId", "99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.metadata").exists());
    }
    @Test
    void testUpdateProduct() throws Exception {
        // Given
        Long productId = 1L;
        Long categoryId = 1L;

        MockMultipartFile picture = new MockMultipartFile(
                "picture",
                "laptop-actualizada.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "imagen actualizada".getBytes()
        );

        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("Laptop actualizada");
        updatedProduct.setPrice(5500);
        updatedProduct.setAccount(8);

        ProductResponseRest response = new ProductResponseRest();
        response.getProduct().setProducts(List.of(updatedProduct));
        response.setMetadata(
                "respuesta ok",
                "00",
                "Producto actualizado"
        );

        when(productService.update(
                any(Product.class),
                eq(categoryId),
                eq(productId)
        )).thenReturn(
                new ResponseEntity<>(response, HttpStatus.OK)
        );

        // When y Then
        mockMvc.perform(multipart(
                        "/api/v1/products/{id}",
                        productId
                )
                        .file(picture)
                        .param("name", "Laptop actualizada")
                        .param("price", "5500")
                        .param("account", "8")
                        .param("categoryId", "1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.products[0].name")
                        .value("Laptop actualizada"));
    }
    @Test
    void testUpdateProductNotFound() throws Exception {
        // Given
        Long productId = 99L;
        Long categoryId = 1L;

        MockMultipartFile picture = new MockMultipartFile(
                "picture",
                "producto.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "imagen de prueba".getBytes()
        );

        ProductResponseRest response = new ProductResponseRest();
        response.setMetadata(
                "respuesta nok",
                "-1",
                "Producto no actualizado"
        );

        when(productService.update(
                any(Product.class),
                eq(categoryId),
                eq(productId)
        )).thenReturn(
                new ResponseEntity<>(response, HttpStatus.NOT_FOUND)
        );

        // When y Then
        mockMvc.perform(multipart(
                        "/api/v1/products/{id}",
                        productId
                )
                        .file(picture)
                        .param("name", "Producto inexistente")
                        .param("price", "100")
                        .param("account", "1")
                        .param("categoryId", "1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void testDeleteProduct() throws Exception {
        // Given
        Long id = 1L;

        ProductResponseRest response = new ProductResponseRest();
        response.setMetadata(
                "Respuesta ok",
                "00",
                "Producto eliminado"
        );

        when(productService.deleteById(id)).thenReturn(
                new ResponseEntity<>(response, HttpStatus.OK)
        );

        // When y Then
        mockMvc.perform(delete("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void testDeleteProductError() throws Exception {
        // Given
        Long id = 99L;

        ProductResponseRest response = new ProductResponseRest();
        response.setMetadata(
                "respuesta nok",
                "-1",
                "Error al eliminar producto"
        );

        when(productService.deleteById(id)).thenReturn(
                new ResponseEntity<>(
                        response,
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
        );

        // When y Then
        mockMvc.perform(delete("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.metadata").exists());
    }
}

