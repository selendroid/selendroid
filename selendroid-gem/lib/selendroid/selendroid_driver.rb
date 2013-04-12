require "selendroid/commands"
require 'selenium-webdriver'
require 'json'
require 'colorize'
require 'retriable'
require "net/http"
require 'uri'

include Selenium::WebDriver::DriverExtensions::HasInputDevices
include Selenium::WebDriver::DriverExtensions::HasTouchScreen
include Selenium::WebDriver::DriverExtensions::HasTouchScreen

def start_selendroid_client
  caps = Selenium::WebDriver::Remote::Capabilities.android
  caps.version = "5"
  caps.platform = :linux
  caps.proxy = nil
  caps[:aut] = "selendroid-test-app"
  caps[:locale]="de_DE"
  caps[:deviceName]="emulator"
  caps[:deviceId]="emulator-5554"
  caps[:maxInstances]="1"
  caps[:browserName]="selendroid"
  caps[:sdkVersion]="4.1"
  end_point = "http://localhost:#{ENV["SELENDROID_SERVER_PORT"]}/wd/hub"
  #log "using url: #{end_point}" 
  
  $selendroid_driver = Selenium::WebDriver.for(
  :remote,
  :url => end_point,
  :desired_capabilities => caps)
  
  $selendroid_driver
end

def driver
  $selendroid_driver
end

def quit
  quit_driver
  exit
end

def quit_driver
  log "Quiting Selendroid client".green
  begin; $selendroid_driver.quit unless $selendroid_driver.nil?; rescue; end
end

def get_window_source
  JSON.pretty_generate( $selendroid_driver.page_source)
end

def inspector
  link = "http://localhost:#{ENV["SELENDROID_SERVER_PORT"]}/inspector"
  if RbConfig::CONFIG['host_os'] =~ /mswin|mingw|cygwin/ then
    system("start #{link}")
  elsif RbConfig::CONFIG['host_os'] =~ /darwin/ then
    system("open #{link}")
  elsif RbConfig::CONFIG['host_os'] =~ /linux/ then
    system("xdg-open #{link}")
  end
end

def helpme
  puts "Client-driver is available under: driver".green
  puts "Official Webdriver Ruby bindins wiki: http://goo.gl/nJ9CJ".green
  puts "To open the selendroid-inspector type: inspector".green
  
end

def start_selendroid_server(activity)
  main_activity = nil
  if activity.nil?
    main_activity = ENV["MAIN_ACTIVITY"]
  else
    main_activity =activity
  end
  log "Starting selendroid-server with main activity: #{main_activity}"    
  selendroid_server_start_cmd = "#{adb_command} shell am instrument -e main_activity '#{main_activity}' org.openqa.selendroid/.ServerInstrumentation"
  system(selendroid_server_start_cmd)
  unless ENV["SELENDROID_SERVER_PORT"]
    ENV["SELENDROID_SERVER_PORT"] = "8080"
  end
  forward_cmd = "#{adb_command} forward tcp:#{ENV["SELENDROID_SERVER_PORT"]} tcp:8080"
  system(forward_cmd)
end

def wait_for_selendroid_server
  unless ENV["SELENDROID_SERVER_PORT"]
    ENV["SELENDROID_SERVER_PORT"] = "8080"
  end
  retriable :tries => 10, :interval => 3 do
    url = URI.parse("http://localhost:#{ENV["SELENDROID_SERVER_PORT"]}/wd/hub/status")
    the_request = Net::HTTP::Get.new("#{url.path}")

    the_response = Net::HTTP.start(url.host, url.port) { |http|
    http.request(the_request)
    }

    raise "Response was not 200, response was #{the_response.code}" if the_response.code != "200"
    log "Selendroid server is started.".green
  end

end
  
  
