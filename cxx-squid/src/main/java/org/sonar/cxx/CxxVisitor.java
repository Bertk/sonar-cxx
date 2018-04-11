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

import com.google.common.collect.ImmutableSet;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.util.List;
import java.util.Set;

import org.sonar.squidbridge.SquidAstVisitor;

public class CxxVisitor <GRAMMAR extends Grammar> extends SquidAstVisitor<GRAMMAR> {

  private CxxVisitorContext visitorContext;

  public Set<AstNodeType> subscribedKinds() {
    return ImmutableSet.of();
  }

  public void visitToken(Token token) {
    // default implementation does nothing
  }

  public void scanFile(CxxVisitorContext visitorContext) {
    this.visitorContext = visitorContext;
    AstNode tree = visitorContext.rootTree();
    if (tree != null) {
      visitFile(tree);
      scanNode(tree, subscribedKinds());
      leaveFile(tree);
    }
  }

public CxxVisitorContext getVisitorContext() {
  return visitorContext;
}

  public void scanNode(AstNode node) {
    scanNode(node, subscribedKinds());
  }

  private void scanNode(AstNode node, Set<AstNodeType> subscribedKinds) {
    boolean isSubscribedType = subscribedKinds.contains(node.getType());

    if (isSubscribedType) {
      visitNode(node);
    }

    List<AstNode> children = node.getChildren();
    if (children.isEmpty()) {
      for (Token token : node.getTokens()) {
        visitToken(token);
      }
    } else {
      for (AstNode child : children) {
        scanNode(child, subscribedKinds);
      }
    }

    if (isSubscribedType) {
      leaveNode(node);
    }
  }

}
