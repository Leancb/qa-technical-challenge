package br.com.leandrobrum.web;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("web")
@ExtendWith(BlogSearchTest.FailureEvidence.class)
class BlogSearchTest {
    private WebDriver driver;
    private BlogSearchPage page;

    @BeforeEach
    void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000", "--disable-dev-shm-usage");
        org.openqa.selenium.logging.LoggingPreferences logging = new org.openqa.selenium.logging.LoggingPreferences();
        logging.enable(org.openqa.selenium.logging.LogType.BROWSER, java.util.logging.Level.ALL);
        options.setCapability("goog:loggingPrefs", logging);
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        page = new BlogSearchPage(driver);
        page.open();
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Pesquisa pela lupa retorna artigos e permite abrir um resultado")
    void findsAndOpensArticle() {
        String term = "emprestimo";
        page.search(term);
        assertTrue(page.heading().toLowerCase().contains(term));
        assertFalse(page.results().isEmpty(), "Pesquisa deve retornar artigos");
        assertTrue(page.results().stream().anyMatch(result ->
                java.text.Normalizer.normalize(result.getText(), java.text.Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "").toLowerCase().contains(term)),
                "Ao menos um título deve corresponder ao termo pesquisado");
        String expectedTitle = page.openFirstResult();
        assertFalse(expectedTitle.isBlank());
        assertEquals(expectedTitle, page.articleTitle());
    }

    @Test
    @DisplayName("Pesquisa inexistente informa ausência de resultados e não exibe artigos")
    void showsNoResults() {
        String term = "qaautomacaosemresultado987654321";
        page.search(term);
        assertTrue(page.heading().contains(term));
        assertTrue(page.emptyMessage().contains("Lamentamos, mas nada foi encontrado para sua pesquisa"));
        assertTrue(page.results().isEmpty(), "Não deve haver artigos para o termo inexistente");
    }

    public static class FailureEvidence implements TestExecutionExceptionHandler {
        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable failure) throws Throwable {
            BlogSearchTest test = (BlogSearchTest) context.getRequiredTestInstance();
            if (test.driver != null) {
                try {
                    Path directory = Path.of("target", "evidence", context.getRequiredTestMethod().getName());
                    Files.createDirectories(directory);
                    Files.write(directory.resolve("screenshot.png"),
                            ((TakesScreenshot) test.driver).getScreenshotAs(OutputType.BYTES));
                    Files.writeString(directory.resolve("page.html"), test.driver.getPageSource());
                    Files.writeString(directory.resolve("url.txt"), test.driver.getCurrentUrl());
                    Files.writeString(directory.resolve("browser.log"),
                            test.driver.manage().logs().get("browser").getAll().toString());
                } catch (Exception captureError) {
                    failure.addSuppressed(captureError);
                }
            }
            throw failure;
        }
    }
}
