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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.sonar.cxx.parser.CxxParser;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.impl.Parser;


public class TestCxxVisitorRunner {
  public static void scanFile(File file, CxxVisitor... visitors) {
    CxxVisitorContext context = createContext(file);
    for (CxxVisitor visitor : visitors) {
      visitor.scanFile(context);
    }
  }

  public static CxxVisitorContext createContext(File file) {
    Parser<Grammar> parser = CxxParser.create(CxxFileTesterHelper.mockCxxLanguage());
    TestCxxFile cxxFile = new TestCxxFile(file);
    AstNode rootTree = parser.parse(cxxFile.content());
    return new CxxVisitorContext(rootTree, cxxFile);
  }

  private static class TestCxxFile implements CxxFile {

    private final File file;

    public TestCxxFile(File file) {
      this.file = file;
    }

    @Override
    public String content() {
      try {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new IllegalStateException("Cannot read " + file, e);
      }
    }

    @Override
    public String fileName() {
      return file.getName();
    }

    @Override
    public String filePath() {
      return file.getPath();
    }

    @Override
    public String absolutePath() {
      return file.getAbsolutePath();
    }

  }

}
