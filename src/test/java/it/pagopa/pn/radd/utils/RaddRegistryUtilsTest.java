package it.pagopa.pn.radd.utils;

import it.pagopa.pn.radd.config.PnRaddFsuConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {RaddRegistryUtils.class, PnRaddFsuConfig.class})
@ExtendWith(SpringExtension.class)
class RaddRegistryUtilsTest {
    @MockBean
    private ObjectMapperUtil objectMapperUtil;

    @Autowired
    private PnRaddFsuConfig pnRaddFsuConfig;

    @Autowired
    private RaddRegistryUtils raddRegistryUtils;

}

