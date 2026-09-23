package br.com.leandrobrum.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("web")
class CalculatorTest {
    private WebDriver driver;
    private CalculatorPage page;

    @BeforeEach
    void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        page = new CalculatorPage(driver, new WebDriverWait(driver, Duration.ofSeconds(20)));
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Calculadora de dias úteis conta os dias úteis de um período")
    void calculatesBusinessDays() {
        page.open(
                        "https://blog.agibank.com.br/calculadora-dias-uteis/",
                        "Calculadora de Dias Úteis")
                .setValue("dataInicio", "2026-04-06")
                .setValue("dataFim", "2026-04-10")
                .calculate("calcular");

        assertEquals("5", page.text("diasUteis"));
        assertEquals("06/04/2026 a 10/04/2026", page.text("periodoTexto"));
    }

    @Test
    @DisplayName("Calculadora de juros compostos calcula o montante de um investimento")
    void calculatesCompoundInterestInvestment() {
        page.open(
                        "https://blog.agibank.com.br/como-calcular-juros-compostos/",
                        "Calculadora de Juros Compostos")
                .selectInvestment()
                .selectByValue("tipoTaxaInvest", "mensal")
                .selectByValue("tipoPeriodoInvest", "meses")
                .calculate("calcularInvestimento");

        assertEquals("R$ 7.371,51", page.text("valorFinal"));
        assertEquals("R$ 7.000,00", page.text("valorInvestido"));
        assertEquals("R$ 371,51", page.text("rendimentoTotal"));
        assertTrue(page.text("periodoTexto").contains("12 meses"));
    }
}
