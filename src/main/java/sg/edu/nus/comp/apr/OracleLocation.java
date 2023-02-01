package sg.edu.nus.comp.apr;

import java.util.List;

public class OracleLocation {
    private String className;
    private String methodName;
    private int lineNumber;
    private List<Integer> instrumentationLines;
    private List<Integer> instrumentationFlagLines;
    private List<Integer> customExceptionLines;

    public OracleLocation() {}
    public OracleLocation(String className, String methodName, int lineNumber, List<Integer> instrumentationLines,
                          List<Integer> instrumentationFlagLines, List<Integer> customExceptionLines) {
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.instrumentationLines = instrumentationLines;
        this.instrumentationFlagLines = instrumentationFlagLines;
        this.customExceptionLines = customExceptionLines;
    }

    public String getClassName() {
        return this.className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public List<Integer> getInstrumentationLines() {
        return this.instrumentationLines;
    }

    public List<Integer> getInstrumentationFlagLines() {
        return this.instrumentationFlagLines;
    }

    public List<Integer> getCustomExceptionLines() {
        return this.customExceptionLines;
    }
}
