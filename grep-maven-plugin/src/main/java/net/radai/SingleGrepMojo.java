package net.radai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Searches through a Maven project for {@link Pattern}s. Will optionally write
 * results to a file, or print to console.
 *
 */
@Mojo(name = "singleGrep", threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class SingleGrepMojo extends AbstractGrepMojo {

    /**
     * An optional pattern to be applied to each line of blacklist file,
     * replacing the token 'abc' with each line read the blacklist file when
     * searching for occurrences.
     */
    @Parameter(property = "pattern")
    private String pattern;
    @Parameter(property = "grep.printMatches")
    private boolean printMatches = false;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateBlacklistFileRequirement();
        List<String> readAllLines;
        try {
            readAllLines = Files.readAllLines(Paths.get(blacklistFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        for (String string : readAllLines) {
            Grep grep = new Grep();
            if (pattern != null) {
                grep.setGrepPattern(pattern.replace("abc", string));
            } else {
                grep.setGrepPattern(string);
            }
            grep.setProperty(string);
            executeGrep(grep);
        }
        printMatches();
        printFile();
    }

    private void printMatches() {
        if (printMatches) {
            Set<Grep> keySet = matches.keySet();
            if (!keySet.isEmpty()) {
                for (Grep grep2 : keySet) {
                    log.info("Matches for " + grep2.getGrepPattern());
                    List<Match> list = matches.get(grep2);
                    for (Match match : list) {
                        log.info(match.toString());
                    }
                }
            }
        }
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setPrintMatches(boolean printMatches) {
        this.printMatches = printMatches;
    }

}
