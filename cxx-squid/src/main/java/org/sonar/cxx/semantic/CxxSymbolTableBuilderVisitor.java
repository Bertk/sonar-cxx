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

import com.google.common.collect.ImmutableSet;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxVisitor;
import org.sonar.cxx.CxxVisitorContext;
import org.sonar.cxx.api.CppPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;

public class CxxSymbolTableBuilderVisitor extends CxxVisitor<Grammar> {

  private static final Logger LOG = Loggers.get(CxxSymbolTableBuilderVisitor.class);
  private Map<AstNode, CxxSymbolScope> scopesByRootTree = new HashMap<>();
  private Set<AstNode> allReadUsages;

  public CxxSymbolTable getSymbolTable() {
    return new CxxSymbolTablImpl(scopesByRootTree);
  }

  @Override
  public void scanFile(CxxVisitorContext context) {
    super.scanFile(context);
    new FirstPhaseVisitor().scanFile(context);
  }

  @Override
  public void visitFile(AstNode node) {
    scopesByRootTree = new HashMap<>();
    allReadUsages = new HashSet<>();
  }

  private static class CxxScopeVisitor extends CxxVisitor<Grammar> {

    private Deque<AstNode> scopeRootTrees = new LinkedList<>();

    @Override
    public void visitFile(AstNode node) {
      enterScope(node);
    }

    public void enterScope(AstNode node) {
      scopeRootTrees.push(node);
    }

    @Override
    public void visitNode(AstNode node) {
      if (node.is(CxxGrammarImpl.functionDefinition, CxxGrammarImpl.classSpecifier)) {
        enterScope(node);
      }
    }

    @Override
    public void leaveNode(AstNode node) {
      if (node.is(CxxGrammarImpl.functionDefinition, CxxGrammarImpl.classSpecifier)) {
        scopeRootTrees.pop();
      }
    }

    public AstNode currentScopeRootTree() {
      return scopeRootTrees.peek();
    }

  }

  private class FirstPhaseVisitor extends CxxScopeVisitor {

    @Override
    public Set<AstNodeType> subscribedKinds() {
      return ImmutableSet.of(
          // derived classes ???
          CxxGrammarImpl.functionDefinition,
          CxxGrammarImpl.classSpecifier,
          CxxGrammarImpl.expressionStatement,
          CxxGrammarImpl.conditionalExpression,
          CxxGrammarImpl.declarationStatement,
          CxxGrammarImpl.memberDeclaration,
          CxxGrammarImpl.simpleDeclaration,
          CxxGrammarImpl.namespaceDefinition);
    }

    @Override
    public void visitFile(AstNode node) {
      super.visitFile(node);
      createScope(node, null);
    }

    @Override
    public void visitNode(AstNode node) {
      CxxSymbolScope currentScope = currentScope();

      super.visitNode(node);
      if (node.is(CxxGrammarImpl.classSpecifier)) {
        createScope(node, currentScope);
        currentScope().addWriteUsage(node);
      } else if (node.is(CxxGrammarImpl.memberDeclaration)) {
        createScope(node, currentScope);
        visitMemberDeclaration(node);
      } else if (node.is(CxxGrammarImpl.functionDefinition)) {
        createScope(node, currentScope);
        visitFunction(node); 
      } else if (node.is(CxxGrammarImpl.namespaceDefinition)) {
        createScope(node, currentScope);
      } else if (node.is(CxxGrammarImpl.expressionStatement)) {
        visitAssignment(node);
      } else if (node.is(CxxGrammarImpl.conditionalExpression)) {
        visitExpression(node);
      } else if (node.is(CxxGrammarImpl.declarationStatement)) {
        visitDeclaration(node);
      } else if (node.is(CxxGrammarImpl.simpleDeclaration)) {
        visitSimpleDeclaration(node);
        } else {
          LOG.debug("unhandled symbol type: '{}'", node.getType(), node.getTokenLine());
      }
    }


    /**
     * @param node
     * @return
     */
    private boolean hasFunctionBody(AstNode node) {
      return node.getFirstDescendant(CxxGrammarImpl.functionBody) != null;
    }
    /**
     * @param node
     * @return
     */
    private boolean hasParameterList(AstNode node) {
      return node.getFirstDescendant(CxxGrammarImpl.parameterDeclaration) != null;
    }

    /**
     * @param node
     */
    private void visitMemberDeclaration(AstNode node) {
    
      node.getDescendants(CxxGrammarImpl.memberDeclarator).forEach(decNode -> {
                          currentScope().addWriteUsage(decNode);
                          LOG.debug("member name: '{}({})'", decNode.getTokenValue(), decNode.getTokenLine());
      });
      if (hasParameterList(node)) {
        createFunctionParameters(node);
      }
    }

    /**
     * @param node
     */
    private void visitFunction(AstNode node) {
      if (hasFunctionBody(node)) {
        currentScope().addWriteUsage(node);
        LOG.debug("func name: '{}({})'", CxxSymbolNameHelper.createFullyQualifiedName(CxxSymbolNameHelper.getSymbolName(node),
                  currentScope().getRootTree()), node.getTokenLine());
      }
      if (hasParameterList(node)) {
        createFunctionParameters(node);
      }
    }

    /**
     * @param node
     */
    private void visitSimpleDeclaration(AstNode node) {
      if (!node.hasAncestor(CxxGrammarImpl.functionDefinition) && !node.getTokenValue().contentEquals("class")) {
        node.getDescendants(CxxGrammarImpl.declaratorId).forEach(decNode -> {
            currentScope().addWriteUsage(decNode);
            LOG.debug("simple dec name: '{}({})'", decNode.getTokenValue(), decNode.getTokenLine());
         });
      }
    }

    /**
     * @param node
     */
    private void visitAssignment(AstNode node) {
      for (AstNode assignOperator : node.getDescendants(CxxGrammarImpl.assignmentOperator)) {
//        if (currentScopeRootTree().is(CxxGrammarImpl.classSpecifier)) {
//          new CxxClassVariableAssignmentVisitor(currentScopeRootTree()).scanNode(assignOperator.getNextSibling());
//        }
        AstNode target = assignOperator.getPreviousSibling();
        if (target.is(CxxGrammarImpl.idExpression)) {
          addWriteUsage(target);
        }
        target = assignOperator.getNextSibling();
        if (target.hasDescendant(CxxGrammarImpl.idExpression)) {
          addReadUsage(target.getFirstAncestor(CxxGrammarImpl.idExpression));
        }
        }
    }

    /**
     * @param node
     */
    private void visitDeclaration(AstNode node) {
      node.getDescendants(CxxGrammarImpl.declaratorId).forEach(decNode -> 
        currentScope().addWriteUsage(decNode));
      if (node.hasDescendant(CxxGrammarImpl.initializer)) {
        addReadUsage(node.getFirstDescendant(CxxGrammarImpl.initializer)
                         .getFirstDescendant(CxxGrammarImpl.idExpression));
      }
    }

    /**
     * @param node
     */
    private void visitExpression(AstNode node) {
      if (node.hasDescendant(CxxGrammarImpl.expression)) {
        node.getDescendants(CxxGrammarImpl.expression).forEach( target ->
          addReadUsage(target.getFirstDescendant(CxxGrammarImpl.idExpression)));
      }
    }

    private void createFunctionParameters(AstNode paramListTree) {
      paramListTree.getDescendants(CxxGrammarImpl.parameterDeclaration).forEach(param -> createParameter(param));
    }

    private void createParameter(AstNode paramTree) {
      if (paramTree.hasDescendant(CxxGrammarImpl.declaratorId)) {
        paramTree.getDescendants(CxxGrammarImpl.declaratorId).forEach( param -> {
          addWriteUsage(param);
          LOG.debug("add parameter: '{}({})'", param.getTokenValue(), param.getTokenLine());
        });
      }
    
    }

    private void createScope(AstNode node, @Nullable CxxSymbolScope parent) {
      scopesByRootTree.put(node, new CxxSymbolScope(parent, node));
    }

    private void addWriteUsage(AstNode nameNode) {
      currentScope().addWriteUsage(nameNode);
    }

    /**
     * @param target
     */
    private void addReadUsage(AstNode target) {
      if (target != null && target.is(CxxGrammarImpl.idExpression)) {
        CxxSymbolScope currentScope = scopesByRootTree.get(currentScopeRootTree());
        CxxSymbolImpl symbol = currentScope.resolve(CxxSymbolNameHelper.getSymbolName(target));
        if (symbol != null && !symbol.writeUsages.contains(target) && !allReadUsages.contains(target)) {
          LOG.debug("addReadUsage symbol: '{}({})'", symbol.name, symbol.scopeRootTree.getTokenLine());
          symbol.addReadUsage(target);
          allReadUsages.add(target);
        }
      }
    }

    private CxxSymbolScope currentScope() {
      return scopesByRootTree.get(currentScopeRootTree());
    }

  }

  private static class CxxSymbolTablImpl implements CxxSymbolTable {

    private final Map<AstNode, CxxSymbolScope> scopesByRootTree;

    public CxxSymbolTablImpl(Map<AstNode, CxxSymbolScope> scopesByRootTree) {
      this.scopesByRootTree = scopesByRootTree;
    }

    @Override
    public Set<CxxSymbol> symbols(AstNode scopeTree) {
      CxxSymbolScope scope = scopesByRootTree.get(scopeTree);
      return scope == null ? ImmutableSet.of() : scope.symbols();
    }

  }

  private static class CxxSymbolScope {
    // ToDo: complete C++ scopes -> http://en.cppreference.com/w/cpp/language/scope
    private final AstNode rootTree;
    private final CxxSymbolScope parent;
    private final Map<String, CxxSymbol> symbolsByName = new HashMap<>();
    private final Set<CxxSymbol> symbols = new HashSet<>();
    private final Set<String> fullyQualifiedNames = new HashSet<>();
    
    private CxxSymbolScope(@Nullable CxxSymbolScope parent, AstNode rootTree) {
      this.parent = parent;
      this.rootTree = rootTree;
    }

    private Set<CxxSymbol> symbols() {
      return Collections.unmodifiableSet(symbols);
    }

    public void addWriteUsage(AstNode nameNode) {
      String symbolName = CxxSymbolNameHelper.getSymbolName(nameNode);
      String fullyQualifiedName = CxxSymbolNameHelper.createFullyQualifiedName(symbolName, rootTree);
      if (!symbolsByName.containsKey(symbolName) 
          && !fullyQualifiedNames.contains(fullyQualifiedName) 
          && !symbolName.contentEquals("")) {
        CxxSymbolImpl symbol = new CxxSymbolImpl(symbolName, rootTree);
        symbols.add(symbol);
        symbolsByName.put(symbolName, symbol);
        fullyQualifiedNames.add(symbol.fullyQualifiedName());
        LOG.debug("Create Symbol: '{}({})'", symbolName, symbol.scopeTree().getTokenLine());
      }
      CxxSymbolImpl symbol = resolve(symbolName);
      if (symbol != null) {
        symbol.addWriteUsage(nameNode);
      }
    }

    @CheckForNull
    public CxxSymbolImpl resolve(String symbolName) {
      CxxSymbol symbol = symbolsByName.get(symbolName);
      if (LOG.isDebugEnabled() && symbol != null) {
        LOG.debug("Resolve Symbol: '{}({})'", symbol.fullyQualifiedName(), symbol.scopeTree().getTokenLine());
      }

      if (parent == null || symbol != null) {
        return (CxxSymbolImpl) symbol;
      }

      return parent.resolve(symbolName);
    }

//    private CxxSymbolImpl resolveNonlocal(String symbolName) {
//      CxxScope scope = parent;
//      if (scope != null) {
//        while (scope.parent != null) {
//          CxxSymbol symbol = scope.symbolsByName.get(symbolName);
//          if (symbol != null) {
//            return (CxxSymbolImpl) symbol;
//          }
//          scope = scope.parent;
//        }
//      }
//      return null;
//    }

    public AstNode getRootTree() {
      return rootTree;
    }
//    private CxxScope rootScope() {
//      CxxScope scope = this;
//      while (scope.parent != null) {
//        scope = scope.parent;
//      }
//      return scope;
//    }
  }

  private static class CxxSymbolImpl implements CxxSymbol {
    private final String name;
    private final String unqualifiedName;
    // ToDo use always qualified name -> http://en.cppreference.com/w/cpp/language/qualified_lookup
    private final String fullyQualifiedName;
    private final AstNode scopeRootTree;
    private final Set<AstNode> writeUsages = new HashSet<>();
    private final Set<AstNode> readUsages = new HashSet<>();

    private CxxSymbolImpl(String name, AstNode scopeRootTree) {
      this.name = name;
      this.fullyQualifiedName = CxxSymbolNameHelper.createFullyQualifiedName(name, scopeRootTree);
      this.unqualifiedName = Stream.of(name.split(CppPunctuator.DOUBLECOLON.getValue()))
                                         .reduce((first,last)->last).orElse(name);
      this.scopeRootTree = scopeRootTree;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public String fullyQualifiedName() {
      return fullyQualifiedName;
    }

    @Override
    public String unqualifiedId() {
      return unqualifiedName;
    }

    @Override
    public int line() {
      return scopeRootTree.getTokenLine();
    }

    @Override
    public int column() {
      return scopeRootTree.getToken().getColumn();
    }

    @Override
    public AstNode scopeTree() {
      return scopeRootTree;
    }

    @Override
    public Set<AstNode> writeUsages() {
      return Collections.unmodifiableSet(writeUsages);
    }

    @Override
    public Set<AstNode> readUsages() {
      return Collections.unmodifiableSet(readUsages);
    }

    public void addWriteUsage(AstNode nameNode) {
      writeUsages.add(nameNode);
    }

    public void addReadUsage(AstNode nameNode) {
      readUsages.add(nameNode);
    }
  }

}

