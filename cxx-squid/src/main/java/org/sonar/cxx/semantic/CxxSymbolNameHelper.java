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
package org.sonar.cxx.semantic;

import java.util.StringJoiner;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.api.CppPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;

public class CxxSymbolNameHelper {

  private static final Logger LOG = Loggers.get(CxxSymbolNameHelper.class);

  public static String createFullyQualifiedName(String name, AstNode scopeNode) {
    if (name.contains(CppPunctuator.DOUBLECOLON.getValue())) {
      return name;
    }
    StringBuilder fullName = new StringBuilder(name);

    if (scopeNode.is(CxxGrammarImpl.translationUnit)) {
      fullName.insert(0, CppPunctuator.DOUBLECOLON.getValue());
    } else {
      AstNode partName = findDeclaration(name, scopeNode).getFirstAncestor(CxxGrammarImpl.functionDefinition,
          CxxGrammarImpl.classHeadName, CxxGrammarImpl.namedNamespaceDefinition, CxxGrammarImpl.translationUnit);
      while (partName != null) {
        fullName.insert(0, CppPunctuator.DOUBLECOLON.getValue());
        if (!partName.is(CxxGrammarImpl.translationUnit)) {
          fullName.insert(0, partName.getTokenValue());
        }
        partName = scopeNode.getFirstAncestor(CxxGrammarImpl.functionDefinition, CxxGrammarImpl.classSpecifier);
      }
    }
    return fullName.toString();
  }

  private static AstNode findDeclaration(String name, AstNode actNode) {
    AstNode scanNode = actNode;
    if (scanNode.hasAncestor(CxxGrammarImpl.declaration)) {
      scanNode = scanNode.getFirstAncestor(CxxGrammarImpl.declaration);
      scanNode = scanNode.getDescendants(CxxGrammarImpl.declarator).stream()
          .filter(f -> f.getFirstDescendant(GenericTokenType.IDENTIFIER).getTokenValue().contains(name)).findFirst()
          .orElse(null);
      if (scanNode == null) {
        // check all siblings above the actual one CxxGrammarImpl.declaration
        AstNode previousSibling = actNode.getFirstAncestor(CxxGrammarImpl.declaration);
        do {
          previousSibling = previousSibling.getPreviousSibling();
          scanNode = previousSibling;
          scanNode = scanNode.getDescendants(CxxGrammarImpl.declarator).stream()
              .filter(f -> f.getFirstDescendant(GenericTokenType.IDENTIFIER).getTokenValue().contains(name))
              .findFirst().orElse(null);
        } while (scanNode == null && previousSibling.getPreviousSibling() != null);
      }
    }
    return scanNode == null ? actNode : scanNode;
  }

  public static String getSymbolName(AstNode node) {
    StringJoiner fullName = new StringJoiner(CppPunctuator.DOUBLECOLON.getValue());
    LOG.debug("node type getSymbolName: '{}({})'", node.getName(), node.getTokenLine());
    if (node.is(CxxGrammarImpl.classSpecifier)) {
      fullName.add(node.getFirstDescendant(CxxGrammarImpl.classHead)
                       .getFirstDescendant(GenericTokenType.IDENTIFIER)
                       .getTokenValue());
    } else if (node.is(CxxGrammarImpl.functionDefinition, CxxGrammarImpl.memberDeclaration)) {
      if (node.hasDescendant(CxxGrammarImpl.nestedNameSpecifier)) {
        node.getFirstDescendant(CxxGrammarImpl.declaratorId)
          .getDescendants(GenericTokenType.IDENTIFIER).forEach(identifier -> fullName.add(identifier.getTokenValue()));
      } else {
        fullName.add(node.getFirstDescendant(CxxGrammarImpl.declaratorId)
                            .getFirstDescendant(GenericTokenType.IDENTIFIER).getTokenValue());
      }
    } else if (node.is(CxxGrammarImpl.declarationStatement, CxxGrammarImpl.memberDeclarator)) {
      fullName.add(node.getFirstDescendant(CxxGrammarImpl.declaratorId)
                       .getFirstDescendant(GenericTokenType.IDENTIFIER)
                       .getTokenValue());
    } else if (node.is(CxxGrammarImpl.declaratorId, CxxGrammarImpl.idExpression)) {
      fullName.add(node.getFirstDescendant(GenericTokenType.IDENTIFIER).getTokenValue());
    } 
    if (LOG.isDebugEnabled()) {
      LOG.debug("node SymbolName: '{}'", fullName.toString());
    }
    return fullName.toString();
  }

}
