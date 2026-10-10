package com.upn.laboratorio4.config;

import com.upn.laboratorio4.model.Producto;
import com.upn.laboratorio4.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    public DataInitializer(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() == 0) {
            productoRepository.save(new Producto("Laptop Lenovo", 2500.00, 10));
            productoRepository.save(new Producto("Mouse Logitech", 80.00, 3));
            productoRepository.save(new Producto("Teclado Mecánico", 180.00, 15));
            productoRepository.save(new Producto("Monitor Samsung 24", 650.00, 5));
        }
    }
}
