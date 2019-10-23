package com.bank.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
  * @author Jyoti Gahan
 * The exception which is thrown once some validation or data consistency error detected. It has additional
 * field {@link ExceptionType} which specify additional nature of the exception
 */
@AllArgsConstructor
@Getter
public class ObjectModificationException extends Exception {
	
    private ExceptionType type;
    
    public ObjectModificationException(ExceptionType exceptionType, Throwable cause) {
        super(exceptionType.getMessage(), cause);
        type = exceptionType;
    }
    
    public ObjectModificationException(ExceptionType exceptionType, String message) {
        super(exceptionType.getMessage() + ": " + message);
        type = exceptionType;
    }

}
