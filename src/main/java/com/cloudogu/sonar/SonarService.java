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

import com.cloudogu.scm.ci.PermissionCheck;
import com.cloudogu.scm.ci.cistatus.CIStatusStore;
import com.cloudogu.scm.ci.cistatus.service.CIStatus;
import com.cloudogu.scm.ci.cistatus.service.CIStatusService;
import com.cloudogu.scm.ci.cistatus.service.Status;
import com.google.common.base.Strings;
import sonia.scm.repository.Repository;

import javax.inject.Inject;

public class SonarService {

  private static final String NAME = "Sonar";
  private static final String TYPE_PULL_REQUEST = "PULL_REQUEST";

  private final CIStatusService service;

  @Inject
  public SonarService(CIStatusService service) {
    this.service = service;
  }

  void updateCiStatus(Repository repository, SonarAnalysisResultDto resultDto) {
    PermissionCheck.checkWrite(repository);
    if (resultDto.getBranch() != null && TYPE_PULL_REQUEST.equals(resultDto.getBranch().getType())) {
      service.put(CIStatusStore.PULL_REQUEST_STORE, repository, resultDto.getBranch().getName(), createCIStatus(resultDto));
    } else {
      service.put(CIStatusStore.CHANGESET_STORE, repository, resultDto.getRevision(), createCIStatus(resultDto));
    }
  }

  private CIStatus createCIStatus(SonarAnalysisResultDto dto) {
    if (dto.getBranch() != null && !Strings.isNullOrEmpty(dto.getBranch().getUrl())) {
      return new CIStatus(NAME, NAME, NAME, resolveStatus(dto.getQualityGate().getStatus()), dto.getBranch().getUrl());
    }
    return new CIStatus(NAME, NAME, NAME, resolveStatus(dto.getQualityGate().getStatus()), dto.getProject().getUrl());
  }

  private Status resolveStatus(String status) {
    switch (status) {
      case "SUCCESS":
      case "OK":
        return Status.SUCCESS;
      case "ERROR":
        return Status.FAILURE;

      default:
        return Status.UNSTABLE;
    }
  }
}
