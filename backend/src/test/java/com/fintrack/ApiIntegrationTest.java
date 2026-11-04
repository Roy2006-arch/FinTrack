package com.fintrack;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private String token;
    @BeforeEach void register() throws Exception { token = registerUser(); }
    private String registerUser() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        JsonNode result = body(mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("name", "Integration User", "email", email, "password", "IntegrationPass123")))).andExpect(status().isCreated()).andExpect(jsonPath("$.user.password").doesNotExist()).andReturn());
        return result.get("token").asText();
    }
    private JsonNode body(MvcResult result) throws Exception { return json.readTree(result.getResponse().getContentAsString()); }
    private String auth() { return "Bearer " + token; }
    private long category(String name) throws Exception {
        JsonNode list = body(mvc.perform(get("/api/categories").header("Authorization", auth())).andExpect(status().isOk()).andReturn());
        for (JsonNode c : list) if (c.get("name").asText().equals(name)) return c.get("id").asLong();
        throw new AssertionError("Missing category " + name);
    }
    private String tx(long category, String amount, String type, String description) throws Exception {
        return json.writeValueAsString(Map.of("categoryId", category, "amount", amount, "type", type, "description", description, "paymentMethod", "UPI", "transactionDate", LocalDate.now().toString()));
    }
    @Test void crudFilteringReportsAndCsvWorkEndToEnd() throws Exception {
        long food = category("Food"), salary = category("Salary");
        mvc.perform(post("/api/transactions").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(tx(salary, "10000.00", "INCOME", "Salary"))).andExpect(status().isCreated());
        long id = body(mvc.perform(post("/api/transactions").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(tx(food, "500.25", "EXPENSE", "Lunch, friends"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        mvc.perform(get("/api/transactions").header("Authorization", auth()).param("search", "lunch").param("type", "EXPENSE").param("paymentMethod", "UPI").param("categoryId", String.valueOf(food)).param("sort", "amount").param("direction", "asc").param("size", "1")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].amount").value(500.25));
        mvc.perform(put("/api/transactions/{id}", id).header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(tx(food, "700.50", "EXPENSE", "Updated lunch"))).andExpect(status().isOk()).andExpect(jsonPath("$.amount").value(700.50));
        mvc.perform(get("/api/dashboard/summary").header("Authorization", auth())).andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(9299.50)).andExpect(jsonPath("$.transactionCount").value(2));
        mvc.perform(get("/api/reports/date-range").header("Authorization", auth()).param("start", LocalDate.now().toString()).param("end", LocalDate.now().toString())).andExpect(status().isOk()).andExpect(jsonPath("$.highestExpense").value(700.50)).andExpect(jsonPath("$.averageTransaction").value(5350.25));
        mvc.perform(get("/api/transactions/export").header("Authorization", auth()).param("type", "EXPENSE")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/csv")).andExpect(content().string(org.hamcrest.Matchers.containsString("Updated lunch")));
        mvc.perform(delete("/api/transactions/{id}", id).header("Authorization", auth())).andExpect(status().isNoContent());
        mvc.perform(get("/api/transactions/{id}", id).header("Authorization", auth())).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Transaction not found"));
    }
    @Test void crossUserAccessAndCategoryInjectionAreRejected() throws Exception {
        long food = category("Food");
        long id = body(mvc.perform(post("/api/transactions").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(tx(food, "100", "EXPENSE", "Private"))).andReturn()).get("id").asLong();
        token = registerUser();
        mvc.perform(get("/api/transactions/{id}", id).header("Authorization", auth())).andExpect(status().isNotFound());
        mvc.perform(delete("/api/transactions/{id}", id).header("Authorization", auth())).andExpect(status().isNotFound());
        mvc.perform(post("/api/transactions").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(tx(food, "100", "EXPENSE", "Injection"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/transactions").header("Authorization", auth())).andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test void validationErrorsAreConsistentAndSortIsAllowlisted() throws Exception {
        mvc.perform(post("/api/transactions").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(tx(category("Food"), "-1", "EXPENSE", "Invalid"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.errors.amount").exists()).andExpect(jsonPath("$.timestamp").exists()).andExpect(jsonPath("$.path").value("/api/transactions"));
        mvc.perform(get("/api/transactions").header("Authorization", auth()).param("sort", "user.password")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/transactions").header("Authorization", auth()).param("size", "1000")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reports/date-range").header("Authorization", auth()).param("start", "2026-02-01").param("end", "2026-01-01")).andExpect(status().isBadRequest());
    }
    @Test void budgetAndGoalRulesAndOwnershipAreEnforced() throws Exception {
        LocalDate now = LocalDate.now(); long food = category("Food");
        mvc.perform(post("/api/transactions").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(tx(food, "3800", "EXPENSE", "Food"))).andExpect(status().isCreated());
        String input = json.writeValueAsString(Map.of("categoryId", food, "amount", "5000", "month", now.getMonthValue(), "year", now.getYear()));
        long budget = body(mvc.perform(post("/api/budgets").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isCreated()).andExpect(jsonPath("$.usagePercentage").value(76)).andExpect(jsonPath("$.warningLevel").value("WARNING")).andReturn()).get("id").asLong();
        mvc.perform(post("/api/budgets").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isConflict());
        String goalInput = json.writeValueAsString(Map.of("goalName", "Laptop", "targetAmount", "80000", "savedAmount", "45000", "deadline", now.plusDays(30).toString()));
        long goal = body(mvc.perform(post("/api/goals").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(goalInput)).andExpect(status().isCreated()).andExpect(jsonPath("$.progressPercentage").value(56.25)).andReturn()).get("id").asLong();
        token = registerUser();
        mvc.perform(get("/api/budgets/{id}", budget).header("Authorization", auth())).andExpect(status().isNotFound());
        mvc.perform(put("/api/goals/{id}", goal).header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(goalInput)).andExpect(status().isNotFound());
    }
    @Test void categoryInUseCannotBeDeletedOrRetyped() throws Exception {
        long food = category("Food"); mvc.perform(post("/api/transactions").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(tx(food, "10", "EXPENSE", "Food"))).andExpect(status().isCreated());
        mvc.perform(delete("/api/categories/{id}", food).header("Authorization", auth())).andExpect(status().isConflict());
        mvc.perform(put("/api/categories/{id}", food).header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Food\",\"type\":\"INCOME\"}")).andExpect(status().isConflict());
        mvc.perform(post("/api/transactions").header("Authorization", auth()).contentType(MediaType.APPLICATION_JSON).content(tx(category("Salary"), "10", "EXPENSE", "Wrong type"))).andExpect(status().isBadRequest());
    }
    @Test void logoutRevokesTokenAndProtectedRoutesRequireAuthentication() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout").header("Authorization", auth())).andExpect(status().isNoContent());
        mvc.perform(get("/api/users/me").header("Authorization", auth())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid-token")).andExpect(status().isUnauthorized());
    }
    @Test void corsOnlyAllowsConfiguredFrontendOrigins() throws Exception {
        mvc.perform(options("/api/transactions").header("Origin", "http://localhost:5500").header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "authorization,content-type")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5500"));
        mvc.perform(options("/api/transactions").header("Origin", "https://untrusted.example").header("Access-Control-Request-Method", "GET")).andExpect(status().isForbidden());
    }
}
