package io.github.guzillo.fruitifystorebackend.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.guzillo.fruitifystorebackend.auth.exception.EmailAlreadyRegisteredException;
import io.github.guzillo.fruitifystorebackend.auth.exception.InvalidTokenException;
import io.github.guzillo.fruitifystorebackend.auth.service.RefreshTokenService;
import io.github.guzillo.fruitifystorebackend.auth.service.SignupService;
import io.github.guzillo.fruitifystorebackend.auth.service.TokenService;
import io.github.guzillo.fruitifystorebackend.category.controller.CategoryController;
import io.github.guzillo.fruitifystorebackend.category.service.CategoryService;
import io.github.guzillo.fruitifystorebackend.common.exception.GlobalExceptionHandler;
import io.github.guzillo.fruitifystorebackend.product.controller.ProductController;
import io.github.guzillo.fruitifystorebackend.product.service.ProductService;
import io.github.guzillo.fruitifystorebackend.security.config.SecurityConfig;
import io.github.guzillo.fruitifystorebackend.security.service.CustomUserDetailsService;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = {AuthController.class, ProductController.class,
    CategoryController.class, AuthControllerIntegrationTest.ProtectedTestController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
    AuthControllerIntegrationTest.ProtectedTestController.class})
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SignupService signupService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private RSAPublicKey jwtPublicKey;

    @MockitoBean
    private RSAPrivateKey jwtPrivateKey;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldAcceptValidSignup() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"john@example.com","password":"password123"}
                    """))
            .andExpect(status().isCreated());

        verify(signupService).signup(any());
    }

    @Test
    void shouldRejectInvalidSignupRequest() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"not-an-email","password":"short"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictForRegisteredEmail() throws Exception {
        doThrow(new EmailAlreadyRegisteredException("john@example.com"))
            .when(signupService).signup(any());

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"john@example.com","password":"password123"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(content().string("Email is already registered: john@example.com"));
    }

    @Test
    void shouldRefreshToken() throws Exception {
        when(refreshTokenService.validateRefreshTokenAndGetUsername("refresh-token"))
            .thenReturn("john@example.com");
        when(userDetailsService.loadUserByUsername("john@example.com"))
            .thenReturn(org.springframework.security.core.userdetails.User
                .withUsername("john@example.com")
                .password("encoded")
                .roles("USER")
                .build());
        when(tokenService.generateToken(
            any(org.springframework.security.core.userdetails.UserDetails.class)))
            .thenReturn("access-token");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"refreshToken":"refresh-token"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void shouldReturnBadRequestForInvalidRefreshToken() throws Exception {
        when(refreshTokenService.validateRefreshTokenAndGetUsername("invalid"))
            .thenThrow(new InvalidTokenException());

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"refreshToken":"invalid"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid token"));
    }

    @Test
    void shouldLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"refreshToken":"refresh-token"}
                    """))
            .andExpect(status().isNoContent());

        verify(refreshTokenService).revokeRefreshToken("refresh-token");
    }

    @Test
    void shouldRejectUnauthenticatedProtectedRequest() throws Exception {
        mockMvc.perform(get("/test/user"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowUserRoleOnUserEndpoint() throws Exception {
        mockMvc.perform(get("/test/user")
                .with(jwt().authorities(() ->
                    "ROLE_USER")))
            .andExpect(status().isOk())
            .andExpect(content().string("hello user"));
    }

    @Test
    void shouldRejectUserRoleOnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/test/admin")
                .with(jwt().authorities(() ->
                    "ROLE_USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminRoleOnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/test/admin")
                .with(jwt().authorities(() ->
                    "ROLE_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string("hello admin"));
    }

    @Test
    void shouldRejectProductCreationForUserRole() throws Exception {
        mockMvc.perform(post("/api/products")
                .with(jwt().authorities(() -> "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Apple",
                      "description": "Fresh apple",
                      "price": 2.49,
                      "stockQuantity": 10,
                      "isActive": true,
                      "categoryId": 1
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowProductCreationForAdminRole() throws Exception {
        when(productService.createProduct(any())).thenReturn(null);

        mockMvc.perform(post("/api/products")
                .with(jwt().authorities(() -> "ROLE_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Apple",
                      "description": "Fresh apple",
                      "price": 2.49,
                      "stockQuantity": 10,
                      "isActive": true,
                      "categoryId": 1
                    }
                    """))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectProductUpdateForUserRole() throws Exception {
        mockMvc.perform(put("/api/products/1")
                .with(jwt().authorities(() -> "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"price": 3.49}
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedProductDeletion() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
            .andExpect(status().isUnauthorized());

        verify(productService, never()).deleteProduct(any());
    }

    @Test
    void shouldRejectProductDeletionForUserRole() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                .with(jwt().authorities(() -> "ROLE_USER")))
            .andExpect(status().isForbidden());

        verify(productService, never()).deleteProduct(any());
    }

    @Test
    void shouldAllowProductDeletionForAdminRole() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                .with(jwt().authorities(() -> "ROLE_ADMIN")))
            .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void shouldRejectUnauthenticatedCategoryCreation() throws Exception {
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Berries", "slug": "berries"}
                    """))
            .andExpect(status().isUnauthorized());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    void shouldRejectCategoryCreationForUserRole() throws Exception {
        mockMvc.perform(post("/api/categories")
                .with(jwt().authorities(() -> "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Berries", "slug": "berries"}
                    """))
            .andExpect(status().isForbidden());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    void shouldAllowCategoryCreationForAdminRole() throws Exception {
        mockMvc.perform(post("/api/categories")
                .with(jwt().authorities(() -> "ROLE_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Berries", "slug": "berries"}
                    """))
            .andExpect(status().isCreated());

        verify(categoryService).createCategory(any());
    }

    @Test
    void shouldRejectUnauthenticatedCategoryDeletion() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
            .andExpect(status().isUnauthorized());

        verify(categoryService, never()).deleteCategory(any());
    }

    @Test
    void shouldRejectCategoryDeletionForUserRole() throws Exception {
        mockMvc.perform(delete("/api/categories/1")
                .with(jwt().authorities(() -> "ROLE_USER")))
            .andExpect(status().isForbidden());

        verify(categoryService, never()).deleteCategory(any());
    }

    @Test
    void shouldAllowCategoryDeletionForAdminRole() throws Exception {
        mockMvc.perform(delete("/api/categories/1")
                .with(jwt().authorities(() -> "ROLE_ADMIN")))
            .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }

    @RestController
    @RequestMapping("/test")
    static class ProtectedTestController {

        @GetMapping("/user")
        @PreAuthorize("hasRole('USER')")
        String user() {
            return "hello user";
        }

        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        String admin() {
            return "hello admin";
        }
    }
}
