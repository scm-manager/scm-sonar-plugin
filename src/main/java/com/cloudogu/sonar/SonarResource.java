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

import com.cloudogu.scm.ci.cistatus.service.CIStatus;
import com.cloudogu.scm.ci.cistatus.service.Status;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("v2/sonar")
public class SonarResource {

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
  public Response processAnalysis(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid SonarAnalysisResultDto resultDto) {
    Repository repository = findRepository(namespace, name);
    service.updateCiStatus(
      repository,
      resultDto.getRevision(),
      new CIStatus("sonar", "sonar", "Sonar", resolveStatus(resultDto.getStatus()), resultDto.getProject().getUrl())
    );
    return Response.ok().build();
  }

  private Status resolveStatus(String status) {
    switch (status) {
      case "SUCCESS":
        return Status.SUCCESS;
      case "FAILED":
        return Status.FAILURE;

      default:
        return Status.UNSTABLE;
    }
  }

  private Repository findRepository(String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw new NotFoundException(NamespaceAndName.class, namespace + "/" + name);
    }
    return repository;
  }

}
