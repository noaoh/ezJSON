package com.noaoh.ezJSON;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

public class ValidParseTests {
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
            if (file.startsWith("y")) {
                StringJoiner joiner = new StringJoiner("/");
                String path = joiner.add(currentDir).add(jsonPath).add(file).toString();
                String testName = file.substring(2).replace("_", " ").replace(".json", "");
                testCases.put(path, testName);
            }
        }
    }

    @TestFactory
    public Collection<DynamicTest> dynamicValidParseTests() {
        ArrayList<DynamicTest> dynamicTests = new ArrayList<DynamicTest>();
        for (Map.Entry<String, String> testCase : testCases.entrySet()) {
            String path = testCase.getKey();
            String testName = testCase.getValue();
            Executable e = () -> Json.load(path);
            Executable x = () -> assertDoesNotThrow(e);
            dynamicTests.add(DynamicTest.dynamicTest(testName, x));
        }
        return dynamicTests;
    }
}