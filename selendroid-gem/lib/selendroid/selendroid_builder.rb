require "selendroid/version"
require "selendroid/commands"
require 'zip/zip'
require 'tempfile'
require 'colorize'

def rebuild_selendroid(app)
    aut_base_package = get_app_base_package(app)
    log "Building selendroid-server for package #{aut_base_package}".green
    
    test_server_file_name = selendroid_server_path(aut_base_package)
    FileUtils.mkdir_p File.dirname(test_server_file_name) unless File.exist? File.dirname(test_server_file_name)

    unsigned_test_apk = File.join(File.dirname(__FILE__), '..','..', 'selendroid-prebuild/selendroid-server.apk')
    android_platform = Dir["#{ENV["ANDROID_HOME"].gsub("\\", "/")}/platforms/android-*"].last
    raise "No Android SDK found in #{ENV["ANDROID_HOME"].gsub("\\", "/")}/platforms/" unless android_platform
    Dir.mktmpdir do |workspace_dir|
      Dir.chdir(workspace_dir) do
        FileUtils.cp(unsigned_test_apk, "TestServer.apk")
        FileUtils.cp(File.join(File.dirname(__FILE__), '..','..', 'selendroid-prebuild/AndroidManifest.xml'), "AndroidManifest.xml")

        unless system %Q{"#{RbConfig.ruby}" -pi.bak -e "gsub(/org.openqa.selendroid.testapp/, '#{aut_base_package}')" AndroidManifest.xml}
          raise "Could not replace package name in manifest"
        end

        unless system %Q{"#{ENV["ANDROID_HOME"]}/platform-tools/aapt" package -M AndroidManifest.xml  -I "#{android_platform}/android.jar" -F dummy.apk}
          raise "Could not create dummy.apk"
        end

        Zip::ZipFile.new("dummy.apk").extract("AndroidManifest.xml","customAndroidManifest.xml")
        Zip::ZipFile.open("TestServer.apk") do |zip_file|
          zip_file.add("AndroidManifest.xml", "customAndroidManifest.xml")
        end
      end
      sign_apk("#{workspace_dir}/TestServer.apk", test_server_file_name)
      begin

      rescue Exception => e
        puts e
        raise "Could not sign test server"
      end
    end
    log "Done signing the test server. Moved it to #{test_server_file_name}".green
    test_server_file_name
end

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

def prepare_device(app)
  device_serial = get_first_android_device
  log "Using first Android device with serial: #{device_serial}".green
  if is_app_installed_on_device("org.openqa.selndroid",device_serial)
    uninstall_cmd = "#{adb_command} -s #{device_serial} uninstall org.openqa.selendroid"
    %x[ #{uninstall_cmd}]
  end
  selendroid_file_name = rebuild_selendroid(app)
  install_cmd = "#{adb_command} -s #{device_serial} install #{File.expand_path(selendroid_file_name)}"
  %x[ #{install_cmd}]
  log "The selendroid server has been rebuild and installed on the device.".green

  aut_base_package = get_app_base_package(app)
  aut_main_activity = get_app_main_activity(app)

  if is_app_installed_on_device(aut_base_package,device_serial)
    uninstall_cmd = "#{adb_command} -s #{device_serial} uninstall #{aut_base_package}"
    %x[ #{uninstall_cmd}]
  end
  resigned_apk = "#{app.chomp('.apk')}-debug.apk"
  resign_apk(app,resigned_apk)
  install_cmd = "#{adb_command} -s #{device_serial} install #{resigned_apk}"    
  %x[ #{install_cmd}]
  log "The app has been resigned #{File.basename(resigned_apk)} and installed on the device.".green
end

def is_windows?
  (RbConfig::CONFIG['host_os'] =~ /mswin|mingw|cygwin/)
end

def resign_apk(app_path, dest_path)
  troido_jar = File.join(File.dirname(__FILE__), '..','..', 'lib/troido/re-sign.jar')
  cmd = "#{java_command} -cp #{troido_jar} de.troido.resigner.main.Main #{File.expand_path(app_path)} #{File.expand_path(dest_path)}"
  resign_output= %x[ #{cmd}]
end

def sign_apk(app_path, dest_path)
  if !File.file?( Dir.home+"/.android/debug.keystore")
     log "keystore missing - creating one"
     cmd= "#{keytool_command} -genkey -v -keystore #{Dir.home}/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname \"CN=Android Debug,O=Android,C=US\" -storetype JKS -sigalg MD5withRSA  -keyalg RSA"
     system(cmd)
  end  

  cmd = "#{jarsigner_command} -sigalg MD5withRSA -digestalg SHA1 -signedjar #{dest_path} -storepass android -keystore #{Dir.home}/.android/debug.keystore #{app_path} androiddebugkey"
  unless system(cmd)
    raise "Could not sign app (#{app_path}"
  end
end

def selendroid_server_path(base_package)
  "selendroid-server-#{Selendroid::VERSION}.apk"
end
