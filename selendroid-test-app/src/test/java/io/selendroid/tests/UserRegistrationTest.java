/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.tests;

import io.selendroid.support.BaseAndroidTest;
import io.selendroid.tests.domain.PreferedProgrammingLanguage;
import io.selendroid.tests.domain.UserDO;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base Test to demonstrate how to test native android apps with Selendroid.
 * 
 * @author ddary
 */
public class UserRegistrationTest extends BaseAndroidTest {
  @Test
  public void assertUserAccountCanRegistered() throws Exception {
    UserDO user =
        new UserDO("u$erNAme", "me@myserver.com", "mySecret", "Selendroid User",
            PreferedProgrammingLanguage.JAVA_SCRIPT);
    registerUser(user);
    verifyUser(user);
  }

  private void registerUser(UserDO user) throws Exception {
    WebElement button = driver().findElement(By.id("startUserRegistration"));
    
    button.click();

    WebElement username = driver().findElement(By.id("inputUsername"));

    username.sendKeys(user.getUsername());
    WebElement nameInput = driver().findElement(By.id("inputName"));
    Assert.assertEquals(nameInput.getText(), "Mr. Burns");
    nameInput.clear();
    nameInput.sendKeys(user.getName());

    driver().findElement(By.id("inputEmail")).sendKeys(user.getEmail());
    driver().findElement(By.id("inputPassword")).sendKeys(user.getPassword());
    try {
      nameInput.submit();
      Assert.fail("submit is not supported by SelendroidNativeDriver");
    } catch (WebDriverException e) {
      // expected behavior
    }

    driver().findElement(By.id("input_preferedProgrammingLanguage")).click();
    driver().findElement(By.linkText(user.getProgrammingLanguage().getValue())).click();
    WebElement acceptAddsCheckbox = driver().findElement(By.id("input_adds"));
    Assert.assertEquals(acceptAddsCheckbox.isSelected(), false);
    acceptAddsCheckbox.click();
    
    Assert.assertEquals(driver().getCurrentUrl(), "and-activity://RegisterUserActivity");
    try {
      driver().getTitle();
      Assert.fail("Get title is not supported by SelendroidNativeDriver");
    } catch (WebDriverException e) {
      // expected behavior
    }

    driver().findElement(By.id("btnRegisterUser")).click();
  }

  private void verifyUser(UserDO user) throws Exception {
    WebDriverWait wait = new WebDriverWait(driver(), 5);
    WebElement inputUsername =
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("label_username_data")));

    Assert.assertEquals(inputUsername.getText(), user.getUsername());
    Assert.assertEquals(driver().findElement(By.id("label_email_data")).getText(), user.getEmail());
    Assert.assertEquals(driver().findElement(By.id("label_password_data")).getText(),
        user.getPassword());
    Assert.assertEquals(driver().findElement(By.id("label_name_data")).getText(), user.getName());
    Assert.assertEquals(driver().findElement(By.id("label_preferedProgrammingLanguage_data"))
        .getText(), user.getProgrammingLanguage().getValue());
    Assert.assertEquals(driver().findElement(By.id("label_acceptAdds_data")).getText(), "true");

    
    driver().findElement(By.id("buttonRegisterUser")).click();
  }
}
