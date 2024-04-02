package it.pagopa.pn.radd.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.AnalogAddressDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.NormalizeItemsRequestDto;
import it.pagopa.pn.radd.alt.generated.openapi.msclient.addressmanager.v1.dto.NormalizeRequestDto;
import it.pagopa.pn.radd.exception.PnRaddException;
import it.pagopa.pn.radd.exception.RaddGenericException;
import it.pagopa.pn.radd.middleware.db.entities.RaddRegistryRequestEntity;
import it.pagopa.pn.radd.pojo.AddressManagerOriginalRequest;
import it.pagopa.pn.radd.pojo.AddressManagerRequest;
import it.pagopa.pn.radd.pojo.AddressManagerRequestAddress;
import lombok.CustomLog;

import java.util.List;

@CustomLog
public class AddressManagerOriginalRequestMapper {

    public static AddressManagerRequestAddress getAddressManagerRequest(String orginalRequest) {
        try {
            return new ObjectMapper().readValue(orginalRequest, AddressManagerRequestAddress.class);
        } catch (JsonProcessingException e) {
            log.error("Error in getAddressManagerRequest, mapping went wrong.",e);
            throw new RaddGenericException(e.getMessage());
        }
    }

    public static List<AddressManagerRequestAddress> getRequestAddressFromOriginalRequest(List<RaddRegistryRequestEntity> entities) {
        return entities.stream().map(entity -> {
            AddressManagerRequestAddress request = getAddressManagerRequest(entity.getOriginalRequest());
            request.setId(entity.getPk());
            return request;
        }).toList();
    }

    public static NormalizeItemsRequestDto getNormalizeRequestDtoFromAddressManagerRequest(AddressManagerRequest request) {
        NormalizeItemsRequestDto requestDto = new NormalizeItemsRequestDto();
        requestDto.setCorrelationId(request.getCorrelationId());
        List<NormalizeRequestDto> listDto = request.getAddresses().stream().map(address -> {
            NormalizeRequestDto dto = new NormalizeRequestDto();
            dto.setId(address.getId());
            AnalogAddressDto addressDto = new AnalogAddressDto();
            addressDto.setAddressRow(address.getAddressRow());
            addressDto.setCap(address.getCap());
            addressDto.setCity(address.getCity());
            addressDto.setPr(address.getPr());
            addressDto.setCountry(address.getCountry());
            dto.setAddress(addressDto);
            return dto;
        }).toList();
        requestDto.setRequestItems(listDto);

        return requestDto;
    }

}
