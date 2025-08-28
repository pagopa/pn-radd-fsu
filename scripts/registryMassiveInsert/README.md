# Script di Sincronizzazione Registri RADD

Questo script consente di sincronizzare i registri (registries) di un partner con quelli presenti in un file CSV. Viene
eseguita un'autenticazione, lettura dei dati dal CSV, confronto con i registri esistenti e aggiornamento remoto tramite
API.

## Requisiti

- Node.js 18+
- Accesso alle API RADD (`https://api.radd.<env>.notifichedigitali.it`)
- File CSV con nome `<partnerId>.csv`

## Installazione

1. Clona il repository o copia lo script in un progetto.
2. Installa le dipendenze (se previste):

   ```bash
   npm install
   ```

## Utilizzo

```bash
# Unix/Linux/MacOS
export AWS_PROFILE=<profile_name>
node index.js <env> <username> <password> <clientId> <csvFilePath>

# Windows
set AWS_PROFILE=<profile_name>
node index.js <env> <username> <password> <clientId> <csvFilePath>
```

### Parametri

| Parametro        | Descrizione                                                                   |
|------------------|-------------------------------------------------------------------------------|
| `<profile_name>` | Nome del profilo AWS                                                          |
| `<env>`          | Ambiente di esecuzione: `dev`, `test`, `uat`, `hotfix`, `prod`                |
| `<username>`     | Username per autenticazione Cognito                                           |
| `<password>`     | Password per autenticazione Cognito                                           |
| `<clientId>`     | Client ID per autenticazione Cognito                                          |
| `<csvFilePath>`  | Percorso completo al file CSV. Il nome del file deve essere `<partnerId>.csv` |

### Esempio

```bash
node index.js test myuser mypass abc123 ./csv/12345678901.csv
```

## Cosa fa lo script

1. Legge il file CSV specificato.
2. Estrae il `partnerId` dal nome del file CSV.
3. Autentica l’utente tramite il client Cognito e ottiene un JWT.
4. Recupera i registri esistenti del partner.
5. Per ogni registro nel CSV:
    - Se esiste già un registro con stesso `locationId`, lo aggiorna con le informazioni fornite nel CSV.
    - Altrimenti crea un nuovo registro con i dati aggiornati.
6. Elimina tutti i registri esistenti non presenti nel CSV per lo stesso partnerId.
7. Salva un report dell’operazione in un file CSV.

## Output

Un file CSV di report con nome `report-<partnerId>-<timestamp>.csv` verrà generato nella directory di output.

## Avvertenze

- Lo script **sovrascrive** tutti i registri remoti con quelli del file CSV.
