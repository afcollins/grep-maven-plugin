package net.radai;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public abstract class AbstractGrepMojoTestCase extends AbstractMojoTestCase {

    protected String testProjectBasedir = getTestProjectDir(getTestProject());

    protected static String getTestProjectDir(String testProject) {
        return "target/test-classes/unit/" + testProject;
    }

    protected abstract String getTestProject();

    protected static Path resolveGrepOutputDir(String basedir) {
        return Paths.get(basedir, "target", "grep-output");
    }

    protected AbstractGrepMojo lookupAbstractGrepMojo(String mojo, File testFile) throws Exception {
        AbstractGrepMojo lookupMojo = (AbstractGrepMojo) lookupMojo(mojo, testFile);
        lookupMojo.setBasedir(testFile.getParentFile());
        return lookupMojo;
    }

}
