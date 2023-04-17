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
          new SonarAnalysisResultDto.Branch("branch.url"),
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
  }
}
