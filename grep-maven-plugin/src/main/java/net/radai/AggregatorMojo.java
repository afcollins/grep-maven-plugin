package net.radai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

@Mojo(name = "aggregate", aggregator = true, threadSafe = true)
public class AggregatorMojo extends AbstractGrepMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateBlacklistFileRequirement();
        Map<String, Collection<String>> aggregatedResults = aggregateResultsFromReactorProjects();
        Set<String> keySet = aggregatedResults.keySet();
        Set<String> propertiesNotUsed = new HashSet<String>();
        for (String string : keySet) {
            Collection<String> matches = aggregatedResults.get(string);
            if (matches.isEmpty()) {
                propertiesNotUsed.add(string);
            }
            Path grepOutputDirectory = getGrepOutputDirectory();
            Path path = grepOutputDirectory.resolve(string);
            safeWrite(path, matches);
        }
        printPropertiesNotUsed(propertiesNotUsed);
        safeWrite(getGrepOutputDirectory().resolve("../" + blacklistFile.getName() + ".black"), propertiesNotUsed);
    }

    private Map<String, Collection<String>> aggregateResultsFromReactorProjects() {
        AggregatingFileVisitor visitor1 = new AggregatingFileVisitor();
        for (MavenProject mavenProject : reactorProjects) {
            if (!"pom".equals(mavenProject.getPackaging())) {
                Path grepOutputs = Paths
                        .get(mavenProject.getBuild().getDirectory() + "/grep-output/" + blacklistFile.getName());
                try {
                    Files.walkFileTree(grepOutputs, visitor1);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(
                            "Error reading grep-output directories.  Did you already run 'singleGrep' goal?");
                }
            }
        }
        AggregatingFileVisitor visitor = visitor1;
        Map<String, Collection<String>> aggregatedResults = visitor.getAggregatedResults();
        return aggregatedResults;
    }

    private void safeWrite(Path path, Collection<String> matches) {
        try {
            Files.write(path, matches);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void printPropertiesNotUsed(Set<String> propertiesNotUsed) {
        getLog().info("Properties not used:");
        for (String string : propertiesNotUsed) {
            getLog().info("   " + string);
        }
    }

}
