package net.radai;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.MojoRule;
import org.codehaus.plexus.PlexusTestCase;
import org.junit.Rule;

public abstract class AbstractGrepMojoTestCase {

    protected String testProjectBasedir = getTestProjectDir(getTestProject());
    @Rule
    public MojoRule mojoRule = new MojoRule();

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

    protected Mojo lookupMojo(String goal, File pom) throws Exception {
        return mojoRule.lookupMojo(goal, pom);
    }

    public static File getTestFile(final String path) {
        return PlexusTestCase.getTestFile(path);
    }

    @SuppressWarnings("hiding")
    public static File getTestFile(final String basedir, final String path) {
        return PlexusTestCase.getTestFile(basedir, path);
    }

}
