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

import com.cloudogu.scm.ci.PermissionCheck;
import com.cloudogu.scm.ci.cistatus.CIStatusStore;
import com.cloudogu.scm.ci.cistatus.service.CIStatus;
import com.cloudogu.scm.ci.cistatus.service.CIStatusService;
import com.cloudogu.scm.ci.cistatus.service.Status;
import com.google.common.base.Strings;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;

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
