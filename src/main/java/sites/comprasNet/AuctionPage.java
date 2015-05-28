package sites.comprasNet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Reversed;

public class AuctionPage extends sites.SitePage {
	static Logger logger = LoggerFactory.getLogger(AuctionPage.class);

	public interface MonitorListener {
		void onNewMessage(String sender, String message);

		void onItemInfo(boolean winning, String id, String description, boolean opened, boolean randomFinish, long myBid, long bestBid);
	}

	private static final String titlePregaoEletronico	= "Pregão Eletrônico";

	private static final String nameHeaderFrame		= "header";
	private static final String nameContentFrame	= "main_lance";
	private static final String nameMessagesFrame	= "display";

	private static final String xpathAuctionInfo	= "//td[2]";
	private static final String xpathItemsTable		= "//table[3]/tbody";
	private static final String xpathMessagesTable	= "//tbody";

	private MonitorListener listener = null;
	
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("(dd/MM/yyyy HH:mm:ss)");
	private LocalDateTime lastMessageDateTime = null;

	private String expectedAuctionId;
	
	private void selectHeaderFrame(WebDriver driver) {
		logger.debug("Selecting header frame.");
		driver.switchTo().defaultContent();
		WebElement elHeaderFrame = driver.findElement(By.name(nameHeaderFrame));
		driver.switchTo().frame(elHeaderFrame);
	}
	
	private void selectContentFrame(WebDriver driver) {
		logger.debug("Selecting content frame.");
		driver.switchTo().defaultContent();
		WebElement elContentFrame = driver.findElement(By.name(nameContentFrame));
		driver.switchTo().frame(elContentFrame);
	}
	
	private void selectMessagesFrame(WebDriver driver) {
		logger.debug("Selecting messages frame.");
		driver.switchTo().defaultContent();
		WebElement elMessagesFrame = driver.findElement(By.name(nameMessagesFrame));
		driver.switchTo().frame(elMessagesFrame);
	}
	
	public void setListener(MonitorListener listener) {
		this.listener = listener;
	}
	
	@Override
	public boolean isLoaded(WebDriver driver) {
    	try {
	        logger.debug("Auction page opened! Switching window.");
	        Set<String> windowSet = driver.getWindowHandles();
	        for (String windowId : windowSet) {
        		driver.switchTo().window(windowId);
        		if (titlePregaoEletronico.equals(driver.getTitle())) {
            		selectHeaderFrame(driver);
        			WebElement elAuctionId = driver.findElement(By.xpath(xpathAuctionInfo));
        			String strAuctionId = elAuctionId.getAttribute("innerHTML").trim();
        			String prefix = "Pregão nº: </span>&nbsp;";
        			String suffix = "<br><span class=\"tex3b\">Login:";
        			int prefixIndex = strAuctionId.indexOf(prefix);
        			int suffixIndex = strAuctionId.indexOf(suffix);
        			if (prefixIndex == -1 || suffixIndex == -1) {
		        		logger.debug("  Could not find Auction Id information. Ignoring window.");
		        		continue;
        			}
        			String auctionId = strAuctionId.substring(prefixIndex + prefix.length(), suffixIndex - 3);
	        		logger.debug("  BID window: ID: \"{}\" - TITLE: {} - WINDOW_ID: {}", auctionId, driver.getTitle(), windowId);
	        		if (expectedAuctionId != null && expectedAuctionId.equals(auctionId)) {
		        		logger.debug("  Found desired BID window.");
		        		return true;
	        		}
        		}
	        }
    	} catch (TimeoutException e) {
    		return false;
    	}
		return false;
	}

	public void monitor(WebDriver driver) {
		refreshMessages(driver);
		refreshAuction(driver);
	}

	private long extractBidValueFromHTML(String bidHtml) {
		int begin = bidHtml.indexOf("('");
		int end = bidHtml.indexOf("')");
		
		if (begin == -1 || end == -1) {
			logger.error("Invalid input value: {}", bidHtml);
			return -1;
		}

		return Long.parseLong(bidHtml.substring(begin + 2, end));
	}

	private void refreshAuction(WebDriver driver) {
		selectContentFrame(driver);
		
		try {
			WebElement elItemsTable = driver.findElement(By.xpath(xpathItemsTable));

			List<WebElement> items = elItemsTable.findElements(By.xpath(".//tr[position() > 1]"));
			for (WebElement elItem : items) {
				WebElement elWinning = elItem.findElement(By.xpath(".//td[1]/img"));
				String strWinning = elWinning.getAttribute("src");
				boolean winning = ("LanceNaoVencedor.git".equals(strWinning));

				WebElement elId = elItem.findElement(By.xpath(".//td[2]/a"));
				String id = elId.getText();
				
				WebElement elDescription = elItem.findElement(By.xpath(".//td[4]/a"));
				String description = elDescription.getText();
				
				boolean opened = false;
				boolean randomFinish = false;
				WebElement elSitutation = elItem.findElement(By.xpath(".//td[5]"));
				String strSituation = elSitutation.getText();
				switch (strSituation) {
				case "Fechado":
					opened = false;
					randomFinish = false;
					break;
				case "Aviso de Iminência":
					opened = true;
					randomFinish = false;
					break;
				case "Encerramento Aleatório":
					opened = true;
					randomFinish = true;
					break;
				case "Em desempate ME/EPP":
					// TODO
					break;
				case "Aguardando convocação ME/EPP":
					// TODO
					break;
				case "Encerrado":
					opened = false;
					randomFinish = false;
					break;
				}
				
				WebElement elMyBid = elItem.findElement(By.xpath(".//td[6]"));
				String htmlMyBid = elMyBid.getAttribute("innerHTML");
				long myBid = extractBidValueFromHTML(htmlMyBid);
				
				WebElement elBestBid = elItem.findElement(By.xpath(".//td[7]"));
				String htmlBestBid = elBestBid.getAttribute("innerHTML");
				long bestBid = extractBidValueFromHTML(htmlBestBid);
				
				listener.onItemInfo(winning, id, description, opened, randomFinish, myBid, bestBid);
			}

		} catch (NoSuchElementException e) {
			logger.error("Messages table not found or malformed.", e);
		}

//		listener.onStartMonitorFail("Error message!!");
//		mode = AuctionMode.Normal;
//		listener.onAuctionStarted();
//		listener.onAuctionModeChanged(mode);
//		mode = AuctionMode.RandomClosingTime;
//		listener.onAuctionModeChanged(mode);
//		listener.onLeadingBidChanged(true, 100);
//		listener.onLeadingBidChanged(false, 100);
//		mode = AuctionMode.Closed;
//		listener.onAuctionModeChanged(mode);
//		listener.onAuctionEnded();
	}

	private void refreshMessages(WebDriver driver) {
		selectMessagesFrame(driver);
		
		try {
			WebElement elMessagesTable = driver.findElement(By.xpath(xpathMessagesTable));

			List<WebElement> messages = elMessagesTable.findElements(By.xpath(".//tr"));
			for (WebElement elMessage : new Reversed<WebElement>(messages)) {
				WebElement elInfoDateTime = elMessage.findElement(By.xpath(".//td[1]//span"));
				LocalDateTime dateTime = LocalDateTime.from(dateFormatter.parse(elInfoDateTime.getText()));

				if (lastMessageDateTime == null || dateTime.isAfter(lastMessageDateTime)) {
					lastMessageDateTime = dateTime;
					
					WebElement elInfo = elMessage.findElement(By.xpath(".//td[1]"));
					WebElement elText = elMessage.findElement(By.xpath(".//td[2]"));
	
					String info = elInfo.getText();
					int infoIndex = info.indexOf(" fala:");
					String sender = "unknown";
					if (infoIndex != -1) {
						sender = info.substring(0, infoIndex);
					}
	
//					String date = dateTime.getYear() + "-" + dateTime.getMonthValue() + "-" + dateTime.getDayOfMonth();
//					String time = dateTime.getHour() + "," + dateTime.getMinute() + "," + dateTime.getSecond();
					String message = elText.getText();
					
					logger.debug("Message Info: SENDER: \"{}\" - WHEN: \"{}\" - WHAT: \"{}\"", sender, dateTime, message);
					listener.onNewMessage(sender, message);
				} else {
					logger.debug("No more new messages.");
					break;
				}
			}
		} catch (NoSuchElementException e) {
			logger.error("Messages table not found or malformed.", e);
		}
	}

	public void setExpectedAuctionId(String actionId) {
		this.expectedAuctionId = actionId;
	}

}
