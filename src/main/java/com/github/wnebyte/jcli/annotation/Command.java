package com.github.wnebyte.jcli.annotation;

import com.github.wnebyte.jcli.CLI;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate any Java Method with this annotation, and it can be initialized and invoked via an
 * instance of {@link CLI} at runtime.<br>
 * A Command consists of an optional prefix, one or more distinct names, a description, and an enumeration of
 * Arguments.<br>
 * Here are some examples of usage:<br><br>
 * <pre>
 *{@literal @}Command
 * public void foo() {
 *     // code
 * }
 * </pre>
 * <p>Here the name field is implicitly set to the name of the method, [ "foo" ].
 * The description is omitted, as is the enumeration of Arguments.</p>
 * <br>
 * <pre>
 *{@literal @}Command(
 *         name = "foo, -foo",
 *         description = "bar"
 * )
 * public void foo() {
 *     // code
 * }
 * </pre>
 * <p>Here the name field is explicitly set to [ "foo", "-foo" ].
 * The description field is set to "bar", and the enumeration of Arguments is omitted.</p>
 * @see Argument
 * @see Controller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    /**
     * <p>Specify a name for this Command.<br>
     * By default the name of the Command will be the same as the name of the Java Method.</p>
     * @return the name of this Command.
     */
    String value() default "";

    /**
     * Specify a description for this Command.
     * @return the description of this Command.
     */
    String description() default "";
}
