package com.noaoh.ezJSON;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

public class InvalidParseTests {
    static HashMap<String, String> testCases;

    @BeforeAll
    static void initializeJSONFiles() {
        testCases = new HashMap<String, String>();
        String currentDir = System.getProperty("user.dir");
        String jsonPath = "src/test/resources/com/noaoh/ezJSON/test_parsing";
        File dir = new File(jsonPath);
        String[] contents = dir.list();
        String file;
        for (int x = 0; x < contents.length; x++) {
            file = contents[x];
            if (file.startsWith("n")) {
                StringJoiner joiner = new StringJoiner("/");
                String path = joiner.add(currentDir).add(jsonPath).add(file).toString();
                String testName = file.substring(2).replace("_", " ").replace(".json", "");
                testCases.put(path, testName);
            }
        }
    }

    @TestFactory
    public Collection<DynamicTest> dynamicInvalidParseTests() {
        ArrayList<DynamicTest> dynamicTests = new ArrayList<DynamicTest>();
        for (Map.Entry<String, String> testCase : testCases.entrySet()) {
            String path = testCase.getKey();
            String testName = testCase.getValue();
            Executable e = () -> Json.load(path);
            Executable x = () -> assertThrows(RuntimeException.class, e);
            dynamicTests.add(DynamicTest.dynamicTest(testName, x));
        }
        return dynamicTests;
    }
}
