package com.upn.laboratorio4.repository;

import com.upn.laboratorio4.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :minimo AND :maximo")
    List<Producto> findByPrecioBetween(@Param("minimo") Double minimo, @Param("maximo") Double maximo);

    @Query("SELECT p FROM Producto p WHERE p.stock >= :minimo")
    List<Producto> findByStockMayorOIgual(@Param("minimo") Integer minimo);
}
