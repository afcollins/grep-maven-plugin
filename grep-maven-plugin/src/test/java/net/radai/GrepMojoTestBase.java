package net.radai;

import java.io.File;

public class GrepMojoTestBase {

    private static final String SINGLE_PROJECT = "src/test/resources/single-project";

    protected GrepMojo newGrepMojo() {
        GrepMojo grepMojo = new GrepMojo();
        grepMojo.setBasedir(new File("../test-project"));
        return grepMojo;
    }

    protected void setBaseDir(AbstractGrepMojo singleGrepMojo) {
        singleGrepMojo.setBasedir(new File(SINGLE_PROJECT));
    }

}
