package com.company.inventory.util;

import com.company.inventory.model.Category;
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

class CategoryExcelExporterTest {

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        // Usamos la clase de Spring que captura la respuesta real
        response = new MockHttpServletResponse();
    }

    @Test
    void testExportWithData() throws IOException {
        List<Category> categories = Arrays.asList(
                new Category(1L, "Lácteos", "Productos derivados de la leche")
        );

        CategoryExcelExporter exporter = new CategoryExcelExporter(categories);
        exporter.export(response);

        // Leemos los bytes capturados y los convertimos en un Excel real
        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheet("Resultado");

        assertNotNull(sheet, "La hoja Resultado debe existir");

        // Comprobamos los encabezados (Como pidió Jira y Copilot)
        XSSFRow headerRow = sheet.getRow(0);
        assertEquals("ID", headerRow.getCell(0).getStringCellValue());
        assertEquals("Nombre", headerRow.getCell(1).getStringCellValue());
        assertEquals("Descripción", headerRow.getCell(2).getStringCellValue());

        // Comprobamos los datos exportados
        XSSFRow dataRow = sheet.getRow(1);
        assertEquals("1", dataRow.getCell(0).getStringCellValue());
        assertEquals("Lácteos", dataRow.getCell(1).getStringCellValue());
        assertEquals("Productos derivados de la leche", dataRow.getCell(2).getStringCellValue());

        workbook.close();
    }

    @Test
    void testExportWithEmptyList() throws IOException {
        List<Category> categories = new ArrayList<>();
        CategoryExcelExporter exporter = new CategoryExcelExporter(categories);
        exporter.export(response);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheet("Resultado");

        // Comprobamos que solo exista la fila de encabezados (Fila 0)
        assertEquals(0, sheet.getLastRowNum());
        assertNotNull(sheet.getRow(0));

        workbook.close();
    }
}