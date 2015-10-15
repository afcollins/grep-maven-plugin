package net.radai;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

final class AggregatingFileVisitor implements FileVisitor<Path> {
    private Map<String, Collection<String>> aggregatedResults = new ConcurrentHashMap<String, Collection<String>>();

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String string = file.getFileName().toString();
        if (!aggregatedResults.containsKey(string)) {
            aggregatedResults.put(string, new TreeSet<String>());
        }
        List<String> readAllLines = Files.readAllLines(file);
        aggregatedResults.get(string).addAll(readAllLines);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        throw new RuntimeException(exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    public Map<String, Collection<String>> getAggregatedResults() {
        return aggregatedResults;
    }
}