package it.pagopa.pn.radd.services.radd.fsu.v1;

import it.pagopa.pn.radd.exception.CoordinatesNotFoundException;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.geoplaces.GeoPlacesAsyncClient;
import software.amazon.awssdk.services.geoplaces.model.*;

import java.util.*;

@Slf4j
@Service
public class AwsGeoService {

    private final GeoPlacesAsyncClient geoPlacesAsyncClient;

    public AwsGeoService(GeoPlacesAsyncClient geoPlacesClient) {
        this.geoPlacesAsyncClient = geoPlacesClient;
    }

    public Mono<CoordinatesResult> getCoordinatesForAddress(String address, String subRegion, String postalCode, String locality, String country)
             {

        log.info("Input parameters - address: {}, subRegion: {}, postalCode: {}, locality: {}, country: {}",
                 address, subRegion, postalCode, locality, country);

        GeocodeRequest request = buildGeocodeRequest(address, subRegion, postalCode, locality, country);

        return Mono.fromFuture(geoPlacesAsyncClient.geocode(request))
                   .flatMap(response -> getCoordinateResult(response, address))
                   .doOnError(e -> log.error("Error during AWS geolocation", e));
    }

    private GeocodeQueryComponents buildGeocodeQueryComponents(
            String address, String province, String zip, String municipality, String country) {

        GeocodeQueryComponents.Builder builder = GeocodeQueryComponents.builder();

        if (StringUtils.isNotBlank(country)) builder.country(country);
        if (StringUtils.isNotBlank(province)) builder.subRegion(province);
        if (StringUtils.isNotBlank(zip)) builder.postalCode(zip);
        if (StringUtils.isNotBlank(municipality)) builder.locality(municipality);
        if (StringUtils.isNotBlank(address)) builder.street(address);

        return builder.build();
    }


    private GeocodeRequest buildGeocodeRequest(
            String address, String subRegion, String postalCode, String locality, String country) {

        GeocodeQueryComponents components = buildGeocodeQueryComponents(address, subRegion, postalCode, locality, country);

        GeocodeFilter filter = GeocodeFilter.builder()
                                            .includeCountries("IT")
                                            .build();
        return GeocodeRequest.builder()
                             .maxResults(1)
                             .filter(filter)
                             .queryComponents(components)
                             .language("it")
                             .build();
    }

    private Mono<CoordinatesResult> getCoordinateResult ( GeocodeResponse response, String address ){

        var results = response.resultItems();
        if (results == null || results.isEmpty()) {
            return Mono.error(new CoordinatesNotFoundException("No geolocation result for address: " + address));
        }
        var geoResult = results.get(0);
        var position = geoResult.position();
        var matchScore = geoResult.matchScores();

        CoordinatesResult result = getCoordinatesResult(position, geoResult, matchScore);

        log.info("AWS geoplacesResult  -> {} ", result.toString());

        return Mono.just(result);
    }

    private CoordinatesResult getCoordinatesResult(List<Double> position, GeocodeResultItem geoResult, MatchScoreDetails matchScore) {

        validateAllFields(geoResult, position);

        CoordinatesResult result = new CoordinatesResult();
        result.setAwsLongitude(position.get(0).toString());
        result.setAwsLatitude(position.get(1).toString());
        result.setAwsAddressRow(geoResult.title());
        result.setAwsPostalCode(geoResult.address().postalCode());
        result.setAwsLocality(geoResult.address().locality());
        result.setAwsSubRegion(geoResult.address().subRegion().code());
        result.setAwsCountry(geoResult.address().country().name());
        result.setAwsMatchScore(matchScore);

        return result;
    }

    @Data
    @ToString
    public static class CoordinatesResult {
        String awsAddressRow;
        String awsPostalCode;
        String awsLocality;
        String awsSubRegion;
        String awsCountry;
        String awsLongitude;
        String awsLatitude;
        MatchScoreDetails awsMatchScore;

    }

    private static void validateAllFields(GeocodeResultItem geoResult, List<Double> position) {
        List<String> missingFields = new ArrayList<>();

        if (position == null || position.size() < 2 || position.get(0) == null || position.get(1) == null) {
            missingFields.add("coordinates (longitude and latitude)");
        }

        if (geoResult.address() == null) {
            missingFields.add("address");
        } else {
            if (StringUtils.isBlank(geoResult.address().postalCode())) {
                missingFields.add("postal code");
            }
            if (StringUtils.isBlank(geoResult.address().locality())) {
                missingFields.add("locality");
            }
            if (geoResult.address().subRegion() == null || StringUtils.isBlank(geoResult.address().subRegion().code())) {
                missingFields.add("subRegion");
            }
            if (geoResult.address().country() == null || StringUtils.isBlank(geoResult.address().country().name())) {
                missingFields.add("country");
            }
            if (geoResult.title() == null || StringUtils.isBlank(geoResult.title())){
                missingFields.add("title");
            }
        }

        if (!missingFields.isEmpty()) {
            throw new CoordinatesNotFoundException("Missing or empty fields from AWS: " + String.join(", ", missingFields));
        }
    }
}
