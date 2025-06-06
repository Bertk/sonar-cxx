/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.sensors.utils;

/**
 * EmptyReportException is thrown when an empty report is detected
 */
@SuppressWarnings("serial")
public class ReportException extends RuntimeException {

  public ReportException() {
  }

  /**
   * {@inheritDoc}
   *
   * @param message is used for additional information
   */
  public ReportException(String message) {
    super(message);
  }

  /**
   * {@inheritDoc}
   *
   * @param throwable is used to forward details
   */
  public ReportException(Throwable throwable) {
    super(throwable);
  }

  /**
   * {@inheritDoc}
   *
   * @param message is used for additional information
   * @param throwable is used to forward details
   */
  public ReportException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
