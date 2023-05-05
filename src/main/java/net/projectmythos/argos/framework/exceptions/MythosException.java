package net.projectmythos.argos.framework.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MythosException extends RuntimeException {

    public MythosException(String message) {
        super(message);
    }

    public MythosException(String message, Throwable cause) {
        super(message, cause);
    }

}
