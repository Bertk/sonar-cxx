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

import com.sonar.cxx.sslr.api.Rule;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;

class RuleAssertTest {

  private Rule rule;

  @BeforeEach
  public void setUp() {
    rule = new MutableParsingRule("ruleName").is("foo");
  }

  @Test
  void ok() {
    new RuleAssert(rule)
      .matches("foo")
      .notMatches("bar");
  }

  @Test
  void test_matches_failure() {
    var thrown = catchThrowableOfType(
      () -> new RuleAssert(rule).matches("bar"),
      ParsingResultComparisonFailure.class);
    assertThat(thrown).hasMessageContaining("Rule 'ruleName' should match:\nbar");
  }

  @Test
  void test_notMatches_failure() {
    var thrown = catchThrowableOfType(
      () -> new RuleAssert(rule).notMatches("foo"),
      AssertionError.class);
    assertThat(thrown).hasMessage("Rule 'ruleName' should not match:\nfoo");
  }

  @Test
  void should_not_accept_null() {
    var thrown = catchThrowableOfType(
      () -> new RuleAssert((Rule) null).matches(""),
      AssertionError.class);
    assertThat(thrown).hasMessageContaining("Expecting actual not to be null");
  }

  @Test
  void notMatches_should_not_accept_prefix_match() {
    new RuleAssert(rule)
      .notMatches("foo bar");
  }

  @Test
  void matchesPrefix_ok() {
    new RuleAssert(rule)
      .matchesPrefix("foo", " bar");
  }

  @Test
  void matchesPrefix_full_mistmatch() {
    var thrown = catchThrowableOfType(
      () -> new RuleAssert(rule).matchesPrefix("bar", " baz"),
      ParsingResultComparisonFailure.class
    );
    assertThat(thrown).hasMessageContaining("Rule 'ruleName' should match:\nbar\nwhen followed by:\n baz");
  }

  @Test
  void matchesPrefix_wrong_prefix() {
    var thrown = catchThrowableOfType(
      () -> new RuleAssert(rule).matchesPrefix("foo bar", " baz"),
      ParsingResultComparisonFailure.class
    );
    assertThat(thrown).hasMessage("Rule 'ruleName' should match:\nfoo bar\nwhen followed by:\n baz\nbut matched:\nfoo");
  }

}
