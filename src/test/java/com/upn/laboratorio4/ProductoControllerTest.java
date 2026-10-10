package com.upn.laboratorio4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upn.laboratorio4.model.Producto;
import com.upn.laboratorio4.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
        productoRepository.save(new Producto("Laptop Lenovo", 2500.00, 10));
        productoRepository.save(new Producto("Mouse Logitech", 80.00, 3));
        productoRepository.save(new Producto("Teclado Mecanico", 180.00, 15));
        productoRepository.save(new Producto("Monitor Samsung 24", 650.00, 5));
    }

    @Test
    @DisplayName("GET /api/productos retorna lista completa de productos con HTTP 200")
    void testListarProductos() throws Exception {
        mockMvc.perform(get("/api/productos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].nombre", is("Laptop Lenovo")))
                .andExpect(jsonPath("$[0].estadoStock", is("OK")))
                .andExpect(jsonPath("$[1].nombre", is("Mouse Logitech")))
                .andExpect(jsonPath("$[1].estadoStock", is("INSUFICIENTE")));
    }

    @Test
    @DisplayName("GET /api/productos/{id} retorna producto existente con HTTP 200")
    void testBuscarPorIdExistente() throws Exception {
        Producto primero = productoRepository.findAll().get(0);

        mockMvc.perform(get("/api/productos/{id}", primero.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(primero.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is(primero.getNombre())))
                .andExpect(jsonPath("$.estadoStock", is(primero.getEstadoStock())));
    }

    @Test
    @DisplayName("GET /api/productos/{id} retorna HTTP 404 para identificador inexistente")
    void testBuscarPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/productos/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/productos registra nuevo producto y retorna HTTP 201 Created")
    void testRegistrarProducto() throws Exception {
        Producto nuevo = new Producto("Impresora Epson", 890.00, 8);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Impresora Epson")))
                .andExpect(jsonPath("$.precio", is(890.0)))
                .andExpect(jsonPath("$.stock", is(8)))
                .andExpect(jsonPath("$.estadoStock", is("OK")));
    }

    @Test
    @DisplayName("POST /api/productos con stock menor a 5 calcula estadoStock INSUFICIENTE")
    void testRegistrarProductoStockBajo() throws Exception {
        Producto nuevo = new Producto("Cable HDMI", 25.00, 2);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stock", is(2)))
                .andExpect(jsonPath("$.estadoStock", is("INSUFICIENTE")));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} actualiza registro existente y retorna HTTP 200")
    void testActualizarProductoExistente() throws Exception {
        Producto primero = productoRepository.findAll().get(0);
        primero.setNombre("Laptop Lenovo ThinkPad");
        primero.setPrecio(2999.00);
        primero.setStock(12);

        mockMvc.perform(put("/api/productos/{id}", primero.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(primero)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(primero.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is("Laptop Lenovo ThinkPad")))
                .andExpect(jsonPath("$.precio", is(2999.0)))
                .andExpect(jsonPath("$.stock", is(12)));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} retorna HTTP 404 para identificador inexistente")
    void testActualizarProductoInexistente() throws Exception {
        Producto producto = new Producto("Audifonos Sony", 350.00, 10);

        mockMvc.perform(put("/api/productos/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} elimina registro existente y retorna HTTP 204")
    void testEliminarProductoExistente() throws Exception {
        Producto primero = productoRepository.findAll().get(0);

        mockMvc.perform(delete("/api/productos/{id}", primero.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/productos/{id}", primero.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} retorna HTTP 404 para identificador inexistente")
    void testEliminarProductoInexistente() throws Exception {
        mockMvc.perform(delete("/api/productos/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/productos/buscar filtra por coincidencia de nombre con HTTP 200")
    void testBuscarPorNombre() throws Exception {
        mockMvc.perform(get("/api/productos/buscar")
                .param("nombre", "Logitech")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Mouse Logitech")));
    }

    @Test
    @DisplayName("GET /api/productos/precio filtra por rango con HTTP 200")
    void testFiltrarPorPrecio() throws Exception {
        mockMvc.perform(get("/api/productos/precio")
                .param("minimo", "70.0")
                .param("maximo", "200.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/productos/stock filtra por stock minimo con HTTP 200")
    void testFiltrarPorStock() throws Exception {
        mockMvc.perform(get("/api/productos/stock")
                .param("minimo", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
