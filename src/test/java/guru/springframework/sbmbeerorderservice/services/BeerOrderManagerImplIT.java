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
import guru.springframework.sbmbeerorderservice.web.model.events.AllocationFailureEvent;
import guru.springframework.sbmbeerorderservice.web.model.events.BeerDto;
import guru.springframework.sbmbeerorderservice.web.model.events.DeallocateBeerOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static guru.springframework.sbmbeerorderservice.config.JmsConfig.ALLOCATE_ORDER_FAILED_QUEUE;
import static guru.springframework.sbmbeerorderservice.config.JmsConfig.DEALLOCATE_ORDER_QUEUE;
import static guru.springframework.sbmbeerorderservice.services.testcomponents.ITUtil.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ExtendWith(WireMockExtension.class)
class BeerOrderManagerImplIT {

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

    @Autowired
    private JmsTemplate jmsTemplate;

    Customer customer;

    UUID beerId = UUID.randomUUID();
    String upc = "12345";

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
        stubForBeerUpc();
        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.ALLOCATED);
        UUID beerOrderId = savedBeerOrder.getId();
        await().untilAsserted(() -> assertBeerOrderLines(beerOrderId, false));

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
        stubForBeerUpc();
        BeerOrder beerOrder = createBeerOrder(FAILED_VALIDATION);

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.VALIDATION_EXCEPTION);
    }

    @Test
    void testFailedAllocation() throws JsonProcessingException {
        stubForBeerUpc();
        BeerOrder beerOrder = createBeerOrder(FAILED_ALLOCATION);

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.ALLOCATION_EXCEPTION);

        AllocationFailureEvent result = (AllocationFailureEvent) jmsTemplate.receiveAndConvert(ALLOCATE_ORDER_FAILED_QUEUE);
        assertNotNull(result);
        assertEquals(savedBeerOrder.getId(), result.getBeerOrderId());
    }

    @Test
    void testPartialAllocation() throws JsonProcessingException {
        stubForBeerUpc();
        BeerOrder beerOrder = createBeerOrder(PARTIAL_ALLOCATION);

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.PENDING_INVENTORY);

        assertBeerOrderLines(savedBeerOrder.getId(), true);
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        stubForBeerUpc();
        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.ALLOCATED);

        beerOrderManager.beerOrderPickedUp(savedBeerOrder.getId());
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.PICKED_UP);
    }

    @Test
    void testValidationPendingToCanceled() throws JsonProcessingException {
        stubForBeerUpc();
        BeerOrder beerOrder = createBeerOrder(VALIDATION_CANCEL_ORDER);

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.VALIDATION_PENDING);

        beerOrderManager.cancelBeerOrder(savedBeerOrder.getId());
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.CANCELLED);
    }

    @Test
    void testAllocationPendingToCanceled() throws JsonProcessingException {
        stubForBeerUpc();
        BeerOrder beerOrder = createBeerOrder(ALLOCATION_CANCEL_ORDER);

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.ALLOCATION_PENDING);

        beerOrderManager.cancelBeerOrder(savedBeerOrder.getId());
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.CANCELLED);
    }

    @Test
    void testAllocationToCanceled() throws JsonProcessingException {
        stubForBeerUpc();
        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.ALLOCATED);

        beerOrderManager.cancelBeerOrder(savedBeerOrder.getId());
        assertStatusChange(savedBeerOrder.getId(), BeerOrderStatusEnum.CANCELLED);

        DeallocateBeerOrderRequest request = (DeallocateBeerOrderRequest) jmsTemplate.receiveAndConvert(DEALLOCATE_ORDER_QUEUE);
        assertNotNull(request);
        assertEquals(savedBeerOrder.getId(), request.getBeerOrderDto().getId());
    }

    private void assertStatusChange(UUID beerOrderId, BeerOrderStatusEnum status) {
        await().untilAsserted(() -> {
            Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderId);

            assertTrue(optionalBeerOrder.isPresent());
            assertEquals(status, optionalBeerOrder.get().getOrderStatus());
        });
    }

    private void assertBeerOrderLines(UUID beerOrderId, boolean partialAllocation) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderId);
        assertTrue(optionalBeerOrder.isPresent());

        BeerOrderLine line = optionalBeerOrder.get().getBeerOrderLines().iterator().next();

        if (partialAllocation) {
            assertEquals(line.getOrderQuantity() - MINUS_BEERS_FOR_PARTIAL_ALLOCATION, line.getQuantityAllocated());
        } else {
            assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
        }
    }

    private BeerDto createBeerDto() {
        return BeerDto.builder()
                .id(beerId)
                .upc(upc)
                .build();
    }

    private BeerOrder createBeerOrder(String customerRefText) {
        BeerOrder beerOrder = this.createBeerOrder();
        beerOrder.setCustomerRef(customerRefText);

        return beerOrder;
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

    private void stubForBeerUpc() throws JsonProcessingException {
        String stringBeerDto = objectMapper.writeValueAsString(createBeerDto());

        wireMockServer.stubFor(get(beerService.getBeerUpcPath() + upc).willReturn(okJson(stringBeerDto)));
    }
}