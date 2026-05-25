package com.boxoffice.hubservice.stocktransfer.entity;

import com.boxoffice.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_stock_transfers")
@Getter
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTransfer extends BaseEntity {

    @Column(name = "from_hub_id", nullable = false)
    private UUID fromHubId;

    @Column(name = "to_hub_id", nullable = false)
    private UUID toHubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "total_product_count", nullable = false)
    private Integer totalProductCount;

    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "note", length = 500)
    private String note;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "company_ids", columnDefinition = "jsonb")
    private List<UUID> companyIds = new ArrayList<>();

    @Column(name = "delivery_manager_id")
    private UUID deliveryManagerId;

    @Builder
    private StockTransfer(UUID fromHubId, UUID toHubId, Integer totalProductCount,
                          UUID managerId, List<UUID> companyIds) {
        this.fromHubId = fromHubId;
        this.toHubId = toHubId;
        this.totalProductCount = totalProductCount;
        this.managerId = managerId;
        this.status = TransferStatus.PENDING;
        this.companyIds = companyIds != null ? companyIds : new ArrayList<>();
    }

    public void assignDeliveryManager(UUID deliveryManagerId) {
        this.deliveryManagerId = deliveryManagerId;
    }

    public void revertDispatch() {
        this.status = TransferStatus.PENDING;
        this.dispatchedAt = null;
        this.deliveryManagerId = null;
    }

    public void dispatch() {
        this.status = TransferStatus.IN_PROGRESS;
        this.dispatchedAt = LocalDateTime.now();
    }

    public void complete(String note) {
        this.status = TransferStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.note = note;
    }

    public void cancel() {
        this.status = TransferStatus.CANCELLED;
    }
}
