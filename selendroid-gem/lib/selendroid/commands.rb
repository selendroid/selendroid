def log(message)
  $stdout.puts "#{Time.now.strftime("%Y-%m-%d %H:%M:%S")} - #{message}".green
end

def adb_command
   if is_windows?
     "\"#{ENV["ANDROID_HOME"]}/platform-tools/adb.exe\""
   else
     "\"#{ENV["ANDROID_HOME"]}/platform-tools/adb\""
   end
end

def is_windows?
  (RbConfig::CONFIG['host_os'] =~ /mswin|mingw|cygwin/)
end

def aapt_command
  aapt_base_path = "\"#{ENV["ANDROID_HOME"]}/platform-tools\""
  current_path=nil
  if File.exist? File.dirname(aapt_base_path)
    current_path=aapt_base_path
  else
    current_path="\"#{ENV["ANDROID_HOME"]}/build-tools/17.0.0\""
  end    
  if is_windows?
    return "\"#{current_path}/aapt.exe\""
  else
    return "\"#{current_path}/aapt\""
  end
end

def java_command
  if is_windows?
    "\"#{ENV["JAVA_HOME"]}/bin/java.exe\""
  else
    "\"#{ENV["JAVA_HOME"]}/bin/java\""
  end
end

