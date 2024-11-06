import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.DataRandomGenerator;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ShoppingSite {
    AppiumDriver driver;
    private List<String> productNames; // List to store product name
    String firstName, lastName, company, phone, email;

    @BeforeClass
    public void setup() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options()
                .setUdid("emulator-5554")
                .setNewCommandTimeout(Duration.ofSeconds(30))
                .withBrowserName("Chrome");

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/"), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        productNames = new ArrayList<>(); // Initialize the product names list
    }

    @Test
    public void testShopping() {
        driver.get("https://magento.softwaretestingboard.com/push-it-messenger-bag.html");
        // Call function with parameters for Women products
        selectProduct("Women", "Jackets", "M", "Blue");
        // Call function with parameters for Men products
         selectProduct("Men", "Hoodies & Sweatshirts", "XL", "Lavender");

        // Wait for the cart button to be visible and clickable
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement toggleNavButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@data-action='toggle-nav' and contains(@class, 'nav-toggle')]")));
        driver.executeScript("arguments[0].scrollIntoView(true);", toggleNavButton);
        WebElement cartButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class, 'action showcart')]")));

        // Click the cart button
        cartButton.click();
        // Verify the product names in the mini-cart
        verifyProductNamesInCart();
        // Proceed to checkout
        proceedToCheckout();
    }

    private void selectProduct(String category, String item, String size, String color) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Click the toggle navigation button
        WebElement toggleNavButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@data-action='toggle-nav' and contains(@class, 'nav-toggle')]")));
        driver.executeScript("arguments[0].scrollIntoView(); window.scrollBy(0, -window.innerHeight / 2);", toggleNavButton);
        toggleNavButton.click();

        // Identify the menu corresponding to the category
        String genderMenuXPath = String.format("//a[contains(@href, '/%s.html')]/span[text()='%s']", category.toLowerCase(), category);
        WebElement genderMenu = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(genderMenuXPath)));
        genderMenu.click();

        // Identify XPath for the product based on its name
        String itemXPath = String.format("//a[contains(@href, '/%s/')]/span[text()='%s']", category.toLowerCase(), item);

        // Select items in the category
        WebElement categoryMenu = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(itemXPath)));
        categoryMenu.click();

        WebElement itemProducts = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[@id='toolbar-amount']")));
        driver.executeScript("arguments[0].scrollIntoView(true);", itemProducts);

        // Get the first product
        WebElement firstProduct = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[@class='item product product-item'][1]")));

        // Get the product name
        String productName = firstProduct.findElement(By.xpath(".//strong[@class='product name product-item-name']/a")).getText();
        productNames.add(productName); // Add product name to the list
        System.out.println("Product Name: " + productName);

        // Check if the category is not Gear
        if (!category.equalsIgnoreCase("Gear")) {
            // Select size
            WebElement sizeOption = firstProduct.findElement(By.xpath(String.format(".//div[@class='swatch-attribute size']//div[@option-label='%s']", size)));
            sizeOption.click();

            // Select color
            WebElement colorOption = firstProduct.findElement(By.xpath(String.format(".//div[@class='swatch-attribute color']//div[@option-label='%s']", color)));
            colorOption.click();
        }

        // Click the "Add to Cart" button
        WebElement addToCartButton = firstProduct.findElement(By.xpath(".//button[@title='Add to Cart']"));
        addToCartButton.click();

        // Wait for success message to appear
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@data-bind, 'html: $parent.prepareMessageForHtml')]")));

        // Get the content of the message
        String messageText = successMessage.getText();

        // Validate the message
        String expectedMessage = String.format("You added %s to your shopping cart.", productName);
        if (messageText.contains(expectedMessage)) {
            System.out.println("Verification successful: The message matches the product name.");
        } else {
            System.out.println("Verification failed: The message does not match the product name.");
        }
    }

    private void verifyProductNamesInCart() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        // Wait for mini-cart to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mini-cart")));

        // Get the list of products in the mini-cart
        List<WebElement> productItems = driver.findElements(By.xpath("//div[@class='minicart-items-wrapper']//li[contains(@class, 'item product product-item')]"));

        // Check product names in the mini-cart
        for (String expectedProductName : productNames) {
            boolean productFound = false;
            for (WebElement item : productItems) {
                String cartProductName = item.findElement(By.xpath(".//strong[@class='product-item-name']/a")).getText();
                if (cartProductName.equals(expectedProductName)) {
                    productFound = true;
                    System.out.println("Verified: " + expectedProductName + " is in the cart.");
                    break;
                }
            }

            if (!productFound) {
                System.out.println("Product not found in cart: " + expectedProductName);
            }
        }
    }

    private void proceedToCheckout() {
        // Click the Checkout button
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.findElement(By.xpath("//button[@id='top-cart-btn-checkout']")).click();
        WebElement estimatedTotal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Estimated Total']")));
        driver.executeScript("arguments[0].scrollIntoView(true);", estimatedTotal);

        firstName = DataRandomGenerator.generateRandomString(5);
        lastName = DataRandomGenerator.generateRandomString(5);
        company = DataRandomGenerator.generateRandomString(5);
        phone = DataRandomGenerator.generateRandomPhoneNumber();
        email = DataRandomGenerator.generateRandomEmail();

        // Input email
        WebElement emailInput = driver.findElement(By.xpath("//div[@class='control _with-tooltip']/input[@class='input-text' and @type='email']"));
        emailInput.clear();
        emailInput.sendKeys(email);

        // Input first name
        WebElement firstNameInput = driver.findElement(By.xpath("//input[@name='firstname']"));
        firstNameInput.clear();
        firstNameInput.sendKeys(firstName);

        // Input last name
        WebElement lastNameInput = driver.findElement(By.xpath("//input[@name='lastname']"));
        lastNameInput.clear();
        lastNameInput.sendKeys(lastName);

        // Input company
        WebElement companyInput = driver.findElement(By.xpath("//input[@name='company']"));
        companyInput.clear();
        companyInput.sendKeys(company);

        // Input street address
        WebElement streetAddressInput = driver.findElement(By.xpath("//input[@name='street[0]']"));
        streetAddressInput.clear();
        streetAddressInput.sendKeys("02 Tan Vien");

        // Input city
        WebElement cityInput = driver.findElement(By.xpath("//input[@name='city']"));
        cityInput.clear();
        cityInput.sendKeys("Ho Chi Minh");

        // Select state/province
        WebElement stateSelect = driver.findElement(By.xpath("//select[@name='region_id']"));
        stateSelect.sendKeys("California");

        // Input zip code
        WebElement zipCodeInput = driver.findElement(By.xpath("//input[@name='postcode']"));
        zipCodeInput.clear();
        zipCodeInput.sendKeys("70000");

        // Input phone number
        WebElement phoneInput = driver.findElement(By.xpath("//input[@name='telephone']"));
        phoneInput.clear();
        phoneInput.sendKeys(phone);

        // Locate the shipping method table
        WebElement shippingMethodTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout-shipping-method-load")));
        driver.executeScript("arguments[0].scrollIntoView(true);", shippingMethodTable);

        // Find the radio button for the desired shipping method
        WebElement shippingMethodRadioButton = driver.findElement(By.xpath("//input[@type='radio' and @value='flatrate_flatrate']"));

        // Click the radio button to select the shipping method
        shippingMethodRadioButton.click();

        // Click the "Next" button
        WebElement nextButton = driver.findElement(By.xpath("//button[span[text()='Next']]"));
        driver.executeScript("arguments[0].scrollIntoView(true);", nextButton);
        nextButton.click();

        // Wait for the "Place Order" button to be clickable
        WebElement placeOrderButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@title='Place Order']")));
        driver.executeScript("arguments[0].scrollIntoView(true);", estimatedTotal);
        placeOrderButton.click();

        // Wait for the success message to appear
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Thank you for your purchase!']")));
        driver.executeScript("arguments[0].scrollIntoView(true);", successMessage);


        // Verify the success message
        if (successMessage.isDisplayed()) {
            System.out.println("Order placed successfully.");
        } else {
            System.out.println("Order placement failed.");
        }

        // Locate the email address element
        WebElement emailAddressElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@data-bind='text: getEmailAddress()']")));

        // Retrieve the text content of the email address element
        String retrievedEmail = emailAddressElement.getText();

        // Compare the retrieved email address with the initially entered email
        if (retrievedEmail.equals(email)) {
            System.out.println("Email address matches the initially entered email.");
        } else {
            System.out.println("Email address does not match the initially entered email.");
        }
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
