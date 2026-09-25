package com.company.inventory.services;

import com.company.inventory.dao.ICategoryDao;
import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponseRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas del servicio de categorías")
class CategoryServiceImplTest {

    @InjectMocks
    CategoryServiceImpl service;

    @Mock
    ICategoryDao categoryDao;

    List<Category> list = new ArrayList<Category>();

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        this.chargeList();
    }

    /**
     * Test de la busqueda de categorias
     * Se espera que retorne una lista con 2 categorias
     * y el estado de la respuesta HTTP sea OK
     */
    @Test
    @DisplayName("Lista las categorías correctamente")
    void testSearchSucess() {

        //Given
        when(categoryDao.findAll()).thenReturn(list);

        //When
        ResponseEntity<CategoryResponseRest> response = service.search();

        //Then
        assertEquals(2, response.getBody().getCategoryResponse().getCategory().size());
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respueesta HTTP debe ser OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Abarrotes", response.getBody().getCategoryResponse().getCategory().get(0).getName(), "El nombre de la primera categoria debe ser Abarrotes");

        //Optional
        verify(categoryDao, times(1)).findAll();

    }

    /**
     * Test de la busqueda de categorias
     * Se espera que retorne un error al consultar
     * y el estado de la respuesta HTTP sea INTERNAL_SERVER_ERROR
     */
    @Test
    @DisplayName("Retorna error interno cuando falla el listado de categorías")
    void testSearchException() {

        //Given
        when(categoryDao.findAll()).thenThrow(new RuntimeException("Error al consultar"));

        //When
        ResponseEntity<CategoryResponseRest> response = service.search();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"), "El tipo de respuesta debe ser Respuesta nok");

        //Optional
        verify(categoryDao, times(1)).findAll();
    }

    /**
     * Test para probar guardar una categoria
     */
    @Test
    @DisplayName("Guarda una categoría correctamente")
    void testSaveCategorySuccess() {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintas tipos de bebidas");

        when(categoryDao.save(ArgumentMatchers.any())).thenReturn(category);

        // When
        ResponseEntity<CategoryResponseRest> response = service.save(category);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Bebidas", response.getBody().getCategoryResponse().getCategory().get(0).getName(), "El nombre de la categoria guardada debe ser Bebidas");

        verify(categoryDao, times(1)).save(ArgumentMatchers.any());
    }

    /**
     * Test para probar retorno nullo al guardar una categoria
     */
    @Test
    @DisplayName("Retorna solicitud incorrecta cuando el DAO no guarda la categoría")
    void testSaveCategoryDaoReturnsNull() {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintas tipos de bebidas");

        when(categoryDao.save(ArgumentMatchers.any())).thenReturn(null);

        // When
        ResponseEntity<CategoryResponseRest> response = service.save(category);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El estado de la respuesta HTTP debe ser BAD_REQUEST");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"), "El tipo de respuesta debe ser Respuesta nok");

        verify(categoryDao, times(1)).save(ArgumentMatchers.any());
    }

    /**
     * Test para probar una excepción al guardar una categoria
     */
    @Test
    @DisplayName("Retorna error interno cuando falla el guardado de la categoría")
    void testSaveCategoryException() {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintas tipos de bebidas");

        when(categoryDao.save(ArgumentMatchers.any())).thenThrow(new RuntimeException("Error al guardar"));

        // When
        ResponseEntity<CategoryResponseRest> response = service.save(category);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"), "El tipo de respuesta debe ser Respuesta nok");

        verify(categoryDao, times(1)).save(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Busca una categoría existente por identificador")
    void testSearchByIdSuccess() {
        // Given
        Category category = list.get(0);
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));

        // When
        ResponseEntity<CategoryResponseRest> response = service.searchById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getCategoryResponse().getCategory().size());
        assertEquals("Abarrotes", response.getBody().getCategoryResponse().getCategory().get(0).getName());
        assertEquals("Respuesta ok", response.getBody().getMetadata().get(0).get("type"));
        verify(categoryDao).findById(1L);
    }

    @Test
    @DisplayName("Retorna no encontrado al buscar una categoría inexistente")
    void testSearchByIdNotFound() {
        // Given
        when(categoryDao.findById(99L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<CategoryResponseRest> response = service.searchById(99L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"));
        assertEquals("Categoria no encontrada", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(99L);
    }

    @Test
    @DisplayName("Retorna error interno cuando falla la búsqueda de categoría por identificador")
    void testSearchByIdException() {
        // Given
        when(categoryDao.findById(1L)).thenThrow(new RuntimeException("Error al consultar por id"));

        // When
        ResponseEntity<CategoryResponseRest> response = service.searchById(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"));
        assertEquals("Error al consultar por id", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(1L);
    }

    @Test
    @DisplayName("Actualiza una categoría correctamente")
    void testUpdateCategorySuccess() {
        // Given
        Category storedCategory = list.get(0);
        Category newValues = new Category(null, "Alimentos", "Productos alimenticios");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(storedCategory));
        when(categoryDao.save(storedCategory)).thenReturn(storedCategory);

        // When
        ResponseEntity<CategoryResponseRest> response = service.update(newValues, 1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Category updatedCategory = response.getBody().getCategoryResponse().getCategory().get(0);
        assertEquals("Alimentos", updatedCategory.getName());
        assertEquals("Productos alimenticios", updatedCategory.getDescription());
        verify(categoryDao).findById(1L);
        verify(categoryDao).save(storedCategory);
    }

    @Test
    @DisplayName("Retorna no encontrado al actualizar una categoría inexistente")
    void testUpdateCategoryNotFound() {
        // Given
        Category newValues = new Category(null, "Alimentos", "Productos alimenticios");
        when(categoryDao.findById(99L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<CategoryResponseRest> response = service.update(newValues, 99L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Categoria no encontrada", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(99L);
        verify(categoryDao, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Retorna solicitud incorrecta cuando el DAO no actualiza la categoría")
    void testUpdateCategoryDaoReturnsNull() {
        // Given
        Category storedCategory = list.get(0);
        Category newValues = new Category(null, "Alimentos", "Productos alimenticios");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(storedCategory));
        when(categoryDao.save(storedCategory)).thenReturn(null);

        // When
        ResponseEntity<CategoryResponseRest> response = service.update(newValues, 1L);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Categoria no actualizada", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(1L);
        verify(categoryDao).save(storedCategory);
    }

    @Test
    @DisplayName("Retorna error interno cuando falla la actualización de la categoría")
    void testUpdateCategoryException() {
        // Given
        Category newValues = new Category(null, "Alimentos", "Productos alimenticios");
        when(categoryDao.findById(1L)).thenThrow(new RuntimeException("Error al actualizar"));

        // When
        ResponseEntity<CategoryResponseRest> response = service.update(newValues, 1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al actualizar categoria", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).findById(1L);
        verify(categoryDao, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Elimina una categoría correctamente")
    void testDeleteByIdSuccess() {
        // Given
        doNothing().when(categoryDao).deleteById(1L);

        // When
        ResponseEntity<CategoryResponseRest> response = service.deleteById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("respuesta ok", response.getBody().getMetadata().get(0).get("type"));
        assertEquals("Registro eliminado", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).deleteById(1L);
    }

    @Test
    @DisplayName("Retorna error interno cuando falla la eliminación de la categoría")
    void testDeleteByIdException() {
        // Given
        doThrow(new RuntimeException("Error al eliminar")).when(categoryDao).deleteById(1L);

        // When
        ResponseEntity<CategoryResponseRest> response = service.deleteById(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"));
        assertEquals("Error al eliminar", response.getBody().getMetadata().get(0).get("date"));
        verify(categoryDao).deleteById(1L);
    }


    /**
     * Método que agrega datos a la lista de categorias
     */
    public void chargeList() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Abarrotes");
        category.setDescription("Distintos tipos de abarrotes");
        list.add(category);

        category = new Category();
        category.setId(2L);
        category.setName("Lacteos");
        category.setDescription("Distintos tipos de lacteos");
        list.add(category);
    }
}
