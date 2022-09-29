package it.pagopa.pn.radd.middleware.msclient;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import it.pagopa.pn.radd.middleware.msclient.common.BaseClient;


public class PnDeliveryInternalClient  extends BaseClient {
    private final PnRaddFsuConfig pnRaddFsuConfig;


    public PnDeliveryInternalClient(PnRaddFsuConfig pnRaddFsuConfig) {
        this.pnRaddFsuConfig = pnRaddFsuConfig;
    }




}
