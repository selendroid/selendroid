/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.standalone.android;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;

import io.selendroid.standalone.android.AndroidSdk;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AndroidSdkTest {
  @Test
  public void testGetLatestPlatformFolder() {
    File mockPlatformsFolder = mock(File.class);
    when(
      mockPlatformsFolder.listFiles(any(FileFilter.class))
    ).thenReturn(new File[] {
        new File("/", "android-9"),
        new File("/", "android-21"),
        new File("/", "android-22"),
        new File("/", "android-13")
      });

    Assert.assertEquals(
      "Find latest platform folder should be android-22",
      AndroidSdk.findLatestAndroidPlatformFolder(mockPlatformsFolder).getAbsolutePath(),
      new File("/", "android-22").getAbsolutePath());
  }

  @Test
  public void testGetLatestBuildToolsFolder() {
    File mockBuildToolsHome = mock(File.class);
    when(
      mockBuildToolsHome.listFiles(any(FileFilter.class))
    ).thenReturn(new File[] {
        new File("/", "22.0.0"),
        new File("/", "22.0.3"),
        new File("/", "22.0.1"),
        new File("/", "25.0.1")
      });

    Assert.assertEquals(
      "Find latest build-tools folder should return 25.0.1",
      AndroidSdk.findLatestBuildToolsFolder(mockBuildToolsHome).getAbsolutePath(),
      new File("/", "25.0.1").getAbsolutePath());
  }
}
