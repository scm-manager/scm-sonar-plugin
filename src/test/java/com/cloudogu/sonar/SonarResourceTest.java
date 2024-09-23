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

import jakarta.ws.rs.core.MediaType;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

  @Test
  void shouldProcessRequestWithRepoKey() throws URISyntaxException {
    Repository repository = RepositoryTestData.create42Puzzle();
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(repository);

    MockHttpRequest request =
      MockHttpRequest
        .post("/v2/sonar")
        .content(("{\n" +
          "    \"qualityGate\": { \"status\":\"SUCCESS\" },\n" +
          "    \"revision\": \"c739069ec7105e01303e8b3065a81141aad9f129\",\n" +
          "    \"project\": {\n" +
          "        \"url\": \"https://scm-manager.org/sonarqube/dashboard?id=myproject\"\n" +
          "    },\n" +
          "    \"properties\": {\n" +
          "        \"sonar.analysis.scmm-repo\": \""+ repository.getNamespaceAndName().toString() + "\"\n" +
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

  @Test
  void shouldFailForMissingRepoKey() throws URISyntaxException {
    MockHttpRequest request =
      MockHttpRequest
        .post("/v2/sonar")
        .content(("{\n" +
          "    \"qualityGate\": { \"status\":\"SUCCESS\" },\n" +
          "    \"revision\": \"c739069ec7105e01303e8b3065a81141aad9f129\",\n" +
          "    \"project\": {\n" +
          "        \"url\": \"https://scm-manager.org/sonarqube/dashboard?id=myproject\"\n" +
          "    },\n" +
          "    \"properties\": {\n" +
          "    }\n" +
          "}").getBytes())
        .contentType(MediaType.APPLICATION_JSON);
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
    verify(service, never()).updateCiStatus(any(), any());
  }

  @Test
  void shouldFailForInvalidRepoKey() throws URISyntaxException {
    MockHttpRequest request =
      MockHttpRequest
        .post("/v2/sonar")
        .content(("{\n" +
          "    \"qualityGate\": { \"status\":\"SUCCESS\" },\n" +
          "    \"revision\": \"c739069ec7105e01303e8b3065a81141aad9f129\",\n" +
          "    \"project\": {\n" +
          "        \"url\": \"https://scm-manager.org/sonarqube/dashboard?id=myproject\"\n" +
          "    },\n" +
          "    \"properties\": {\n" +
          "        \"sonar.analysis.scmm-repo\": \""+ "abc" + "\"\n" +
          "    }\n" +
          "}").getBytes())
        .contentType(MediaType.APPLICATION_JSON);
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
    verify(service, never()).updateCiStatus(any(), any());
  }
}
