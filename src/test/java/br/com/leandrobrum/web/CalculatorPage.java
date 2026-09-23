package br.com.leandrobrum.web;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

class CalculatorPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    CalculatorPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    CalculatorPage open(String url, String frameTitle) {
        driver.get(url);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.cssSelector("iframe[title='" + frameTitle + "']")));
        return this;
    }

    CalculatorPage setValue(String id, String value) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; "
                        + "arguments[0].dispatchEvent(new Event('input', {bubbles:true})); "
                        + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                field, value);
        return this;
    }

    CalculatorPage selectInvestment() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".choice-card.investimento"))).click();
        return this;
    }

    CalculatorPage selectByValue(String id, String value) {
        new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id))))
                .selectByValue(value);
        return this;
    }

    CalculatorPage calculate(String action) {
        By selector = By.cssSelector("button[onclick='" + action + "()']");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(selector));
        try {
            button.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException exception) {
            // Os calculadores ficam dentro de iframes do próprio blog. O fallback é
            // restrito ao botão já considerado clicável pelo WebDriver.
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        }
        return this;
    }

    String text(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)))
                .getText().trim();
    }
}
