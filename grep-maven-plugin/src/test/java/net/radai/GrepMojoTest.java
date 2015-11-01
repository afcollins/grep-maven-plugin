package net.radai;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.testing.WithoutMojo;
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

    @WithoutMojo
    @Test
    public void withoutMojo() throws Exception {
        GrepMojo grepMojo = new GrepMojo();
        grepMojo.setBasedir(Paths.get("").toFile());
        Grep grep = new Grep();
        grep.setFile("target/test-classes/archive.zip!file.txt");
        grep.setGrepPattern("prop1");
        grepMojo.setGreps(asList(grep));

        grepMojo.execute();

        Map<Grep, List<Match>> matches = grepMojo.getMatches();
        assertEquals(1, matches.size());
        List<Match> value = matches.entrySet().iterator().next().getValue();
        assertEquals(1, value.size());
    }

    @WithoutMojo
    @Test
    public void testName() throws Exception {
        Path path = Paths.get("src/test/resources/archive.zip");
        List<Object> list = new ArrayList<>();
        ArrayList<Match> matches = new ArrayList<Match>();
        URI create = URI.create("jar:file:" + path.toUri().getPath());
        String path2 = create.getPath();
        String rawPath = create.getRawPath();
        FileSystem fs = FileSystems.newFileSystem(create, new HashMap<>());
        Path root = fs.getPath("/");
        Files.walk(root).forEach(p -> {
            try {
                if (!Files.isDirectory(p)) {
                    int i = 0;
                    Files.readAllLines(p).stream().filter(line -> Pattern.compile("prop1").matcher(line).find())
                            .forEach(line -> {
                        addMatch(line, "prop1", matches, i, p);
                    });
                    ;
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        Set<String> collect = matches.stream()
                .collect(Collector.of((Supplier<Set<String>>) HashSet::new, (left, right) -> {
                    String fileName = ((Match) right).getFileName();
                    URI uri = URI.create(fileName);
                    Path uriPath = Paths.get(uri);
                    Path paths = Paths.get("");
                    uriPath.relativize(paths);
                    ((Set<String>) left).add(fileName);
                } , (left, right) -> {
                    left.addAll(right);
                    return left;
                }));
        assertEquals(new HashSet<>(asList("src/test/resources/archive.zip!/file.txt",
                "src/test/resources/archive.zip!/directory/file.txt")), collect);
    }

    private void containsText(ZipFile zipFile, ZipEntry ze, String pattern, List<Match> matches) {
        try {
            InputStream inputStream = zipFile.getInputStream(ze);
            Stream<String> readAllLines = new BufferedReader(new InputStreamReader(inputStream)).lines();
            int i = 0;
            readAllLines.forEach(e -> addMatch(e, pattern, matches, i, null));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void addMatch(String e, String pattern, List<Match> matches, int i, Path path) {
        i++;
        if (e.contains(pattern)) {
            matches.add(new Match(path, e, i));
        }
    }

}
