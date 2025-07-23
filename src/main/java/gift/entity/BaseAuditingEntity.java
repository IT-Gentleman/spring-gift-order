package gift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

// Entity에 이 추상클래스를 직접 상속받는것은 권장하지 않습니다.
// 대신 HardDeleteEntity, SoftDeleteEntity를 상속받아 사용하시길 권장합니다. (명확성 증가)
@MappedSuperclass
public abstract class BaseAuditingEntity {

    @CreatedDate
    @Column(updatable = false)
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column
    protected LocalDateTime updatedAt;

    @CreatedBy
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    protected Member createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    protected Member updatedBy;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Member getCreatedBy() {
        return createdBy;
    }

    public Member getUpdatedBy() {
        return updatedBy;
    }
}
