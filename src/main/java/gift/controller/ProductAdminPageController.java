package gift.controller;

import gift.dto.CreateProductRequest;
import gift.dto.NewProductCommand;
import gift.dto.NewProductOptionCommand;
import gift.dto.PageRequest;
import gift.dto.PageResponse;
import gift.dto.ProductDto;
import gift.dto.ProductResponse;
import gift.dto.UpdateProductCommand;
import gift.dto.UpdateProductRequest;
import gift.service.ProductService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class ProductAdminPageController {

    private final ProductService productService;

    public ProductAdminPageController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String getProducts(
            @RequestParam(defaultValue = "true", required = false) Boolean validated,
            Model model,
            @Valid PageRequest pageRequest
    ) {
        Set<String> allowedSortFields = Set.of("id", "name", "price");
        String defaultSortField = "id";
        Sort.Direction defaultSortDirection = Sort.Direction.ASC;
        Pageable pageable = pageRequest.toPageable(allowedSortFields, defaultSortField,
                defaultSortDirection);

        Page<ProductDto> pagedDto = productService.getProductList(validated, pageable);
        Page<ProductResponse> response = pagedDto.map(ProductResponse::from);
        model.addAttribute("products", PageResponse.from(response));
        model.addAttribute("validated", validated);
        return "admin/product-list";
    }

    // 신규상품 등록 form 제공
    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("productId", null);
        model.addAttribute("product", CreateProductRequest.empty());
        return "admin/product-form";
    }

    // 신규상품 등록 form 받고, 검증 및 redirection 수행
    @PostMapping
    public String newProduct(
            @Valid @ModelAttribute CreateProductRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", null);
            model.addAttribute("product", request);
            String errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> "- " + error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            model.addAttribute("message", "Invalid input. Check again.\n" + errorMessages);
            return "admin/product-form";
        }
        NewProductCommand command = new NewProductCommand(
                request.name(),
                request.price(),
                request.imageUrl(),
                request.options().stream()
                        .map(o -> new NewProductOptionCommand(o.name(), o.quantity(), null))
                        .toList()
        );
        ProductDto created = productService.createProduct(command);
        redirectAttributes.addFlashAttribute("message",
                writeMessageWithValidated("Product created", created.validated()));
        return "redirect:/admin/products/" + created.id();
    }

    @PatchMapping("/{id}")
    public String setProductValidated(
            @PathVariable Long id,
            @RequestParam Boolean validated,
            RedirectAttributes redirectAttributes
    ) {
        productService.setProductValidated(id, validated);
        redirectAttributes.addFlashAttribute("message", "Product Validated Status Changed");
        return "redirect:/admin/products/" + id;
    }

    @GetMapping("/{id}")
    public String getProduct(
            @PathVariable Long id,
            Model model
    ) {
        ProductDto dto = productService.getProductWhetherDeletedById(id);
        UpdateProductRequest request = UpdateProductRequest.from(dto);
        Boolean validated = dto.validated();
        Boolean deleted = dto.deleted();
        model.addAttribute("productId", id);
        model.addAttribute("product", dto);
        model.addAttribute("validated", validated);
        model.addAttribute("deleted", deleted); // 상품이 삭제된 경우, 이후 이 페이지에서 추가 수정은 일어나지 않음
        return "admin/product-form";
    }

    @PutMapping("/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateProductRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            ProductDto dto = productService.getProductWhetherDeletedById(id);
            model.addAttribute("productId", id);
            model.addAttribute("validated", dto.validated());
            if (dto.deleted()) {
                model.addAttribute("message",
                        "This product has been deleted and cannot be modified.");
                model.addAttribute("product", UpdateProductRequest.from(dto));
                model.addAttribute("deleted", true);
            } else {
                String errorMessages = bindingResult.getFieldErrors().stream()
                        .map(error -> "- " + error.getDefaultMessage())
                        .collect(Collectors.joining("\n"));
                model.addAttribute("message", "Invalid input. Check again.\n" + errorMessages);
                model.addAttribute("product", request);
                model.addAttribute("deleted", false);
            }
            return "admin/product-form";
        }
        UpdateProductCommand command = new UpdateProductCommand(
                id,
                request.name(),
                request.price(),
                request.imageUrl()
        );
        ProductDto updated = productService.updateProductById(command);
        redirectAttributes.addFlashAttribute("message",
                writeMessageWithValidated("Product updated", updated.validated()));
        return "redirect:/admin/products/" + id;
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        productService.softDeleteProductById(id);
        redirectAttributes.addFlashAttribute("message", "Product deleted");
        return "redirect:/admin/products";
    }

    private String writeMessageWithValidated(String base, Boolean validated) {
        String message = base;
        if (!validated) {
            message += "\n이 상품은 담당MD의 승인을 거쳐 게시됩니다. 그 이전까지는 상품은 조회되지 않으나, 수정은 가능합니다.";
        }
        return message;
    }

}
