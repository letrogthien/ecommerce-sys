package com.chuadatten.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.chuadatten.product.dto.ProductDto;
import com.chuadatten.product.dto.SearchResultDto;
import com.chuadatten.product.requests.SearchRequest;
import com.chuadatten.product.responses.ApiResponse;
import com.chuadatten.product.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for SearchController
 */
@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSearchProducts_POST() throws Exception {
        // Given
        SearchRequest request = SearchRequest.builder()
                .keyword("minecraft")
                .minPrice(BigDecimal.valueOf(10))
                .maxPrice(BigDecimal.valueOf(100))
                .page(0)
                .size(20)
                .build();

        SearchResultDto<ProductDto> searchResult = SearchResultDto.<ProductDto>builder()
                .items(Collections.emptyList())
                .totalItems(0)
                .currentPage(0)
                .pageSize(20)
                .totalPages(0)
                .executionTimeMs(10)
                .build();

        ApiResponse<SearchResultDto<ProductDto>> response = ApiResponse.<SearchResultDto<ProductDto>>builder()
                .data(searchResult)
                .build();

        when(searchService.searchProducts(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/product-service/search/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(0))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(20));
    }

    @Test
    void testSearchProducts_GET() throws Exception {
        // Given
        SearchResultDto<ProductDto> searchResult = SearchResultDto.<ProductDto>builder()
                .items(Collections.emptyList())
                .totalItems(0)
                .currentPage(0)
                .pageSize(20)
                .totalPages(0)
                .executionTimeMs(10)
                .build();

        ApiResponse<SearchResultDto<ProductDto>> response = ApiResponse.<SearchResultDto<ProductDto>>builder()
                .data(searchResult)
                .build();

        when(searchService.searchProducts(any(SearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/product-service/search/products")
                .param("keyword", "minecraft")
                .param("minPrice", "10")
                .param("maxPrice", "100")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(0));
    }

    @Test
    void testGetProductSuggestions() throws Exception {
        // Given
        List<String> suggestions = List.of("Minecraft Premium", "Minecraft Mods");
        ApiResponse<List<String>> response = ApiResponse.<List<String>>builder()
                .data(suggestions)
                .build();

        when(searchService.getProductSuggestions("minecr", 10)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/product-service/search/suggestions/products")
                .param("q", "minecr")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}