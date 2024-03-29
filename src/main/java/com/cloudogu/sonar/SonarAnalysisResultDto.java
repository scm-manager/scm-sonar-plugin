/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
