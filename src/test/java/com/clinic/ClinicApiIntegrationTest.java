package com.clinic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class ClinicApiIntegrationTest {

    private static final String PASSWORD = "Secret123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicCatalogsAreAvailableWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/clinic/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        mockMvc.perform(get("/api/clinic/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
        mockMvc.perform(get("/api/clinic/doctors").param("specialty", "DERMATOLOGY").param("date", "2026-10-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Dr. Felipe Herrera"))
                .andExpect(jsonPath("$[0].availableSlots.length()").value(14));
        mockMvc.perform(get("/api/clinic/doctors").param("specialty", "SURGERY"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/clinic/lab/exams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void registrationValidatesInputAndRejectsDuplicateDocuments() throws Exception {
        register("4000000001", "maria@example.com", "[]")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentNumber").value("4000000001"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        register("4000000001", "maria.other@example.com", "[]")
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/clinic/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentNumber\":\"12\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists());
    }

    @Test
    void protectedEndpointsRequireTheOwnersToken() throws Exception {
        long ownerId = registerAndGetId("4000000002", "owner@example.com", "[]");
        registerAndGetId("4000000003", "intruder@example.com", "[]");

        mockMvc.perform(get("/api/clinic/history/" + ownerId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/clinic/history/" + ownerId).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/clinic/history/" + ownerId).header(HttpHeaders.AUTHORIZATION, bearer("4000000003")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/clinic/history/" + ownerId).header(HttpHeaders.AUTHORIZATION, bearer("4000000002")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/clinic/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentNumber\":\"4000000002\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void schedulingReturnsRemindersAndHandlesConflictsAndCancellation() throws Exception {
        long anaId = registerAndGetId("4000000004", "ana@example.com", "[\"Latex\"]");
        long luisId = registerAndGetId("4000000005", "luis@example.com", "[]");
        String ana = bearer("4000000004");
        String luis = bearer("4000000005");

        MvcResult booked = mockMvc.perform(post("/api/clinic/appointment")
                        .header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointment(anaId, "GENERAL_MEDICINE", "2026-10-20T09:00:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointment.doctorName").value("Dr. Laura Martinez"))
                .andExpect(jsonPath("$.reminders.length()").value(5))
                .andReturn();
        long appointmentId = readJson(booked).at("/appointment/id").asLong();

        mockMvc.perform(post("/api/clinic/appointment").header(HttpHeaders.AUTHORIZATION, luis)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointment(anaId, "CARDIOLOGY", "2026-10-21T09:00:00")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/clinic/appointment").header(HttpHeaders.AUTHORIZATION, luis)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointment(luisId, "GENERAL_MEDICINE", "2026-10-20T09:00:00")))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/clinic/appointment").header(HttpHeaders.AUTHORIZATION, luis)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointment(luisId, "CARDIOLOGY", "2026-10-24T09:00:00")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/clinic/appointment/" + appointmentId).header(HttpHeaders.AUTHORIZATION, luis))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/clinic/appointment/" + appointmentId).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post("/api/clinic/appointment").header(HttpHeaders.AUTHORIZATION, luis)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointment(luisId, "GENERAL_MEDICINE", "2026-10-20T09:00:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void prescriptionIsRejectedWhenItConflictsWithAllergies() throws Exception {
        long patientId = registerAndGetId("4000000006", "pedro@example.com", "[\"Penicillin\"]");
        String token = bearer("4000000006");

        mockMvc.perform(post("/api/clinic/prescription").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prescription(patientId, "Amoxicillin", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.details[0]").value(containsString("Amoxicillin")));

        mockMvc.perform(post("/api/clinic/prescription").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prescription(patientId, "Loratadine", "Allergic rhinitis")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.medications[0].medication").value("Loratadine"))
                .andExpect(jsonPath("$.medications[0].durationDays").value(7));

        mockMvc.perform(get("/api/clinic/history/" + patientId).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultations[0].diagnosis").value("Allergic rhinitis"))
                .andExpect(jsonPath("$.prescriptions.length()").value(1));
    }

    @Test
    void labRequestReturnsResultsWithReferenceRanges() throws Exception {
        long patientId = registerAndGetId("4000000007", "carla@example.com", "[]");
        String token = bearer("4000000007");

        mockMvc.perform(post("/api/clinic/lab").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientId\":" + patientId + ",\"exams\":[\"CBC\",\"GLUCOSE\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exams.length()").value(2))
                .andExpect(jsonPath("$.exams[0].exam").value("CBC"))
                .andExpect(jsonPath("$.exams[0].parameters.length()").value(4))
                .andExpect(jsonPath("$.exams[0].parameters[0].referenceMin").exists())
                .andExpect(jsonPath("$.exams[0].parameters[0].status").exists())
                .andExpect(jsonPath("$.exams[0].parameters[0].withinRange").isBoolean());

        mockMvc.perform(post("/api/clinic/lab").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientId\":" + patientId + ",\"exams\":[\"XRAY\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void demoPatientHasAConsolidatedHistory() throws Exception {
        MvcResult login = login("1000000001", "Demo1234!").andExpect(status().isOk()).andReturn();
        JsonNode session = readJson(login);
        long patientId = session.at("/patient/id").asLong();

        mockMvc.perform(get("/api/clinic/history/" + patientId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + session.get("token").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pastAppointments.length()").value(2))
                .andExpect(jsonPath("$.upcomingAppointments[0].dateTime").value(startsWith("2026-10-20T09:00")))
                .andExpect(jsonPath("$.consultations.length()").value(2))
                .andExpect(jsonPath("$.prescriptions.length()").value(1))
                .andExpect(jsonPath("$.labOrders[0].exams.length()").value(2))
                .andExpect(jsonPath("$.allergies[0].substance").value("Penicillin"));
    }

    @Test
    void unknownEndpointsReturnJsonErrors() throws Exception {
        mockMvc.perform(get("/api/clinic/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private ResultActions register(String document, String email, String allergiesJson) throws Exception {
        String body = """
                {"documentType":"CITIZEN_ID","documentNumber":"%s","firstName":"Test","lastName":"Patient",
                 "birthDate":"1990-01-15","email":"%s","phone":"3009998877","password":"%s","allergies":%s}
                """.formatted(document, email, PASSWORD, allergiesJson);
        return mockMvc.perform(post("/api/clinic/patient").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long registerAndGetId(String document, String email, String allergiesJson) throws Exception {
        MvcResult result = register(document, email, allergiesJson).andExpect(status().isCreated()).andReturn();
        return readJson(result).get("id").asLong();
    }

    private ResultActions login(String document, String password) throws Exception {
        return mockMvc.perform(post("/api/clinic/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"documentNumber\":\"" + document + "\",\"password\":\"" + password + "\"}"));
    }

    private String bearer(String document) throws Exception {
        MvcResult result = login(document, PASSWORD).andExpect(status().isOk()).andReturn();
        return "Bearer " + readJson(result).get("token").asText();
    }

    private static String appointment(long patientId, String specialty, String dateTime) {
        return "{\"patientId\":" + patientId + ",\"specialty\":\"" + specialty + "\",\"dateTime\":\"" + dateTime + "\"}";
    }

    private static String prescription(long patientId, String medication, String diagnosis) {
        String diagnosisJson = diagnosis == null ? "null" : "\"" + diagnosis + "\"";
        return """
                {"patientId":%d,"diagnosis":%s,"medications":[
                  {"name":"%s","dose":"10 mg","frequency":"Once a day","durationDays":7}]}
                """.formatted(patientId, diagnosisJson, medication);
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
