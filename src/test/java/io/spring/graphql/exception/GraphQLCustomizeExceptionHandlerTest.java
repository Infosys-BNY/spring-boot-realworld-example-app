package io.spring.graphql.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphQLCustomizeExceptionHandlerTest {
  @InjectMocks private GraphQLCustomizeExceptionHandler exceptionHandler;

  private DataFetcherExceptionHandlerParameters handlerParameters;

  @BeforeEach
  void setUp() {
    handlerParameters = mock(DataFetcherExceptionHandlerParameters.class);
  }

  @Test
  void onException_invalidAuthenticationException_returnsUnauthenticatedError() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    when(handlerParameters.getException()).thenReturn(exception);
    ResultPath path = ResultPath.parse("/test/path");
    when(handlerParameters.getPath()).thenReturn(path);

    DataFetcherExceptionHandlerResult result = exceptionHandler.onException(handlerParameters);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).hasSize(1);
    GraphQLError error = result.getErrors().get(0);
    assertThat(error).isInstanceOf(TypedGraphQLError.class);
    assertThat(error.getExtensions()).containsEntry("errorType", ErrorType.UNAUTHENTICATED.name());
    assertThat(error.getPath()).isEqualTo(path.toList());
  }

  @Test
  void onException_invalidAuthenticationExceptionWithMessage_includesMessage() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    when(handlerParameters.getException()).thenReturn(exception);
    ResultPath path = ResultPath.parse("/auth");
    when(handlerParameters.getPath()).thenReturn(path);

    DataFetcherExceptionHandlerResult result = exceptionHandler.onException(handlerParameters);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).hasSize(1);
    GraphQLError error = result.getErrors().get(0);
    assertThat(error.getMessage()).isNotNull();
  }

  @Test
  void onException_constraintViolationException_returnsBadRequestError() {
    ConstraintViolation<?> violation = createMockViolation("email", "must not be blank", "NotBlank");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    when(handlerParameters.getException()).thenReturn(exception);
    ResultPath path = ResultPath.parse("/register");
    when(handlerParameters.getPath()).thenReturn(path);

    DataFetcherExceptionHandlerResult result = exceptionHandler.onException(handlerParameters);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).hasSize(1);
    GraphQLError error = result.getErrors().get(0);
    assertThat(error).isInstanceOf(TypedGraphQLError.class);
    assertThat(error.getMessage()).isNotNull();
    assertThat(error.getExtensions()).isNotNull();
    assertThat(error.getExtensions()).containsKey("email");
  }

  @Test
  void onException_constraintViolationExceptionMultipleViolations_includesAllErrors() {
    ConstraintViolation<?> violation1 = createMockViolation("email", "must not be blank", "NotBlank");
    ConstraintViolation<?> violation2 =
        createMockViolation("username", "size must be between 1 and 255", "Size");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    when(handlerParameters.getException()).thenReturn(exception);
    ResultPath path = ResultPath.parse("/register");
    when(handlerParameters.getPath()).thenReturn(path);

    DataFetcherExceptionHandlerResult result = exceptionHandler.onException(handlerParameters);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).hasSize(1);
    GraphQLError error = result.getErrors().get(0);
    assertThat(error.getExtensions()).containsKeys("email", "username");
  }

  @Test
  void onException_otherException_delegatesToDefaultHandler() {
    RuntimeException exception = new RuntimeException("Some other error");
    when(handlerParameters.getException()).thenReturn(exception);
    ResultPath path = ResultPath.parse("/test");
    when(handlerParameters.getPath()).thenReturn(path);

    DataFetcherExceptionHandlerResult result = exceptionHandler.onException(handlerParameters);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).isNotEmpty();
  }

  @Test
  void onException_nullPointerException_delegatesToDefaultHandler() {
    NullPointerException exception = new NullPointerException("Null value");
    when(handlerParameters.getException()).thenReturn(exception);
    ResultPath path = ResultPath.parse("/test");
    when(handlerParameters.getPath()).thenReturn(path);

    DataFetcherExceptionHandlerResult result = exceptionHandler.onException(handlerParameters);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).isNotEmpty();
  }

  @Test
  void getErrorsAsData_constraintViolationException_returnsError() {
    ConstraintViolation<?> violation = createMockViolation("email", "must not be blank", "NotBlank");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertThat(result).isNotNull();
    assertThat(result.getMessage()).isEqualTo("BAD_REQUEST");
    assertThat(result.getErrors()).isNotNull();
    assertThat(result.getErrors()).hasSize(1);
    ErrorItem errorItem = result.getErrors().get(0);
    assertThat(errorItem.getKey()).isEqualTo("email");
    assertThat(errorItem.getValue()).contains("must not be blank");
  }

  @Test
  void getErrorsAsData_multipleViolationsSameField_groupsErrors() {
    ConstraintViolation<?> violation1 = createMockViolation("email", "must not be blank", "NotBlank");
    ConstraintViolation<?> violation2 =
        createMockViolation("email", "must be a valid email", "Email");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).hasSize(1);
    ErrorItem errorItem = result.getErrors().get(0);
    assertThat(errorItem.getKey()).isEqualTo("email");
    assertThat(errorItem.getValue()).hasSize(2);
    assertThat(errorItem.getValue()).containsExactlyInAnyOrder("must not be blank", "must be a valid email");
  }

  @Test
  void getErrorsAsData_multipleViolationsDifferentFields_createsMultipleErrorItems() {
    ConstraintViolation<?> violation1 = createMockViolation("email", "must not be blank", "NotBlank");
    ConstraintViolation<?> violation2 =
        createMockViolation("username", "size must be between 1 and 255", "Size");
    ConstraintViolation<?> violation3 =
        createMockViolation("password", "must not be blank", "NotBlank");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation1);
    violations.add(violation2);
    violations.add(violation3);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).hasSize(3);
    List<String> keys = new ArrayList<>();
    for (ErrorItem item : result.getErrors()) {
      keys.add(item.getKey());
    }
    assertThat(keys).containsExactlyInAnyOrder("email", "username", "password");
  }

  @Test
  void getErrorsAsData_emptyViolations_returnsEmptyErrors() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertThat(result).isNotNull();
    assertThat(result.getMessage()).isEqualTo("BAD_REQUEST");
    assertThat(result.getErrors()).isEmpty();
  }

  @Test
  void getErrorsAsData_nestedPropertyPath_extractsCorrectFieldName() {
    ConstraintViolation<?> violation =
        createMockViolationWithPath("user.profile.email", "must not be blank", "NotBlank");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertThat(result).isNotNull();
    assertThat(result.getErrors()).hasSize(1);
    ErrorItem errorItem = result.getErrors().get(0);
    assertThat(errorItem.getKey()).isEqualTo("email");
  }

  private ConstraintViolation<?> createMockViolation(
      String propertyPath, String message, String annotationType) {
    return createMockViolationWithPath(propertyPath, message, annotationType);
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<?> createMockViolationWithPath(
      String propertyPath, String message, String annotationType) {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) createAnnotationType(annotationType));
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(((ConstraintViolation) violation).getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }

  private Class<?> createAnnotationType(String name) {
    try {
      return Class.forName("javax.validation.constraints." + name);
    } catch (ClassNotFoundException e) {
      return mock(Annotation.class).annotationType();
    }
  }
}
