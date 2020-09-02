package guru.springframework.sbmbeerorderservice.services;

import guru.springframework.sbmbeerorderservice.bootstrap.BeerLoader;
import guru.springframework.sbmbeerorderservice.domain.Customer;
import guru.springframework.sbmbeerorderservice.repositories.CustomerRepository;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderDto;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class TastingRoomService {

    private final CustomerRepository customerRepository;
    private final BeerOrderService beerOrderService;
    private final List<String> availableBeerUpcs = new ArrayList<>(3);

    public TastingRoomService(CustomerRepository customerRepository, BeerOrderService beerOrderService) {
        this.customerRepository = customerRepository;
        this.beerOrderService = beerOrderService;

        availableBeerUpcs.add(BeerLoader.BEER_1_UPC);
        availableBeerUpcs.add(BeerLoader.BEER_2_UPC);
        availableBeerUpcs.add(BeerLoader.BEER_3_UPC);
    }

    @Scheduled(fixedRate = 12000)
    public void placeTastingRoomOrder() {
        List<Customer> customerList = customerRepository.findAllByCustomerNameLike(BeerLoader.TASTING_ROOM);

        if (customerList.size() == 1){ //should be just one
            doPlaceOrder(customerList.get(0));
        } else {
            log.error("Too many or too few tasting room customers found");
        }
    }

    private void doPlaceOrder(Customer customer) {
        String randomBeerUpc = getRandomBeerUpc();

        BeerOrderLineDto beerOrderLineDto = BeerOrderLineDto.builder()
                .upc(randomBeerUpc)
                .orderQuantity(getRandomIntegerForQuantity())
                .build();

        List<BeerOrderLineDto> beerOrderLineDtoList = new ArrayList<>();
        beerOrderLineDtoList.add(beerOrderLineDto);

        BeerOrderDto beerOrderDto = BeerOrderDto.builder()
                .customerId(customer.getId())
                .customerRef(UUID.randomUUID().toString())
                .beerOrderLines(beerOrderLineDtoList)
                .build();

        beerOrderService.placeOrder(customer.getId(), beerOrderDto);
    }

    private String getRandomBeerUpc() {
        return availableBeerUpcs.get(new Random().nextInt(availableBeerUpcs.size()));
    }

    private Integer getRandomIntegerForQuantity() {
        return new Random().nextInt(5) + 1;
    }
}
