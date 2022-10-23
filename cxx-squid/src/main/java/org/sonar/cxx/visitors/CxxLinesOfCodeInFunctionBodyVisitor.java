/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstVisitor;
import com.sonar.cxx.sslr.api.Grammar;
import java.util.List;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.preprocessor.PPPunctuator;
import org.sonar.cxx.squidbridge.SquidAstVisitor;

/**
 * Visitor that computes the NCLOCs in function body, leading and trailing {} do not count
 *
 * @param <GRAMMAR>
 */
public class CxxLinesOfCodeInFunctionBodyVisitor<GRAMMAR extends Grammar> extends SquidAstVisitor<GRAMMAR>
  implements AstVisitor {

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.functionBody);
  }

  @Override
  public void visitNode(AstNode node) {
    List<AstNode> allChilds = node.getDescendants(CxxGrammarImpl.statement, PPPunctuator.CURLBR_LEFT,
                                                  PPPunctuator.CURLBR_RIGHT);
    var lines = 1;
    int firstLine = node.getTokenLine();
    if (allChilds != null && !allChilds.isEmpty()) {
      int previousLine = firstLine;
      for (var child : allChilds) {
        int currentLine = child.getTokenLine();
        if (currentLine != previousLine) {
          lines++;
          previousLine = currentLine;
        }
      }
    }
    getContext().peekSourceCode().add(CxxMetric.LINES_OF_CODE_IN_FUNCTION_BODY, lines);
  }

}
