/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import org.sonar.api.batch.fs.IndexedFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import com.google.common.io.Files;

/**
 *
 * @author jocs
 */
public class CxxFileTesterHelper {
  static String DEFAULT_INPUT_MODULE_NAME = "projectKey";
  
  public static CxxFileTester CreateCxxFileTester(String fileName, String basePath, String module) throws UnsupportedEncodingException, IOException {
    CxxFileTester tester = new CxxFileTester();
    tester.baseDir = Paths.get(basePath);
    tester.sensorContext = SensorContextTester.create(tester.baseDir);

    tester.sensorContext.fileSystem().add(TestInputFileBuilder.create(module, fileName).setModuleBaseDir(tester.baseDir).build());
    tester.cxxFile = tester.sensorContext.fileSystem().inputFile(tester.sensorContext.fileSystem().predicates().hasPath(fileName));

    return tester;
  }

  public static CxxFileTester CreateCxxFileTester(String fileName, String baseDir) throws UnsupportedEncodingException, IOException {
    return CreateCxxFileTester(fileName, baseDir, DEFAULT_INPUT_MODULE_NAME);
  }

  public static void AddFileToContext(CxxFileTester tester, String fileName, String module) throws UnsupportedEncodingException, IOException {
    tester.sensorContext.fileSystem().add(TestInputFileBuilder.create(module, fileName).setModuleBaseDir(tester.baseDir).build());
    tester.cxxFile = tester.sensorContext.fileSystem().inputFile(tester.sensorContext.fileSystem().predicates().hasPath(fileName));
  }
  
  public static void AddFileToContext(CxxFileTester tester, String fileName) throws UnsupportedEncodingException, IOException {
    AddFileToContext(tester, fileName, DEFAULT_INPUT_MODULE_NAME);
  }

  public static CxxLanguage mockCxxLanguage() {
    CxxLanguage language = Mockito.mock(CxxLanguage.class);
    when(language.getKey()).thenReturn("c++");
    when(language.getName()).thenReturn("c++");
    when(language.getPropertiesKey()).thenReturn("cxx");
    when(language.IsRecoveryEnabled()).thenReturn(Optional.of(Boolean.TRUE));
    when(language.getFileSuffixes())
      .thenReturn(new String[]{".cpp", ".hpp", ".h", ".cxx", ".c", ".cc", ".hxx", ".hh"});
    when(language.getHeaderFileSuffixes()).thenReturn(new String[]{".hpp", ".h", ".hxx", ".hh"});

    return language;
  }


public static IndexedFile InputFile(CxxFileTester tester, String fileName, String baseDir) {
  return tester.cxxFile = tester.sensorContext.fileSystem().inputFile(tester.sensorContext.fileSystem().predicates().hasPath(fileName));
}

  public static String content(String fileName, String baseDir) {
    try {
      return Files.toString(new File(baseDir+"/"+fileName), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read " + baseDir+"/"+fileName, e);
    }
  }

}
