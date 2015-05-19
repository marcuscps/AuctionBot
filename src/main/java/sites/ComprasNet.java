package sites;

import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.WebDriverWrapper;

public class ComprasNet extends Site {

	static Logger logger = LoggerFactory.getLogger(ComprasNet.class);

	private static final String idPerfil	= "perfil";
	private static final String idLogin		= "txtLogin";
	private static final String idSenha		= "txtSenha";
	private static final String idAcessar	= "acessar";
	
	private static final String textFornecedor = "Fornecedor";
	
	private static final String classError = "error";
	
//	private static final By byPerfil	= By.id(idPerfil);
//	private static final By byLogin		= By.id(idLogin);
//	private static final By bySenha		= By.id(idSenha);
//	private static final By byAcessar	= By.id(idAcessar);
	
    boolean loginOk = false;

	public ComprasNet(String baseURL, String projectId, Listener listener) {
		super(new FirefoxDriver(), baseURL, projectId, listener);
	}

    @Override
	public void load() {
        logger.debug("Loading: {}", baseURL);
    
        boolean success = false;
        for (int retry = 0; retry < 3 && success == false; ++retry) {
			logger.debug("Loading. Try: " + retry);
        	driver.get(baseURL);
        	try {
	        	success = (new WebDriverWait(driver, 1)).until(new ExpectedCondition<Boolean>() {
	            	public Boolean apply(WebDriver d) {
	            		return WebDriverWrapper.isElementFound(d, By.id(idPerfil));
	            	}
	        	});
        	} catch (TimeoutException e) {
        	}
        }
        
        if (success) {
            logger.debug("Page load successfully");
        	listener.onPageLoadSuccess();
        } else {
			logger.error("Load error: Could not load page.");
        	listener.onPageLoadFail("Could not load page.");
        }
	}
	
	public void login() {
        logger.debug("Login: {}", username);
        
        String mainWindowId = driver.getWindowHandle();

        // Fill login data
        logger.debug("Setting perfil type");
    	WebElement elPerfil  = driver.findElement(By.id(idPerfil));
        new Select(elPerfil).selectByVisibleText(textFornecedor);
        WebElement elLogin = driver.findElement(By.id(idLogin));
        WebElement elSenha = driver.findElement(By.id(idSenha));
        logger.debug("Filling username and password");
        elLogin.sendKeys(username);
        elSenha.sendKeys(password);

        // Click "acessar"
        logger.debug("Submitting login");
        WebElement elAcessar = driver.findElement(By.id(idAcessar));
        elAcessar.click();
       
        // Wait for result 
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
        	public Boolean apply(WebDriver d) {
        		if (WebDriverWrapper.isElementFound(d, By.className(classError))) {
        			return true;
        		} else if (WebDriverWrapper.isElementFound(d, By.name("nav"))) { 
        			loginOk = true;
        			return true;
        		}
        		return false;
        	}
        });

        if (loginOk == false) {
    		WebElement elErrorMsg = driver.findElement(By.className(classError));
    		if (elErrorMsg == null) {
    			logger.error("Could not load page.");
    			listener.onLoginFail("Could not load page.");
    		} else {
    			logger.error("Login error: {}", elErrorMsg.getText());
    			listener.onLoginFail(elErrorMsg.getText());
    		}

    		driver.close();
    		return;
        }
		logger.debug("Login succeeded!");

		logger.debug("Closing pop-ups...");
        Set<String> windowSet = driver.getWindowHandles();
        for (String windowId : windowSet) {
        	if (windowId.equals(mainWindowId) == false) {
        		driver.switchTo().window(windowId);
        		logger.debug("  Window ID: {}: {}: pop-up closed!", windowId, driver.getTitle());
        		driver.close();
        	}
        }

        driver.switchTo().window(mainWindowId);
        logger.debug("Login successfull.");
        listener.onLoginSuccess();
	}

	@Override
	public void close() {
		logger.debug("Closing WebDriver");
	    driver.quit();
	}

	@Override
	public void openProject() {
		logger.debug("Opening project");
		
//		div[id="Header"].div[0].div[0]

		listener.onOpenProjectSuccess();
		listener.onOpenProjectFail("Error message!!");
		listener.onAuctionModeChanged(mode);
	}

}
