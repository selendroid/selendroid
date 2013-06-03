require "selendroid/version"
require "selendroid/commands"
require 'zip/zip'
require 'tempfile'
require 'colorize'

def get_app_base_package(app)
  default_cmd = "#{aapt_command} dump badging #{app} "
  windows_cmd = "#{default_cmd} | findSTR package "
  x_cmd = "#{default_cmd} | grep package "
  if is_windows?
    manifest_package = %x[#{windows_cmd} ]
  else
    manifest_package = %x[ #{x_cmd} ]
  end    

  manifest_package.match(/name=['"]([^'"]+)['"]/)[1]
end

def get_app_main_activity(app)
  default_cmd = "#{aapt_command} dump badging #{app} "
  windows_cmd = "#{default_cmd} | findSTR launchable-activity "
  x_cmd = "#{default_cmd} | grep launchable-activity "
  if is_windows?
    manifest_activity = %x[ #{windows_cmd}]
  else
    manifest_activity = %x[ #{x_cmd}]
  end    

  manifest_activity.match(/name=['"]([^'"]+)['"]/)[1]
end  

def is_app_installed_on_device(app_base_package, device_arg)
  cmd = "#{adb_command} -s #{device_arg} shell pm list packages #{app_base_package}"
  package_name = %x[ #{cmd}]
  if package_name.nil? || package_name.empty?
    return false
  else  
    return true
  end  
end

def get_first_android_device
  windows_cmd = "#{adb_command} devices | more +1"
  x_cmd = "#{adb_command} devices |  head -2 | tail -1 "

  if is_windows?
     device = %x[ #{windows_cmd}]
  else
     device = %x[ #{x_cmd}]    
  end
  message_no_device = "No running Android device was found. Is the device plugged in or the emulator started?"
  if device.nil? || device.empty?
    raise message_no_device
  end  
  
  device = device.split[0]
  if device.nil? || device.empty?
    raise message_no_device
  end  

  device.tr("\n","")
end

def is_windows?
  (RbConfig::CONFIG['host_os'] =~ /mswin|mingw|cygwin/)
end