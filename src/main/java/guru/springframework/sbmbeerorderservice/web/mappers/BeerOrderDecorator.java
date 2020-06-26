package guru.springframework.sbmbeerorderservice.web.mappers;

import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderDto;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BeerOrderDecorator implements BeerOrderMapper {

    private BeerOrderMapper beerOrderMapper;

    @Autowired
    public void setBeerOrderMapper(BeerOrderMapper beerOrderMapper) {
        this.beerOrderMapper = beerOrderMapper;
    }

    @Override
    public BeerOrderDto beerOrderToDto(BeerOrder beerOrder) {
        BeerOrderDto beerOrderDto = beerOrderMapper.beerOrderToDto(beerOrder);
        beerOrderDto.setCustomerId(beerOrder.getCustomer().getId());

        return beerOrderDto;
    }

    @Override
    public BeerOrder dtoToBeerOrder(BeerOrderDto dto) {
        return beerOrderMapper.dtoToBeerOrder(dto);
    }
}
