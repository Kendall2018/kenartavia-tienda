package com.tiendaTech.tienda.service;

import com.tiendaTech.tienda.domain.Constante;
import com.tiendaTech.tienda.repository.ConstanteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConstanteService {

    private final ConstanteRepository constanteRepository;

    public ConstanteService(ConstanteRepository constanteRepository) {
        this.constanteRepository = constanteRepository;
    }

    @Transactional(readOnly = true)
    public List<Constante> getConstantes() {
        return constanteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Constante> getConstante(Integer idConstante) {
        return constanteRepository.findById(idConstante);
    }

    @Transactional(readOnly = true)
    public Optional<Constante> getConstantePorAtributo(String atributo) {
        if (atributo == null || atributo.isBlank()) {
            return Optional.empty();
        }

        return constanteRepository.findByAtributo(atributo.trim());
    }

    @Transactional
    public void save(Constante constante) {
        if (constante == null) {
            throw new IllegalArgumentException(
                    "La constante no puede ser nula."
            );
        }

        if (constante.getAtributo() == null
                || constante.getAtributo().isBlank()) {
            throw new IllegalArgumentException(
                    "El atributo es obligatorio."
            );
        }

        if (constante.getValor() == null
                || constante.getValor().isBlank()) {
            throw new IllegalArgumentException(
                    "El valor es obligatorio."
            );
        }

        constante.setAtributo(constante.getAtributo().trim());
        constante.setValor(constante.getValor().trim());

        boolean atributoDuplicado;

        if (constante.getIdConstante() == null) {
            atributoDuplicado
                    = constanteRepository.existsByAtributo(
                            constante.getAtributo()
                    );
        } else {
            atributoDuplicado
                    = constanteRepository.existsByAtributoAndIdConstanteNot(
                            constante.getAtributo(),
                            constante.getIdConstante()
                    );

            Constante constanteExistente
                    = constanteRepository
                            .findById(constante.getIdConstante())
                            .orElseThrow(() -> new IllegalArgumentException(
                            "La constante no existe."
                    ));

            constante.setFechaCreacion(
                    constanteExistente.getFechaCreacion()
            );
        }

        if (atributoDuplicado) {
            throw new DataIntegrityViolationException(
                    "Ya existe una constante con el atributo "
                    + constante.getAtributo()
            );
        }

        constanteRepository.save(constante);
    }

    @Transactional
    public void delete(Integer idConstante) {
        if (idConstante == null
                || !constanteRepository.existsById(idConstante)) {
            throw new IllegalArgumentException(
                    "La constante indicada no existe."
            );
        }

        try {
            constanteRepository.deleteById(idConstante);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "No se puede eliminar la constante porque tiene datos asociados.",
                    e
            );
        }
    }
}