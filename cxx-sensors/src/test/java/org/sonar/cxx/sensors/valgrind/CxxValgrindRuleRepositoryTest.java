/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.sensors.valgrind;

import java.io.File;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.cxx.sensors.utils.RulesDefinitionXmlLoader;
import org.sonar.cxx.sensors.utils.TestUtils;

class CxxValgrindRuleRepositoryTest {

  @Test
  void shouldContainProperNumberOfRules() {
    var def = new CxxValgrindRuleRepository(mock(ServerFileSystem.class), new RulesDefinitionXmlLoader());
    var context = new RulesDefinition.Context();
    def.define(context);
    RulesDefinition.Repository repo = context.repository(CxxValgrindRuleRepository.KEY);
    assertThat(repo.rules()).hasSize(17);
  }

  @ParameterizedTest
  @CsvSource({
    "'/org/sonar/cxx/sensors/rules-repository/CustomRulesOldFormat.xml', 19",
    "'/org/sonar/cxx/sensors/rules-repository/CustomRulesNewFormat.xml', 18",
    "'/org/sonar/cxx/sensors/rules-repository/CustomRulesInvalid.xml', 17",
    "'/org/sonar/cxx/sensors/rules-repository/CustomRulesEmptyFile.xml', 17"
  })
  void containsValidFormatInExtensionRules(String reportFile, int issues) {
    ServerFileSystem filesystem = mock(ServerFileSystem.class);
    var extensionFile = new ArrayList<File>();
    extensionFile.add(TestUtils.loadResource(reportFile));
    var obj = new CxxValgrindRuleRepository(filesystem, new RulesDefinitionXmlLoader());
    CxxValgrindRuleRepository def = spy(obj);
    String repositoryKey = CxxValgrindRuleRepository.KEY;
    doReturn(extensionFile).when(def).getExtensions(repositoryKey, "xml");

    var context = new RulesDefinition.Context();
    def.define(context);
    RulesDefinition.Repository repo = context.repository(repositoryKey);
    assertThat(repo.rules()).hasSize(issues);
  }

}
