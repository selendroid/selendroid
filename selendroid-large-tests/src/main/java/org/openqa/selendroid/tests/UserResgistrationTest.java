/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.tests;

import junit.framework.Assert;

import org.junit.Test;
import org.openqa.selendroid.tests.domain.PreferedProgrammingLanguage;
import org.openqa.selendroid.tests.domain.UserDO;
import org.openqa.selendroid.tests.internal.BaseAndroidTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class UserResgistrationTest extends BaseAndroidTest {
  @Test
  public void assertUserAccountCanRegistered() throws Exception {
    UserDO user =
        new UserDO("u$erNAme", "me@myserver.com", "mySecret", "Dominik Dary",
            PreferedProgrammingLanguage.JAVA_SCRIPT);
    registerUser(user);
    verifyUser(user);
  }

  private void registerUser(UserDO user) throws Exception {
    WebElement button = driver.findElement(By.id("startUserRegistration"));
    button.click();
    WebDriverWait wait = new WebDriverWait(driver, 5);
    WebElement inputUsername =
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inputUsername")));

    inputUsername.sendKeys(user.getUsername());
    driver.findElement(By.id("inputEmail")).sendKeys(user.getEmail());
    driver.findElement(By.id("inputPassword")).sendKeys(user.getPassword());
    driver.findElement(By.id("inputName")).sendKeys(user.getName());
    driver.findElement(By.id("input_preferedProgrammingLanguage")).click();
    driver.findElement(By.linkText(user.getProgrammingLanguage().getValue())).click();
    driver.findElement(By.id("input_adds")).click();
    //takeScreenShoot();
    driver.findElement(By.id("btnRegisterUser")).click();
  }

  private void verifyUser(UserDO user) throws Exception {
    WebDriverWait wait = new WebDriverWait(driver, 5);
    WebElement inputUsername =
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("label_username_data")));

    Assert.assertEquals(inputUsername.getText(), user.getUsername());
    Assert.assertEquals(driver.findElement(By.id("label_email_data")).getText(), user.getEmail());
    Assert.assertEquals(driver.findElement(By.id("label_password_data")).getText(),
        user.getPassword());
    Assert.assertEquals(driver.findElement(By.id("label_name_data")).getText(), user.getName());
    Assert.assertEquals(driver.findElement(By.id("label_preferedProgrammingLanguage_data"))
        .getText(), user.getProgrammingLanguage().getValue());
    Assert.assertEquals(driver.findElement(By.id("label_acceptAdds_data")).getText(), "true");

    //takeScreenShoot();
    // driver.getPageSource();
    driver.findElement(By.id("buttonRegisterUser")).click();
  }
}
