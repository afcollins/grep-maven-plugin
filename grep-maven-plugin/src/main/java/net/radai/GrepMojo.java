package net.radai;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Mojo(name = "grep", threadSafe = true)
public class GrepMojo extends AbstractGrepMojo {

    @Parameter(required = true)
    private List<Grep> greps;

    @Parameter(required = false)
    private String outputPattern;

    public List<Grep> getGreps() {
        return greps;
    }

    public void setGreps(List<Grep> greps) {
        this.greps = greps;
    }

    public String getOutputPattern() {
        return outputPattern;
    }

    public void setOutputPattern(String outputPattern) {
        this.outputPattern = outputPattern;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            for (Grep grep : greps) {
                executeGrep(grep);
            }
            for (Grep grep2 : matches.keySet()) {
                List<Match> list = matches.get(grep2);
                for (Match match : list) {
                    printMatch(match.getFileName(), match.getTheLine(), grep2, match.getLineNumber());
                }
            }
        } catch (Exception e) {
            throw new MojoFailureException("error grepping", e);
        }
    }

    private void printMatch(String fileName, String theLine, Grep grep, int lineNumber)
            throws IOException, TemplateException {
        String templateToUse = grep.getOutputPattern();
        if (templateToUse == null) {
            templateToUse = outputPattern;
        }
        if (templateToUse == null) {
            log.info(fileName + ": " + lineNumber + ": " + theLine);
        } else {
            Template template = new Template("templateName", new StringReader(templateToUse), new Configuration());
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("line", theLine);
            parameters.put("fileName", fileName);
            parameters.put("lineNumber", lineNumber + "");
            StringWriter output = new StringWriter();
            template.process(parameters, output);
            log.info(output.toString());
        }
    }
}
