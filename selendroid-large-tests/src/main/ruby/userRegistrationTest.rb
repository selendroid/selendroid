require 'rubygems'
require "test/unit"
require 'selenium-webdriver'
require 'json'

class ExampleTest < Test::Unit::TestCase
  def test_registration_flow
    #goto user registration
    @driver.find_element(:id, 'startUserRegistration').click
    #fill form
    username='u$erNAme'
    email='me@myserver.com'
    password='mySecret'
    name= 'Dominik Dary'
    prefered_programming_language ='Javascript'
    #wait = Selenium::WebDriver::Wait.new(:timeout => 10) # seconds
    #wait.until { @driver.find_element(:id => "inputUsername") }
    sleep(2)
    username_elemet=@driver.find_element(:id, 'inputUsername')
    username_elemet.send_keys(username)
    email_element=@driver.find_element(:id, 'inputEmail')
    email_element.send_keys(email)
    password_element=@driver.find_element(:id, 'inputPassword')
    password_element.send_keys(password)
    name_element=@driver.find_element(:id, 'inputName')
    name_element.send_keys(name)
    #Select spinner
    spinner_element=@driver.find_element(:id, 'input_preferedProgrammingLanguage')
    spinner_element.click
    java=@driver.find_element(:link_text, prefered_programming_language)
    java.click
    #select checkbox
    adds=@driver.find_element(:id, 'input_adds')
    adds.click
    #register user
    @driver.save_screenshot("./target/nativeScreen01.png")
    @driver.find_element(:id, 'btnRegisterUser').click

    sleep(2)
    #wait.until { driver.find_element(:id => "label_username_data") }
    #Verify data
    assert username == @driver.find_element(:id, 'label_username_data').text
    assert email == @driver.find_element(:id, 'label_email_data').text
    assert password == @driver.find_element(:id, 'label_password_data').text
    assert name == @driver.find_element(:id, 'label_name_data').text
    assert prefered_programming_language == @driver.find_element(:id,
     'label_preferedProgrammingLanguage_data').text
    assert "true" == @driver.find_element(:id, 'label_acceptAdds_data').text

    @driver.save_screenshot("./target/nativeScreen02.png")
    puts "The source of current screen:"
    page_source = JSON.pretty_generate( @driver.page_source)
    write_to_file( page_source )
    #goto main screen
    @driver.find_element(:id, 'buttonRegisterUser').click
  end

  def setup
    caps = Selenium::WebDriver::Remote::Capabilities.android
    caps.version = "5"
    caps.platform = :linux
    caps.proxy = nil
    caps[:maxInstances]="1"
    
    client = Selenium::WebDriver::Remote::Http::Default.new
    client.timeout = 120 # seconds
    
    @driver = Selenium::WebDriver.for(
    :remote, :http_client => client, 
    :url => "http://localhost:8090/wd/hub",
    :desired_capabilities => caps)
  end

  def write_to_file( myStr )
    aFile = File.new("reviewDialog.json", "w")
    aFile.write(myStr)
    aFile.close
  end  

  def teardown
    @driver.quit
  end
end
