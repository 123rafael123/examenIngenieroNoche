package com.company.inventory.util;

import com.company.inventory.model.Category;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class CategoryExcelExporterTest {

    private HttpServletResponse response;
    private ServletOutputStream outputStream;

    @BeforeEach
    void setUp() throws IOException {
        response = mock(HttpServletResponse.class);
        outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    void testExportWithData() throws IOException {
        List<Category> categories = Arrays.asList(
                new Category(1L, "Lácteos", "Productos derivados de la leche"),
                new Category(2L, "Carnes", "Carnes rojas y blancas")
        );

        CategoryExcelExporter exporter = new CategoryExcelExporter(categories);
        exporter.export(response);

        // Verificamos que se solicitó el stream de salida y se cerró correctamente
        verify(response, times(1)).getOutputStream();
        verify(outputStream, times(1)).close();
    }

    @Test
    void testExportWithEmptyList() throws IOException {
        // Requisito Jira SCRUM-10: Listas vacías
        List<Category> categories = new ArrayList<>();
        CategoryExcelExporter exporter = new CategoryExcelExporter(categories);
        exporter.export(response);

        verify(response, times(1)).getOutputStream();
        verify(outputStream, times(1)).close();
    }
}