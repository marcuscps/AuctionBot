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

		void onMonitorDone();
	}

	private static final String titlePregaoEletronico	= "Preg�o Eletr�nico";

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
        			String prefix = "Preg�o n�: </span>&nbsp;";
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
		listener.onMonitorDone();
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
				ElementInfo elInfo = new ElementInfo(elItem);
				listener.onItemInfo(elInfo.winning, elInfo.id, elInfo.description, elInfo.opened, elInfo.randomFinish, elInfo.myBid, elInfo.bestBid);
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

	public void placeBid(WebDriver driver, String id, long value) {
		selectContentFrame(driver);
		
		try {
			WebElement elItemsTable = driver.findElement(By.xpath(xpathItemsTable));

			List<WebElement> items = elItemsTable.findElements(By.xpath(".//tr[position() > 1]"));
			for (WebElement elItem : items) {
				ElementInfo info = new ElementInfo(elItem);
				if (id.equals(info.id)) {
					logger.debug("Found element!");
					WebElement elBid = info.getPlaceBidElement();
					WebElement elSend = info.getPlaceBidButtonElement();
					
					long cents = value % 10000;
					long fixed = value / 10000;
					
					String valueStr = String.format("%d,%04d", fixed, cents);
					logger.debug("Bid value: \"{}\"", valueStr);
					
					elBid.sendKeys(valueStr);
					elSend.click();
				}
			}
		} catch (NoSuchElementException e) {
			logger.error("Messages table not found or malformed.", e);
		}
	}

	private class ElementInfo {
		WebElement elItem;
		
		boolean winning;
		String id;
		String description;
		boolean opened;
		boolean randomFinish;
		long myBid;
		long bestBid;

		public WebElement getWinningElement() {
			return elItem.findElement(By.xpath(".//td[1]/img"));
		}
		
		public WebElement getIdElement() {
			return elItem.findElement(By.xpath(".//td[2]/a"));
		}
		
		public WebElement getDescriptionElement() {
			return elItem.findElement(By.xpath(".//td[4]/a"));
		}
		
		public WebElement getSituationElement() {
			return elItem.findElement(By.xpath(".//td[5]"));
		}
		
		public WebElement getMyBidElement() {
			return elItem.findElement(By.xpath(".//td[6]"));
		}
		
		public WebElement getBestBidElement() {
			return elItem.findElement(By.xpath(".//td[7]"));
		}
		
		public WebElement getPlaceBidElement() {
			return elItem.findElement(By.xpath(".//td[8]/input"));
		}
		
		public WebElement getPlaceBidButtonElement() {
			return elItem.findElement(By.xpath(".//td[10]/a"));
		}
		
		public ElementInfo(WebElement elItem) {
			this.elItem = elItem;
			
			String strWinning = getWinningElement().getAttribute("src");
			winning = ("LanceNaoVencedor.git".equals(strWinning));
			id = getIdElement().getText();
			description = getDescriptionElement().getText();
			
			opened = false;
			randomFinish = false;
			String strSituation = getSituationElement().getText();
			switch (strSituation) {
			case "Fechado":							opened = false;	randomFinish = false;	break;
			case "Aviso de Imin�ncia":				opened = true;	randomFinish = false;	break;
			case "Encerramento Aleat�rio":			opened = true;	randomFinish = true;	break;
			case "Em desempate ME/EPP":				/* TODO */								break;
			case "Aguardando convoca��o ME/EPP":	/* TODO */								break;
			case "Encerrado":						opened = false;	randomFinish = false;	break;
			}
			
			String htmlMyBid = getMyBidElement().getAttribute("innerHTML");
			myBid = extractBidValueFromHTML(htmlMyBid);
			
			String htmlBestBid = getBestBidElement().getAttribute("innerHTML");
			bestBid = extractBidValueFromHTML(htmlBestBid);
		}
	}
	
}
