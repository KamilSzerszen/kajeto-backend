package pl.kajeto.jobs.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.kajeto.jobs.domain.ConflictException;
import pl.kajeto.jobs.domain.ResourceNotFoundException;
import pl.kajeto.jobs.domain.TenantNotResolvedException;

class ProblemDetailsAdviceTest {

  record Body(@NotBlank String name) {}

  @RestController
  static class ThrowingController {
    @GetMapping("/t/not-found")
    void notFound() {
      throw new ResourceNotFoundException("Client not found");
    }

    @GetMapping("/t/conflict")
    void conflict() {
      throw new ConflictException("Transition DONE -> PLANNED is not allowed");
    }

    @GetMapping("/t/version")
    void version() {
      throw new OptimisticLockingFailureException("secret internal detail");
    }

    @GetMapping("/t/tenant")
    void tenant() {
      throw new TenantNotResolvedException("no jwt");
    }

    @GetMapping("/t/denied")
    void denied() {
      throw new AccessDeniedException("nope");
    }

    @GetMapping("/t/boom")
    void boom() {
      throw new IllegalStateException("password=hunter2 leaked in message");
    }

    @PostMapping("/t/validate")
    void validate(@Valid @RequestBody Body body) {}
  }

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new ProblemDetailsAdvice())
            .build();
  }

  @Test
  void notFoundIs404Problem() throws Exception {
    mockMvc
        .perform(get("/t/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:kajeto:problem:not-found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Client not found"));
  }

  @Test
  void conflictIs409Problem() throws Exception {
    mockMvc
        .perform(get("/t/conflict"))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:kajeto:problem:conflict"))
        .andExpect(jsonPath("$.detail").value("Transition DONE -> PLANNED is not allowed"));
  }

  @Test
  void optimisticLockIs409WithoutLeakingInternals() throws Exception {
    mockMvc
        .perform(get("/t/version"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.type").value("urn:kajeto:problem:version-conflict"))
        .andExpect(
            jsonPath("$.detail")
                .value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));
  }

  @Test
  void validationFailureIs400WithFieldList() throws Exception {
    mockMvc
        .perform(
            post("/t/validate").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:kajeto:problem:validation"))
        .andExpect(jsonPath("$.errors[0].field").value("name"))
        .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
  }

  @Test
  void malformedJsonIs400Problem() throws Exception {
    mockMvc
        .perform(post("/t/validate").contentType(MediaType.APPLICATION_JSON).content("{not json"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Malformed request"));
  }

  @Test
  void unresolvedTenantIs401Problem() throws Exception {
    mockMvc
        .perform(get("/t/tenant"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.type").value("urn:kajeto:problem:unauthorized"));
  }

  @Test
  void accessDeniedIs403Problem() throws Exception {
    mockMvc
        .perform(get("/t/denied"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.type").value("urn:kajeto:problem:forbidden"));
  }

  @Test
  void unexpectedErrorIs500AndHidesTheMessage() throws Exception {
    mockMvc
        .perform(get("/t/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.detail").value("Something went wrong"))
        .andExpect(
            content()
                .string(
                    org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("hunter2"))));
  }
}
