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
package org.sonar.cxx.sensors.tests.dotnet;

// origin https://github.com/SonarSource/sonar-dotnet-tests-library/
// SonarQube .NET Tests Library
// Copyright (C) 2014-2017 SonarSource SA
// mailto:info AT sonarsource DOT com
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WildcardPatternFileProviderTest {

  @TempDir
  File tempDir;

  private static String path(String... elements) {
    return String.join(File.separator, elements);
  }

  private static Set<File> listFiles(String pattern) {
    return listFiles(pattern, null);
  }

  private static Set<File> listFiles(String pattern, File baseDir) {
    return new WildcardPatternFileProvider(baseDir, File.separator).listFiles(pattern);
  }

  @BeforeEach
  public void init() throws Exception {
    Path root = tempDir.toPath();

    Files.createFile(root.resolve("foo.txt"));
    Files.createFile(root.resolve("bar.txt"));

    Files.createDirectories(Path.of(root.toString(), "a"));
    Files.createFile(root.resolve("a/foo.txt"));
    Files.createDirectories(Path.of(root.toString(), "a", "a21"));

    Files.createDirectories(Path.of(root.toString(), "b"));

    Files.createDirectories(Path.of(root.toString(), "c", "c21"));
    Files.createFile(root.resolve("c/c21/foo.txt"));
    Files.createDirectories(Path.of(root.toString(), "c", "c22", "c31"));
    Files.createFile(root.resolve("c/c22/c31/foo.txt"));
    Files.createFile(root.resolve("c/c22/c31/bar.txt"));
  }

  @Test
  void absolute_paths() {
    assertThat(listFiles(new File(tempDir, "foo.txt").getAbsolutePath()))
      .containsOnly(new File(tempDir, "foo.txt"));

    assertThat(listFiles(new File(tempDir, path("c", "c22", "c31", "foo.txt")).getAbsolutePath()))
      .containsOnly(new File(tempDir, path("c", "c22", "c31", "foo.txt")));

    assertThat(listFiles(new File(tempDir, "nonexisting.txt").getAbsolutePath())).isEmpty();
  }

  @Test
  void absolute_paths_with_current_and_parent_folder_access() {
    assertThat(listFiles(new File(tempDir, path("a", "..", ".", "foo.txt")).getAbsolutePath()))
      .containsOnly(new File(tempDir, path("a", "..", ".", "foo.txt")));
  }

  @Test
  void absolute_paths_with_wildcards() {
    assertThat(listFiles(new File(tempDir, "*.txt").getAbsolutePath()))
      .containsOnly(new File(tempDir, "foo.txt"), new File(tempDir, "bar.txt"));

    assertThat(listFiles(new File(tempDir, "f*").getAbsolutePath()))
      .containsOnly(new File(tempDir, "foo.txt"));

    assertThat(listFiles(new File(tempDir, "fo?.txt").getAbsolutePath()))
      .containsOnly(new File(tempDir, "foo.txt"));

    assertThat(listFiles(new File(tempDir, "foo?.txt").getAbsolutePath())).isEmpty();

    assertThat(listFiles(new File(tempDir, path("**", "foo.txt")).getAbsolutePath()))
      .containsOnly(
        new File(tempDir, "foo.txt"),
        new File(tempDir, path("a", "foo.txt")),
        new File(tempDir, path("c", "c21", "foo.txt")),
        new File(tempDir, path("c", "c22", "c31", "foo.txt")));

    assertThat(listFiles(new File(tempDir, path("**", "c31", "foo.txt")).getAbsolutePath()))
      .containsOnly(new File(tempDir, path("c", "c22", "c31", "foo.txt")));

    assertThat(listFiles(new File(tempDir, path("**", "c?1", "foo.txt")).getAbsolutePath()))
      .containsOnly(new File(tempDir, path("c", "c21", "foo.txt")), new File(tempDir,
                                                                             path("c", "c22", "c31", "foo.txt")));

    assertThat(listFiles(new File(tempDir, path("?", "**", "foo.txt")).getAbsolutePath()))
      .containsOnly(
        new File(tempDir, path("a", "foo.txt")),
        new File(tempDir, path("c", "c21", "foo.txt")),
        new File(tempDir, path("c", "c22", "c31", "foo.txt")));
  }

  @Test
  void relative_paths() {
    assertThat(listFiles("foo.txt", tempDir))
      .containsOnly(new File(tempDir, "foo.txt"));

    assertThat(listFiles(path("c", "c22", "c31", "foo.txt"), tempDir))
      .containsOnly(new File(tempDir, path("c", "c22", "c31", "foo.txt")));

    assertThat(listFiles("nonexisting.txt", tempDir)).isEmpty();
  }

  @Test
  void relative_paths_with_current_and_parent_folder_access() {
    assertThat(listFiles(path("..", "foo.txt"), new File(tempDir, "a")))
      .containsOnly(new File(new File(tempDir, "a"), path("..", "foo.txt")));

    assertThat(listFiles(path(".", "foo.txt"), tempDir))
      .containsOnly(new File(tempDir, path(".", "foo.txt")));

    assertThat(listFiles(path("a", "..", "foo.txt"), tempDir))
      .containsOnly(new File(tempDir, path("a", "..", "foo.txt")));
  }

  @Test
  void relative_paths_with_wildcards() {
    assertThat(listFiles("*.txt", tempDir))
      .containsOnly(new File(tempDir, "foo.txt"), new File(tempDir, "bar.txt"));

    assertThat(listFiles("f*", tempDir))
      .containsOnly(new File(tempDir, "foo.txt"));

    assertThat(listFiles("fo?.txt", tempDir))
      .containsOnly(new File(tempDir, "foo.txt"));

    assertThat(listFiles("foo?.txt", tempDir)).isEmpty();

    assertThat(listFiles(path("**", "foo.txt"), tempDir))
      .containsOnly(
        new File(tempDir, "foo.txt"),
        new File(tempDir, path("a", "foo.txt")),
        new File(tempDir, path("c", "c21", "foo.txt")),
        new File(tempDir, path("c", "c22", "c31", "foo.txt")));

    assertThat(listFiles(path("**", "c31", "foo.txt"), tempDir))
      .containsOnly(new File(tempDir, path("c", "c22", "c31", "foo.txt")));

    assertThat(listFiles(path("**", "c?1", "foo.txt"), tempDir))
      .containsOnly(new File(tempDir, path("c", "c21", "foo.txt")), new File(tempDir,
                                                                             path("c", "c22", "c31", "foo.txt")));

    assertThat(listFiles(path("?", "**", "foo.txt"), tempDir))
      .containsOnly(
        new File(tempDir, path("a", "foo.txt")),
        new File(tempDir, path("c", "c21", "foo.txt")),
        new File(tempDir, path("c", "c22", "c31", "foo.txt")));
  }

  @Test
  void should_fail_with_current_folder_access_after_wildcard() {
    IllegalArgumentException thrown = catchThrowableOfType(() -> {
      listFiles(new File(tempDir, path("?", ".", "foo.txt")).getAbsolutePath());
    }, IllegalArgumentException.class);
    assertThat(thrown).hasMessage("Cannot contain '.' or '..' after the first wildcard.");
  }

  @Test
  void should_fail_with_parent_folder_access_after_wildcard() {
    IllegalArgumentException thrown = catchThrowableOfType(() -> {
      listFiles(path("*", "..", "foo.txt"), tempDir);
    }, IllegalArgumentException.class);
    assertThat(thrown).hasMessage("Cannot contain '.' or '..' after the first wildcard.");
  }

}
