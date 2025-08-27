package dev.miguel.usecase.user.validation;

import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.UserRepository;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class EmailValidation implements UserValidation {

    private final UserRepository userRepository;

    @Override
    public Mono<Void> validate(User user) {
        if (user.getCorreoElectronico() == null || user.getCorreoElectronico().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El campo correo_electronico no puede ser nulo o vacío"));
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!user.getCorreoElectronico().matches(emailRegex)) {
            return Mono.error(new IllegalArgumentException("El campo correo_electronico debe tener un formato de email válido"));
        }
        return userRepository.findUserByEmail(user.getCorreoElectronico())
                .flatMap(existing -> Mono.error(new IllegalArgumentException("Correo ya existe")))
                .then();
    }
}