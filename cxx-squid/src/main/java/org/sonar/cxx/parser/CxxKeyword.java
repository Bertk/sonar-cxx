/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.parser;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.TokenType;
import javax.annotation.Nullable;

/**
 * C++ Standard, Section 2.12 "Keywords"
 *
 * In this list are only C++ keywords allowed.
 * Identifiers with special meaning when appearing in a certain context: final, import, module, override (not in list).
 * All other extensions must be handled as identifiers.
 */
public enum CxxKeyword implements TokenType {

  ALIGNAS("alignas"),
  ALIGNOF("alignof"),
  ASM("asm"),
  AUTO("auto"),
  BOOL("bool"),
  BREAK("break"),
  CASE("case"),
  CATCH("catch"),
  CHAR("char"),
  CHAR8_T("char8_t"),
  CHAR16_T("char16_t"),
  CHAR32_T("char32_t"),
  CLASS("class"),
  CONST("const"),
  CONCEPT("concept"),
  CONSTEVAL("consteval"),
  CONSTEXPR("constexpr"),
  CONSTINIT("constinit"),
  CONST_CAST("const_cast"),
  CONTINUE("continue"),
  CO_AWAIT("co_await"),
  CO_RETURN("co_return"),
  CO_YIELD("co_yield"),
  DECLTYPE("decltype"),
  DEFAULT("default"),
  DELETE("delete"),
  DO("do"),
  DOUBLE("double"),
  DYNAMIC_CAST("dynamic_cast"),
  ELSE("else"),
  ENUM("enum"),
  EXPLICIT("explicit"),
  EXTERN("extern"),
  FALSE("false"),
  FLOAT("float"),
  FOR("for"),
  FRIEND("friend"),
  GOTO("goto"),
  IF("if"),
  INLINE("inline"),
  INT("int"),
  LONG("long"),
  MUTABLE("mutable"),
  NAMESPACE("namespace"),
  NEW("new"),
  NOEXCEPT("noexcept"),
  NULLPTR("nullptr"),
  OPERATOR("operator"),
  PRIVATE("private"),
  PROTECTED("protected"),
  PUBLIC("public"),
  REGISTER("register"),
  REINTERPRET_CAST("reinterpret_cast"),
  RETURN("return"),
  REQUIRES("requires"),
  SHORT("short"),
  SIGNED("signed"),
  SIZEOF("sizeof"),
  STATIC("static"),
  STATIC_ASSERT("static_assert"),
  STATIC_CAST("static_cast"),
  STRUCT("struct"),
  SWITCH("switch"),
  TEMPLATE("template"),
  THIS("this"),
  THREAD_LOCAL("thread_local"),
  THROW("throw"),
  TRUE("true"),
  TRY("try"),
  TYPEDEF("typedef"),
  TYPENAME("typename"),
  UNION("union"),
  UNSIGNED("unsigned"),
  USING("using"),
  VIRTUAL("virtual"),
  VOID("void"),
  VOLATILE("volatile"),
  WCHAR_T("wchar_t"),
  WHILE("while"),
  // Operators
  AND("and"),
  AND_EQ("and_eq"),
  BITAND("bitand"),
  BITOR("bitor"),
  COMPL("compl"),
  NOT("not"),
  NOT_EQ("not_eq"),
  OR("or"),
  OR_EQ("or_eq"),
  XOR("xor"),
  XOR_EQ("xor_eq"),
  TYPEID("typeid");

  private final String value;

  CxxKeyword(String value) {
    this.value = value;
  }

  public static String[] keywordValues() {
    CxxKeyword[] keywordsEnum = CxxKeyword.values();
    var keywords = new String[keywordsEnum.length];
    for (var i = 0; i < keywords.length; i++) {
      keywords[i] = keywordsEnum[i].getValue();
    }
    return keywords;
  }

  @Override
  public String getName() {
    return name();
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public boolean hasToBeSkippedFromAst(@Nullable AstNode node) {
    return false;
  }

}
