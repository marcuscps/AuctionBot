package sites.comprasNet;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionActionsPage extends sites.SitePage {
	static Logger logger = LoggerFactory.getLogger(AuctionsListPage.class);

	private static final String nameContentFrame	= "main2";

	private static final String textLances 		= "Lances";

	private void selectContentFrame(WebDriver driver) {
		logger.debug("Selecting content frame.");
		driver.switchTo().defaultContent();
		WebElement elContentFrame = driver.findElement(By.name(nameContentFrame));
		driver.switchTo().frame(elContentFrame);
	}
	
	@Override
	public boolean isLoaded(WebDriver driver) {
    	try {
    		selectContentFrame(driver);

    		logger.debug("Checking if Auction Actions page is loaded.");
    		WebElement elPlacar = (new WebDriverWait(driver, 1)).until(ExpectedConditions.presenceOfElementLocated(By.linkText(textLances)));
    		return elPlacar != null;
    	} catch (TimeoutException e) {
    		return false;
    	}
	}

	public boolean gotoAuctionsList(WebDriver driver) {
		selectContentFrame(driver);

		logger.debug("Clicking \"Lances\"");
		WebElement elLances = driver.findElement(By.linkText(textLances));
		elLances.click();
		
		return true;
	}

}
