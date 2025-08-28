```mermaid
sequenceDiagram
    participant OPS as Operatore OPS
    participant PN as pn-radd-alt
    participant ALS as Amazon Location Service
    participant DB as DynamoDB

    OPS->>PN: POST /registry/partnerId
    Note right of OPS: Payload: {addressRow, city, pr, cap, country}
    activate PN
    
    PN->>PN: Validazione input
    Note left of PN: Verifica campi obbligatori e formati
    
    alt Input non valido
        PN-->>OPS: 400 Bad Request
        Note left of PN: **ERRORE: VALIDATION_ERROR**<br>{error: "INPUT_NON_VALIDO",<br>details: "Campi mancanti o errati"}
    else Input valido
        PN->>ALS: ValidateAddressRequest
        Note left of PN: {Text: "VIA SALVO D'ACQUISTO 24, TARANTO",<br>Country: "ITA"}
        activate ALS
        
        alt Address found
            ALS-->>PN: ValidateAddressResponse
            Note right of ALS: {Valid: true,<br>Latitude: 40.4762,<br>Longitude: 17.2297,<br>MatchType: "EXACT"}
        else Address not found
            ALS-->>PN: {Valid: false,<br>Error: "NO_MATCH"}
        end
        deactivate ALS
        
        alt Valid address
            PN->>DB: Salvataggio dati
            Note left of PN: {addressData, coordinates, quality, timestamp}
            activate DB
            DB-->>PN: Conferma salvataggio
            deactivate DB
            
            PN-->>OPS: 200 OK
            Note left of PN: {coordinates: [40.4762,17.2297],<br>quality: "EXACT"}
        else Invalid address
            PN-->>OPS: 400 Bad Request
            Note left of PN: **ERRORE: ADDRESS_VALIDATION_FAILED**<br>{error: "INDIRIZZO_NON_VALIDO",<br>details: "Non trovato in ALS"}
        end
    end
    deactivate PN