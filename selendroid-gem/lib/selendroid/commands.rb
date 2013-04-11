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
  if is_windows?
     "\"#{ENV["ANDROID_HOME"]}/platform-tools/aapt.exe\""
  else
    "\"#{ENV["ANDROID_HOME"]}/platform-tools/aapt\""
  end
end

def java_command
  if is_windows?
    "\"#{ENV["JAVA_HOME"]}/bin/java.exe\""
  else
    "\"#{ENV["JAVA_HOME"]}/bin/java\""
  end
end

def jarsigner_command
  if is_windows?
    "\"#{ENV["JAVA_HOME"]}/bin/jarsigner.exe\""
  else
    "\"#{ENV["JAVA_HOME"]}/bin/jarsigner\""
  end
end  

def keytool_command
  if is_windows?
    "\"#{ENV["JAVA_HOME"]}/bin/keytool.exe\""
  else
    "\"#{ENV["JAVA_HOME"]}/bin/keytool\""
  end
end