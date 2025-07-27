package com.server.money_touch.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.money_touch.global.apiPayload.code.BaseErrorCode;
import com.server.money_touch.global.apiPayload.code.ErrorReasonDTO;
import com.server.money_touch.global.apiPayload.code.ReasonDTO;
import com.server.money_touch.global.apiPayload.code.status.SuccessStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import com.server.money_touch.global.validation.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


@Slf4j
@Configuration
public class SwaggerConfig {
    @Value("${swagger.server-url}")
    private String swaggerServerUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public OpenAPI moneyTouchAPI() {
        Info info = new Info()
                .title("돈터치 API")
                .description("돈터치 API 명세서")
                .version("1.0.0");

        String jwtSchemeName = "JWT TOKEN";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .addServersItem(new Server().url(swaggerServerUrl))
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            ApiErrorCodeExamples errorAnnotations = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples.class);
            if (errorAnnotations != null) {
                for (ApiErrorCodeExample e : errorAnnotations.value()) {
                    handleErrorCode(operation, e.value(), e.name());
                }
            } else {
                ApiErrorCodeExample single = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
                if (single != null) {
                    handleErrorCode(operation, single.value(), single.name());
                }
            }

            ApiSuccessCodeExample successAnnotation = handlerMethod.getMethodAnnotation(ApiSuccessCodeExample.class);
            if (successAnnotation != null) {
                generateSuccessCodeResponseExample(operation, successAnnotation.resultClass());
            }

            return operation;
        };
    }

    private void handleErrorCode(Operation operation, Class<? extends Enum<?>> enumClass, String name) {
        for (Enum<?> constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(name) && constant instanceof BaseErrorCode errorCode) {
                generateSingleErrorCodeResponseExample(operation, errorCode);
                break;
            }
        }
    }

    private void generateSingleErrorCodeResponseExample(Operation operation, BaseErrorCode errorCode) {
        ErrorReasonDTO reason = errorCode.getReasonHttpStatus();
        String httpStatusCode = String.valueOf(reason.getHttpStatus().value());

        String exampleJson = String.format("""
        {
          "isSuccess": false,
          "code": "%s",
          "message": "%s"
        }
        """, reason.getCode(), reason.getMessage());

        io.swagger.v3.oas.models.responses.ApiResponse apiResponse =
                operation.getResponses().computeIfAbsent(httpStatusCode, code ->
                        new io.swagger.v3.oas.models.responses.ApiResponse()
                                .description("에러 응답")
                                .content(new Content()));

        MediaType mediaType = apiResponse.getContent()
                .computeIfAbsent("application/json", k -> new MediaType());

        mediaType.addExamples(errorCode.getReason().getCode(), new Example().value(exampleJson));
    }

    private void generateSuccessCodeResponseExample(Operation operation, Class<?> resultClass) {
        ReasonDTO reason = SuccessStatus._OK.getReasonHttpStatus();
        String httpStatusCode = String.valueOf(reason.getHttpStatus().value());

        String resultJson;
        try {
            Object dtoInstance = generateDtoFromSchemaExample(resultClass); // ← 수정된 부분
            String dtoJson = objectMapper.writeValueAsString(dtoInstance);

            resultJson = String.format("""
            {
              "isSuccess": true,
              "code": "%s",
              "message": "%s",
              "result": %s
            }
            """, reason.getCode(), reason.getMessage(), dtoJson);

        } catch (Exception e) {
            resultJson = String.format("""
            {
              "isSuccess": true,
              "code": "%s",
              "message": "%s",
              "result": {}
            }
            """, reason.getCode(), reason.getMessage());
        }

        Content content = new Content();
        MediaType mediaType = new MediaType();
        content.addMediaType("application/json", mediaType);

        io.swagger.v3.oas.models.responses.ApiResponse apiResponse =
                new io.swagger.v3.oas.models.responses.ApiResponse()
                        .description("성공 응답")
                        .content(content);

        operation.getResponses().put(httpStatusCode, apiResponse);
        mediaType.addExamples("COMMON200", new Example().value(resultJson));
    }

    private Object generateDtoFromSchemaExample(Class<?> dtoClass) throws Exception {
        Object instance = dtoClass.getDeclaredConstructor().newInstance();

        for (Field field : dtoClass.getDeclaredFields()) {
            field.setAccessible(true);

            Schema schema = field.getAnnotation(Schema.class);
            if (schema == null) continue;

            String exampleValue = schema.example();
            if (exampleValue.isEmpty()) continue;

            Class<?> fieldType = field.getType();

            if (fieldType == String.class) {
                field.set(instance, exampleValue);

            } else if (fieldType == Integer.class || fieldType == int.class) {
                field.set(instance, Integer.parseInt(exampleValue));

            } else if (fieldType == Long.class || fieldType == long.class) {
                field.set(instance, Long.parseLong(exampleValue));

            } else if (fieldType == LocalDateTime.class) {
                try {
                    // Swagger에 입력된 example 문자열을 LocalDateTime으로 변환
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    LocalDateTime parsed = LocalDateTime.parse(exampleValue.trim(), formatter);
                    field.set(instance, parsed);
                    log.info(parsed.toString());
                } catch (DateTimeParseException e) {
//                    log.warn("LocalDateTime 파싱 실패: {}, 기본값 null 처리", exampleValue);
                    field.set(instance, null);
                }
            } else if (List.class.isAssignableFrom(fieldType)) {
                // 리스트 타입 처리
                Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (genericType instanceof Class<?> genericClass) {
                    Object childDto = generateDtoFromSchemaExample(genericClass);
                    field.set(instance, List.of(childDto));
                }

            } else {
                // nested DTO 객체 처리
                Object nestedObject = generateDtoFromSchemaExample(fieldType);
                field.set(instance, nestedObject);
            }
        }

        return instance;
    }
}
