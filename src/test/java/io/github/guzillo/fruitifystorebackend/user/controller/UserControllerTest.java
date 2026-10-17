package io.github.guzillo.fruitifystorebackend.user.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guzillo.fruitifystorebackend.user.service.UserService;
import io.github.guzillo.fruitifystorebackend.user.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private Authentication authentication;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setMessageConverters(new JacksonJsonHttpMessageConverter())
            .build();
    }

    @Test
    void shouldReceiveUserDataForAuthenticatedUser() throws Exception {
        when(authentication.getName()).thenReturn("user@email.com");
        when(userService.findUserByEmailAndReturnDto("user@email.com"))
            .thenReturn(new UserDto("user@email.com", "John", "Doe"));

        mockMvc.perform(get("/api/users/me")
                .principal(authentication))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.email").value("user@email.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.roles").doesNotExist());

        verify(userService).findUserByEmailAndReturnDto("user@email.com");

    }
}
