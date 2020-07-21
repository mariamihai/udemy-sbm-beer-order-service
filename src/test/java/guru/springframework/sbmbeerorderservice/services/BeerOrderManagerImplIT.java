package guru.springframework.sbmbeerorderservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderLine;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.sbmbeerorderservice.domain.Customer;
import guru.springframework.sbmbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.sbmbeerorderservice.repositories.CustomerRepository;
import guru.springframework.sbmbeerorderservice.services.beer.BeerService;
import guru.springframework.sbmbeerorderservice.web.model.events.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ExtendWith(WireMockExtension.class)
public class BeerOrderManagerImplIT {

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerService beerService;

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    ObjectMapper objectMapper;

    Customer customer;

    UUID beerId = UUID.randomUUID();
    String upc = "12345";

    public static final String FAILED_VALIDATION = "fail-validation";

    @TestConfiguration
    static class RestTemplateBuilderProvider {

        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer wireMockServer = with(wireMockConfig().port(8083));
            wireMockServer.start();

            return wireMockServer;
        }
    }

    @BeforeEach
    void setUp() {
        customer = customerRepository.save(Customer.builder()
                .customerName("Test Customer")
                .build());
    }

    @Test
    void testNewToAllocate() throws JsonProcessingException {
        String stringBeerDto = objectMapper.writeValueAsString(createBeerDto());
        wireMockServer.stubFor(get(beerService.getBeerUpcPath() + upc).willReturn(okJson(stringBeerDto)));
        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrder.getId());

            assertTrue(optionalBeerOrder.isPresent());
            assertEquals(BeerOrderStatusEnum.ALLOCATED, optionalBeerOrder.get().getOrderStatus());
        });

        await().untilAsserted(() -> {
            Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrder.getId());

            assertTrue(optionalBeerOrder.isPresent());
            BeerOrderLine line = optionalBeerOrder.get().getBeerOrderLines().iterator().next();

            assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
        });

        Optional<BeerOrder> optionalResult = beerOrderRepository.findById(savedBeerOrder.getId());
        assertTrue(optionalResult.isPresent());

        savedBeerOrder = optionalResult.get();

        assertNotNull(savedBeerOrder);
        assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder.getOrderStatus());
        savedBeerOrder.getBeerOrderLines().forEach(line ->
                                                   assertEquals(line.getOrderQuantity(), line.getQuantityAllocated()));
    }

    @Test
    void testFailedValidation() throws JsonProcessingException {
        String stringBeerDto = objectMapper.writeValueAsString(createBeerDto());
        wireMockServer.stubFor(get(beerService.getBeerUpcPath() + upc).willReturn(okJson(stringBeerDto)));
        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef(FAILED_VALIDATION);

        beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrder.getId());

            assertTrue(optionalBeerOrder.isPresent());
            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, optionalBeerOrder.get().getOrderStatus());
        });
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        String stringBeerDto = objectMapper.writeValueAsString(createBeerDto());
        wireMockServer.stubFor(get(beerService.getBeerUpcPath() + upc).willReturn(okJson(stringBeerDto)));
        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrder.getId());

            assertTrue(optionalBeerOrder.isPresent());
            assertEquals(BeerOrderStatusEnum.ALLOCATED, optionalBeerOrder.get().getOrderStatus());
        });

        beerOrderManager.beerOrderPickedUp(savedBeerOrder.getId());

        await().untilAsserted(() -> {
            Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrder.getId());

            assertTrue(optionalBeerOrder.isPresent());
            assertEquals(BeerOrderStatusEnum.PICKED_UP, optionalBeerOrder.get().getOrderStatus());
        });
    }

    private BeerDto createBeerDto() {
        return BeerDto.builder()
                .id(beerId)
                .upc(upc)
                .build();
    }
    private BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(customer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                .beerId(beerId)
                .upc(upc)
                .orderQuantity(1)
                .beerOrder(beerOrder)
                .build());

        beerOrder.setBeerOrderLines(lines);

        return beerOrder;
    }
}