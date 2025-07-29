package gift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_order")
public class Order {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", updatable = false)
    @NotNull
    private ProductOption productOption;

    @Column(name = "sender_member_id", updatable = false)
    @NotNull
    private Long senderMemberId;

    @Column(name = "receiver_member_id", updatable = false)
    @NotNull
    private Long receiverMemberId;

    @Column(updatable = false)
    @NotNull
    private Integer quantity;

    @Column(name = "order_date_time", updatable = false)
    private LocalDateTime orderDateTime = LocalDateTime.now();

    @Column(updatable = false)
    @NotNull
    private String message;

    protected Order() {
    }

    public Order(ProductOption productOption, Long senderMemberId, Long receiverMemberId, Integer quantity,
            String message) {
        this.productOption = productOption;
        this.senderMemberId = senderMemberId;
        this.receiverMemberId = receiverMemberId;
        this.quantity = quantity;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public ProductOption getProductOption() {
        return productOption;
    }

    public Long getSenderMemberId() {
        return senderMemberId;
    }

    public Long getReceiverMemberId() {
        return receiverMemberId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public String getMessage() {
        return message;
    }

}
