package net.radai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractGrepMojo extends AbstractMojo {

    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;
    protected Log log = getLog();
    protected Map<Grep, List<Match>> matches = new HashMap<>();
    /**
     * A file of patterns. Each line will be handled as a Java {@link Pattern}
     * used to find matches in the project. Required for 'singleGrep' and
     * 'aggregate' goals.
     */
    @Parameter(property = "blacklistFile")
    protected File blacklistFile;
    /**
     * Whether or not to write matches to output files.
     */
    @Parameter(property = "grep.fileOutput")
    protected boolean outputFile = false;
    @Parameter(defaultValue = "${reactorProjects}", readonly = true)
    protected List<MavenProject> reactorProjects;

    protected void executeGrep(Grep grep) {
        String grepPattern = grep.getGrepPattern();
        if (grepPattern != null) {
            grepFile(grep, grepPattern);
        }
        if (grep.getGrepFile() != null) {
            List<String> patternLines;
            try {
                patternLines = Files.readAllLines(Paths.get(grep.getGrepFile().getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            for (String patternLine : patternLines) {
                grepFile(grep, patternLine);
            }
        }
    }

    private void grepFile(Grep grep, String grepPattern) {
        Pattern lookingFor = Pattern.compile(grepPattern);
        File searchFile = null;
        if (grep.getFile() == null) {
            searchFile = Paths.get(basedir.getAbsolutePath(), "src").toFile();
        } else {
            searchFile = Paths.get(basedir.getAbsolutePath(), grep.getFile()).toFile();
        }
        grepIn(searchFile, lookingFor, grep);
        if (grep.getFilePattern() != null) {
            log.error("file patterns not implemented yet");
        }
    }

    private void grepIn(File theFile, Pattern lookingFor, Grep grep) {
        if (!theFile.exists()) {
            return;
        }
        if (!theFile.canRead()) {
            return;
        }
        if (theFile.isDirectory()) {
            File[] listFiles = theFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return !name.endsWith(".zip") && !name.endsWith(".properties");
                }
            });
            for (File file : listFiles) {
                grepIn(file, lookingFor, grep);
            }
            return;
        }
        List<Match> grepThroughFile = grepThroughFile(theFile, lookingFor, grep);
        addMatches(grep, grepThroughFile);
    }

    private void addMatches(Grep grep, List<Match> grepThroughFile) {
        if (!matches.containsKey(grep)) {
            matches.put(grep, new ArrayList<>());
        }
        if (grepThroughFile.isEmpty()) {
            return;
        }
        matches.get(grep).addAll(grepThroughFile);
    }

    private List<Match> grepThroughFile(File theFile, Pattern lookingFor, Grep grep) {
        Matcher m;
        FileReader reader = null;
        List<Match> fileMatches = Collections.emptyList();
        try {
            reader = new FileReader(theFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            int lineNumber = 0;
            boolean found = false;
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                m = lookingFor.matcher(line);
                if (!(m.find())) {
                    continue;
                }
                Match processMatchingLine = processMatchingLine(theFile, line, grep, lineNumber);
                if (fileMatches.isEmpty()) {
                    fileMatches = new ArrayList<>();
                }
                fileMatches.add(processMatchingLine);
                found = true;
            }
            if (!found) {
                failIfNotFound(theFile, grep);
            }
            return fileMatches;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private Match processMatchingLine(File theFile, String theLine, Grep grep, int lineNumber) throws Exception {
        failIfFound(theFile, theLine, grep, lineNumber);
        return new Match(theFile, theLine, lineNumber);
    }

    private void failIfFound(File theFile, String theLine, Grep grep, int lineNumber)
            throws IOException, MojoFailureException {
        if (grep.isFailIfFound()) {
            String msg = grep.getGrepPattern() + " found in  " + theFile.getCanonicalPath() + ":" + lineNumber + " ("
                    + theLine + ")";
            log.error(msg);
            throw new MojoFailureException(msg);
        }
    }

    private void failIfNotFound(File theFile, Grep grep) throws IOException, MojoFailureException {
        if (grep.isFailIfNotFound()) {
            String msg = grep.getGrepPattern() + " not found in  " + theFile.getCanonicalPath();
            log.error(msg);
            throw new MojoFailureException(msg);
        }
    }

    public File getBasedir() {
        return basedir;
    }

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    protected void printFile() {
        if (outputFile) {
            try {
                Path path = getGrepOutputDirectory();
                Set<Grep> keySet = matches.keySet();
                for (Grep grep : keySet) {
                    Path createFile = Paths.get(path.toAbsolutePath().toString(), "/" + grep.getProperty());
                    List<Match> list = matches.get(grep);
                    List<String> strings = new ArrayList<>();
                    for (Match match : list) {
                        strings.add(match.toString());
                    }
                    Files.write(createFile, strings);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    protected Path getGrepOutputDirectory() {
        String name = blacklistFile.getName();
        String outputDirectoryName = name.replaceAll("\\" + File.separator, ".");
        Path path = Paths.get(basedir.getAbsolutePath() + "/target/grep-output/" + outputDirectoryName);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating GrepOutputDirectory " + e);
        }
        return path;
    }

    public File getBlacklistFile() {
        return blacklistFile;
    }

    public void setBlacklistFile(File blacklistFile) {
        this.blacklistFile = blacklistFile;
    }

    public void setOutputFile(boolean outputFile) {
        this.outputFile = outputFile;
    }

    public Map<Grep, List<Match>> getMatches() {
        return matches;
    }

    protected void validateBlacklistFileRequirement() throws MojoFailureException {
        if (blacklistFile == null) {
            throw new MojoFailureException(
                    "Cannot run singleGrep without a blacklistFile containing searchable patterns.");
        }
    }

}
