package io.fabric8.maven.docker.access;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/*
 * 
 * Copyright 2014 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author roland
 * @since 27/03/15
 */
public class ContainerCreateConfigTest {

    @Test
    public void testEnvironment() throws Exception {
        final ContainerCreateConfig cc = new ContainerCreateConfig("testImage");
        final Map<String, String> envMap = getEnvMap();
        cc.environment(copyPropsToFile(), envMap, false, Collections.<String, String>emptyMap());
        final JSONArray env = getEnvArray(cc);
        assertNotNull(env);
        assertEquals(3, env.length());
        final List<String> envAsString = convertToList(env);
        assertTrue(envAsString.contains("JAVA_OPTS=-Xmx512m"));
        assertTrue(envAsString.contains("TEST_SERVICE=SECURITY"));
        assertTrue(envAsString.contains("EXTERNAL_ENV=TRUE"));
    }

    @Test
    public void testEnvironmentEmptyPropertiesFile() {
        final ContainerCreateConfig cc = new ContainerCreateConfig("testImage");
        cc.environment(null, getEnvMap(), false, Collections.<String, String>emptyMap());
        final JSONArray env = getEnvArray(cc);
        assertEquals(2, env.length());
    }

    @Test
    public void testBind() {
        final String[] testData = new String[]{
                "c:\\this\\is\\my\\path:/data", "/data",
                "/home/user:/user", "/user",
                "c:\\this\\too:/data:ro", "/data"};
        for (int i = 0; i < testData.length; i += 2) {
            final ContainerCreateConfig cc = new ContainerCreateConfig("testImage");
            cc.binds(Arrays.asList(testData[i]));

            final JSONObject volumes = (JSONObject) new JSONObject(cc.toJson()).get("Volumes");
            assertEquals(1, volumes.length());
            assertTrue(volumes.has(testData[i + 1]));
        }
    }


    @Test
    public void testNullEnvironment() {
        final ContainerCreateConfig cc = new ContainerCreateConfig("testImage");
        cc.environment(null, null, false, Collections.<String, String>emptyMap());
        final JSONObject config = new JSONObject(cc.toJson());
        assertFalse(config.has("Env"));
    }

    @Test
    public void testEnvNoMap() throws IOException {
        final ContainerCreateConfig cc = new ContainerCreateConfig("testImage");
        cc.environment(copyPropsToFile(), null, false, Collections.<String, String>emptyMap());
        final JSONArray env = getEnvArray(cc);
        assertEquals(2, env.length());
        final List<String> envAsString = convertToList(env);
        assertTrue(envAsString.contains("EXTERNAL_ENV=TRUE"));
    }

    @Test
    public void testEnvironmentKeepEnvs() throws IOException {
        final ContainerCreateConfig cc = new ContainerCreateConfig("testImage");
        final Map<String, String> envs = getEnvMap();
        cc.environment(copyPropsToFile(), envs, true, Collections.<String, String>emptyMap());
        final JSONArray env = getEnvArray(cc);
        assertEquals(3, env.length());
        assertEquals(env.getString(2), "TEST_SERVICE=LOGGING");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPropFile() {
        final ContainerCreateConfig cc = new ContainerCreateConfig("testImage");
        cc.environment("/not/really/a/file", null, false, Collections.<String, String>emptyMap());
    }


    private JSONArray getEnvArray(final ContainerCreateConfig cc) {
        final JSONObject config = new JSONObject(cc.toJson());
        return (JSONArray) config.get("Env");
    }

    private String copyPropsToFile() throws IOException {
        final File tempFile = File.createTempFile("dockertest", "props");
        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("test-environment.props"), tempFile);
        return tempFile.getAbsolutePath();
    }


    private List<String> convertToList(final JSONArray env) {
        final List<String> envAsString = new ArrayList<>();
        for (int i = 0; i < env.length(); i++) {
            envAsString.add(env.getString(i));
        }
        return envAsString;
    }

    private Map<String, String> getEnvMap() {
        final Map<String, String> envMap = new HashMap<>();
        envMap.put("JAVA_OPTS", "-Xmx512m");
        envMap.put("TEST_SERVICE", "LOGGING");
        return envMap;
    }

}