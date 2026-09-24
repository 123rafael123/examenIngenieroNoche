package com.company.inventory.util;

import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class ProductExcelExporterTest {

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
        Category cat = new Category(1L, "Electrónica", "Aparatos");

        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("TV 50 Pulgadas");
        p1.setPrice(3500);
        p1.setAccount(15);
        p1.setCategory(cat);

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Radio");
        p2.setPrice(150);
        p2.setAccount(50);
        p2.setCategory(cat);

        List<Product> products = Arrays.asList(p1, p2);

        ProductExcelExporter exporter = new ProductExcelExporter(products);
        exporter.export(response);

        verify(response, times(1)).getOutputStream();
        verify(outputStream, times(1)).close();
    }

    @Test
    void testExportWithEmptyList() throws IOException {
        List<Product> products = new ArrayList<>();
        ProductExcelExporter exporter = new ProductExcelExporter(products);
        exporter.export(response);

        verify(response, times(1)).getOutputStream();
        verify(outputStream, times(1)).close();
    }
}