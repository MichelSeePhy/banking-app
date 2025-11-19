package springframework.springbankinapp;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import springframework.springbankinapp.common.ErrorDto;
import springframework.springbankinapp.customers.CustomerNotFoundException;
import springframework.springbankinapp.users.*;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDto> handleUserAlreadyExistsException() {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto("Email already exists"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundUser() {
        System.out.println("UserNotFoundException handler called");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto("User not found"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorDto("Access denied"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCustomerNotFound() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto("Customer not found"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleInvalidInput(HttpMessageNotReadableException ex) {
        String message = "Invalid input";

        if (ex.getCause() instanceof InvalidFormatException ife) {
            if (ife.getTargetType().isEnum()) {
                @SuppressWarnings("unchecked")
                Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) ife.getTargetType();

                String enumName = enumClass.getSimpleName();
                String allowedValues = Arrays.stream(enumClass.getEnumConstants())
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));

                message = String.format("Invalid %s. Allowed values: %s", enumName, allowedValues);
            }
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(message));
    }

}
