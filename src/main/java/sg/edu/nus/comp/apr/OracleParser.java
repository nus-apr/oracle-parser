package sg.edu.nus.comp.apr;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OracleParser {
    private Path srcPath;
    private List<OracleLocation> oracleLocations;

    private String packageName;
    private String className;
    public OracleParser(Path srcPath) {
        this.srcPath = srcPath;
        this.oracleLocations = new ArrayList<>();
    }
    private void traverseMethodOrConstructor(Node node) {
        List<IfStmt> oracleStmts = new ArrayList<>();
        List<ThrowStmt> throwStmts = new ArrayList<>();
        for (Node child : node.getChildNodes()) {
            traverseBlockNodes(child, oracleStmts, throwStmts);
        }

        if (oracleStmts.isEmpty() && throwStmts.isEmpty()) {
            return;
        }

        String methodName = (node instanceof MethodDeclaration) ? ((MethodDeclaration) node).getNameAsString() : "<init>";
        int lineNumber = node.getBegin().get().line;
        List<Integer> instrumentationLines = new ArrayList<>();
        List<Integer> instrumentationFlagLines = new ArrayList<>();
        List<Integer> customExceptionLines = new ArrayList<>();

        System.out.printf("* Found the following oracle locations in method %s.%s.%s:%d\n", packageName, className, methodName, lineNumber);
        for (IfStmt stmt : oracleStmts) {
            int begin = stmt.getBegin().get().line;
            int end = stmt.getEnd().get().line;
            instrumentationFlagLines.add(begin);
            System.out.printf("\t[Line %d-%d] %s\n", begin, end, stmt.getCondition());
            instrumentationLines.addAll(IntStream.rangeClosed(begin, end).boxed().collect(Collectors.toList()));
        }

        for (ThrowStmt stmt : throwStmts) {
            int begin = stmt.getBegin().get().line;
            int end = stmt.getEnd().get().line;
            System.out.printf("\t[Line %d-%d] %s\n", begin, end, stmt.getExpression());
            customExceptionLines.addAll(IntStream.rangeClosed(begin, end).boxed().collect(Collectors.toList()));
        }

        OracleLocation loc = new OracleLocation(packageName + "." + className, methodName, lineNumber,
                instrumentationLines, instrumentationFlagLines, customExceptionLines);
        oracleLocations.add(loc);
    }

    private void traverseBlockNodes(Node node, List<IfStmt> oracleStmts, List<ThrowStmt> throwStmts) {
        for (Node child : node.getChildNodes()) {
            if (child instanceof IfStmt) {
                IfStmt stmt = (IfStmt) child;
                String condition = stmt.getCondition().toString();
                if (condition.equals("Boolean.parseBoolean(System.getProperty(\"defects4j.instrumentation.enabled\"))")
                    || condition.equals("Boolean.valueOf(System.getProperty(\"defects4j.instrumentation.enabled\"))")) {
                    oracleStmts.add(stmt);
                }
            } else if (child instanceof ThrowStmt) {
                ThrowStmt stmt = (ThrowStmt) child;
                if (stmt.getExpression().toString().equals("new RuntimeException(\"[Defects4J_BugReport_Violation]\")")) {
                    throwStmts.add(stmt);
                }
            }
            traverseBlockNodes(child, oracleStmts, throwStmts);
        }
    }

    private void traverse(Node node) {
        for (Node child : node.getChildNodes()) {
            if (child instanceof MethodDeclaration || child instanceof ConstructorDeclaration) {
                traverseMethodOrConstructor(child);
            }
            traverse(child);
        }
    }

    public List<OracleLocation> parse() throws IOException {
        //StaticJavaParser.setConfiguration(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8));
        try {
            CompilationUnit cu = StaticJavaParser.parse(srcPath.toFile());
            for (Node child : cu.getChildNodes()) {
                if (child instanceof PackageDeclaration) {
                    packageName = ((PackageDeclaration) child).getNameAsString();
                }
                if (child instanceof ClassOrInterfaceDeclaration) {
                    className = ((ClassOrInterfaceDeclaration) child).getNameAsString();
                }
                traverse(child);
            }
        } catch (ParseProblemException e) {
            // Can happen for example if the source code uses 'enum' as an identifier (keyword only since Java 6?)
            return Collections.emptyList();
        }
        assert (packageName != null && className != null);
        return oracleLocations;
    }
}
