package sg.edu.nus.comp.apr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void printUsage() {
        System.out.println("Usage: java -jar path/to/oracle-parser.jar path/to/src/main/java path/to/output outputFileName");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            printUsage();
            return;
        }

        // Parse args
        String srcBasePath = args[0];
        File srcDir = new File(srcBasePath);
        if (!srcDir.isDirectory()) {
            System.err.println("Path " + srcBasePath + " is not a directory!");
            printUsage();
        }

        Path outputFilePath = Paths.get(args[1], args[2]);
        Path parent = outputFilePath.getParent();

        // Prepare output
        File outputFile;
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(outputFilePath);
        }

        // Possibly throws a FileAlreadyExists exception
        if (Files.deleteIfExists(outputFilePath)) {
            System.out.println("Overwriting file: " + outputFilePath);
        }
        outputFile = Files.createFile(outputFilePath).toFile();

        // Iterate over all files in the base dir and filter out all .java-files
        List<Path> result;
        try (Stream<Path> walk = Files.walk(Paths.get(srcBasePath))) {
            result = walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".java"))
                    .collect(Collectors.toList());
        }

        // Parse all java files and search for oracle annotations
        List<OracleLocation> oracleLocations = new ArrayList<>();
        for (Path p : result) {
            OracleParser parser = new OracleParser(p);
            oracleLocations.addAll(parser.parse());
        }

        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(outputFile, oracleLocations);
    }
}
