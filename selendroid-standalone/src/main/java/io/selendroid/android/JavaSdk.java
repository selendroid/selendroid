/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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
package io.selendroid.android;

import static io.selendroid.android.AndroidSdk.platformExecutableSuffixExe;
import static org.openqa.selenium.Platform.MAC;

import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;
import org.openqa.selenium.Platform;
import org.openqa.selenium.os.CommandLine;

import java.io.File;
import java.util.Arrays;

public class JavaSdk {
  public static String javaHome = null;

  public static String javaHome() {
    if (javaHome == null) {
     // Sniff JAVA_HOME first
        javaHome = System.getenv("JAVA_HOME");

      // If that's not present, and we're on a Mac...
      if (javaHome == null && Platform.getCurrent() == MAC) {
        try {
          javaHome = ShellCommand.exec(Arrays.asList("/usr/libexec/java_home"));
          if (javaHome != null) {
            javaHome = javaHome.replaceAll("\\r|\\n", "");
          }
       
        } catch (ShellCommandException e) {
        }     
      }
      // Finally, check java.home, though this may point to a JRE.
      if (javaHome == null) {
    	  javaHome = System.getProperty("java.home");
      }
    }
    return javaHome;
  }

  public static File jarsigner() {
    StringBuffer jarsigner = new StringBuffer();
    jarsigner.append(javaHome());
    jarsigner.append(File.separator);
    jarsigner.append("bin");
    jarsigner.append(File.separator);

    return new File(jarsigner.toString(), "jarsigner" + platformExecutableSuffixExe());
  }

  public static File keytool() {
    StringBuffer keytool = new StringBuffer();
    keytool.append(javaHome());
    keytool.append(File.separator);
    keytool.append("bin");
    keytool.append(File.separator);

    return new File(keytool.toString(), "keytool" + platformExecutableSuffixExe());
  }
}
