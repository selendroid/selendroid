require 'rubygems'
require "test/unit"
require 'selenium-webdriver'
require 'json'

class ExampleTest < Test::Unit::TestCase
  def test_simple_webview
    text='Start Webview'
    button = @driver.find_element(:link_text, text)
    button.click()
    
    wait = Selenium::WebDriver::Wait.new(:timeout => 10) # seconds
    wait.until { @driver.find_element(:link_text => 'Go to home screen') }
    @driver.switch_to.window('WEBVIEW')
    inputField = @driver.find_element(:id, 'name_input')
    inputField.clear();
    inputField.sendKeys("Dominik");
    @driver.save_screenshot("./target/webviewScreen01.png")
    inputField.submit();
    @driver.save_screenshot("./target/webviewScreen02.png")
  end

  def setup
    caps = Selenium::WebDriver::Remote::Capabilities.android
    caps.version = "5"
    caps.platform = :linux
    caps.proxy = nil
    caps[:aut] = "io.selendroid.testapp:0.5.0-SNAPSHOT"
    caps[:locale]="de_DE"
    caps[:browserName]="selendroid"
    

    @driver = Selenium::WebDriver.for(
    :remote,
    :url => "http://localhost:5555/wd/hub",
    :desired_capabilities => caps)
  end
end

