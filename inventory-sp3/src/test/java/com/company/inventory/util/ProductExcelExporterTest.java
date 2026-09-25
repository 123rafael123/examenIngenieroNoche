package com.company.inventory.util;

import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductExcelExporterTest {

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        response = new MockHttpServletResponse();
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

        List<Product> products = Arrays.asList(p1);

        ProductExcelExporter exporter = new ProductExcelExporter(products);
        exporter.export(response);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheet("Resultado");

        assertNotNull(sheet);

        // Verificamos Encabezados
        XSSFRow headerRow = sheet.getRow(0);
        assertEquals("ID", headerRow.getCell(0).getStringCellValue());
        assertEquals("Nombre", headerRow.getCell(1).getStringCellValue());
        assertEquals("Precio", headerRow.getCell(2).getStringCellValue());
        assertEquals("Cantidad", headerRow.getCell(3).getStringCellValue());
        assertEquals("Categoría", headerRow.getCell(4).getStringCellValue());

        // Verificamos Datos
        XSSFRow dataRow = sheet.getRow(1);
        assertEquals("1", dataRow.getCell(0).getStringCellValue());
        assertEquals("TV 50 Pulgadas", dataRow.getCell(1).getStringCellValue());

        // CORRECCIÓN: Leer los enteros como valores NUMERIC (double en POI)
        assertEquals(3500.0, dataRow.getCell(2).getNumericCellValue());
        assertEquals(15.0, dataRow.getCell(3).getNumericCellValue());

        assertEquals("Electrónica", dataRow.getCell(4).getStringCellValue());

        workbook.close();
    }

    @Test
    void testExportWithEmptyList() throws IOException {
        List<Product> products = new ArrayList<>();
        ProductExcelExporter exporter = new ProductExcelExporter(products);
        exporter.export(response);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheet("Resultado");

        assertEquals(0, sheet.getLastRowNum());
        assertNotNull(sheet.getRow(0));

        workbook.close();
    }
}