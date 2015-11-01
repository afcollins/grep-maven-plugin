package net.radai;

import java.nio.file.Path;

public class Match {
    private Path file;
    private int lineNumber;
    private String line;
    private String fileName;

    public Match(Path theFile, String theLine, int lineNumber) {
        super();
        this.file = theFile;
        this.line = theLine;
        this.lineNumber = lineNumber;
        fileName = file.toUri().toString();
    }

    public String getFileName() {
        return fileName;
    }

    public String getTheLine() {
        return line;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return "[" + fileName + ":" + lineNumber + ":" + line + "]";
    }

}
