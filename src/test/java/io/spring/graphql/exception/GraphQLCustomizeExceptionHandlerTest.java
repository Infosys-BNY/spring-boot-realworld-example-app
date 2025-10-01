package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.constraints.NotBlank;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @Mock private DataFetcherExceptionHandlerParameters handlerParameters;

  @Mock private ConstraintViolation<?> violation;

  @Mock private Path propertyPath;

  @Mock private ConstraintDescriptor<?> constraintDescriptor;

  @Mock private Annotation annotation;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @Test
  void onException_withInvalidAuthenticationException_returnsUnauthenticatedError() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    ResultPath path = ResultPath.parse("/user/login");

    when(handlerParameters.getException()).thenReturn(exception);
    when(handlerParameters.getPath()).thenReturn(path);

    DataFetcherExceptionHandlerResult result = handler.onException(handlerParameters);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());

    GraphQLError error = result.getErrors().get(0);
    assertTrue(error instanceof TypedGraphQLError);
    assertEquals("invalid email or password", error.getMessage());
    assertEquals(path.toList(), error.getPath());
  }

  @Test
  void onException_withConstraintViolationException_returnsBadRequestError() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    ResultPath path = ResultPath.parse("/user/register");

    when(handlerParameters.getException()).thenReturn(exception);
    when(handlerParameters.getPath()).thenReturn(path);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(propertyPath.toString()).thenReturn("email");
    when(violation.getMessage()).thenReturn("must not be blank");
    when(violation.getConstraintDescriptor()).thenAnswer(invocation -> constraintDescriptor);
    when(constraintDescriptor.getAnnotation()).thenReturn(annotation);
    when(annotation.annotationType()).thenReturn((Class) NotBlank.class);

    DataFetcherExceptionHandlerResult result = handler.onException(handlerParameters);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());

    GraphQLError error = result.getErrors().get(0);
    assertTrue(error instanceof TypedGraphQLError);
    assertNotNull(error.getExtensions());
  }

  @Test
  void onException_withConstraintViolationException_extractsFieldErrors() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    ResultPath path = ResultPath.parse("/user/register");

    when(handlerParameters.getException()).thenReturn(exception);
    when(handlerParameters.getPath()).thenReturn(path);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(propertyPath.toString()).thenReturn("arg0.registerParam.email");
    when(violation.getMessage()).thenReturn("must be a valid email");
    when(violation.getConstraintDescriptor()).thenAnswer(invocation -> constraintDescriptor);
    when(constraintDescriptor.getAnnotation()).thenReturn(annotation);
    when(annotation.annotationType()).thenReturn((Class) NotBlank.class);

    DataFetcherExceptionHandlerResult result = handler.onException(handlerParameters);

    assertNotNull(result);
    GraphQLError error = result.getErrors().get(0);
    Map<String, Object> extensions = error.getExtensions();
    assertTrue(extensions.containsKey("email"));
  }

  @Test
  void onException_withOtherException_delegatesToDefaultHandler() {
    RuntimeException exception = new RuntimeException("Some error");
    ResultPath path = ResultPath.parse("/test/path");

    when(handlerParameters.getException()).thenReturn(exception);
    when(handlerParameters.getPath()).thenReturn(path);

    DataFetcherExceptionHandlerResult result = handler.onException(handlerParameters);

    assertNotNull(result);
  }

  @Test
  void getErrorsAsData_withConstraintViolationException_returnsErrorObject() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(propertyPath.toString()).thenReturn("email");
    when(violation.getMessage()).thenReturn("must not be blank");
    when(violation.getConstraintDescriptor()).thenAnswer(invocation -> constraintDescriptor);
    when(constraintDescriptor.getAnnotation()).thenReturn(annotation);
    when(annotation.annotationType()).thenReturn((Class) NotBlank.class);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertEquals(1, error.getErrors().size());
  }

  @Test
  void getErrorsAsData_withMultipleViolations_groupsByField() {
    ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
    Path propertyPath2 = mock(Path.class);
    ConstraintDescriptor<?> constraintDescriptor2 = mock(ConstraintDescriptor.class);
    Annotation annotation2 = mock(Annotation.class);

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    violations.add(violation2);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(propertyPath.toString()).thenReturn("email");
    when(violation.getMessage()).thenReturn("must not be blank");
    when(violation.getConstraintDescriptor()).thenAnswer(invocation -> constraintDescriptor);
    when(constraintDescriptor.getAnnotation()).thenReturn(annotation);
    when(annotation.annotationType()).thenReturn((Class) NotBlank.class);

    when(violation2.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation2.getPropertyPath()).thenReturn(propertyPath2);
    when(propertyPath2.toString()).thenReturn("email");
    when(violation2.getMessage()).thenReturn("must be a valid email");
    when(violation2.getConstraintDescriptor()).thenAnswer(invocation -> constraintDescriptor2);
    when(constraintDescriptor2.getAnnotation()).thenReturn(annotation2);
    when(annotation2.annotationType()).thenReturn((Class) NotBlank.class);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertNotNull(error);
    assertEquals(1, error.getErrors().size());
    assertEquals(2, error.getErrors().get(0).getValue().size());
  }

  @Test
  void getErrorsAsData_withNestedPropertyPath_extractsCorrectFieldName() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(propertyPath.toString()).thenReturn("registerParam.user.email");
    when(violation.getMessage()).thenReturn("must not be blank");
    when(violation.getConstraintDescriptor()).thenAnswer(invocation -> constraintDescriptor);
    when(constraintDescriptor.getAnnotation()).thenReturn(annotation);
    when(annotation.annotationType()).thenReturn((Class) NotBlank.class);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertNotNull(error);
    assertEquals(1, error.getErrors().size());
    assertEquals("email", error.getErrors().get(0).getKey());
  }
}
