package dev.miguel.usecase.solicitud.validations;

import dev.miguel.model.utils.exception.ExceptionMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ExceptionMessagesTest {


    @Test
    void constructorEsPrivadoYEvitaInstancias() throws Exception {
        Constructor<ExceptionMessages> ctor = ExceptionMessages.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(ctor.getModifiers()));
        ctor.setAccessible(true);

        InvocationTargetException ite =
                assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(AssertionError.class, ite.getCause());
        assertEquals("No instances", ite.getCause().getMessage());
    }
}
