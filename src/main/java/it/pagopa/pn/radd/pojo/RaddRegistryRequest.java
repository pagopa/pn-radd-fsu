package it.pagopa.pn.radd.pojo;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaddRegistryRequest {

    @CsvBindByPosition(position = 0)
    private String paese;

    @CsvBindByPosition(position = 1)
    private String citta;

    @CsvBindByPosition(position = 2)
    private String provincia;

    @CsvBindByPosition(position = 3)
    private String cap;

    @CsvBindByPosition(position = 4)
    private String via;

    @CsvBindByPosition(position = 5)
    private String dataInizioValidita;

    @CsvBindByPosition(position = 6)
    private String dataFineValidita;

    @CsvBindByPosition(position = 7)
    private String descrizione;

    @CsvBindByPosition(position = 8)
    private String orariApertura;

    @CsvBindByPosition(position = 9)
    private String coordinateGeoReferenziali;

    @CsvBindByPosition(position = 10)
    private String telefono;

    @CsvBindByPosition(position = 11)
    private String capacita;

    @CsvBindByPosition(position = 12)
    private String externalCode;

}
