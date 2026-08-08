package com.automation.utils;

import com.automation.base.BaseTest;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScreenshotListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        if (!(result.getInstance() instanceof BaseTest test) || test.getDriver() == null) {
            return;
        }

        String fileName = result.getMethod().getMethodName() + "_" + System.currentTimeMillis() + ".png";
        Path destination = Path.of("target", "screenshots", fileName);
        try {
            Files.createDirectories(destination.getParent());
            FileUtils.copyFile(((TakesScreenshot) test.getDriver()).getScreenshotAs(OutputType.FILE), destination.toFile());
        } catch (IOException exception) {
            System.err.println("Unable to save failure screenshot: " + exception.getMessage());
        }
    }
}
