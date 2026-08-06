package com.tiendaTech.tienda.service;

import com.tiendaTech.tienda.domain.Rol;
import com.tiendaTech.tienda.domain.Usuario;
import com.tiendaTech.tienda.repository.RolRepository;
import com.tiendaTech.tienda.repository.UsuarioRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            FirebaseStorageService firebaseStorageService,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios(boolean activo) {
        if (activo) {
            return usuarioRepository.findByActivoTrue();
        }

        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameYPassword(
            String username,
            String password
    ) {
        return usuarioRepository.findByUsernameAndPassword(
                username,
                password
        );
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameOCorreo(
            String username,
            String correo
    ) {
        return usuarioRepository.findByUsernameOrCorreo(
                username,
                correo
        );
    }

    @Transactional(readOnly = true)
    public boolean existeUsuarioPorUsernameOCorreo(
            String username,
            String correo
    ) {
        return usuarioRepository.existsByUsernameOrCorreo(
                username,
                correo
        );
    }

    /*
     * Obtiene un usuario y carga sus roles mientras la transacción
     * todavía está activa. Esto evita errores de carga LAZY en la vista.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioConRolesPorUsername(
            String username
    ) {
        Optional<Usuario> usuarioOpt
                = usuarioRepository.findByUsername(username);

        usuarioOpt.ifPresent(usuario -> usuario.getRoles().size());

        return usuarioOpt;
    }

    @Transactional(readOnly = true)
    public List<Rol> getRoles() {
        return rolRepository.findAll();
    }

    @Transactional
    public void save(
            Usuario usuario,
            MultipartFile imagenFile,
            boolean encriptaClave
    ) {

        final Integer idUser = usuario.getIdUsuario();

        Optional<Usuario> usuarioDuplicado
                = usuarioRepository.findByUsernameOrCorreo(
                        null,
                        usuario.getCorreo()
                );

        if (usuarioDuplicado.isPresent()) {
            Usuario encontrado = usuarioDuplicado.get();

            if (idUser == null
                    || !encontrado.getIdUsuario().equals(idUser)) {

                throw new DataIntegrityViolationException(
                        "El correo ya está en uso por otro usuario."
                );
            }
        }

        boolean asignarRol = false;

        if (usuario.getIdUsuario() == null) {

            if (usuario.getPassword() == null
                    || usuario.getPassword().isBlank()) {

                throw new IllegalArgumentException(
                        "La contraseña es obligatoria para nuevos usuarios."
                );
            }

            usuario.setPassword(
                    encriptaClave
                            ? passwordEncoder.encode(usuario.getPassword())
                            : usuario.getPassword()
            );

            asignarRol = true;

        } else {

            Usuario usuarioExistente
                    = usuarioRepository
                            .findById(usuario.getIdUsuario())
                            .orElseThrow(() -> new IllegalArgumentException(
                            "Usuario a modificar no encontrado."
                    ));

            /*
             * Conserva los roles actuales cuando se modifica un usuario
             * desde el mantenimiento normal.
             */
            usuario.setRoles(usuarioExistente.getRoles());

            if (usuario.getPassword() == null
                    || usuario.getPassword().isBlank()) {

                usuario.setPassword(
                        usuarioExistente.getPassword()
                );

            } else if (encriptaClave) {

                usuario.setPassword(
                        passwordEncoder.encode(usuario.getPassword())
                );
            }
        }

        usuario = usuarioRepository.save(usuario);

        if (imagenFile != null && !imagenFile.isEmpty()) {

            try {
                String rutaImagen
                        = firebaseStorageService.uploadImage(
                                imagenFile,
                                "usuario",
                                usuario.getIdUsuario()
                        );

                usuario.setRutaImagen(rutaImagen);
                usuarioRepository.save(usuario);

            } catch (IOException e) {
                throw new IllegalStateException(
                        "No fue posible guardar la imagen del usuario.",
                        e
                );
            }
        }

        if (asignarRol) {
            asignarRolPorUsername(
                    usuario.getUsername(),
                    "USER"
            );
        }
    }

    @Transactional
    public void delete(Integer idUsuario) {

        if (!usuarioRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException(
                    "El usuario con ID "
                    + idUsuario
                    + " no existe."
            );
        }

        try {
            usuarioRepository.deleteById(idUsuario);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "No se puede eliminar el usuario. Tiene datos asociados.",
                    e
            );
        }
    }

    @Transactional
    public Usuario asignarRolPorUsername(
            String username,
            String rolStr
    ) {

        Usuario usuario = usuarioRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                "Usuario no encontrado: " + username
        ));

        Rol rol = rolRepository
                .findByRol(rolStr)
                .orElseThrow(() -> new IllegalArgumentException(
                "Rol no encontrado: " + rolStr
        ));

        if (usuario.getRoles().contains(rol)) {
            throw new IllegalStateException(
                    "El usuario ya tiene asignado el rol " + rolStr + "."
            );
        }

        usuario.getRoles().add(rol);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario revocarRolPorUsername(
            String username,
            Integer idRol
    ) {

        Usuario usuario = usuarioRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                "Usuario no encontrado: " + username
        ));

        Rol rol = rolRepository
                .findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException(
                "El rol indicado no existe."
        ));

        boolean eliminado = usuario
                .getRoles()
                .removeIf(rolUsuario
                        -> rolUsuario.getIdRol().equals(rol.getIdRol())
                );

        if (!eliminado) {
            throw new IllegalStateException(
                    "El usuario no tiene asignado ese rol."
            );
        }

        return usuarioRepository.save(usuario);
    }
}