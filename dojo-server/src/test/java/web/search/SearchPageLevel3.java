package web.search;


import org.automation.dojo.web.bugs.NullBug;
import org.automation.dojo.web.scenario.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.automation.dojo.web.model.ShopService.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@ContextConfiguration(locations = {"classpath:/org/automation/dojo/applicationContext.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class SearchPageLevel3 extends SearchPageLevel2 {

    @Override
    protected int getMajorRelease() {
        return 2;
    }

    @Override
    protected void resetAllElements() {
        super.resetAllElements();
    }

    private WebElement getAddToCartButton() {
        return tester.findElement(By.id("add_to_cart_button"));
    }

    @Override
    protected List<?> getMinorRelease() {
        return Arrays.asList(SearchByTextScenario.class, NullBug.class,
                SearchByPriceScenario.class, NullBug.class,
                PriceSortingAscDescScenario.class, NullBug.class,
                AddToUserCartScenario.class, NullBug.class,
                ShowUserCartScenario.class, NullBug.class);
    }

    @Override
    @Test
    public void shouldSearchPageAsWelcomePage() {
        super.shouldSearchPageAsWelcomePage();

        assertAddToCartFormNotPresent();
    }

    @Test
    public void shouldCartFormPresentIfSomeFound() {
        getListFor("mouse");

        assertSearchForm();
        assertAddToCartForm();
    }

    private void assertAddToCartForm() {
        assertNotNull(getAddToCartButton());
    }

    private void assertAddToCartFormNotPresent() {
        try {
            getAddToCartButton();
            fail("Expected exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void shouldAddToCartSelectedRecord(){
        getListFor("mouse");

        assertPageContain("List: Code Description Price " +
                "1 'Mouse 1' 30.0$ " +
                "3 'Mouse 3' 40.0$ " +
                "2 'Mouse 2' 50.0$ " +
                "4 'Mouse 4 - the best mouse!' 66.0$ ");

        List<WebElement> checkboxes = getSelectCheckboxes();
        assertEquals(4, checkboxes.size());

        checkboxes.get(0).click();
        checkboxes.get(2).click();
        checkboxes.get(3).click();

        submitAddToCartForm();
        assertCartPage();
        assertPageContain("1 'Mouse 1' 30.0$");
        assertPageNotContain("3 'Mouse 3' 40.0$");
        assertPageContain("2 'Mouse 2' 50.0$");
        assertPageContain("4 'Mouse 4 - the best mouse!' 66.0$");
    }

    private void submitAddToCartForm() {
        getAddToCartButton().click();
        resetAllElements();
    }

    @Test
    public void shouldSaveSearchFormStateWhenGoToCart() {
        enterText("mouse");
        enterPrice(LESS_THAN, 30);
        submitSearchForm();

        getSelectCheckboxes().get(0).click();
        submitAddToCartForm();

        assertFormContains("mouse", LESS_THAN, 30);
    }

    private void gotoCart() {
        enterText("");
        submitSearchForm();

        getSelectCheckboxes().get(0).click();
        submitAddToCartForm();
    }

    @Test
    public void shouldSaveSearchFormStateWhenGoFromCart() {
        gotoCart();

        enterText("mouse");
        enterPrice(LESS_THAN, 30);
        submitSearchForm();

        assertSortingOrder(ASC);
        assertFormContains("mouse", LESS_THAN, 30);
    }

    @Test
    public void shouldSaveSearchFormPriceSortingOrder() {
        getListFor("mouse");
        selectSortingOrder(DESC);
        submitSearchForm();

        getSelectCheckboxes().get(0).click();
        submitAddToCartForm();

        submitSearchForm();
        assertSortingOrder(DESC);
    }

    @Test
    public void shouldSearchAtShoppingCartWorksCorrectly(){
        gotoCart();

        enterText("mo");
        enterPrice(MORE_THAN, 120);
        submitSearchForm();

        assertPageContain("List: Code Description Price " +
            "6 'Monitor 2' 120.0$ " +
            "5 'Monitor 1' 150.0$ " +
            "7 'Monitor 3 - the best monitor!' 190.0$");
    }

    @Test
    public void shouldSaveShoppingCart(){
        getListFor("mouse");

        assertPageContain("List: Code Description Price " +
                "1 'Mouse 1' 30.0$ " +
                "3 'Mouse 3' 40.0$ " +
                "2 'Mouse 2' 50.0$ " +
                "4 'Mouse 4 - the best mouse!' 66.0$ ");

        List<WebElement> checkboxes = getSelectCheckboxes();
        assertEquals(4, checkboxes.size());

        checkboxes.get(0).click();
        checkboxes.get(3).click();

        submitAddToCartForm();
        assertCartPage();

        enterText("monitor");
        submitSearchForm();

        assertPageContain("List: Code Description Price " +
                "6 'Monitor 2' 120.0$ " +
                "5 'Monitor 1' 150.0$ " +
                "7 'Monitor 3 - the best monitor!' 190.0$");
        checkboxes = getSelectCheckboxes();
        assertEquals(3, checkboxes.size());

        checkboxes.get(0).click();
        checkboxes.get(2).click();

        submitAddToCartForm();
        assertCartPage();

        assertPageContain("1 'Mouse 1' 30.0$");
        assertPageNotContain("3 'Mouse 3' 40.0$");
        assertPageNotContain("2 'Mouse 2' 50.0$");
        assertPageContain("4 'Mouse 4 - the best mouse!' 66.0$");
        assertPageContain("6 'Monitor 2' 120.0$ ");
        assertPageNotContain("5 'Monitor 1' 150.0$ ");
        assertPageContain("7 'Monitor 3 - the best monitor!' 190.0$");

    }


    private void assertCartPage() {
        assertPageContain("Your cart list:");
    }

    private void getListFor(String text) {
        enterText("");
        enterText(text);
        submitSearchForm();
    }


    public List<WebElement> getSelectCheckboxes() {
        return tester.findElements(By.xpath("//input[contains(@id,'record')]"));
    }
}