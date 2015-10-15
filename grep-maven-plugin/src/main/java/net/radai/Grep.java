package net.radai;

import java.io.File;

public class Grep {
    private String file;
    private String filePattern;
    private String grepPattern;
    private File grepFile;
    private String outputPattern;
    private boolean failIfFound;
    private boolean failIfNotFound;
    private String property;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public String getGrepPattern() {
        return grepPattern;
    }

    public void setGrepPattern(String grepPattern) {
        this.grepPattern = grepPattern;
    }

    public String getOutputPattern() {
        return outputPattern;
    }

    public void setOutputPattern(String outputPattern) {
        this.outputPattern = outputPattern;
    }

    public boolean isFailIfFound() {
        return failIfFound;
    }

    public void setFailIfFound(boolean failIfFound) {
        this.failIfFound = failIfFound;
    }

    public boolean isFailIfNotFound() {
        return failIfNotFound;
    }

    public void setFailIfNotFound(boolean failIfNotFound) {
        this.failIfNotFound = failIfNotFound;
    }

    @Override
    public String toString() {
        return "Grep [file=" + file + ", filePattern=" + filePattern + ", grepPattern=" + grepPattern + ", grepFile="
                + grepFile + ", outputPattern=" + outputPattern + ", failIfFound=" + failIfFound + ", failIfNotFound="
                + failIfNotFound + "]";
    }

    public File getGrepFile() {
        return grepFile;
    }

    public void setGrepFile(File grepFile) {
        this.grepFile = grepFile;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
