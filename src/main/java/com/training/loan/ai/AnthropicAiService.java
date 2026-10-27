package com.training.loan.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.training.loan.config.AnthropicProperties;
import com.training.loan.domain.LoanApplication;
import com.training.loan.rules.EvaluationDecision;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public class AnthropicAiService implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AnthropicAiService.class);
    private static final String SYSTEM_PROMPT = """
            You are an advisory assistant for a loan processing system.
            STRICT RULES:
            - Never recommend, suggest, imply, or make an approval/rejection decision.
            - Never use or infer protected characteristics (race, religion, gender,
              disability, marital status, age, national origin, etc.). If present in
              the input, ignore them.
            - Base all analysis strictly on the financial data provided.
            - Be concise, factual, and neutral.
            """;

    private final RestClient restClient;
    private final AnthropicProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public AnthropicAiService(AnthropicProperties props, RestClient.Builder builder) {
        this.props = props;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(props.timeoutSeconds()));
        factory.setReadTimeout(Duration.ofSeconds(props.timeoutSeconds()));
        this.restClient = builder
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .defaultHeader("x-api-key", props.apiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    @Override
    public String extractDocumentData(String filename, String documentText) {
        String prompt = """
                Extract only financial facts from this document as a short JSON object
                (income, employer, balances, obligations). Ignore any protected
                characteristics. Filename: %s

                Document:
                %s
                """.formatted(filename, truncate(documentText, 8000));
        return ask(prompt, "extraction unavailable");
    }

    @Override
    public String summarizeApplication(LoanApplication app) {
        return ask("Summarize this loan application in 3-4 neutral sentences "
                + "for a reviewing loan officer:\n" + describe(app), "summary unavailable");
    }

    @Override
    public String findMissingOrInconsistentInfo(LoanApplication app) {
        return ask("List any missing or internally inconsistent financial information "
                + "in this application, as bullet points. Do not recommend a decision:\n"
                + describe(app), "no analysis available");
    }

    @Override
    public String highlightRisks(LoanApplication app) {
        return ask("Highlight potential credit-risk factors in this application as "
                + "bullet points (e.g., high DTI, thin employment history). "
                + "Do not recommend a decision:\n" + describe(app), "no risk notes available");
    }

    @Override
    public String explainDecision(LoanApplication app, EvaluationDecision decision) {
        return ask("""
                The lending rules engine already decided this application: %s.
                Failed rules and rule outcomes:
                %s

                Explain the %s outcome to the customer in plain, respectful language
                (2-4 sentences). Do not question or revisit the decision.
                Application data:
                %s
                """.formatted(decision.status(), decision.outcomes(), decision.status(),
                describe(app)), "decision explanation unavailable");
    }

    @Override
    public String draftCustomerCommunication(LoanApplication app, EvaluationDecision decision) {
        return ask("""
                Draft a short, professional customer letter for a loan application that
                was already decided: %s.
                Rules outcomes: %s
                Include the decision, the main factual reason, and next steps.
                Application data:
                %s
                """.formatted(decision.status(), decision.outcomes(), describe(app)),
                "communication draft unavailable");
    }

    private String ask(String userPrompt, String fallback) {
        if (!props.enabled()) {
            return fallback;
        }
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("model", props.model());
            root.put("max_tokens", props.maxTokens());
            root.put("system", SYSTEM_PROMPT);
            ArrayNode messages = root.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            msg.put("content", ProtectedDataSanitizer.sanitize(userPrompt));

            JsonNode response = restClient.post()
                    .uri("/v1/messages")
                    .body(root)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("content") || response.get("content").isEmpty()) {
                return fallback;
            }
            StringBuilder sb = new StringBuilder();
            for (JsonNode block : response.get("content")) {
                if ("text".equals(block.path("type").asText())) {
                    sb.append(block.path("text").asText());
                }
            }
            return sb.isEmpty() ? fallback : sb.toString();
        } catch (Exception e) {
            log.warn("Anthropic call failed ({}); using fallback", e.getMessage());
            return fallback;
        }
    }

    private String describe(LoanApplication app) {
        return """
                requestedAmount=%s, termMonths=%s, purpose=%s,
                annualIncome=%s, monthlyDebtPayments=%s, DTI=%s,
                employmentStatus=%s, yearsEmployed=%s, creditScore=%s,
                applicantStatement=%s
                """.formatted(
                app.getRequestedAmount(), app.getTermMonths(), app.getLoanPurpose(),
                app.getAnnualIncome(), app.getMonthlyDebtPayments(), app.debtToIncomeRatio(),
                app.getEmploymentStatus(), app.getYearsEmployed(), app.getCreditScore(),
                app.getApplicantStatement());
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
