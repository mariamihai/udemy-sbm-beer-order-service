package guru.springframework.sbmbeerorderservice.web.mappers;

import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class, BeerOrderLineMapper.class})
public interface BeerOrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    BeerOrderDto beerOrderToDto(BeerOrder beerOrder);

    @Mapping(target = "customer.id", source = "customerId")
    BeerOrder dtoToBeerOrder(BeerOrderDto dto);
}