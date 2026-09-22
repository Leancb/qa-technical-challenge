package br.com.leandrobrum.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("web")
class CalculatorTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Calculadora de dias úteis conta os dias úteis de um período")
    void calculatesBusinessDays() {
        openCalculator(
                "https://blog.agibank.com.br/calculadora-dias-uteis/",
                "Calculadora de Dias Úteis");

        setValue("dataInicio", "2026-04-06");
        setValue("dataFim", "2026-04-10");
        clickButton("calcular");

        assertEquals("5", text("diasUteis"));
        assertEquals("06/04/2026 a 10/04/2026", text("periodoTexto"));
    }

    @Test
    @DisplayName("Calculadora de juros compostos calcula o montante de um investimento")
    void calculatesCompoundInterestInvestment() {
        openCalculator(
                "https://blog.agibank.com.br/como-calcular-juros-compostos/",
                "Calculadora de Juros Compostos");

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".choice-card.investimento"))).click();

        // A página inicializa este caso: R$ 1.000 + R$ 500 mensais,
        // 0,8% a.m. por 12 meses.
        new Select(driver.findElement(By.id("tipoTaxaInvest"))).selectByValue("mensal");
        new Select(driver.findElement(By.id("tipoPeriodoInvest"))).selectByValue("meses");
        clickButton("calcularInvestimento");

        assertEquals("R$ 7.371,51", text("valorFinal"));
        assertEquals("R$ 7.000,00", text("valorInvestido"));
        assertEquals("R$ 371,51", text("rendimentoTotal"));
        assertTrue(text("periodoTexto").contains("12 meses"));
    }

    private void openCalculator(String url, String frameTitle) {
        driver.get(url);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.cssSelector("iframe[title='" + frameTitle + "']")));
    }

    private void fill(String id, String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
        field.clear();
        field.sendKeys(value);
    }

    private void setValue(String id, String value) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; "
                        + "arguments[0].dispatchEvent(new Event('input', {bubbles:true})); "
                        + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                field, value);
    }

    private void clickButton(String action) {
        WebElement button = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("button[onclick='" + action + "()']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
    }

    private String text(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id))).getText().trim();
    }

    private String quote(String text) {
        return "'" + text.replace("'", "&apos;") + "'";
    }
}
