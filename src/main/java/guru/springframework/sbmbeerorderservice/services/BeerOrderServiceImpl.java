package guru.springframework.sbmbeerorderservice.services;

import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.domain.Customer;
import guru.springframework.sbmbeerorderservice.domain.OrderStatusEnum;
import guru.springframework.sbmbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.sbmbeerorderservice.repositories.CustomerRepository;
import guru.springframework.sbmbeerorderservice.web.controllers.NotFoundException;
import guru.springframework.sbmbeerorderservice.web.mappers.BeerOrderMapper;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderDto;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderPagedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BeerOrderServiceImpl implements BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final CustomerRepository customerRepository;

    public BeerOrderServiceImpl(BeerOrderRepository beerOrderRepository, BeerOrderMapper beerOrderMapper,
                                CustomerRepository customerRepository) {
        this.beerOrderRepository = beerOrderRepository;
        this.beerOrderMapper = beerOrderMapper;
        this.customerRepository = customerRepository;
    }

    @Override
    public BeerOrderPagedList listOrders(UUID customerId, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(NotFoundException::new);

        Page<BeerOrder> beerOrderPage = beerOrderRepository.findAllByCustomer(customer, pageable);

        return new BeerOrderPagedList(
                    beerOrderPage.stream()
                                 .map(beerOrderMapper::beerOrderToDto)
                                 .collect(Collectors.toList()),
                    PageRequest.of(
                            beerOrderPage.getPageable().getPageNumber(),
                            beerOrderPage.getPageable().getPageSize()),
                    beerOrderPage.getTotalElements());
    }

    @Override
    @Transactional
    public BeerOrderDto placeOrder(UUID customerId, BeerOrderDto beerOrderDto) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if(customerOptional.isPresent()) {
            BeerOrder beerOrder = beerOrderMapper.dtoToBeerOrder(beerOrderDto);
            beerOrder.setCustomer(customerOptional.get());
            beerOrder.setOrderStatus(OrderStatusEnum.NEW);

            beerOrder.getBeerOrderLines().forEach(line -> line.setBeerOrder(beerOrder));

            BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);

            log.debug("Saved Beer Order: " + savedBeerOrder.getId());

            return beerOrderMapper.beerOrderToDto(savedBeerOrder);
        }

        throw new NotFoundException("Customer not found");
    }

    @Override
    public BeerOrderDto getOrderById(UUID customerId, UUID orderId) {
        return beerOrderMapper.beerOrderToDto(getOrder(customerId, orderId));
    }

    @Override
    public void pickupOrder(UUID customerId, UUID orderId) {
        BeerOrder beerOrder = getOrder(customerId, orderId);
        beerOrder.setOrderStatus(OrderStatusEnum.PICKED_UP);

        beerOrderRepository.save(beerOrder);
    }

    private BeerOrder getOrder(UUID customerId, UUID orderId) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if(customerOptional.isPresent()){
            Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(orderId);

            if(beerOrderOptional.isPresent()) {
                BeerOrder beerOrder = beerOrderOptional.get();

                if(validOrderForCustomer(beerOrder, customerId)) {
                    return beerOrder;
                }
            }

            throw new NotFoundException("Beer Order Not Found");
        }

        throw new NotFoundException("Customer Not Found");
    }

    private boolean validOrderForCustomer(BeerOrder beerOrder, UUID customerId) {
        return beerOrder.getCustomer().getId().equals(customerId);
    }
}
