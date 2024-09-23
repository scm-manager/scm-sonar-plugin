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

import com.cloudogu.scm.ci.cistatus.CIStatusStore;
import com.cloudogu.scm.ci.cistatus.service.CIStatusService;
import com.cloudogu.scm.ci.cistatus.service.Status;
import org.apache.shiro.authz.AuthorizationException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "Trillian")
class SonarServiceTest {

  private final Repository repository = RepositoryTestData.create42Puzzle("git");

  @Mock
  private CIStatusService ciStatusService;

  @InjectMocks
  private SonarService service;

  @BeforeEach
  void init() {
    repository.setId("id-1");
  }

  @Test
  void shouldThrowAuthorizationExceptionIfNotPermitted() {
    assertThrows(AuthorizationException.class, () -> service.updateCiStatus(repository, new SonarAnalysisResultDto()));
  }

  @Nested
  @SubjectAware(permissions = "repository:writeCIStatus:id-1")
  class WithPermissions {
    @Test
    void shouldUpdateNewCIStatusWithDefaultUrl() {
      service.updateCiStatus(
        repository,
        new SonarAnalysisResultDto(
          "123",
          new SonarAnalysisResultDto.Project("test.url"),
          null,
          new SonarAnalysisResultDto.QualityGate("SUCCESS"),
          emptyMap()
        )
      );

      verify(ciStatusService).put(eq(CIStatusStore.CHANGESET_STORE), eq(repository), eq("123"), argThat(ciStatus -> {
        assertThat(ciStatus.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(ciStatus.getUrl()).isEqualTo("test.url");
        assertThat(ciStatus.getDisplayName()).isEqualTo("Sonar");
        assertThat(ciStatus.getName()).isEqualTo("Sonar");
        return true;
      }));
    }

    @Test
    void shouldUpdateNewCIStatusWithBranchUrl() {
      service.updateCiStatus(
        repository,
        new SonarAnalysisResultDto(
          "123",
          new SonarAnalysisResultDto.Project("test.url"),
          new SonarAnalysisResultDto.Branch("branch.url", null, ""),
          new SonarAnalysisResultDto.QualityGate("SUCCESS"),
          emptyMap()
        )
      );

      verify(ciStatusService).put(eq(CIStatusStore.CHANGESET_STORE), eq(repository), eq("123"), argThat(ciStatus -> {
        assertThat(ciStatus.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(ciStatus.getUrl()).isEqualTo("branch.url");
        assertThat(ciStatus.getDisplayName()).isEqualTo("Sonar");
        assertThat(ciStatus.getName()).isEqualTo("Sonar");
        return true;
      }));
    }

    @Test
    void shouldUpdateNewCIStatusForPullRequest() {
      service.updateCiStatus(
        repository,
        new SonarAnalysisResultDto(
          "123",
          new SonarAnalysisResultDto.Project("test.url"),
          new SonarAnalysisResultDto.Branch("branch.url", "PULL_REQUEST", "42"),
          new SonarAnalysisResultDto.QualityGate("SUCCESS"),
          emptyMap()
        )
      );

      verify(ciStatusService).put(eq(CIStatusStore.PULL_REQUEST_STORE), eq(repository), eq("42"), argThat(ciStatus -> {
        assertThat(ciStatus.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(ciStatus.getUrl()).isEqualTo("branch.url");
        assertThat(ciStatus.getDisplayName()).isEqualTo("Sonar");
        assertThat(ciStatus.getName()).isEqualTo("Sonar");
        return true;
      }));
    }
  }
}
