# Enterprise REST API Deployment Architecture - Spring Boot, MySQL Docker and GitHub Codespaces (Lab 04B)

[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-26.0-blue?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![GitHub Codespaces](https://img.shields.io/badge/GitHub%20Codespaces-Cloud%20Ready-black?style=for-the-badge&logo=github)](https://github.com/features/codespaces)

> Repositorio academico y profesional desarrollado para la asignatura Soluciones Web y Aplicaciones Distribuidas (Semana 04B / Integracion Semana 08) en la Universidad Privada del Norte (UPN). Implementa la migracion y publicacion en la nube de una API RESTful desarrollada con Spring Boot 3, persistencia relacional con Spring Data JPA sobre MySQL 8 contenedorizado en Docker, exposicion publica segura del puerto HTTP 8080 mediante GitHub Codespaces y blindaje del puerto privado 3306.

---

## 1. Resumen Ejecutivo para Evaluadores Tecnicos

En entornos contemporaneos de ingenieria de software, la portabilidad y la reproducibilidad del entorno de ejecucion son factores criticos para acelerar el ciclo de desarrollo y facilitar la integracion continua (CI/CD). Este proyecto traslada una arquitectura de persistencia empresarial hacia un entorno de contenedores en la nube, demostrando:

1. **Aislamiento de Infraestructura de Datos**: Despliegue automatizado de un contenedor MySQL 8 mediante Docker Engine y Docker Compose, manteniendo la base de datos `lab4` aislada en la red local del host sin exposicion publica innecesaria.
2. **Capa RESTful de Alto Desacoplamiento**: Implementacion completa de operaciones CRUD sobre la entidad `Producto`, incorporando calculo reactivo de metricas de inventario (`estadoStock: OK | INSUFICIENTE`), filtros parametricos mediante Derived Queries y JPQL, y habilitacion de CORS para consumo desde clientes SPA Angular.
3. **Reproducibilidad Cloud-Native en GitHub Codespaces**: Configuracion declarativa mediante `.devcontainer/devcontainer.json`, administracion automatizada de puertos reenviados (Puerto 8080 en visibilidad Publica para la API REST, Puerto 3306 en visibilidad Privada para la base de datos) y orquestacion del ciclo de vida del contenedor.

---

## 2. Diagrama de Arquitectura de Despliegue

El siguiente diagrama detalla la arquitectura de red y el flujo de peticiones implementado entre el cliente externo, el proxy inverso de GitHub Codespaces, el contenedor Docker de base de datos y la capa de persistencia de Spring Boot:

![Diagrama de Arquitectura de Despliegue](mermaid%20diagramas/arquitectura.svg)

---

## 3. Especificacion de Endpoints RESTful

Ruta base del controlador: `/api/productos`

| Operacion | Metodo HTTP | Ruta | Descripcion | Codigo de Estado |
|---|---|---|---|---|
| Listar catalogo | `GET` | `/api/productos` | Obtiene la coleccion completa de productos registrados | `200 OK` |
| Consultar por ID | `GET` | `/api/productos/{id}` | Busca un producto por su clave primaria | `200 OK` / `404 Not Found` |
| Registrar producto | `POST` | `/api/productos` | Da de alta un nuevo producto y valida constraints | `201 Created` |
| Actualizar producto | `PUT` | `/api/productos/{id}` | Modifica los atributos de un producto existente | `200 OK` / `404 Not Found` |
| Eliminar producto | `DELETE` | `/api/productos/{id}` | Remueve un producto del repositorio | `204 No Content` / `404 Not Found` |
| Busqueda por coincidencia | `GET` | `/api/productos/buscar?nombre={valor}` | Filtra registros que contengan el termino ingresado | `200 OK` |
| Filtro por rango de precio | `GET` | `/api/productos/precio?minimo={min}&maximo={max}` | Consulta via JPQL productos dentro del rango de precios | `200 OK` |
| Filtro por stock disponible | `GET` | `/api/productos/stock?minimo={min}` | Consulta via JPQL productos con stock mayor o igual al umbral | `200 OK` |

### Regla de Negocio: Atributo Calculado `estadoStock`
Cada objeto `Producto` serializado incluye el campo dinamico `estadoStock`:
- Si `stock < 5`: devuelve `"INSUFICIENTE"`.
- Si `stock >= 5`: devuelve `"OK"`.

---

## 4. Estructura del Proyecto

```
SWAD-LAB04B-spring-boot-codespaces-docker-mysql-api/
├── .devcontainer/
│   └── devcontainer.json          # Configuracion de GitHub Codespaces y politicas de puertos
├── mermaid diagramas/
│   ├── arquitectura.mmd           # Definicion fuente del diagrama Mermaid
│   └── arquitectura.svg           # Diagrama compilado en formato SVG vectorial
├── src/
│   ├── main/
│   │   ├── java/com/upn/laboratorio4/
│   │   │   ├── config/DataInitializer.java    # Seeder de datos iniciales requeridos
│   │   │   ├── controller/ProductoController.java
│   │   │   ├── model/Producto.java            # Entidad JPA con logica de estadoStock
│   │   │   ├── repository/ProductoRepository.java
│   │   │   ├── service/ProductoService.java
│   │   │   └── Laboratorio4Application.java
│   │   └── resources/
│   │       └── application.properties         # Parametros de conexion a MySQL Docker
│   └── test/
│       ├── java/com/upn/laboratorio4/ProductoControllerTest.java # Suite de 12 pruebas MockMvc
│       └── resources/
│           └── application-test.properties    # Configuracion hermetica con H2 in-memory
├── docker-compose.yml              # Orquestacion de servicios MySQL 8 y API
├── Dockerfile                      # Empaquetado multi-stage para produccion
├── mvnw / mvnw.cmd                 # Maven Wrapper reproducible
└── pom.xml                         # Dependencias Spring Boot 3.4.3
```

---

## 5. Guia de Despliegue y Ejecucion

### Opcion A: Ejecucion Local con Docker Compose
1. Iniciar el servicio de base de datos MySQL 8:
   ```bash
   docker compose up -d mysql-lab4
   ```
2. Ejecutar la aplicacion Spring Boot mediante Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```

### Opcion B: Despliegue en GitHub Codespaces
1. Crear un Codespace desde el repositorio de GitHub (`Code` > `Codespaces` > `Create codespace on main`).
2. El contenedor Docker `mysql-lab4` se inicia automaticamente a traves del comando definido en `devcontainer.json`.
3. Iniciar el backend:
   ```bash
   chmod +x mvnw
   ./mvnw spring-boot:run
   ```
4. En la pestana **Ports** de VS Code:
   - Verificar que el puerto `3306` se encuentre en visibilidad **Private**.
   - Configurar el puerto `8080` en visibilidad **Public**.
   - Utilizar la URL publica asignada (por ejemplo `https://codespace-id-8080.app.github.dev/api/productos`) para la integracion con clientes externos.

---

## 6. Verificacion y Validacion de Calidad

El proyecto incluye una suite integral de 12 pruebas automatizadas (`ProductoControllerTest`) que validan la totalidad de contratos HTTP, codigos de respuesta (200, 201, 204, 404), reglas de negocio de stock y filtros de consulta.

Para ejecutar los tests automatizados:
```bash
mvn test
```

Resultado verificado:
```
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 7. Informacion Academica
- **Institucion**: Universidad Privada del Norte (UPN)
- **Curso**: Soluciones Web y Aplicaciones Distribuidas (SIST1402A)
- **Periodo**: 2026-II
- **Estudiante**: Orlando Dorival
