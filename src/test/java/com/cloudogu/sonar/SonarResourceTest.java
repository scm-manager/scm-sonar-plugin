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

import com.cloudogu.scm.ci.cistatus.service.Status;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SonarResourceTest {

  private RestDispatcher dispatcher;

  @Mock
  private SonarService service;

  @Mock
  private RepositoryManager repositoryManager;

  @InjectMocks
  private SonarResource sonarResource;

  @BeforeEach
  void initResource() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(sonarResource);
  }

  @Test
  void shouldThrowNotFoundExceptionForMissingRepository() throws URISyntaxException {
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(null);

    MockHttpRequest request =
      MockHttpRequest
        .post("/v2/sonar/space/name/")
        .content("".getBytes())
        .contentType(MediaType.APPLICATION_JSON);
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  void shouldProcessRequest() throws URISyntaxException {
    Repository repository = RepositoryTestData.create42Puzzle();
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(repository);

    MockHttpRequest request =
      MockHttpRequest
        .post("/v2/sonar/hitchhiker/42puzzle/")
        .content(("{\n" +
          "    \"qualityGate\": { \"status\":\"SUCCESS\" },\n" +
          "    \"revision\": \"c739069ec7105e01303e8b3065a81141aad9f129\",\n" +
          "    \"project\": {\n" +
          "        \"url\": \"https://scm-manager.org/sonarqube/dashboard?id=myproject\"\n" +
          "    }\n" +
          "}").getBytes())
        .contentType(MediaType.APPLICATION_JSON);
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    verify(service).updateCiStatus(eq(repository), argThat(dto -> {
      assertThat(dto.getRevision()).isEqualTo("c739069ec7105e01303e8b3065a81141aad9f129");
      assertThat(dto.getQualityGate().getStatus()).isEqualTo("SUCCESS");
      assertThat(dto.getProject().getUrl()).isEqualTo("https://scm-manager.org/sonarqube/dashboard?id=myproject");
      return true;
    }));
  }

}
