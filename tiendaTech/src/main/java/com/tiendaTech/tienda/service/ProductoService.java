/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tiendaTech.tienda.service;

import com.tiendaTech.tienda.domain.Producto;
import com.tiendaTech.tienda.repository.ProductoRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final FirebaseStorageService firebaseStorageService;

    public ProductoService(ProductoRepository productoRepository, FirebaseStorageService firebaseStorageService) {
        this.productoRepository = productoRepository;
        this.firebaseStorageService = firebaseStorageService;
    }

    @Transactional(readOnly = true)
    public List<Producto> getProductos(boolean activo) {
        if (activo) {
            return productoRepository.findByActivoTrue();
        }
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> getProducto(Integer idProducto) {
        return productoRepository.findById(idProducto);
    }

    @Transactional
    public void save(Producto producto, MultipartFile imagenFile) {
        producto = productoRepository.save(producto);

        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String rutaImagen = firebaseStorageService.uploadImage(
                        imagenFile,
                        "producto",
                        producto.getIdProducto()
                );

                producto.setRutaImagen(rutaImagen);
                productoRepository.save(producto);

            } catch (IOException e) {
                // Si falla la imagen, igual queda guardado el producto
            }
        }
    }

    @Transactional
    public void delete(Integer idProducto) {
        Optional<Producto> producto = productoRepository.findById(idProducto);

        if (producto.isEmpty()) {
            throw new IllegalArgumentException("Producto con ID " + idProducto + " no existe.");
        }

        productoRepository.deleteById(idProducto);
    }
    
    @Transactional(readOnly = true)
    public List<Producto> consultaDerivada(double precioInf, double precioSup) {
        return productoRepository.findByPrecioBetweenOrderByPrecioAsc(precioInf, precioSup);
    }

    @Transactional(readOnly = true)
    public List<Producto> consultaJPQL(double precioInf, double precioSup) {
        return productoRepository.consultaJPQL(precioInf, precioSup);
    }

    @Transactional(readOnly = true)
    public List<Producto> consultaSQL(double precioInf, double precioSup) {
        return productoRepository.consultaSQL(precioInf, precioSup);
    }

    @Transactional(readOnly = true)
    public List<Producto> consultaAmpliada(double precioInf, double precioSup, int existencias) {
        return productoRepository.consultaAmpliada(precioInf, precioSup, existencias);
    }
}
