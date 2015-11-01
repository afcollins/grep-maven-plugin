package net.radai;

import static org.junit.Assert.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

public class AggregatorMojoTest extends AbstractGrepMojoTestCase {

    public String getTestProject() {
        return "project-005";
    }

    @Test
    public void testAggregatorMojo() throws Exception {
        File testFile = getTestFile(testProjectBasedir, "pom.xml");
        assertTrue(testFile.exists());
        lookupAbstractGrepMojo("singleGrep", getTestFile(testProjectBasedir + "/child/pom.xml")).execute();
        lookupAbstractGrepMojo("aggregate", testFile).execute();

        Path grepOutputDir = resolveGrepOutputDir(testProjectBasedir);
        assertPathExists(grepOutputDir);
        Path blacklistDir = grepOutputDir.resolve("blacklist.txt");
        assertPathExists(blacklistDir);
        assertTrue(Files.isDirectory(blacklistDir));
        Path propertyMatches = blacklistDir.resolve("application.property");
        assertPathExists(propertyMatches);
        Path blacklistedProperties = grepOutputDir.resolve("blacklist.txt.black");
        List<String> readAllLines = Files.readAllLines(blacklistedProperties);
        assertEquals(1, readAllLines.size());
        assertEquals("unusedProperty", readAllLines.get(0));
    }

    private void assertPathExists(Path path) {
        assertTrue(Files.exists(path));
    }

}
