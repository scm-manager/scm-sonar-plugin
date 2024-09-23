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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.NotFoundException;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("v2/sonar")
public class SonarResource {

  private static final String REPO_KEY = "sonar.analysis.scmm-repo";
  private final RepositoryManager repositoryManager;
  private final SonarService service;

  @Inject
  SonarResource(RepositoryManager repositoryManager, SonarService service) {
    this.repositoryManager = repositoryManager;
    this.service = service;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("{namespace}/{name}")
  @Operation(
    summary = "Update sonar ci status",
    description = "Updates single sonar ci status.",
    tags = "Sonar Plugin",
    operationId = "sonar_put_status"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the \"writeCIStatus\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response processAnalysis(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid SonarAnalysisResultDto resultDto) {
    Repository repository = findRepository(namespace, name);
    service.updateCiStatus(repository, resultDto);
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("")
  @Operation(
    summary = "Update sonar ci status by repo key",
    description = "Updates single sonar ci status by repo key \"sonar.analysis.scmm-repo\".",
    tags = "Sonar Plugin",
    operationId = "sonar_put_status"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "Invalid payload")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the \"writeCIStatus\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response processAnalysisWithRepoKey(@Valid SonarAnalysisResultDto resultDto) {
    String repoKey = resultDto.getProperties().get(REPO_KEY);
    if (repoKey == null || !repoKey.contains("/")) {
      return Response.status(400).build();
    }
    String[] split = repoKey.split("/");
    Repository repository = findRepository(split[0], split[1]);
    service.updateCiStatus(repository, resultDto);
    return Response.ok().build();
  }

  private Repository findRepository(String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw new NotFoundException(NamespaceAndName.class, namespace + "/" + name);
    }
    return repository;
  }
}
