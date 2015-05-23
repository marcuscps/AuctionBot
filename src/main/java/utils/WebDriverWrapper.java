package utils;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

public abstract class WebDriverWrapper {

	public static boolean isElementFound(WebDriver d, By by) {
		try {
			d.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
			d.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		} finally {
			d.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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
}
