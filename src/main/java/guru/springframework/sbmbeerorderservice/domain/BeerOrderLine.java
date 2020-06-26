package guru.springframework.sbmbeerorderservice.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class BeerOrderLine extends BaseEntity {

    @Builder
    public BeerOrderLine(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate,
                         BeerOrder beerOrder, UUID beerId, String upc, Integer orderQuantity,
                         Integer quantityAllocated) {
        super(id, version, createdDate, lastModifiedDate);

        this.upc = upc;
        this.beerOrder = beerOrder;
        this.beerId = beerId;
        this.orderQuantity = orderQuantity;
        this.quantityAllocated = quantityAllocated;
    }

    @ManyToOne
    private BeerOrder beerOrder;

    private String upc;
    private UUID beerId;
    private Integer orderQuantity = 0;
    private Integer quantityAllocated = 0;
}
