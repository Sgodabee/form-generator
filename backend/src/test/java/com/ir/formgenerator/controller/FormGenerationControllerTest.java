package com.ir.formgenerator.controller;

import com.ir.formgenerator.model.GenerationResult;
import com.ir.formgenerator.service.FormGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FormGenerationController.class)
@Import(com.ir.formgenerator.config.SecurityConfig.class)
class FormGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FormGenerationService formGenerationService;

    @Test
    @WithMockUser(username = "testuser")
    void generate_authenticated_returns200WithResult() throws Exception {
        GenerationResult result = new GenerationResult(
                "form_20260101_120000.pdf",
                "/local/form_20260101_120000.pdf",
                "s3://bucket/form_20260101_120000.pdf",
                15L,
                45L
        );
        when(formGenerationService.generate(anyString())).thenReturn(result);

        mockMvc.perform(post("/api/forms/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pdfFileName").value("form_20260101_120000.pdf"))
                .andExpect(jsonPath("$.localTransferMillis").value(15))
                .andExpect(jsonPath("$.s3TransferMillis").value(45))
                .andExpect(jsonPath("$.message").value("PDF generated successfully."));
    }

    @Test
    void generate_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/forms/generate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void generate_serviceThrows_returns500() throws Exception {
        when(formGenerationService.generate(anyString()))
                .thenThrow(new RuntimeException("Generation failed"));

        mockMvc.perform(post("/api/forms/generate"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Generation failed"));
    }
}
