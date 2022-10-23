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
package org.sonar.cxx.sslr.tests;

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Rule;
import com.sonar.cxx.sslr.impl.Lexer;
import com.sonar.cxx.sslr.impl.Parser;
import com.sonar.cxx.sslr.impl.channel.BlackHoleChannel;
import com.sonar.cxx.sslr.impl.channel.RegexpChannel;
import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParserAssertTest {

  private Rule rule;
  private Parser parser;

  @BeforeEach
  public void setUp() {
    var lexer = Lexer.builder()
      .withFailIfNoChannelToConsumeOneCharacter(true)
      .withChannel(new RegexpChannel(GenericTokenType.IDENTIFIER, "[a-z]++"))
      .withChannel(new BlackHoleChannel(" "))
      .build();
    rule = new RuleDefinition("ruleName").is("foo");
    Grammar grammar = new Grammar() {
      @Override
      public Rule getRootRule() {
        return rule;
      }
    };
    parser = Parser.builder(grammar).withLexer(lexer).build();
  }

  @Test
  void ok() {
    new ParserAssert(parser)
      .matches("foo")
      .notMatches("bar")
      .notMatches("foo foo");
  }

  @Test
  void test_matches_failure() {
    var thrown = catchThrowableOfType(
      () -> new ParserAssert(parser).matches("bar"),
      ParsingResultComparisonFailure.class
    );
    assertThat(thrown).hasMessageContaining("Rule 'ruleName' should match:\nbar");
  }

  @Test
  void test2_matches_failure() {
    var thrown = catchThrowableOfType(
      () -> new ParserAssert(parser).matches("foo bar"),
      ParsingResultComparisonFailure.class);
    assertThat(thrown).hasMessageContaining("Rule 'ruleName' should match:\nfoo bar");
  }

  @Test
  void test_notMatches_failure() {
    var thrown = catchThrowableOfType(
      () -> new ParserAssert(parser).notMatches("foo"),
      AssertionError.class);
    assertThat(thrown.getMessage()).isEqualTo("Rule 'ruleName' should not match:\nfoo");
  }

  @Test
  void test_notMatches_failure2() {
    rule.override("foo", GenericTokenType.EOF);
    var thrown = catchThrowableOfType(
      () -> new ParserAssert(parser).notMatches("foo"),
      AssertionError.class);
    assertThat(thrown).hasMessage("Rule 'ruleName' should not match:\nfoo");
  }

  @Test
  void should_not_accept_null() {
    var thrown = catchThrowableOfType(
      () -> new ParserAssert((Parser) null).matches(""),
      AssertionError.class);
    assertThat(thrown).hasMessageContaining("Expecting actual not to be null");
  }

  @Test
  void should_not_accept_null_root_rule() {
    parser.setRootRule(null);
    var thrown = catchThrowableOfType(
      () -> new ParserAssert(parser).matches(""),
      AssertionError.class);
    assertThat(thrown).hasMessage("Root rule of the parser should not be null");
  }

  @Test
  void test_lexer_failure() {
    var thrown = catchThrowableOfType(
      () -> new ParserAssert(parser).matches("_"),
      ParsingResultComparisonFailure.class);
    var expectedMessage = new StringBuilder()
      .append("Rule 'ruleName' should match:\n")
      .append("_\n")
      .append("Lexer error: Unable to lex")
      .toString();
    assertThat(thrown).hasMessageContaining(expectedMessage);
  }

}
