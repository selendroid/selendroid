require 'rubygems'
require "test/unit"
require 'selenium-webdriver'
require 'json'

class ExampleTest < Test::Unit::TestCase
  def test_simple_native
    text='Start Webview'
    button = @driver.find_element(:link_text, text)
    puts @driver.session_id
  end

  def setup
    caps = Selenium::WebDriver::Remote::Capabilities.android
    caps.version = "5"
    caps.platform = :linux
    caps.proxy = nil
    caps[:aut] = "selendroid-test-app"
    caps[:locale]="de_DE"
    caps[:deviceName]="emulator"
    caps[:deviceId]="emulator-5554"
    caps[:maxInstances]="1"
    caps[:browserName]="native-android-driver"
    caps[:sdkVersion]="4.1"

    @driver = Selenium::WebDriver.for(
    :remote,
    :url => "http://localhost:8080/wd/hub",
    :desired_capabilities => caps)
  end
end
