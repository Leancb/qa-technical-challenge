package br.com.leandrobrum.web;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

class BlogSearchPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By results = By.cssSelector("#main article .entry-title a");

    BlogSearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    void open() {
        driver.get(System.getProperty("web.baseUrl", "https://blogdoagi.com.br/"));
        // O site adia scripts ate a primeira interacao real do usuario.
        new org.openqa.selenium.interactions.Actions(driver)
                .moveToElement(driver.findElement(By.cssSelector("#ast-desktop-header")))
                .perform();
    }

    void search(String term) {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#ast-desktop-header a.astra-search-icon"))).click();
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".ast-search-box.full-screen input[name='s']")));
        field.clear();
        field.sendKeys(term, Keys.ENTER);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#main .page-title")));
    }

    String heading() {
        return driver.findElement(By.cssSelector("#main .page-title")).getText();
    }

    List<WebElement> results() {
        return driver.findElements(results);
    }

    String emptyMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#main .no-results"))).getText();
    }

    String openFirstResult() {
        WebElement first = wait.until(ExpectedConditions.elementToBeClickable(results));
        String title = first.getText();
        first.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("article h1.entry-title")));
        return title;
    }

    String articleTitle() {
        return driver.findElement(By.cssSelector("article h1.entry-title")).getText();
    }
}
