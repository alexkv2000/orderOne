package kvo.order.controller;

import kvo.order.config.DivisionConfig;
import kvo.order.model.ErrorIndicator;
import kvo.order.model.TargetIndicator;
import kvo.order.service.IndicatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndicatorController.class)
class IndicatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IndicatorService service;

    @MockBean
    private DivisionConfig divisionConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetData() throws Exception {
        List<TargetIndicator> indicators = Arrays.asList(new TargetIndicator());
        List<ErrorIndicator> errors = Arrays.asList(new ErrorIndicator());
        List<TargetIndicator.Division> divisions = Arrays.asList(new TargetIndicator.Division("Div1"));

        when(service.getAllIndicators()).thenReturn(indicators);
        when(service.getAllErrors()).thenReturn(errors);
        when(divisionConfig.getDivisions()).thenReturn(divisions);

        mockMvc.perform(get("/api/order/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indicators").isArray())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.divisions").isArray());
    }

    @Test
    void testShowOrderPage() throws Exception {
        List<TargetIndicator> indicators = Arrays.asList(new TargetIndicator());
        List<ErrorIndicator> errors = Arrays.asList(new ErrorIndicator());
        List<TargetIndicator.Division> divisions = Arrays.asList(new TargetIndicator.Division("Div1"));

        when(service.getAllIndicators()).thenReturn(indicators);
        when(service.getAllErrors()).thenReturn(errors);
        when(divisionConfig.getDivisions()).thenReturn(divisions);

        mockMvc.perform(get("/api/order"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("indicators", "errors", "divisions"));
    }

    @Test
    void testUploadFileSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "File uploaded successfully!");

        doNothing().when(service).importFromXls(any());

        mockMvc.perform(multipart("/api/order/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("File uploaded successfully!"));
    }

    @Test
    void testUploadFileInvalidFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());

        mockMvc.perform(multipart("/api/order/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only .xlsx files are allowed!"));
    }

    @Test
    void testTransferErrors() throws Exception {
        List<Long> errorIds = Arrays.asList(1L, 2L);

        mockMvc.perform(post("/api/order/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(errorIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).transferErrors(errorIds);
    }

    @Test
    void testTransferErrorsEmptyList() throws Exception {
        List<Long> errorIds = Arrays.asList();

        mockMvc.perform(post("/api/order/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(errorIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ошибка: Выберите хотя бы одну строку для переноса."));
    }
}
