package net.radai;

import java.io.File;

public class Match {
    private File file;
    private int lineNumber;
    private String line;

    public Match(File theFile, String theLine, int lineNumber) {
        super();
        this.file = theFile;
        this.line = theLine;
        this.lineNumber = lineNumber;
    }

    public File getTheFile() {
        return file;
    }

    public String getTheLine() {
        return line;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return "[" + file + ":" + lineNumber + ":" + line + "]";
    }

}
