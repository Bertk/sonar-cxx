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

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.internal.apachecommons.lang.SystemUtils;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

/**
 *
 * @author rudolfgrauberger
 */
public class CxxVCppBuildLogParserTest {

    private static final String VC_CHARSET = "UTF8";

    public static final String OVERALLINCLUDEKEY = "CxxOverallInclude";
    public static final String OVERALLDEFINEKEY = "CxxOverallDefine";

    private final Map<String, List<String>> uniqueIncludes = new HashMap<>();
    private final Map<String, Set<String>> uniqueDefines = new HashMap<>();


    @Before
    public void init()
    {
        org.junit.Assume.assumeTrue(SystemUtils.IS_OS_WINDOWS);
        uniqueIncludes.clear();
        uniqueDefines.clear();
        uniqueIncludes.put(OVERALLINCLUDEKEY, new ArrayList<>());
        uniqueDefines.put(OVERALLDEFINEKEY, new HashSet<>());
    }

    /**
     * We can test this at the moment only if we check how the relative include paths translated to absolute paths.
     */
    @Test
    public void shouldExtractProjectFileFromEnglishVCpp2013Log() {

        File vcLog = new File("src/test/resources/logfile/vc++13.txt");

        CxxVCppBuildLogParser parser = new CxxVCppBuildLogParser(uniqueIncludes, uniqueDefines);
        parser.parseVCppLog(vcLog, ".", VC_CHARSET);

        // Checks only the includes with relative paths
        SoftAssertions softly = new SoftAssertions();

        // Extracted project path: C:\prod\SonarQube\cxx\sonar-cxx\integration-tests\testdata\googletest_bullseye_vs_project\PathHandling
        List<String> includes = uniqueIncludes.get("C:\\prod\\SonarQube\\cxx\\sonar-cxx\\integration-tests\\testdata\\googletest_bullseye_vs_project\\PathHandling\\PathHandle.cpp");

        // .\interface -> C:\prod\SonarQube\cxx\sonar-cxx\integration-tests\testdata\googletest_bullseye_vs_project\PathHandling\interface
        softly.assertThat(includes.contains("C:\\prod\\SonarQube\\cxx\\sonar-cxx\\integration-tests\\testdata\\googletest_bullseye_vs_project\\PathHandling\\interface")).isTrue();
        // ..\tools\interface -> C:\prod\SonarQube\cxx\sonar-cxx\integration-tests\testdata\googletest_bullseye_vs_project\tools\interface
        softly.assertThat(includes.contains("C:\\prod\\SonarQube\\cxx\\sonar-cxx\\integration-tests\\testdata\\googletest_bullseye_vs_project\\tools\\interface")).isTrue();
        // ..\memlib\interface -> C:\prod\SonarQube\cxx\sonar-cxx\integration-tests\testdata\googletest_bullseye_vs_project\memlib\interface
        softly.assertThat(includes.contains("C:\\prod\\SonarQube\\cxx\\sonar-cxx\\integration-tests\\testdata\\googletest_bullseye_vs_project\\memlib\\interface")).isTrue();

        // Currently this relative path converted to a wrong path (should be C:\prod\SonarQube\cxx\sonar-cxx\integration-tests\testdata\googletest_bullseye_vs_project)
        // To make sure that the new implementation at least initially handles everything exactly that way (to be able to compare). Also, this change should
        // only include the language independent recognition of the project file, so we have accepted the state.
        // .. -> C:\
        softly.assertThat(includes.contains("C:\\")).isTrue();
        softly.assertAll();
    }

    /**
     * We can test this at the moment only if we check how the relative include paths translated to absolute paths.
     */
    @Test
    public void shouldExtractProjectFileFromGermanVCpp2017Log() {

        File vcLog = new File("src/test/resources/logfile/vc++2017-de.txt");

        CxxVCppBuildLogParser parser = new CxxVCppBuildLogParser(uniqueIncludes, uniqueDefines);
        parser.parseVCppLog(vcLog, ".", VC_CHARSET);

        // Checks only the includes with relative paths
        SoftAssertions softly = new SoftAssertions();

        List<String> includes = uniqueIncludes.get("D:\\Development\\Source\\cpp\\Projects\\SyncEngine\\Source\\Synchronisation.cpp");

        // Extracted project path: D:\Development\Source\cpp\Projects\SyncEngine
        // ..\..\..\ThirdParty\VS2017\Qt-4.8.7\include -> D:\Development\Source\ThirdParty\VS2017\Qt-4.8.7\include
        softly.assertThat(includes.contains("D:\\Development\\Source\\ThirdParty\\VS2017\\Qt-4.8.7\\include")).isTrue();
        // Interface -> D:\Development\Source\cpp\Projects\SyncEngine\Interface
        softly.assertThat(includes.contains("D:\\Development\\Source\\cpp\\Projects\\SyncEngine\\Interface")).isTrue();
        // ..\Shared -> D:\Development\Source\cpp\Projects\Shared
        softly.assertThat(includes.contains("D:\\Development\\Source\\cpp\\Projects\\Shared")).isTrue();
        // ..\Shared\Cpp -> D:\Development\Source\cpp\Projects\Shared\Cpp
        softly.assertThat(includes.contains("D:\\Development\\Source\\cpp\\Projects\\Shared\\Cpp")).isTrue();
        // ..\Shared\Cpp\Interface -> D:\Development\Source\cpp\Projects\Shared\Cpp\Interface
        softly.assertThat(includes.contains("D:\\Development\\Source\\cpp\\Projects\\Shared\\Cpp\\Interface")).isTrue();
        // ..\..\..\ThirdParty\VS2017\Boost-1.67.0 -> D:\Development\Source\ThirdParty\VS2017\Boost-1.67.0
        softly.assertThat(includes.contains("D:\\Development\\Source\\ThirdParty\\VS2017\\Boost-1.67.0")).isTrue();
        // ..\..\..\ThirdParty\VS2017\Qt-4.8.7\include\ActiveQt -> D:\Development\Source\ThirdParty\VC2017\Qt-4.8.7\include\ActiveQt
        softly.assertThat(includes.contains("D:\\Development\\Source\\ThirdParty\\VS2017\\Qt-4.8.7\\include\\ActiveQt")).isTrue();
        // release -> D:\Development\Source\cpp\Projects\SyncEngine\release
        softly.assertThat(includes.contains("D:\\Development\\Source\\cpp\\Projects\\SyncEngine\\release")).isTrue();
        // ..\..\..\ThirdParty\VS2017\Qt-4.8.7\mkspecs\win32-msvc2017 -> D:\Development\Source\ThirdParty\VS2017\Qt-4.8.7\mkspecs\win32-msvc2017
        softly.assertThat(includes.contains("D:\\Development\\Source\\ThirdParty\\VS2017\\Qt-4.8.7\\mkspecs\\win32-msvc2017")).isTrue();
        softly.assertAll();
    }
}