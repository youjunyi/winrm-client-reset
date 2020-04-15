package com.spcow.plugins.winrmclient;

import hudson.FilePath;
import org.apache.commons.lang.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Utils {

    public static String getFileExtension() {
        return ".ps1";
    }

    public static String getContents(String command) {
        return command + "\r\nexit $LastExitCode";
    }

    public static String[] buildCommandLine(FilePath script) {
        if (isRunningOnWindows(script)) {
            return new String[]{"powershell.exe", "-NonInteractive", "-ExecutionPolicy", "Bypass", "& \'" + script.getRemote() + "\'"};
        } else {
            return new String[]{"powershell", "-NonInteractive", "& \'" + script.getRemote() + "\'"};
        }
    }

    private static boolean isRunningOnWindows(FilePath scriptFile) {
        if (!scriptFile.isRemote()) {
            return SystemUtils.IS_OS_WINDOWS;
        }
        String path = scriptFile.getRemote();
        return path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\';
    }

    public static String getStringFromInputStream(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

}
