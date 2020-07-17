package guru.springframework.sbmbeerorderservice.web.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BeerOrderLineDto extends BaseItem {

    @Builder
    public BeerOrderLineDto(UUID id, Integer version, OffsetDateTime createdDate, OffsetDateTime lastModifiedDate,
                            String upc, String beerName, UUID beerId, String beerStyle, BigDecimal price,
                            Integer orderQuantity, Integer quantityAllocated) {
        super(id, version, createdDate, lastModifiedDate);

        this.upc = upc;
        this.beerName = beerName;
        this.beerId = beerId;
        this.beerStyle = beerStyle;
        this.price = price;
        this.orderQuantity = orderQuantity;
        this.quantityAllocated = quantityAllocated;
    }

    private String upc;
    private String beerName;
    private UUID beerId;
    private String beerStyle;
    private BigDecimal price;
    private Integer orderQuantity = 0;
    private Integer quantityAllocated;
}