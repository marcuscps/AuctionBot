package sites;

import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.WebDriverWrapper;

public class ComprasNet extends Site {

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
	
	private static final String login = "ALSTOMTED";
//	private static final String login = "BLA";
	private static final String passw = "SALESFORCE2015";
//	private static final String passw = "wrong";
	
    boolean loginOk = false;

    WebDriver driver;

	public ComprasNet(String baseURL, String projectId, Listener listener) {
		super(baseURL, projectId, listener);
		
		driver = new FirefoxDriver();
	}

    @Override
	public void load() {
    
        boolean success = false;
        for (int retry = 0; retry < 3 && success == false; ++retry) {
        	System.out.println("Try: " + retry);
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
        	listener.onPageLoadSuccess();
        } else {
        	listener.onPageLoadFail("Could not load page.");
        }
	}
	
	public void login() {
        String mainWindowId = driver.getWindowHandle();

        // Fill login data
    	WebElement elPerfil  = driver.findElement(By.id(idPerfil));
        new Select(elPerfil).selectByVisibleText(textFornecedor);
        WebElement elLogin = driver.findElement(By.id(idLogin));
        WebElement elSenha = driver.findElement(By.id(idSenha));
        elLogin.sendKeys(login);
        elSenha.sendKeys(passw);

        // Click "acessar"
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
    			listener.onLoginFail("Could not load page.");
    		} else {
    			listener.onLoginFail(elErrorMsg.getText());
    		}

    		driver.close();
    		return;
        }
        
        System.out.println("Login succeeded! Continuing...");

        Set<String> windowSet = driver.getWindowHandles();
        for (String windowId : windowSet) {
        	if (windowId.equals(mainWindowId) == false) {
        		driver.switchTo().window(windowId);
                System.out.println("Window ID: " + windowId + ": " + driver.getTitle() + ": pop-up closed!");
        		driver.close();
        	}
        }

        driver.switchTo().window(mainWindowId);

        listener.onLoginSuccess();
	}

	@Override
	public void close() {
	    driver.quit();
	}

}
