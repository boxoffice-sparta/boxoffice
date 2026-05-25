package boxoffice.deliveryservice.domain.delivery.repository;


import boxoffice.deliveryservice.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

}
