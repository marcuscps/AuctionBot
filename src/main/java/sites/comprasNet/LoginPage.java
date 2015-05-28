package sites.comprasNet;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.WebDriverWrapper;

public class LoginPage extends sites.SitePage {
	static Logger logger = LoggerFactory.getLogger(LoginPage.class);

	private static final String idPerfil	= "perfil";
	private static final String idLogin		= "txtLogin";
	private static final String idSenha		= "txtSenha";
	private static final String idAcessar	= "acessar";

	private static final String textFornecedor	= "Fornecedor";

	private static final String classError = "error";

	@Override
	public boolean isLoaded(WebDriver driver) {
    	try {
    		WebElement elPerfil = (new WebDriverWait(driver, 1)).until(ExpectedConditions.presenceOfElementLocated(By.id(idPerfil)));
    		return elPerfil != null;
    	} catch (TimeoutException e) {
    		return false;
    	}
	}

	public boolean doLogin(WebDriver driver, String username, String password) {
        logger.debug("Login: {}", username);
   
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
        int loginOk = (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Integer>() {
        	public Integer apply(WebDriver d) {
        		if (WebDriverWrapper.isElementFound(d, By.className(classError))) {
        			return 1;
        		} else if (WebDriverWrapper.isElementFound(d, By.name("nav"))) { 
        			return 0;
        		}
        		return null;
        	}
        });

        return loginOk == 0;
	}

	public String getErrorMessage(WebDriver driver) {
    	WebElement elErrorMsg = driver.findElement(By.className(classError));
		if (elErrorMsg == null) {
			return "Could not load page.";
		}

		return elErrorMsg.getText();
	}
}
