package com.github.wnebyte.jcli;

import com.github.wnebyte.jcli.annotation.Command;
import com.github.wnebyte.jcli.annotation.Controller;
import com.github.wnebyte.jcli.annotation.Inject;
import com.github.wnebyte.jcli.annotation.Scope;
import com.github.wnebyte.jcli.exception.ConfigException;
import org.junit.Test;

public class ControllerLifeCycleTest {

    /*
    ###########################
    #          TEST00         #
    ###########################
    */

    @Test
    public void testTransientClassNested00() {
        CLI cli = new CLI(new Configuration()
                .nullifyScanPackages()
                .setScanClasses(TransientClassNested00.class)
        );
        cli.accept("foo");
        cli.accept("foo");
    }

    @Controller(Scope.TRANSIENT)
    private static class TransientClassNested00 {

        @Inject
        IConsole console;

        @Command
        public void foo() {
            console.println(this);
        }
    }

    /*
    ###########################
    #          TEST01         #
    ###########################
    */

    @Test
    public void testTransientClassNested01() {
        CLI cli = new CLI(new Configuration()
                .nullifyScanPackages()
                .setScanClasses(TransientClassNested01.class)
        );
        cli.accept("foo");
        cli.accept("foo");
    }

    @Controller(Scope.TRANSIENT)
    private static class TransientClassNested01 {

        private IConsole console;

        @Inject
        public TransientClassNested01(IConsole console) {
            this.console = console;
        }

        @Command
        public void foo() {
            console.println(this);
        }
    }

    /*
    ###########################
    #          TEST03         #
    ###########################
    */

    @Test
    public void testSingletonClassNested00() {
        CLI cli = new CLI(new Configuration()
                .nullifyScanPackages()
                .setScanClasses(SingletonClassNested00.class)
        );
        cli.accept("foo");
        cli.accept("foo");
    }

    @Controller(Scope.SINGLETON)
    private static class SingletonClassNested00 {

        @Inject
        private IConsole console;

        @Command
        public void foo() {
            console.println(this);
        }
    }

    /*
    ###########################
    #          TEST04         #
    ###########################
    */

    @Test
    public void testSingletonClassNested01() {
        CLI cli = new CLI(new Configuration()
                .nullifyScanPackages()
                .setScanClasses(SingletonClassNested01.class)
        );
        cli.accept("foo");
        cli.accept("foo");
    }

    @Controller(Scope.SINGLETON)
    private static class SingletonClassNested01 {

        private IConsole console;

        @Inject
        public SingletonClassNested01(IConsole console) {
            this.console = console;
        }

        @Command
        public void foo() {
            console.println(this);
        }
    }

    /*
    ###########################
    #          TEST05         #
    ###########################
    */

    @Test(expected = ConfigException.class)
    public void testNoRegisteredDependency00() {
        CLI cli = new CLI(new Configuration()
                .nullifyScanPackages()
                .setScanClasses(Cls0.class)
        );

    }

    @Controller(Scope.SINGLETON)
    private static class Cls0 {

        @Inject
        private String junk; // has not been registered with the dependency container.

        @Command
        public void foo() {
            System.out.println(junk);
        }
    }

    /*
    ###########################
    #          TEST06         #
    ###########################
    */

    @Test(expected = ConfigException.class)
    public void testNoRegisteredDependency01() {
        CLI cli = new CLI(new Configuration()
                .nullifyScanPackages()
                .setScanClasses(Cls1.class)
        );

    }

    @Controller(Scope.TRANSIENT)
    private static class Cls1 {

        @Inject
        private String junk; // has not been registered with the dependency container.

        @Command
        public void foo() {
            System.out.println(junk);
        }
    }

    /*
    ###########################
    #          TEST07         #
    ###########################
    */

    @Test(expected = ConfigException.class)
    public void testNoRegisteredDependency02() {
        CLI cli = new CLI(new Configuration()
                .nullifyScanPackages()
                .setScanClasses(Cls2.class)
        );

    }

    @Controller(Scope.SINGLETON)
    private static class Cls2 {

        private String junk; // has not been registered with the dependency container.

        @Inject
        public Cls2(String junk) {
            this.junk = junk;
        }

        @Command
        public void foo() {
            System.out.println(junk);
        }
    }

    /*
    ###########################
    #          TEST08         #
    ###########################
    */

    @Test(expected = ConfigException.class)
    public void testNoRegisteredDependency03() {
        CLI cli = new CLI(new Configuration()
                .nullifyScanPackages()
                .setScanClasses(Cls3.class)
        );

    }

    @Controller(Scope.TRANSIENT)
    private static class Cls3 {

        private String junk; // has not been registered with the dependency container.

        @Inject
        public Cls3(String junk) {
            this.junk = junk;
        }

        @Command
        public void foo() {
            System.out.println(junk);
        }
    }

    /*
    ###########################
    #          TEST08         #
    ###########################
    */

    /*
    expecting an exception because #foo is not static and is declared in a non-static class.
     */
    @Test(expected = ConfigException.class)
    public void testNonStaticMethodNonStaticClass() {
        CLI cli = new CLI(new Configuration()
                .nullifyScanPackages()
                .setScanClasses(Cls4.class)
        );
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class Cls4 {

        @Command
        public void foo() {

        }
    }
}