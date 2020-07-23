package guru.springframework.sbmbeerorderservice.bootstrap;

import guru.springframework.sbmbeerorderservice.domain.Customer;
import guru.springframework.sbmbeerorderservice.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerLoader implements CommandLineRunner {

    public static final String TASTING_ROOM = "Tasting Room";

    public static final String BEER_1_UPC = "0631234200036";
    public static final String BEER_2_UPC = "0631234300019";
    public static final String BEER_3_UPC = "0083783375213";

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {
        loadCustomerData();
    }

    private void loadCustomerData() {
        if (isTastingRoomCustomerNotAvailable()) {
            Customer savedCustomer = customerRepository.saveAndFlush(Customer.builder()
                    .customerName(TASTING_ROOM)
                    .apiKey(UUID.randomUUID())
                    .build());

            log.debug("Saved 'Tasting Room' customer with customerId = " + savedCustomer.getId());
        }
    }

    private boolean isTastingRoomCustomerNotAvailable() {
        return customerRepository.findAllByCustomerNameLike(TASTING_ROOM).isEmpty();
    }
}
