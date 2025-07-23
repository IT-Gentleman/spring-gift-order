package gift.entity;

import gift.exception.BadRequestException;
import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table
@EntityListeners(AuditingEntityListener.class)
public class Product extends SoftDeleteEntity {

    private static final List<String> prohibitedNames = List.of("카카오");

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean validated;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProductOption> optionList = new ArrayList<>();

    // non-argument constructor for JPA
    protected Product() {
    }

    // all arguments constructor for test code
    public Product(Long id, String name, Integer price, String imageUrl, Boolean validated,
            Boolean deleted) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.validated = validated;
        if (deleted != null && deleted) {
            super.setDeleted();
        }
    }

    // constructor for product creation. use as a factory method
    public Product(String name, Integer price, String imageUrl, List<ProductOption> optionList) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.validated = checkValidatedByName(name);
        optionList.forEach(option -> option.setProduct(this));
        this.optionList = optionList;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<ProductOption> getOptionList() {
        return Collections.unmodifiableList(optionList);
    }

    public ProductOption addOption(String name, Integer quantity) {
        validateOptionNameDuplication(name);

        ProductOption productOption = new ProductOption(name, quantity, this);
        this.optionList.add(productOption);
        return productOption;
    }

    private void validateOptionNameDuplication(String name) {
        if (this.optionList.stream().anyMatch(option -> option.getName().equals(name.trim()))) {
            throw new ConflictException("Option with name '" + name + "' already exists.");
        }
    }

    public ProductOption updateOption(Long optionId, String name, Integer quantity) {
        ProductOption target = findProductOptionById(optionId);
        if (name != null && !target.getName().equals(name.trim())) {
            validateOptionNameDuplication(name);
        }
        target.applyPatch(name, quantity);
        return target;
    }

    public void deleteOption(Long optionId) {
        ProductOption target = findProductOptionById(optionId);
        if (this.optionList.size() == 1) {
            throw new BadRequestException("Cannot delete the last option of a product.");
        } else {
            this.optionList.remove(target);
        }
    }

    private ProductOption findProductOptionById(Long optionId) {
        return this.optionList.stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Option not found with id: " + optionId));
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public Boolean isValidated() {
        return validated;
    }

    public void applyPatch(String name, Integer price, String imageUrl) {
        if (name != null) {
            this.name = name;
        }
        if (price != null) {
            this.price = price;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        this.validated = checkValidatedByName(this.name);
    }

    private Boolean checkValidatedByName(String name) {
        for (String prohibitedName : prohibitedNames) {
            if (name.contains(prohibitedName)) {
                return false;
            }
        }
        return true;
    }
}
