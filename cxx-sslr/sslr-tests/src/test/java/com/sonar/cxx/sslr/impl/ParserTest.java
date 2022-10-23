/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package com.sonar.cxx.sslr.impl;

import static com.sonar.cxx.sslr.api.GenericTokenType.EOF;
import com.sonar.cxx.sslr.api.RecognitionException;
import static com.sonar.cxx.sslr.test.minic.MiniCParser.parseFile;
import static com.sonar.cxx.sslr.test.minic.MiniCParser.parseString;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ParserTest {

  @Test
  void lexerErrorStringWrappedInRecognitionException() {
    var thrown = catchThrowableOfType(() -> {
      parseString(".");
    }, RecognitionException.class);
    assertThat(thrown).isExactlyInstanceOf(RecognitionException.class);
  }

  @Test
  void lexerErrorFileWrappedInRecognitionException() {
    var thrown = catchThrowableOfType(() -> {
      parseFile("/OwnExamples/lexererror.mc");
    }, RecognitionException.class);
    assertThat(thrown).isExactlyInstanceOf(RecognitionException.class);
  }

  @Test
  void parse() {
    var compilationUnit = parseString("");
    assertThat(compilationUnit.getNumberOfChildren()).isEqualTo(1);
    assertThat(compilationUnit.getFirstChild().is(EOF)).isTrue();
  }

}
