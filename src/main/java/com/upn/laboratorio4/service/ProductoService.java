package com.upn.laboratorio4.service;

import com.upn.laboratorio4.model.Producto;
import com.upn.laboratorio4.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto registrar(Producto producto) {
        producto.setId(null);
        return productoRepository.save(producto);
    }

    public Optional<Producto> actualizar(Long id, Producto productoActualizado) {
        return productoRepository.findById(id).map(productoExistente -> {
            productoExistente.setNombre(productoActualizado.getNombre());
            productoExistente.setPrecio(productoActualizado.getPrecio());
            productoExistente.setStock(productoActualizado.getStock());
            return productoRepository.save(productoExistente);
        });
    }

    public boolean eliminar(Long id) {
        return productoRepository.findById(id).map(p -> {
            productoRepository.delete(p);
            return true;
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional(readOnly = true)
    public List<Producto> filtrarPorPrecio(Double minimo, Double maximo) {
        return productoRepository.findByPrecioBetween(minimo, maximo);
    }

    @Transactional(readOnly = true)
    public List<Producto> filtrarPorStock(Integer minimo) {
        return productoRepository.findByStockMayorOIgual(minimo);
    }
}
