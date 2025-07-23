package gift.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import gift.dto.ProductDto;
import gift.entity.Product;
import gift.handler.LoginMemberArgumentResolver;
import gift.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ProductAdminPageController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class})
class ProductAdminPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Test
    void 상품목록_조회_시_상품목록페이지() throws Exception {
        when(productService.getProductList(any(), any())).thenReturn(Page.empty());
        mockMvc.perform(get("/admin/products"))
                .andExpect(view().name("admin/product-list"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    void 상품등록_폼_요청_시_상품상세페이지() throws Exception {
        mockMvc.perform(get("/admin/products/new"))
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void 유효한_상품등록_폼_제출_시_리다이렉션() throws Exception {
        Product mockProduct = new Product(
                1L, "아이스 카페 아메리카노 T", 4700,
                "https://st.kakaocdn.net/product/gift/product/20231010111814_9a667f9eccc943648797925498bdd8a3.jpg",
                false,
                false
        );
        when(productService.createProduct(any())).thenReturn(ProductDto.from(mockProduct));

        mockMvc.perform(
                        post("/admin/products")
                                .param("name", mockProduct.getName())
                                .param("price", String.valueOf(mockProduct.getPrice()))
                                .param("imageUrl", mockProduct.getImageUrl())
                                .param("options[0].name", "Option 1")
                                .param("options[0].quantity", "10")
                )
                .andExpect(redirectedUrl("/admin/products/" + mockProduct.getId()));
    }

    @Test
    void 유효하지_않은_상품등록_폼_제출_시_상품상세페이지() throws Exception {
        Product mockProduct = new Product(
                1L, "&%&각하오커피&%&", -5000,
                "https://st.kakaocdn.net/product/gift/product/20231010111814_9a667f9eccc943648797925498bdd8a3.jpg",
                false,
                false
        );
        when(productService.createProduct(any())).thenReturn(ProductDto.from(mockProduct));

        mockMvc.perform(
                        post("/admin/products")
                                .param("name", mockProduct.getName())
                                .param("price", String.valueOf(mockProduct.getPrice()))
                                .param("imageUrl", mockProduct.getImageUrl())
                                .param("options[0].name", "Option 1")
                                .param("options[0].quantity", "10")
                )
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeHasErrors("createProductRequest"));
    }

    @Test
    void 상품_벨리데이션_수정_시_리다이렉션() throws Exception {
        mockMvc.perform(
                        patch("/admin/products/1")
                                .queryParam("validated", "true")
                )
                .andExpect(redirectedUrl("/admin/products/1"));
    }

    @Test
    void 유효한_상품_조회_시_상품상세페이지() throws Exception {
        Product mockProduct = new Product(1L, "각하오커피", 7800,
                "https://...", false, false);
        when(productService.getProductWhetherDeletedById(any())).thenReturn(
                ProductDto.from(mockProduct));

        mockMvc.perform(get("/admin/products/1"))
                .andExpect(view().name("admin/product-form"));

    }

    @Test
    void 유효한_상품_수정_시_리다이렉션() throws Exception {
        Product updated = new Product(1L, "각하오 커피", 7800, "https://...", false, false);
        when(productService.updateProductById(any())).thenReturn(ProductDto.from(updated));

        mockMvc.perform(
                        put("/admin/products/1")
                                .param("name", "각하오 커피")
                                .param("price", "7800")
                                .param("imageUrl", "https://...")
                )
                .andExpect(redirectedUrl("/admin/products/1"));
    }

    @Test
    void 유효하지_않은_상품_수정_시_상품상세페이지() throws Exception {
        Product existing = new Product(1L, "각하오 커피", 7800, "https://...", false, false);
        when(productService.getProductById(any())).thenReturn(ProductDto.from(existing));
        when(productService.getProductWhetherDeletedById(any())).thenReturn(
                ProductDto.from(existing));

        mockMvc.perform(
                        put("/admin/products/1")
                                .param("name", "%&각하오커피&%")
                                .param("price", "-5800")
                                .param("imageUrl", "")
                )
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeHasErrors("updateProductRequest"));
    }

    @Test
    void 유효한_상품_삭제_시_리다이렉션() throws Exception {
        mockMvc.perform(
                        delete("/admin/products/1")
                )
                .andExpect(redirectedUrl("/admin/products"));
    }
}