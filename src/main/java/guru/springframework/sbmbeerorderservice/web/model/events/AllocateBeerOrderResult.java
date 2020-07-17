package guru.springframework.sbmbeerorderservice.web.model.events;

import guru.springframework.sbmbeerorderservice.web.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocateBeerOrderResult {

    private BeerOrderDto beerOrderDto;
    private boolean allocationError = false;
    private boolean pendingInventory = false;
}
