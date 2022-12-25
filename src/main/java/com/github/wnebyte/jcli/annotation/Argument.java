package com.github.wnebyte.jcli.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.github.wnebyte.jarguments.adapter.TypeAdapter;

/**
 * Annotate the Parameters of your {@link Command} annotated Java Methods with this annotation to set their
 * name, description, defaultValue and more.<br>
 * Here are some examples of usage:<br><br>
 * <pre>
 *{@literal @}Command
 * public void foo(
 *        {@literal @}Argument(value = "bar", defaultValue = "hello")
 *         String bar
 * ) {
 *     // code
 * }
 * </pre>
 * <p>Here the name of this optional argument is set to [ "bar" ].
 * <br>
 * The description field is omitted, and the defaultValue is set to "hello".
 *
 * <br>
 * </p>
 * <br>
 * <pre>
 *{@literal @}Command
 * public void foo(
 *        {@literal @}Argument(value = "bar, foo", required = true, choices = {"hello", "there"})
 *         String bar
 * ) {
 *     // code
 * }
 * </pre>
 * <p>Here the name of this required argument is set to [ "bar", "foo" ]. The description field is omitted.
 * The value is constrained to the set {"hello", "there"}.
 * </p>
 * <br>
 * <pre>
 *{@literal @}Command
 * public void foo(
 *        {@literal @}Argument(
 *                 typeConverter = BarTypeConverter.class
 *         )
 *         Bar bar
 * ) {
 *     // code
 * }
 * </pre>
 * <p>A user-defined {@link TypeAdapter} can be specified by assigning to the typeConverter field a class
 * which implements the interface (<b>Note</b> that said class is required to have a no-args Constructor).
 * <br>
 * Built in support exists for primitive types, wrapper classes, and arrays where the component type is either a
 * primitive type, or a wrapper class.
 * <br>
 * This field only needs to be specified if the type of the Java Parameter is not one of the
 * aforementioned, and limitations exists for types that have one or more parameterized types.
 * </p>
 * @see Command
 * @see Controller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Argument {

    /**
     * <p>Specify a set of names for this Argument, separated by a comma character.</p>
     * Defaults to the name of the Java Parameter.
     * @return the name of this Argument.
     */
    String value() default "";

    /**
     * Specify a description for this Argument.
     * @return the description of this Argument.
     */
    String description() default "";

    /**
     * Specify a default value for this Optional Argument.
     * @return the default value of this Argument.
     */
    String defaultValue() default "";

    /**
     * Specify that this Argument is required.
     */
    boolean required() default false;

    String[] choices() default "";

    String metavar() default "";
}