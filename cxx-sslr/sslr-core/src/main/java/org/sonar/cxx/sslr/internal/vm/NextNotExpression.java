/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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
package org.sonar.cxx.sslr.internal.vm; // cxx: in use

public class NextNotExpression implements ParsingExpression {

  private final ParsingExpression subExpression;

  public NextNotExpression(ParsingExpression subExpression) {
    this.subExpression = subExpression;
  }

  /**
   * Compiles this expression into a sequence of instructions:
   * <pre>
   * Choice L1
   * subExpression
   * FailTwice
   * L1: ...
   * </pre>
   *
   * Should be noted that can be compiled without usage of instruction "FailTwice":
   * <pre>
   * Choice L2
   * subExpression
   * Commit L1
   * L1: Fail
   * L2: ...
   * </pre>
   */
  @Override
  public Instruction[] compile(CompilationHandler compiler) {
    var sub = compiler.compile(subExpression);
    var result = new Instruction[sub.length + 2];
    result[0] = Instruction.predicateChoice(sub.length + 2);
    System.arraycopy(sub, 0, result, 1, sub.length);
    result[sub.length + 1] = Instruction.failTwice();
    return result;
  }

  @Override
  public String toString() {
    return "NextNot[" + subExpression + "]";
  }

}
