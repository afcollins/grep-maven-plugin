package net.radai;

import static org.junit.Assert.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SingleGrepMojoTest extends AbstractGrepMojoTestCase {

    private File testProject = getTestFile(testProjectBasedir, "pom.xml");

    @Override
    protected String getTestProject() {
        return "project-003";
    }

    @Test
    public void testSimpleGrep() throws Exception {
        AbstractGrepMojo lookupMojo = lookupMojo();
        lookupMojo.execute();

        Map<Grep, List<Match>> matches = lookupMojo.getMatches();
        assertEquals(2, matches.size());
    }

    @Test
    public void testOutputFile() throws Exception {
        AbstractGrepMojo mojo = lookupMojo();
        mojo.execute();

        Path grepOutput = resolveGrepOutputDir(testProjectBasedir);
        assertTrue("target/grep-output should have been created", Files.exists(grepOutput));
        Path resolve = grepOutput.resolve(mojo.getBlacklistFile().getName());
        assertTrue(Files.exists(resolve));
        Path resolve2 = resolve.resolve("application.property");
        assertTrue(Files.exists(resolve2));
        List<String> readAllLines = Files.readAllLines(resolve2);
        assertEquals(2, readAllLines.size());
    }

    private SingleGrepMojo lookupMojo() throws Exception {
        return (SingleGrepMojo) lookupAbstractGrepMojo("singleGrep", testProject);
    }

    @Test
    public void testPattern() throws Exception {
        SingleGrepMojo lookupMojo = lookupMojo();
        lookupMojo.setPattern("[{\\\"]abc[}\\\"]");
        lookupMojo.execute();
        Map<Grep, List<Match>> matches = lookupMojo.getMatches();
        Set<Grep> keySet = matches.keySet();
        for (Grep grep : keySet) {
            if (grep.getGrepPattern().contains("application.property")) {
                assertEquals(2, matches.get(grep).size());
            }
        }
    }

    @Test
    public void testPatternWithQuotesOnly() throws Exception {
        SingleGrepMojo mojo = lookupMojo();
        mojo.setPattern("[\\\"]abc[\\\"]");
        mojo.execute();
        Map<Grep, List<Match>> matches = mojo.getMatches();
        int size = matches.size();
        assertEquals(2, size);
        Set<Grep> keySet = matches.keySet();
        for (Grep grep : keySet) {
            if (grep.getGrepPattern().contains("application.property")) {
                List<Match> patternMatches = matches.get(grep);
                assertEquals(1, patternMatches.size());
                Match match = patternMatches.get(0);
                String[] split = match.getFileName().split("/");
                assertEquals("App.java", split[split.length - 1]);
            }
        }
    }

}
