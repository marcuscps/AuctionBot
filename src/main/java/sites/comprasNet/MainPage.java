package sites.comprasNet;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.JSUtils;
import utils.WebDriverWrapper;

public class MainPage extends sites.SitePage {
	static Logger logger = LoggerFactory.getLogger(MainPage.class);

//	private static final String xpathAtualizacoes	= "//span[regexp:test(string(.), 'ltima Atualiza')]";

	private static final String jsGoToPregaoEletronico =
			JSUtils.getElementByXpathDef +
			"_el1 = getElementByXPath('//html[1]/frameset[1]/frameset[1]/frame[1]');" +
			"_el2 = getElementByXPath('//div[1]/div[4]', _el1.contentWindow.document);" +
			"_el2.click();";

	@Override
	public boolean isLoaded(WebDriver driver) {
    	try {
    		// TODO
//    		logger.debug("Checking if Main page is loaded.");
//    		WebElement elPlacar = (new WebDriverWait(driver, 1)).until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpathAtualizacoes)));
//    		return elPlacar != null;

    		logger.debug("Checking if Main page is loaded - SKIPPED!");
    		return true;
    	} catch (TimeoutException e) {
    		return false;
    	}
	}

	public boolean gotoAuctionActions(WebDriver driver) {
//		WebDriverWrapper.loadJavaScript(driver, "D:/AuctionBotUtils.js");

		logger.debug("Clicking menu item: \"Servicos do Fornecedor\" -> \"Pregao Eletronico\" (JS HACK)");
		WebDriverWrapper.executeScript(driver, jsGoToPregaoEletronico);
		return true;
	}

}
