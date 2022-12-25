package com.github.wnebyte.jcli;

import java.util.*;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.lang.reflect.Method;
import com.github.wnebyte.jarguments.ContextView;
import com.github.wnebyte.jarguments.parser.AbstractParser;
import com.github.wnebyte.jarguments.parser.Parser;
import com.github.wnebyte.jarguments.Formatter;
import com.github.wnebyte.jarguments.exception.*;
import com.github.wnebyte.jarguments.util.TokenSequence;
import com.github.wnebyte.jarguments.util.Strings;
import com.github.wnebyte.jcli.annotation.Command;
import com.github.wnebyte.jcli.exception.UnknownCommandException;
import com.github.wnebyte.jcli.processor.*;
import com.github.wnebyte.jcli.util.CommandIdentifier;
import com.github.wnebyte.jcli.util.Objects;

@SuppressWarnings("resource")
public class CLI {

    /*
    ###########################
    #     UTILITY METHODS     #
    ###########################
    */

    public static void launch() {
        launch(null);
    }

    public static void launch(Configuration conf) {
        CLI cli = new CLI(conf);
        cli.run();
    }

    static ContextView contextViewOf(AbstractCommand cmd) {
        if (cmd.hasPrefix()) {
            return ContextView.of(
                    cmd.getPrefix().concat(Strings.WHITESPACE).concat(String.join(", ", cmd.getNames())),
                    cmd.getDescription(),
                    cmd.getArguments());
        } else {
            return ContextView.of(
                    String.join(", ", cmd.getNames()),
                    cmd.getDescription(),
                    cmd.getArguments());
        }
    }

    static TokenSequence slice(TokenSequence tokens, AbstractCommand cmd) {
        return tokens.subTokens(cmd.hasPrefix() ? 2 : 1, tokens.size());
    }

    static boolean isHelp(TokenSequence tokens) {
        return (tokens != null) && (tokens.size() == 1) &&
                (tokens.get(0).equals("--help") || tokens.get(0).equals("-h"));
    }

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    protected final Configuration conf;

    protected final List<AbstractCommand> commands;

    protected final Set<String> prefixes;

    protected final Map<String, AbstractCommand> index;

    protected final AbstractParser parser;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    /**
     * Constructs a new instance using a default {@link Configuration}.
     */
    public CLI() {
        this(null);
    }

    /**
     * Constructs a new instance using the specified <code>Configuration</code>.
     * @param conf to be used.
     */
    public CLI(Configuration conf) {
        this.conf = Objects.requireNonNullElseGet(conf, Configuration::new);
        this.prefixes = new HashSet<>();
        this.index = new HashMap<>();
        this.parser = new Parser();
        this.commands = build(new MethodScannerImpl(), new InstanceTrackerImpl(this.conf.getDependencyContainer()));
    }

    /*
    ###########################
    #         METHODS         #
    ###########################
    */

    private List<AbstractCommand> build(MethodScanner scanner, InstanceTracker tracker) {
        scan(scanner, tracker);
        List<AbstractCommand> commands = map(scanner, tracker);
        index(commands);
        sort(commands);
        return commands;
    }

    protected void scan(MethodScanner scanner, InstanceTracker tracker) {
        Set<Object> objects = conf.getScanObjects();
        Set<Class<?>> classes = conf.getScanClasses();
        Set<String> packages = conf.getScanPackages();
        Set<CommandIdentifier> commandIdentifiers = conf.getScanCommandIdentifiers();
        Set<Method> methods = conf.getScanMethods();

        if (objects != null) {
            scanner.scanObjects(objects);
            tracker.addAll(objects);
        }
        if (classes != null) {
            scanner.scanClasses(classes);
        }
        if (packages != null) {
            scanner.scanUrls(packages);
        }
        if (commandIdentifiers != null) {
            scanner.scanMethods(commandIdentifiers.stream().map(CommandIdentifier::getMethod).collect(Collectors.toSet()));
        }
        if (methods != null) {
            scanner.scanMethods(methods);
        }
        if (!conf.isMapHelpCommand()) {
            scanner.removeScannedElementIf(m -> m.getDeclaringClass() == CLI.class);
        }
        else {
            scanner.scanClass(CLI.class);
            tracker.add(this);
        }
        if (conf.getExcludeClasses() != null) {
            scanner.removeScannedElementIf(m -> conf.getExcludeClasses().contains(m.getDeclaringClass()));
        }
    }

    protected List<AbstractCommand> map(MethodScanner scanner, InstanceTracker tracker) {
        return scanner.getScannedElements().stream()
                .filter(new PreMappingFilter())
                .map(new MethodMapperBuilder()
                        .setInstanceTracker(tracker)
                        .setTypeAdapterRegistry(conf.getTypeAdapterRegistry())
                        .build()
                )
                .filter(Objects::nonNull)
                .filter(new PostMappingFilter())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected void index(List<AbstractCommand> commands) {
        for (AbstractCommand cmd : commands) {
            for (String name : cmd.getNames()) {
                String key;

                if (cmd.hasPrefix()) {
                    key = cmd.getPrefix().concat(name);
                    prefixes.add(cmd.getPrefix());
                } else {
                    key = name;
                }

                if (index.containsKey(key)) {
                    throw new IllegalStateException(
                            String.format(
                                    "Command with name: '%s' has already been indexed.", key
                            )
                    );
                } else {
                    index.put(key, cmd);
                }
            }
        }
    }

    protected void sort(List<AbstractCommand> commands) {
        commands.sort(AbstractCommand::compareTo);
    }

    public void accept(String[] input) {
        String s = String.join(Strings.WHITESPACE, (input == null) ? new String[0] : input);
        accept(s);
    }

    public void accept(String input) {
        TokenSequence tokens = TokenSequence.tokenize(input);

        try {
            AbstractCommand cmd = lookup(input, tokens);
            tokens = slice(tokens, cmd);
            if (isHelp(tokens)) {
                Formatter<ContextView> formatter
                        = conf.getHelpFormatter();
                conf.out().println(formatter.apply(contextViewOf(cmd)));
            } else {
                Object[] args = parser.parse(input, tokens, cmd.getArguments());
                cmd.execute(args);
            }
        }
        catch (UnknownCommandException e) {
            Formatter<UnknownCommandException> formatter
                    = conf.getUnknownCommandFormatter();
            conf.err().println(formatter.apply(e));
        }
        catch (TypeConversionException e) {
            Formatter<TypeConversionException> formatter
                    = conf.getFormatter(TypeConversionException.class);
            conf.err().println(formatter.apply(e));
        }
        catch (NoSuchArgumentException e) {
            Formatter<NoSuchArgumentException> formatter
                    = conf.getFormatter(NoSuchArgumentException.class);
            conf.err().println(formatter.apply(e));
        }
        catch (MalformedArgumentException e) {
            Formatter<MalformedArgumentException> formatter
                    = conf.getFormatter(MalformedArgumentException.class);
            conf.err().println(formatter.apply(e));
        }
        catch (MissingArgumentException e) {
            Formatter<MissingArgumentException> formatter
                    = conf.getFormatter(MissingArgumentException.class);
            conf.err().println(formatter.apply(e));
        }
        catch (ConstraintException e) {
            Formatter<ConstraintException> formatter
                    = conf.getFormatter(ConstraintException.class);
            conf.err().println(formatter.apply(e));
        }
        catch (ParseException ignored) {}
    }

    public Consumer<String> toConsumer() {
        return CLI.this::accept;
    }

    public void run() {
        Scanner scanner = new Scanner(conf.in());

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            accept(input);
        }
    }

    protected AbstractCommand lookup(String input, TokenSequence tokens) throws UnknownCommandException {
        AbstractCommand cmd = getCommand(tokens);
        if (cmd == null) {
            throw new UnknownCommandException(String.format(
                    "'%s' is not recognized as an internal command.", input), input);
        }
        return cmd;
    }

    protected String getKey(TokenSequence tokens) {
        String key = tokens.get(0);

        if (tokens.size() >= 2 && prefixes.contains(key)) {
            key = tokens.get(0).concat(tokens.get(1));
        }

        return key;
    }

    protected AbstractCommand getCommand(TokenSequence tokens) {
        if (tokens == null || tokens.size() == 0) {
            return null;
        }
        String key = getKey(tokens);
        AbstractCommand cmd = index.get(key);
        return cmd;
    }

    @Command("--help, -h")
    protected final void help() {
        for (AbstractCommand cmd : commands) {
            conf.out().println(conf.getHelpFormatter().apply(contextViewOf(cmd)));
        }
    }
}