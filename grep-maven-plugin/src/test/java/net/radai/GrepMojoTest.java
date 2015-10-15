package net.radai;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class GrepMojoTest extends AbstractGrepMojoTestCase {

    @Test
    public void testSimpleGrep() throws Exception {
        File testFile = getTestFile(testProjectBasedir, "pom.xml");
        assertNotNull(testFile);

        GrepMojo lookupMojo = lookupMojo(testFile);
        assertNotNull(lookupMojo);
        lookupMojo.execute();

        Map<Grep, List<Match>> matches = lookupMojo.getMatches();
        assertEquals(1, matches.size());
        Set<Grep> keySet = matches.keySet();
        List<Match> list = matches.get(keySet.iterator().next());
        assertEquals(1, list.size());
    }

    private GrepMojo lookupMojo(File testFile) throws Exception {
        return (GrepMojo) lookupAbstractGrepMojo("grep", testFile);
    }

    @Test
    public void testOutputFile() throws Exception {
        File testFile = getTestFile(testProjectBasedir, "pom.xml");
        GrepMojo mojo = lookupMojo(testFile);
        mojo.execute();

        Path grepOutput = resolveGrepOutputDir(testProjectBasedir);
        assertFalse(Files.exists(grepOutput));
    }

    @Override
    protected String getTestProject() {
        return "project-001";
    }
}
