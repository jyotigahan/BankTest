package com.bank.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
  * @author Jyoti Gahan
 * Used mostly in the {@link com.bank.exceptions.ObjectModificationException} to
 * specify the particular type of the exception
 */
@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public enum ExceptionType {
	
    OBJECT_IS_MALFORMED("The entity passed has been malformed"),
    OBJECT_IS_NOT_FOUND("The entity with provided ID has not been found"),
    COULD_NOT_OBTAIN_ID("The system could not generate ID for this entity. Creation is failed."),
    UNEXPECTED_EXCEPTION("Unexpected exception");

    private String message;  

}
