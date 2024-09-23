/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.sonar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Payload of SonarQube / SonarCloud webhook
 * @link <a href="https://docs.sonarqube.org/latest/project-administration/webhooks/"/>
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SonarAnalysisResultDto {
  private String revision;
  private Project project;
  private Branch branch;
  private QualityGate qualityGate;
  private Map<String, String> properties = new HashMap<>();

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  static class Project {
    private String url;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  static class Branch {
    private String url;
    private String type;
    private String name;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  static class QualityGate {
    private String status;
  }
}
