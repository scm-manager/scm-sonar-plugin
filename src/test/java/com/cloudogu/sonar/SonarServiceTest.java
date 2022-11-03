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
    void shouldUpdateNewCIStatus() {
      service.updateCiStatus(
        repository,
        new SonarAnalysisResultDto(
          "123",
          new SonarAnalysisResultDto.Project("test.url"),
          new SonarAnalysisResultDto.QualityGate("SUCCESS")
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
  }
}
