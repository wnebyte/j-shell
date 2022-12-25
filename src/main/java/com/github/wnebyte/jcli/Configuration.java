package com.github.wnebyte.jcli;

import java.util.*;
import java.io.PrintStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import com.github.wnebyte.jarguments.BaseConfiguration;
import com.github.wnebyte.jarguments.ContextView;
import com.github.wnebyte.jarguments.Formatter;
import com.github.wnebyte.jarguments.adapter.AbstractTypeAdapterRegistry;
import com.github.wnebyte.jarguments.adapter.TypeAdapter;
import com.github.wnebyte.jarguments.adapter.TypeAdapterRegistry;
import com.github.wnebyte.jarguments.exception.ParseException;
import com.github.wnebyte.jarguments.util.IConsole;
import com.github.wnebyte.jcli.annotation.Command;
import com.github.wnebyte.jcli.exception.UnknownCommandException;
import com.github.wnebyte.jcli.di.DependencyContainer;
import com.github.wnebyte.jcli.di.IDependencyContainer;
import com.github.wnebyte.jcli.util.CommandIdentifier;

/**
 * This class is used to specify configuration options for instances of {@link CLI}.
 */
public class Configuration extends BaseConfiguration {

    /*
    ###########################
    #      STATIC METHODS     #
    ###########################
    */

    /**
     * Constructs and returns a new <code>Configuration</code> instance.
     * @return a new instance.
     */
    public static Configuration newInstance() {
        return new Configuration();
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final Formatter<UnknownCommandException> DEFAULT_UNKNOWN_COMMAND_EXCEPTION_FORMATTER
            = Throwable::getMessage;

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private InputStream in
            = System.in;

    private AbstractTypeAdapterRegistry adapters
            = TypeAdapterRegistry.getInstance();

    private IDependencyContainer dependencyContainer
            = new DependencyContainer();

    private Set<Object> objects
            = null;

    private Set<Method> methods
            = null;

    private Set<Class<?>> classes
            = null;

    private Set<CommandIdentifier> commandIdentifiers
            = null;

    private Set<Class<?>> excludeClasses
            = null;

    private Set<String> packages = new HashSet<String>() {{
        add("");
    }};

    private boolean mapHelpCommand
            = true;

    private Formatter<UnknownCommandException> unknownCommandExceptionFormatter
            = DEFAULT_UNKNOWN_COMMAND_EXCEPTION_FORMATTER;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    /**
     * Constructs a new instance.
     */
    public Configuration() { }

    /*
    ###########################
    #         METHODS         #
    ###########################
    */

    public <T, R extends T> Configuration registerDependency(Class<T> base, R impl) {
        if (base != null && impl != null) {
            dependencyContainer.register(base, impl);
        }
        return this;
    }

    public <T> Configuration registerTypeAdapter(Class<T> cls, TypeAdapter<T> adapter) {
        if (cls != null && adapter != null) {
            adapters.register(cls, adapter);
        }
        return this;
    }

    /*
    ###########################
    #      SETTER METHODS     #
    ###########################
    */

    @Override
    public <T extends ParseException> Configuration setFormatter(Class<T> cls, Formatter<T> formatter) {
        super.setFormatter(cls, formatter);
        return this;
    }

    @Override
    public Configuration setHelpFormatter(Formatter<ContextView> formatter) {
        super.setHelpFormatter(formatter);
        return this;
    }

    @Override
    public Configuration setConsole(IConsole console) {
        super.setConsole(console);
        return this;
    }

    @Override
    public Configuration setOut(PrintStream out) {
        super.setOut(out);
        return this;
    }

    @Override
    public Configuration setErr(PrintStream err) {
        super.setErr(err);
        return this;
    }

    /**
     * Specifies that the respective <code>CLI</code> instance should use the specified <code>InputStream</code> when
     * reading input from the user.
     * @param in to be used.
     * @return this (for chaining).
     */
    public Configuration setIn(InputStream in) {
        if (in != null) {
            this.in = in;
        }
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should use the specified <code>Formatter</code> when catching a
     * thrown <code>UnknownCommandException</code>.
     * @param formatter to be used.
     * @return this (for chaining).
     */
    public Configuration setUnknownCommandFormatter(Formatter<UnknownCommandException> formatter) {
        if (formatter != null) {
            this.unknownCommandExceptionFormatter = formatter;
        }
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should use the specified <code>AbstractTypeConverterMap</code>.
     * @param adapters to be used.
     * @return this (for chaining).
     */
    public Configuration setTypeAdapterRegistry(AbstractTypeAdapterRegistry adapters) {
        if (adapters != null) {
            this.adapters = adapters;
        }
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should scan the specified <code>objects</code> for
     * {@link com.github.wnebyte.jcli.annotation.Command} annotated Java Methods.
     * @param objects to be scanned.
     * @return this (for chaining).
     */
    public Configuration setScanObjects(Object... objects) {
        if (objects != null) {
            this.objects = new HashSet<>(Arrays.asList(objects));
            this.disableScanPackages();
        }
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should scan the specified <code>methods</code> for
     * {@link com.github.wnebyte.jcli.annotation.Command} annotated Java Methods.
     * @param methods to be scanned.
     * @return this (for chaining).
     */
    public Configuration setScanMethods(Method... methods) {
        if (methods != null) {
            this.methods = new HashSet<>(Arrays.asList(methods));
            this.disableScanPackages();
        }
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should scan the specified <code>classes</code> for
     * {@link com.github.wnebyte.jcli.annotation.Command} annotated Java Methods.
     * @param classes to be scanned.
     * @return this (for chaining).
     */
    public Configuration setScanClasses(Class<?>... classes) {
        if (classes != null) {
            this.classes = new HashSet<>(Arrays.asList(classes));
            this.disableScanPackages();
        }
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should scan the specified <code>identifiers</code> for
     * {@link com.github.wnebyte.jcli.annotation.Command} annotated Java Methods.
     * @param commandIdentifiers to be scanned.
     * @return this (for chaining).
     */
    public Configuration setScanCommandIdentifiers(CommandIdentifier... commandIdentifiers) {
        if (commandIdentifiers != null) {
            this.commandIdentifiers = new HashSet<>(Arrays.asList(commandIdentifiers));
            this.disableScanPackages();
        }
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should scan the specified <code>packages</code> for
     * {@link com.github.wnebyte.jcli.annotation.Command} annotated Java Methods.
     * @param packages to be scanned.
     * @return this (for chaining).
     * @see org.reflections.util.ClasspathHelper#forPackage(String, ClassLoader...)
     */
    public Configuration setScanPackages(String... packages) {
        if (packages != null) {
            this.packages = new HashSet<>(Arrays.asList(packages));
        }
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should exclude the specified <code>classes</code>
     * from being scanned for {@link com.github.wnebyte.jcli.annotation.Command} annotated Java Methods.
     * @param classes to be excluded from scanning.
     * @return this (for chaining).
     */
    public Configuration setExcludeClasses(Class<?>... classes) {
        if (classes != null) {
            this.excludeClasses = new HashSet<>(Arrays.asList(classes));
        }
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should <b>not</b> build its default help <code>Command</code>.
     * @return this (for chaining).
     */
    public Configuration disableMapHelpCommand() {
        this.mapHelpCommand = false;
        return this;
    }

    /**
     * Specifies that the <code>CLI</code> should <b>not</b> scan any packages for {@link Command} annotated Java Methods.
     * <b>Note</b> by default all packages are scanned.
     * @return this (for chaining).
     */
    public Configuration disableScanPackages() {
        this.packages = null;
        return this;
    }

    /*
    ###########################
    #      GETTER METHODS     #
    ###########################
    */

    public InputStream in() {
        return in;
    }

    /**
     * @return the <code>IDependencyContainer</code> associated with this Configuration.
     */
    public IDependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }

    /**
     * @return the <code>AbstractTypeConverterMap</code> associated with this Configuration.
     */
    public AbstractTypeAdapterRegistry getTypeAdapterRegistry() {
        return adapters;
    }

    /**
     * @return the <code>Set</code> of classes to be scanned for annotated Java Methods
     * associated with this Configuration.
     */
    public Set<Class<?>> getScanClasses() {
        return classes;
    }

    /**
     * @return the <code>Set</code> of objects to be scanned for annotated Java Methods
     * associated with this Configuration.
     */
    public Set<Object> getScanObjects() {
        return objects;
    }

    /**
     * @return the <code>Set</code> of methods to be scanned for annotated Java Methods
     * associated with this Configuration.
     */
    public Set<Method> getScanMethods() {
        return methods;
    }

    /**
     * @return the <code>Set</code> of packages to be scanned for annotated Java Methods
     * associated with this Configuration.
     */
    public Set<String> getScanPackages() {
        return packages;
    }

    /**
     * @return the <code>Set</code> of commandIdentifiers to be scanned for annotated Java Methods
     * associated with this Configuration.
     */
    public Set<CommandIdentifier> getScanCommandIdentifiers() {
        return commandIdentifiers;
    }

    /**
     * @return the <code>Set</code> of classes that are to be excluded from being scanned for annotated
     * Java Methods associated with this Configuration.
     */
    public Set<Class<?>> getExcludeClasses() {
        return excludeClasses;
    }

    /**
     * Returns whether the <code>CLI</code> should build its default help Command.
     * @return <code>true</code> if the CLI should build its default help Command,
     * otherwise <code>false</code>.
     */
    public boolean isMapHelpCommand() {
        return mapHelpCommand;
    }

    /**
     * @return the UnknownCommandException Formatter associated with this instance.
     */
    public Formatter<UnknownCommandException> getUnknownCommandFormatter() {
        return unknownCommandExceptionFormatter;
    }
}