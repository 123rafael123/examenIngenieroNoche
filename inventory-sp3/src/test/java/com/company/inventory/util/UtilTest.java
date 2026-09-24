package com.company.inventory.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void testCompressZLib() {
        String input = "Texto de prueba para verificar la compresión ZLib en el sistema de inventario.";
        byte[] compressed = Util.compressZLib(input.getBytes());
        assertNotNull(compressed);
        assertTrue(compressed.length > 0);
    }

    @Test
    void testCompressZLibEmptyData() {
        // Given
        byte[] data = new byte[0];
        // When
        byte[] compressed = Util.compressZLib(data);
        // Then
        assertNotNull(compressed);
        assertTrue(compressed.length > 0); // Deflater agrega cabeceras incluso si está vacío
    }

    @Test
    void testDecompressZLib() {
        // Secuencia completa de comprimir y recuperar (Requisito Jira SCRUM-9)
        String input = "Texto de prueba para verificar la compresión y descompresión secuencial.";
        byte[] compressed = Util.compressZLib(input.getBytes());

        byte[] decompressed = Util.decompressZLib(compressed);
        assertNotNull(decompressed);
        assertEquals(input, new String(decompressed));
    }

    @Test
    void testDecompressZLibInvalidData() {
        // Probamos descompresión con datos corruptos para cubrir los catch (IOException/DataFormatException)
        byte[] invalidData = new byte[] { 1, 2, 3, 4, 5 };
        byte[] decompressed = Util.decompressZLib(invalidData);

        assertNotNull(decompressed);
        assertEquals(0, decompressed.length); // Falla silenciosamente y devuelve arreglo vacío según el código
    }
}