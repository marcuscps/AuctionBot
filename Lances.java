import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Lances {

	public static void main(String[] args) {
		WebServer webServer = new WebServer(8075);

		Lances lances = new Lances();
		lances.runTest();
		
		try { Thread.sleep(20000); } catch (InterruptedException e) { e.printStackTrace(); }
		
		webServer.shutdown();
        System.out.println("[UI]: " + "DONE!");
	}
	
	private void runTest() {
        WebDriver driver = new FirefoxDriver();

        driver.get("http://127.0.0.1:8075/test.html");

        WebElement q = driver.findElement(By.id("q"));
        WebElement go = driver.findElement(By.id("go"));

        q.sendKeys("Cheese!");
        go.click();

        System.out.println("[UI]: " + "Page title is: " + driver.getTitle());
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.getTitle().toLowerCase().startsWith("cheese!");
            }
        });
        System.out.println("[UI]: " + "Page title is: " + driver.getTitle());

		((JavascriptExecutor) driver).executeScript("document.getElementById('lastText').onchange = function(){ xmlhttp.open('GET', '127.0.0.1:8075/update.html&v=blabla', false); }");
		//$(this).text())
		
		q.clear();
        q.sendKeys("Test!");
        go.click();

        System.out.println("[UI]: " + "Page title is: " + driver.getTitle());
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.getTitle().toLowerCase().startsWith("test!");
            }
        });
        System.out.println("[UI]: " + "Page title is: " + driver.getTitle());

        driver.quit();
	}

}
