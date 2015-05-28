package utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.io.Files;

public abstract class WebDriverWrapper {

	public static boolean isElementFound(WebDriver d, By by) {
		try {
			d.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	public static WebElement getElement(WebDriver d, By by) {
		try {
			return d.findElement(by);
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	public static FluentWait<WebDriver> getWait(WebDriver d, int timeout, int polling) {
    	return new FluentWait<WebDriver>(d)
    			.withTimeout(timeout, TimeUnit.SECONDS)
    			.pollingEvery(polling, TimeUnit.SECONDS)
    			.ignoring(NoSuchElementException.class);
	}

	public static void executeScript(WebDriver d, String script) {
		try {
			JavascriptExecutor jse = ((JavascriptExecutor) d);
			jse.executeScript(script);
		} catch (Exception e) {
			throw new RuntimeException("Error executing javascript", e);
		}
	}
	
	public static void loadJavaScript(WebDriver d, String path) {
		try {
			List<String> lines = Files.readLines(new File(path), Charset.defaultCharset());
			StringBuilder sb = new StringBuilder();
			for (String line : lines) {
				sb.append(line);
			}
			String js = sb.toString();
			String script = "var s=window.document.createElement('script'); s.type='text/javascript'; s.innerHtml='" + js + "';  window.document.head.appendChild(s);";
			executeScript(d, script);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
