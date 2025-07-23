package gift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class SoftDeleteEntity extends BaseAuditingEntity {

    @Column
    protected LocalDateTime deletedAt;

    public void setDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    public Boolean isDeleted() {
        return this.deletedAt != null;
    }
}
