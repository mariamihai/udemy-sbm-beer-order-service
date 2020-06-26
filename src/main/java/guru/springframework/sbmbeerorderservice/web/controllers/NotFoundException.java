package guru.springframework.sbmbeerorderservice.web.controllers;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
        super();
    }
}