import io.appium.java_client.*;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DataRandomGenerator;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class TestContactApp {
    AndroidDriver driver;
    String firstName, lastName, company, phone, email;

    @BeforeClass
    public void setup() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options()
                .setUdid("emulator-5554")
                .setNewCommandTimeout(Duration.ofSeconds(30))
                .setAppPackage("com.google.android.contacts")
                .setAppActivity("com.android.contacts.activities.PeopleActivity");

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/"), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @Test
    public void testContactApp() {
        addNewContact();
        verifyContactSaved();
        deleteContact();
    }

    private void addNewContact() {
        // Generate random contact data
        firstName = DataRandomGenerator.generateRandomString(5);
        lastName = DataRandomGenerator.generateRandomString(5);
        company = DataRandomGenerator.generateRandomString(5);
        phone = DataRandomGenerator.generateRandomPhoneNumber();
        email = DataRandomGenerator.generateRandomEmail();

        driver.findElement(AppiumBy.accessibilityId("Create contact")).click();
        driver.findElement(AppiumBy.xpath("//android.widget.EditText[@text='First name']")).sendKeys(firstName);
        driver.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Last name']")).sendKeys(lastName);
        driver.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Company']")).sendKeys(company);
        driver.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Phone']")).sendKeys(phone);
        driver.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Email'][1]")).sendKeys(email);
        scrollToExpectedText("Birthday");
        inputBirthday("Oct", "02", "2000");
        driver.findElement(AppiumBy.xpath("//android.widget.Button[@text='Save']")).click();
    }

    private void inputBirthday(String month, String day, String year) {
        // Click the date picker button to open the date picker
        driver.findElement(AppiumBy.xpath("//android.widget.ImageButton[@content-desc='Show date picker']")).click();

        // Set the month
        WebElement monthPicker = driver.findElement(AppiumBy.xpath("//android.widget.NumberPicker[@index='0']//android.widget.EditText"));
        monthPicker.click();
        monthPicker.clear();
        monthPicker.sendKeys(month);

        // Set the day
        WebElement dayPicker = driver.findElement(AppiumBy.xpath("//android.widget.NumberPicker[@index='1']//android.widget.EditText"));
        dayPicker.click();
        dayPicker.clear();
        dayPicker.sendKeys(day);

        // Set the year
        WebElement yearPicker = driver.findElement(AppiumBy.xpath("//android.widget.NumberPicker[@index='2']//android.widget.EditText"));
        yearPicker.click();
        yearPicker.clear();
        yearPicker.sendKeys(year);

        // Confirm the selection
        driver.findElement(AppiumBy.xpath("//android.widget.Button[@text='Set']")).click();
    }

    private void scrollToExpectedText(String textExpected) {
        driver.findElement(AppiumBy.androidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(" +
                "new UiSelector().text(\"" + textExpected + "\").instance(0))"));
    }

    private void verifyContactSaved() {
        // Wait for the Toast message
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement toastMessage = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//android.widget.Toast[1]")));
        String toastText = toastMessage.getText();
        System.out.println("Toast message: " + toastText);
        assert toastText.equals(firstName + " " + lastName + " saved") : "Contact was not saved correctly.";

        // Navigate back to contact list
        driver.findElement(AppiumBy.xpath("//android.widget.ImageView[@content-desc='Close Popup Window']")).click();
        driver.findElement(AppiumBy.xpath("//android.widget.ImageButton[@content-desc='Navigate up']")).click();

        // Search for the newly created contact
        WebElement searchContact = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//android.widget.ImageButton[@content-desc='Search']")));
        searchContact.click();
        driver.findElement(AppiumBy.xpath("//android.widget.EditText[@text='Search contacts']")).sendKeys(firstName + " " + lastName);

        // Verify the presence of the contact list
        boolean isContactPresent = driver.findElements(AppiumBy.xpath("//android.widget.TextView[@text='" + firstName + " " + lastName + "']")).size() > 0;
        if (isContactPresent) {
            System.out.println("Contact is present in the list: " + firstName + " " + lastName);
        } else {
            assert false : "Contact is not present in the list.";
        }

        // Open the contact to verify details
        driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='" + firstName + " " + lastName + "']")).click();
        verifyContactDetails();
    }

    private void verifyContactDetails() {
        String titleContact = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@resource-id='com.google.android.contacts:id/large_title']")).getText();
        String organizationName = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@resource-id='com.google.android.contacts:id/organization_name']")).getText();
        String retrievedPhone = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@resource-id='com.google.android.contacts:id/header']")).getText();
        String retrievedEmail = driver.findElement(AppiumBy.xpath("//android.widget.TextView[contains(@text, '@')]")).getText();

        assert titleContact.equals(firstName + " " + lastName) : "Title does not match.";
        assert organizationName.equals(company) : "Company name does not match.";
        assert retrievedPhone.equals(phone) : "Phone number does not match.";
        assert retrievedEmail.equals(email) : "Email does not match.";

        // Verify the birthday
        boolean isBirthdayCorrect = driver.findElements(AppiumBy.xpath("//android.widget.TextView[@resource-id='com.google.android.contacts:id/header' and @text='October 2, 2000']")).size() > 0;
        assert isBirthdayCorrect : "Birthday is not correct.";
    }

    private void deleteContact() {
        // Click More options to get the delete option
        driver.findElement(AppiumBy.xpath("//android.widget.ImageView[@content-desc='More options']")).click();
        driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text='Delete']")).click();

        // Verify alert delete title is present
        String alertTitle = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@resource-id='com.google.android.contacts:id/alertTitle']")).getText();
        assert alertTitle.equals("Delete contact?") : "Delete alert title does not match.";

        // Confirm delete
        driver.findElement(AppiumBy.xpath("//android.widget.Button[@text='Delete']")).click();

        // Wait for the Toast delete message
        WebDriverWait waitToastDelete = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement toastDeleteMessage = waitToastDelete.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//android.widget.Toast[1]")));
        String toastDeleteText = toastDeleteMessage.getText();
        if (toastDeleteText.equals("1 contact deleted")) {
            System.out.println("Contact has been deleted.");
        } else {
            assert false : "Contact was not deleted.";
        }
        // Verify contact is deleted
        driver.findElement(AppiumBy.xpath("//android.widget.ImageButton[@content-desc='Search']")).click();
        boolean isContactPresent = driver.findElements(AppiumBy.xpath("//android.widget.TextView[@text='" + firstName + " " + lastName + "']")).size() > 0;
        assert !isContactPresent : "Contact is still present.";
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
