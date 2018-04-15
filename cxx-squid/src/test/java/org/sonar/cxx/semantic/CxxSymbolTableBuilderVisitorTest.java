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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxVisitor;
import org.sonar.cxx.CxxVisitorContext;
import org.sonar.cxx.TestCxxVisitorRunner;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.semantic.CxxSymbol;
import org.sonar.cxx.semantic.CxxSymbolTable;
import org.sonar.cxx.semantic.CxxSymbolTableBuilderVisitor;

import static org.assertj.core.api.Assertions.assertThat;

public class CxxSymbolTableBuilderVisitorTest {

  private static final Logger LOG = Loggers.get(CxxSymbolTableBuilderVisitorTest.class);
  private CxxSymbolTable symbolTable;
  private AstNode rootTree;
  private Map<String, AstNode> functionTreesByName = new HashMap<>();
  private CxxVisitorContext visitorContext;

  @Before
  public void init() throws UnsupportedEncodingException, IOException {
    String filePath = "src/test/resources/semantic/symbols.cc";
    visitorContext = TestCxxVisitorRunner.createContext(new File(filePath));
    CxxSymbolTableBuilderVisitor symbolTableBuilderVisitor = new CxxSymbolTableBuilderVisitor();
    symbolTableBuilderVisitor.scanFile(visitorContext);
    symbolTable = symbolTableBuilderVisitor.getSymbolTable();
    new CxxTestVisitor().scanFile(visitorContext);
    LOG.info("****** module Variable {} ******", symbolTable.symbols(rootTree).toString());
  }

  @Test
  public void non_scope_tree() throws Exception {
    assertThat(symbolTable.symbols(rootTree.getFirstDescendant(CxxGrammarImpl.expressionStatement))).isEmpty();
  }

  @Test
  public void module_variable() {
    LOG.info("rootTree {}", serialize(rootTree.getTokens()));
    LOG.info("scope {}", symbolTable.symbols(rootTree).stream().count());
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(symbolTable.symbols(rootTree)).extracting(CxxSymbol::scopeTree).containsOnly(rootTree);
    softly.assertThat(symbolTable.symbols(rootTree)).extracting(CxxSymbol::name).containsOnly("global1");
    softly.assertAll();
  }

  @Test
  public void local_variable() {
    AstNode functionTree = functionTreesByName.get("CMotorController::checkSpeed");
    Set<CxxSymbol> symbols = symbolsInFunction("CMotorController::checkSpeed");
    assertThat(symbols).extracting(CxxSymbol::name).contains("test1");
    CxxSymbol a = symbols.stream().filter(item->item.name().equals("test1")).findFirst().get();
    assertThat(a.scopeTree()).isEqualTo(functionTree);
    assertThat(a.writeUsages()).extracting(AstNode::getTokenLine).containsOnly(functionTree.getTokenLine() + 3);
    assertThat(a.readUsages()).extracting(AstNode::getTokenLine).isEmpty();
  }

//  @Test
  public void global_variable_reference() {
    CxxSymbol a = lookup(rootTree, "global1");
    assertThat(a.writeUsages()).extracting(AstNode::getTokenLine).contains(15, 87);
  }


  @Test
  public void assignment_expression() {
    assertThat(symbolsInFunction("CMotorController::setDirectionAndSpeed")).extracting(CxxSymbol::name).containsOnly("CMotorController::setDirectionAndSpeed", "speed", "direction");
  }

  @Test
  public void simple_parameter() {
    assertThat(symbolsInFunction("CMotorController::setSpeed")).extracting(CxxSymbol::name).containsOnly("speed", "CMotorController::setSpeed");
  }

  @Test
  public void list_parameter() {
    assertThat(symbolsInFunction("CMotorController::setDirectionAndSpeed")).extracting(CxxSymbol::name).containsOnly("direction", "speed", "CMotorController::setDirectionAndSpeed");
  }

//  @Test
  public void class_variable() {
    AstNode classMotorController = rootTree.getFirstDescendant(CxxGrammarImpl.classSpecifier);
    assertThat(symbolTable.symbols(classMotorController)).hasSize(12);
    CxxSymbol classVariableA = lookup(classMotorController, "speed");
    assertThat(classVariableA.writeUsages()).extracting(AstNode::getTokenLine).containsOnly(19,27,50,66);
    assertThat(classVariableA.readUsages()).extracting(AstNode::getTokenLine).containsOnly(90,66,55);
  }

  private Set<CxxSymbol> symbolsInFunction(String functionName) {
    AstNode functionTree = functionTreesByName.get(functionName);
    return symbolTable.symbols(functionTree);
  }

  private CxxSymbol lookup(AstNode scopeRootTree, String symbolName) {
    Set<CxxSymbol> check = symbolTable.symbols(scopeRootTree);
    LOG.debug("CxxSymbols lookup: {}", check);
    return symbolTable.symbols(scopeRootTree).stream()
      .filter(s -> s.name().equals(symbolName))
      .findFirst().get();
  }

  private class CxxTestVisitor extends CxxVisitor<Grammar> {

    @Override
    public void visitFile(AstNode node) {
      rootTree = node;
      LOG.info("****** CxxTestVisitor 'CxxGrammarImpl.functionDefinition' ******");
      for (AstNode functionTree : node.getDescendants(CxxGrammarImpl.functionDefinition)) {
        functionTreesByName.put(CxxSymbolNameHelper.getSymbolName(functionTree), functionTree);
      }
    }
  }

  private static String serialize(List<Token> tokens) {
    return serialize(tokens, " ");
  }

  private static String serialize(List<Token> tokens, String spacer) {
    StringJoiner js = new StringJoiner(spacer);
    for (Token t : tokens) {
      js.add(t.getValue());
    }
    return js.toString();
  }
  
//  private static File loadResource(String resourceName) {
//    URL resource = CxxSymbolTableBuilderVisitorTest.class.getResource(resourceName);
//    File resourceAsFile = null;
//    try {
//      resourceAsFile = new File(resource.toURI());
//    } catch (URISyntaxException e) {
//      System.out.println("Cannot load resource: " + resourceName);
//    }
//
//    return resourceAsFile;
//  }

}

