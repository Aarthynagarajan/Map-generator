# AI Provider Migration Report: OpenAI to Google Gemini

This report documents the migration of the artificial intelligence provider in the **ProcessPro** project from Spring AI OpenAI to Spring AI Google GenAI (Gemini) using a Google AI Studio API key.

---

## 1. Files Modified

| Relative File Path | Purpose of Change |
| :--- | :--- |
| [backend/pom.xml](file:///d:/New%20folder%20(4)/processpro/backend/pom.xml) | Replaced OpenAI starter with the Google GenAI model starter (`1.1.0-M1` version integration). |
| [AiService.java](file:///d:/New%20folder%20(4)/processpro/backend/src/main/java/com/processmap/ai/service/AiService.java) | Replaced `OpenAiChatOptions` with `GoogleGenAiChatOptions` and structured output formatting. |
| [application.yml](file:///d:/New%20folder%20(4)/processpro/backend/src/main/resources/application.yml) | Swapped the `spring.ai.openai` configuration block for the new `spring.ai.google.genai` properties mapping. |
| [application-dev.yml](file:///d:/New%20folder%20(4)/processpro/backend/src/main/resources/application-dev.yml) | Updated dev profile properties block to Gemini configuration. |
| [application-test.yml](file:///d:/New%20folder%20(4)/processpro/backend/src/main/resources/application-test.yml) | Updated test profile properties key fallback to `mock-test-key`. |
| [.env.example](file:///d:/New%20folder%20(4)/processpro/.env.example) | Renamed the required key variable to `GEMINI_API_KEY`. |
| [docker-compose.yml](file:///d:/New%20folder%20(4)/processpro/docker-compose.yml) | Mapped `GEMINI_API_KEY` into the container environmental properties. |
| [INSTALL.md](file:///d:/New%20folder%20(4)/processpro/INSTALL.md) | Documented the new credential requirements. |

---

## 2. Dependency & POM Configuration Changes

Removed the old OpenAI starter dependency:
```xml
<!-- REMOVED -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

Configured the new Spring AI Google GenAI starter and version BOM adjustments:
```xml
<!-- ADDED -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai</artifactId>
    <version>1.1.0-M1</version>
</dependency>
```
*Note: Changed `<spring-ai.version>` property from `1.0.0-M4` to `1.1.0-M1` for Google GenAI compatibility.*

---

## 3. Configuration Properties Comparison

### Before (OpenAI):
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:mock-key}
      chat:
        options:
          model: gpt-4o
          temperature: 0.1
```

### After (Google Gemini / GenAI):
```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY:mock-key}
        chat:
          options:
            model: gemini-1.5-flash
            temperature: 0.1
```

---

## 4. Code Refactoring Details (`AiService.java`)

1. **Option Builder Mapping**:
   Mapped OpenAI options to `GoogleGenAiChatOptions`:
   ```java
   GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
           .model("gemini-1.5-flash")
           .temperature(0.1)
           .responseMimeType("application/json")
           .build();
   ```
2. **Text Retrieval**:
   Updated the response extraction to retrieve output string content using the standard `getText()` method instead of `getContent()`.

---

## 5. Verification Performed

- **Clean Compilation**: Compiled the source successfully using `mvn clean test-compile` (BUILD SUCCESS).
- **Test Executions**: Executed the entire test suite `mvn test` verifying zero regression failures (25/25 tests passed).
- **Structured JSON Safety**: Ensured that the configured options enforce the same clean JSON payload structure, integrating perfectly with the rest of the parsing and generation pipelines.
