package celestial.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Indicates that this method is to be used by a serializer
 * implementation and is referenced no-where directly
 * by the surrounding source code.
 * 
 * This annotation is intended to be a simple note to
 * programmers that this method is only to be called by
 * a native java serialization implementation.
 * 
 * @author Max Derbenwick
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface SerializerImpl {
}
