package com.github.wnebyte.jshell;

import com.github.wnebyte.jshell.annotation.Type;
import com.github.wnebyte.jshell.exception.config.ConfigException;
import com.github.wnebyte.jshell.exception.config.IllegalAnnotationException;
import com.github.wnebyte.jshell.exception.runtime.ParseException;
import com.github.wnebyte.jshell.exception.runtime.UnknownCommandException;
import com.github.wnebyte.jshell.util.CollectionUtil;
import com.github.wnebyte.jshell.util.InstanceTracker;
import com.github.wnebyte.jshell.util.ObjectUtil;
import com.github.wnebyte.jshell.util.Scanner;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static com.github.wnebyte.jshell.util.ReflectionUtil.isNested;
import static com.github.wnebyte.jshell.util.ReflectionUtil.isStatic;

/**
 * This class is the API class of the jshell library.
 */
public class Shell {

    // lateinit
    private Map<Pattern, Command> commands = new HashMap<>();

    private final Configuration config;

    /**
     * Constructs a new <code>Shell</code> instance using the settings of the specified
     * <code>Configuration</code>.
     * @param config the <code>Configuration</code> to be used when building this <code>Shell</code>.
     */
    public Shell(Configuration config) {
        this.config = ObjectUtil.requireNonNullElseGet(config, Configuration::new);
        build();
    }

    /**
     * Unsupported constructor.
     */
    private Shell() {
        throw new UnsupportedOperationException(
                "This constructor is not supported."
        );
    }

    /**
     * Builds this <code>Shell</code> using the settings defined in the constructors
     * <code>Configuration</code>.
     */
    private void build() {
        AnnotationProcessor annotationProcessor = new AnnotationProcessor();
        Scanner scanner = new Scanner();
        InstanceTracker tracker = (config.getObjects() != null) ?
                new InstanceTracker(config.getObjects()) : new InstanceTracker();

        if (config.getConsole() != null) {
            tracker.setInjectable(config.getConsole());
        }
        if (config.getObjects() != null) {
            scanner.scanObjects(config.getObjects());
        }
        if (config.getClasses() != null) {
            scanner.scanClasses(config.getClasses());
        }
        if (!(config.isNullifyScanPackages()) && (config.getPackages() != null)) {
            scanner.scanURLS(config.getPackages());
        }
        if (config.getBundle() != null) {
            scanner.scanBundle(config.getBundle());
            tracker.trackObject(config.getBundle().getOwner());
        }
        if (config.isNullifyHelpCommands()) {
            scanner.removeIf(method -> method.getDeclaringClass() == this.getClass());
        } else {
            tracker.trackObject(this);
            scanner.scanClass(this.getClass());
        }

        Set<Command> commands = scanner.getScanned().stream().map(method -> {
            try {
                Class<?> cls = method.getDeclaringClass();
                if ((isNested(cls)) && !(isStatic(cls))) {
                    throw new ConfigException(
                            "\n\tA Command may not be declared within a nested class that is non static."
                    );
                }
                Object object = isStatic(method) ?
                        null :
                        tracker.getObject(cls);
                return new Command(object, method);
            }
            catch (ConfigException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());

        try {
            this.commands = annotationProcessor.process(commands);
        }
        catch (IllegalAnnotationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Accepts input from the user.
     * @param input the input to match against a known <code>Command</code>.
     */
    public void accept(final String input) {
        try {
            match(input).execute(input);
        }
        catch (UnknownCommandException e) {
            handleUnknownCommandException(input);
        }
        catch (ParseException e) {
            handleParseException(e);
        }
    }

    /**
     * Attempts to read from the specified {@link IConsole} until it returns <code>null</code> or "exit",
     * and calls {@link Shell#accept(String)}, in a continuous loop.
     * @throws IllegalStateException if no IConsole implementation has been specified.
     */
    public void run() {
        IConsole console = config.getConsole();
        boolean cont = true;

        if (console == null) {
            throw new IllegalStateException(
                    "No console has been been specified."
            );
        }
        while (cont) {
            String in = console.read();
            if (cont=((in != null) && !(in.equals("exit")))) {
                accept(in);
            }
        }
    }

    public final Consumer<String> toConsumer() {
        return this::accept;
    }

    /**
     * Handles an {@link UnknownCommandException} in accordance with the specified Configuration.
     * @param input the input received from the user.
     */
    protected void handleUnknownCommandException(final String input) {
        IConsole console = config.getConsole();
        Consumer<String> handler = config.getUnknownCommandHandler();
        Function<String, String> formatter = config.getUnknownCommandOutputFormatter();

        if (handler != null) {
            handler.accept(input);
        }
        else if (console != null) {

            if (config.isSuggestCommand()) {
                Command command = getBestGuess(input);

                if (command != null) {
                    console.println(config.getHelpOutputFormatter().apply(command));
                } else {
                    console.printerr(formatter.apply(input));
                }
            }
            else {
                console.printerr(formatter.apply(input));
            }
        }
    }

    /**
     * Handles a {@link ParseException} in accordance with the specified Configuration.
     * @param e the thrown ParseException.
     */
    protected void handleParseException(final ParseException e) {
        IConsole console = config.getConsole();
        Consumer<ParseException> handler = config.getParseExceptionHandler();
        Function<ParseException, String> formatter = config.getParseExceptionOutputFormatter();

        if (handler != null) {
            handler.accept(e);
        }
        else if (console != null) {
            console.printerr(formatter.apply(e));
        }
    }

    protected final Command match(final String input) throws UnknownCommandException {
        for (Map.Entry<Pattern, Command> kv : commands.entrySet()) {
            if (kv.getKey().matcher(input).matches()) {
                return kv.getValue();
            }
        }
        throw new UnknownCommandException();
    }

    protected Command getBestGuess(final String input) {
        float max = 0f;
        Command cmd = null;

        for (Command command : getCommands()) {
            float tmp = command.getLikeness(input);
            if (max < tmp) {
                max = tmp;
                cmd = command;
            }
        }

        return cmd;
    }

    protected final Collection<Command> getCommands() {
        return commands.values();
    }

    protected final Configuration getConfigurationBuilder() {
        return config;
    }

    public void printKeys() {
        commands.keySet().forEach(System.out::println);
    }

    @com.github.wnebyte.jshell.annotation.Command(
            name = "--help", description = "lists all commands"
    )
    private void help(
            @com.github.wnebyte.jshell.annotation.Argument(
                    name = "-name", type = Type.OPTIONAL, description = "name of command"
            )
            String name,
            @com.github.wnebyte.jshell.annotation.Argument
                    (name = "-prefix", type = Type.OPTIONAL, description = "prefix of command"
                    )
            String prefix,
            @com.github.wnebyte.jshell.annotation.Argument(
                    name = "-args", type = Type.OPTIONAL, description = "args of command"
            )
            String[] args
    ) {
        IConsole console = config.getConsole();
        Function<Command, String> formatter = config.getHelpOutputFormatter();

        if (console != null) {
            Collection<Command> commands = this.commands.values();

            if (name != null) {
                commands = commands.stream()
                        .filter(command -> command.getName().equals(name)).collect(Collectors.toList());
            }
            if (prefix != null) {
                commands = commands.stream()
                        .filter(command -> command.getPrefix().equals(prefix)).collect(Collectors.toList());
            }
            if ((args != null) && (args.length != 0)) {
                List<String> argsAsList = Arrays.asList(args);
                commands = commands.stream()
                        .filter(command -> CollectionUtil.intersection(argsAsList, command.getArguments()
                                .stream().map(Argument::getName).collect(Collectors.toList()))
                                .size() == args.length).collect(Collectors.toList());
            }
            commands.forEach(command -> console.println(formatter.apply(command)));
        }
    }
}