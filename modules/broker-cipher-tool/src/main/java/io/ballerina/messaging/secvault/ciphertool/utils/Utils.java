/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.secvault.ciphertool.utils;

import io.ballerina.messaging.secvault.ciphertool.CipherToolConstants;
import io.ballerina.messaging.secvault.ciphertool.CipherToolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Cipher Tool utility methods.
 */
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * Remove default constructor and make it not available to initialize.
     */
    private Utils() {
        throw new AssertionError("Instantiating utility class...");
    }

    public static CommandLineParser createCommandLineParser(String... toolArgs) throws CipherToolException {
        return new CommandLineParser(toolArgs);
    }

    public static URLClassLoader getCustomClassLoader(Optional<String> optCustomLibPath) {
        List<URL> urls = new ArrayList<>();

        optCustomLibPath.map(Paths::get)
                .filter(path -> path.toFile().exists() && path.toFile().isDirectory())
                .ifPresent(path -> urls.addAll(getJarURLs(path.toString())));

        Optional.ofNullable(System.getProperty("message.broker.home"))
                .ifPresent(messageBrokerHome -> {
                    urls.addAll(getJarURLs(Paths.get(messageBrokerHome, "lib").toString()));
                });

        return (URLClassLoader) AccessController.doPrivileged(
                (PrivilegedAction<Object>) () -> new URLClassLoader(urls.toArray(new URL[urls.size()])));
    }

    public static Object createCipherTool(URLClassLoader urlClassLoader, Path secureVaultConfigPath) throws
            CipherToolException {
        Object objCipherTool;

        try {
            objCipherTool = urlClassLoader.loadClass(CipherToolConstants.CIPHER_TOOL_CLASS).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new CipherToolException("Unable to instantiate Cipher Tool", e);
        }

        try {
            Method initMethod = objCipherTool.getClass()
                    .getMethod(CipherToolConstants.INIT_METHOD, URLClassLoader.class, Path.class);
            initMethod.invoke(objCipherTool, urlClassLoader, secureVaultConfigPath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new CipherToolException("Failed to initialize Cipher Tool", e);
        }
        return objCipherTool;
    }

    private static List<URL> getJarURLs(String location) {
        File fileLocation = new File(location);
        List<URL> urls = new ArrayList<>();
        File[] fileList = fileLocation.listFiles((File file) -> file.getPath().toLowerCase().endsWith(".jar"));
        if (fileList != null) {
            for (File file : fileList) {
                urls.addAll(getInternalJarURLs(file));
            }
        }
        return urls;
    }

    private static List<URL> getInternalJarURLs(File file) {
        List<URL> urls = new ArrayList<>();

        try {
            urls.add(file.getAbsoluteFile().toURI().toURL());
        } catch (MalformedURLException e) {
            LOGGER.error("Unable to add file url in to URL list", e);
        }

        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".jar")) {
                    JarEntry internalJar = jarFile.getJarEntry(entry.getName());
                    try (InputStream inputStream = jarFile.getInputStream(internalJar)) {
                        File tempFile = File.createTempFile(internalJar.getName(), ".tmp");
                        tempFile.deleteOnExit();
                        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, length);
                            }
                        }
                        urls.add(tempFile.getAbsoluteFile().toURI().toURL());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("CipherTool exits with error", e);
        }

        return urls;
    }
}
