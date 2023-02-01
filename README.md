# oracle-parser

Searches for java source files instrumented with oracle annotations (as in [defects4j-instrumented](https://github.com/nus-apr/defects4j-instrumented))
and outputs information about the oracle locations as a .json file.

## Usage
```
java -jar path/to/oracle-parser-with-deps.jar path/to/src/main/java path/to/output outputFileName
```

## Example output:
```
[ {
  "className" : "org.apache.commons.math.optimization.fitting.GaussianFitter",
  "methodName" : "fit",
  "lineNumber" : 119,
  "instrumentationLines" : [ 120, 121, 122, 123, 124, 125, 126 ],
  "instrumentationFlagLines" : [ 120 ],
  "customExceptionLines" : [ 124 ]
} ]
```

