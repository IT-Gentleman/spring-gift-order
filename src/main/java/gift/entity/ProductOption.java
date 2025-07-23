package gift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "name"}),
})
@EntityListeners(AuditingEntityListener.class)
public class ProductOption extends HardDeleteEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Min(0)
    @Max(100_000_000 - 1)
    private Integer quantity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    protected ProductOption() {
    }

    // only for test code
    public ProductOption(Long id, String name, Integer quantity, Product product) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.product = product;
    }

    // 생성자로 들어오는 값은 RequestDto에서 Validation 수행 상정
    public ProductOption(String name, Integer quantity, Product product) {
        this.name = name;
        this.quantity = quantity;
        this.product = product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void applyPatch(String name, Integer quantity) {
        if (name != null) {
            this.name = name;
        }
        if (quantity != null) {
            this.quantity = quantity;
        }
    }

    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity < 0 || quantity >= 100_000_000) {
            throw new IllegalArgumentException(
                    "Quantity must be between 0 (inclusive) and 100,000,000 (exclusive).");
        }
        this.quantity = quantity;
    }

    public void increaseQuantity(Integer increment) {
        if (increment == null || increment <= 0) {
            throw new IllegalArgumentException("Increment must be a positive number.");
        }
        int newQuantity = this.quantity + increment;
        if (newQuantity >= 100_000_000) {
            throw new IllegalStateException("Quantity cannot exceed 100,000,000.");
        }
        this.quantity = newQuantity;
    }

    public void decreaseQuantity(Integer decrement) {
        if (decrement == null || decrement <= 0) {
            throw new IllegalArgumentException("Decrement must be a positive number.");
        }
        int newQuantity = this.quantity - decrement;
        if (newQuantity < 0) {
            throw new IllegalStateException(
                    "Not enough stock available. Requested: " + decrement + ", Available: "
                            + this.quantity);
        }
        this.quantity = newQuantity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

}
