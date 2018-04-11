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

import com.sonar.sslr.api.AstNode;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.cxx.semantic.CxxSymbolTable;
import org.sonar.cxx.semantic.CxxSymbolTableBuilderVisitor;

public class CxxVisitorContext {

  private final AstNode rootTree;
  private final CxxFile file;
  private CxxSymbolTable symbolTable = null;  

  public CxxVisitorContext(SensorContext sensorContext, AstNode rootTree, CxxFile cxxFile) {
    this(rootTree, cxxFile);
    CxxSymbolTableBuilderVisitor symbolTableBuilderVisitor = new CxxSymbolTableBuilderVisitor();
    symbolTableBuilderVisitor.scanFile(this);
    symbolTable = symbolTableBuilderVisitor.getSymbolTable();
  }

  CxxVisitorContext(AstNode rootTree, CxxFile cxxFile) {
    this.rootTree = rootTree;
    this.file = cxxFile;
  }

  public AstNode rootTree() {
    return rootTree;
  }

  public CxxFile getFile() {
    return file;
  }

  public CxxSymbolTable symbolTable() {
    return symbolTable;
  }
}

