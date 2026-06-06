package com.piringkita.demo.controller;

import com.piringkita.demo.service.ReviewService;
import com.piringkita.demo.service.UserService;
import com.piringkita.demo.service.WarungService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(WarungController.class)
class WarungControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WarungService warungService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private UserService userService;

    @Test
    void testFormTambahWarung() throws Exception {
        mockMvc.perform(get("/tambahwarung"))
                .andExpect(status().isOk())
                .andExpect(view().name("tambahwarung"));
    }
}
