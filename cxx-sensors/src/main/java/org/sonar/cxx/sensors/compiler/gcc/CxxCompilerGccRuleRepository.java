/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
package org.sonar.cxx.sensors.compiler.gcc;

import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.cxx.sensors.utils.RulesDefinitionXml;

/**
 * {@inheritDoc}
 */
public class CxxCompilerGccRuleRepository extends RulesDefinitionXml {

  private static final String LANGUAGE = "cxx";
  public static final String KEY = "compiler-gcc";
  private static final String NAME = "Compiler-GCC";
  private static final String FILE = "/compiler-gcc.xml";

  /**
   * {@inheritDoc}
   */
  public CxxCompilerGccRuleRepository(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader) {
    super(fileSystem, xmlRuleLoader, LANGUAGE, KEY, NAME, FILE);
  }

}
