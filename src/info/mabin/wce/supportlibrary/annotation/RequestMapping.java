package info.mabin.wce.supportlibrary.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * for Define URI Mapping
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
	/**
	 * for Mapping URI.
	 * <p>
	 * You can use '*' and '**'.
	 * </p>
	 * <p>
	 * '*' must be located between '/'.
	 * </p>
	 * <p>
	 * '**' must be located behind last '/' for forward all character of behind last '/'
	 * </p>
	 */
	String value();
	/**
	 * for Define Method
	 */
	RequestMethod[] method() default {};
}
