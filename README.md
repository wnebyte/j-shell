# j-cli

Annotation based framework that can be used to quickly set up a 
Command-Line-Interface.

<strong>README is out of date</strong>

## Table of Contents

- [About](#about)
- [Sample](#sample)
  - [Command](#command)
  - [Argument](#argument)
  - [Group](#group)
  - [Controller](#controller)
  - [CLI](#cli)
  - [Configuration]()
- [Build](#build)
- [Documentation](#documentation)
- [Licence](#licence)

## About

This library enables a prospective user to quickly configure a command-line interface around an 
arbitrary Java application.<br> 

## Sample

### Command

Annotate any Java Method with the @Command annotation, and it can be mapped to a 
<code>Command</code> object at runtime.<br>
Each <code>Command</code> has an optional prefix, one or more distinct names, an optional description, 
and an optional enumeration of Arguments.<br>
Here are some examples of usage:<br>

#### Example 1:
  
    @Command
    public void foo() {
        // code
    }
  
<p>Here the name field is implicitly set to the name of the method, [ "foo" ].
The description is omitted, as is the enumeration of Arguments.</p>

#### Example 2:
    
    @Command(name = "foo, -foo", description = "bar")
    public void foo() {
        // code
    }
  
<p>Here the name field is explicitly set to [ "foo", "-foo" ].
The description field is set to "bar", and the enumeration of Arguments is omitted.</p>

### Argument

Annotate the Parameters of any @Command annotated Java Method with the @Argument annotation to explicitly set their
name, description, group, and typeConverter properties.<br>
Here are some examples of usage:<br>

#### Example 1:

    @Command
    public void foo(
            @Argument
            String bar
    ) {
        // code
    }

<p>Here the name field is implicitly set to [ "bar" ], or [ "arg0" ] depending on compiler options.
<br>
The description field is omitted, and the group field is implicitly set to <code>Group.REQUIRED</code>.
<br>
In this example the annotation could have been omitted, and the same configuration
would have been achieved.</p>

#### Example 2:

    @Command
    public void foo(
            @Argument(name = "-b, --b", description = "bar")
            boolean bar
    ) {
        // code
    }

<p>Here the name field is explicitly set to [ "-b", "--b" ], and
the description field is set to "bar". The Group field is forcefully set to <code>Group.FLAG</code> due to the
fact that the Parameter is of type <code>boolean</code>.<br>
<b>Note</b> that <code>Group.REQUIRED</code> is the default group for every other type.</p>

#### Example 3:

    @Command
    public void foo(
            @Argument(typeConverter = BarTypeConverter.class)
            Bar bar
    ) {
        // code
    }  

<p>A user-defined <code>TypeConverter</code> can be specified by assigning to the typeConverter field a class
which implements the <code>TypeConverter</code> interface and has a no-args constructor. 
<br>
<b>Note</b> that the type-safe and preferred way of specifying a user-defined <code>TypeConverter</code> is 
to register it with the <code>CLI</code> via its <code>Configuration</code> object. 
<br>
Built in support exists for primitive types, wrapper classes, and arrays where the component type is either a
primitive type, or a wrapper class.
<br>
This field only needs to be specified if the type of the Java Parameter is not one of the
aforementioned. 
<br>
Limitations exists for types that have one or more parameterized types.</p>

### Group

<code>**Group.REQUIRED**</code> has the following properties: <br>
<ol>
<li>Has to be included when a <code>Command</code> is specified for the <code>Command</code> to match and execute.</li>
<li>Has no fixed position.</li>
<li>Has a name.</li>
<li>Is initialized by including its name together with a value separated by a whitespace character.</li>
</ol>

<code>**Group.OPTIONAL**</code> has the following properties: <br>
<ol>
<li>Does not have to be included when a <code>Command</code> is specified for the <code>Command</code> to match and execute.</li>
<li>Has no fixed position.</li>
<li>Has a name.</li>
<li>Has a default value.</li>
<li>Is initialized by including its name together with a value separated by a whitespace character, or by omission.</li>
</ol>

<code>**Group.FLAG**</code> has the following properties: <br>
<ol>
<li>Does not have to be included when a <code>Command</code> is specified for the <code>Command</code> to match and execute.</li>
<li>Has no fixed position.</li>
<li>Has a name.</li>
<li>Has a default value.</li>
<li>Has a flag value.</li>
<li>Is initialized by including its name, or by omission.</li>
</ol>

<code>**Group.POSITIONAL**</code> has the following properties: <br>
<ol>
<li>Has to be included when a <code>Command</code> is specified for the <code>Command</code> to match and execute.</li>
<li>Has a fixed relative position.<br>
<li>Has no name.</li>
<li>Is initialized by including a value at the Argument's fixed relative position.</li>
</ol>

### Controller

Annotate your <code>Class</code> with the @Controller annotation if it declares any @Command
annotated Java Methods, to give each declared <code>Command</code> an optional prefix, and to control
the life-cycle of the Objects on which your (non-static) Commands are executed.
<br>
Here are some examples of usages:
<br>

#### Example 1:

    @Controller(name = ""foo)
    public class Foo {
        // code
    }

<p>You can give each declared <code>Command</code> a prefix by specifying a
name on the Controller level.<br>
Here the name field is set to "foo".<br>
<b>Note</b> that static Commands will also receive the assigned prefix.</p>
  
#### Example 2:
  
    @Controller(Scope.SINGLETON)
    public class Foo {
        // code
    }  

<p>You can also specify how instances of your <code>Class</code> are to be constructed and (re)used.<br>
By specifying a value of type <code>Scope.SINGLETON</code> on the Controller level,
you're specifying that one instance of your <code>Class</code> should be used for all subsequent <code>Command</code> invocations.<br>
If instead a value of type <code>Scope.TRANSIENT</code> is specified, a new instance will be constructed
prior to each invocation.<br>
<b>Note</b> that your class will only be instantiated if it declares at least one non-static @Command annotated
Java Method.</p>

### CLI

#### Example 1:

    public class Sample {
        public static void main(String[] args) {
            CLI cli = new CLI(new Configuration());
            cli.accept("--help");
        }
    }
    
<p>Input is passed to the <code>CLI</code> by calling <code>accept(input: String)</code>.<br>
Unless explicitly told not to, the <code>CLI</code> will build its declared Help <code>Command</code>. 
Which when invoked will print a view of all of the mapped Commands to the standard 
<code>PrintStream</code> specified by the <code>Configuration</code>.</p>

#### Example 2:

    public class Sample {
        public static void main(String[] args) {
            CLI cli = new CLI(new Configuration());
            cli.read();
        }
    }
    
<p>You can also specify that the <code>CLI</code> should continuously block and poll for input from the 
<code>InputStream</code> specified by the <code>Configuration</code>.
</p>

### Configuration

coming soon

## Build

### Gradle 

    compileJava {
        options.compilerArgs.add("-parameters")
    }
    
## Documentation
coming soon

## Licence
tbd
