package guru.springframework.sbmbeerorderservice.services;

import guru.springframework.sbmbeerorderservice.web.model.CustomerPagedList;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerPagedList listCustomers(Pageable pageable);
}
