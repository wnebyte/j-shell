package com.github.wnebyte.jcli.sample;

import com.github.wnebyte.jcli.CLI;
import com.github.wnebyte.jcli.Configuration;

public class Sample {

    public static void main(String[] args) {
        CLI.launch(new Configuration().setScanClasses(BarController.class, FooController.class));
    }
}
