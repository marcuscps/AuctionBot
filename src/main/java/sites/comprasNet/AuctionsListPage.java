package sites.comprasNet;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionsListPage extends sites.SitePage {
	static Logger logger = LoggerFactory.getLogger(AuctionsListPage.class);

	private static final String xpathCadastramento	= "//img[@src='T_cadastramento_lances.gif']";

	private static final String nameContentFrame	= "main2";

	private static final String xpathItemsTable	= "//body/table/tbody/tr[2]/td/table[2]/tbody/tr[3]/td[2]/table/tbody";
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
    		
    		logger.debug("Checking if Auctions List page is loaded.");
    		WebElement elCadastramento = (new WebDriverWait(driver, 1)).until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpathCadastramento)));
    		return elCadastramento != null;
    	} catch (TimeoutException e) {
    		return false;
    	}
	}

	public boolean gotoAuction(WebDriver driver, String auctionId) {
		if (auctionId == null || auctionId == "") {
			throw new InvalidParameterException("Invalid Auction Id: " + auctionId);
		}
		
		selectContentFrame(driver);
		
		try {
			WebElement elItemsTable = driver.findElement(By.xpath(xpathItemsTable));

			List<WebElement> items = elItemsTable.findElements(By.xpath(".//tr[position() > 1]"));
			for (WebElement elItem : items) {
				WebElement elAuctionUASG = elItem.findElement(By.xpath(".//td[3]"));
				WebElement elAuctionId = elItem.findElement(By.xpath(".//td[2]/a"));
				String strAuctionId = elAuctionId.getText();
				
				if (auctionId.equals(strAuctionId) == false) {
					logger.debug("Auction Id {} does not match desired value {}", strAuctionId, auctionId);
					continue;
				}
				
				logger.debug("Auction Id {} is a match", strAuctionId);
				
				String strAuctionUASG = elAuctionUASG.getText();
				
				WebElement elCompany = elItem.findElement(By.xpath(".//td[4]"));
				String strCompany = elCompany.getText();
				
				WebElement elOpenDateTime = elItem.findElement(By.xpath(".//td[5]"));
				LocalDateTime openDateTime = LocalDateTime.from(dateFormatter.parse(elOpenDateTime.getText()));
				
				WebElement elSRP = elItem.findElement(By.xpath(".//td[6]"));
				String strSRP = elSRP.getText();
				boolean srp = ("Não".equals(strSRP) == false);
				
				WebElement elICMS = elItem.findElement(By.xpath(".//td[6]"));
				String strICMS = elICMS.getText();
				boolean icms = ("Não".equals(strICMS) == false);
				
				logger.debug("Detailed info:\n" +
						"    Auction ID: \"{}\"\n" +
						"    IASG ID:    \"{}\"\n" +
						"    Company:    \"{}\"\n" +
						"    Date Time:  \"{}\"\n" +
						"    SRP:        {}\n" +
						"    ICMS:       {}",
						strAuctionId, strAuctionUASG, strCompany, openDateTime, srp, icms);
				
				logger.debug("Clicking the Auction Id link");
				WebElement elBids = elItem.findElement(By.xpath(".//td[1]/a"));
				elBids.click();
				
				return true;
			}				

		} catch (NoSuchElementException e) {
			logger.error("Messages table not found or malformed.", e);
		}
		return false;
	}

}
