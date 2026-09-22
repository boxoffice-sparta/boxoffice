package boxoffice.orderservice.domain.repository;

import boxoffice.orderservice.domain.entity.Order;
import java.util.Optional;
import java.util.UUID;
import boxoffice.orderservice.domain.vo.OrderSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {

    Optional<Order> findByIdWithProducts(UUID orderId);
    Page<Order> searchOrders(OrderSearchCondition condition, Pageable pageable);
}
