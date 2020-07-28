package guru.springframework.sbmbeerorderservice.web.mappers;

import guru.springframework.sbmbeerorderservice.domain.BeerOrderLine;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderLineDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
@DecoratedWith(BeerOrderLineDecorator.class)
public interface BeerOrderLineMapper {

    BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line);

//    @Mapping(target = "beerOrder.id", source = "beerId")
//    @Mapping(target = "beerId", source = "beerId")
    BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto);
}