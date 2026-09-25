# Inventory SP3

Proyecto Spring Boot para el control de inventario. Las pruebas unitarias de la capa de servicios usan JUnit 5 y Mockito para simular los DAO, por lo que no necesitan una conexión a MySQL.

## Requisitos

- Java 17
- PowerShell, CMD o Git Bash
- Maven Wrapper incluido en el proyecto

## Ejecutar las pruebas

Desde la carpeta `inventory-sp3`, en PowerShell o CMD:

```powershell
.\mvnw.cmd test
```

En Git Bash:

```bash
./mvnw test
```

Para ejecutar solamente las pruebas de servicios en PowerShell o CMD:

```powershell
.\mvnw.cmd "-Dtest=CategoryServiceImplTest,ProductServiceImplTest" test
```

## Generar y consultar la cobertura

Desde `inventory-sp3`, en PowerShell o CMD:

```powershell
.\mvnw.cmd clean verify
```

En Git Bash:

```bash
./mvnw clean verify
```

Cuando la ejecución termine con `BUILD SUCCESS`, abrir el siguiente archivo en un navegador:

```text
target/site/jacoco/index.html
```

El reporte de JaCoCo muestra la cobertura real por paquete, clase, método y línea.
