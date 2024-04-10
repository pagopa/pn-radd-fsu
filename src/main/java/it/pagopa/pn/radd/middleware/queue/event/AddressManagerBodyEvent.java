package it.pagopa.pn.radd.middleware.queue.event;

import lombok.Data;

@Data
public class AddressManagerBodyEvent {
    private PnAddressManagerEvent body;
}
