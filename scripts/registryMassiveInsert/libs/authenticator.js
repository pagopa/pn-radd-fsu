const {
  CognitoIdentityProviderClient,
  InitiateAuthCommand
} = require('@aws-sdk/client-cognito-identity-provider');

class Authenticator {
  /**
   * @param {string} username - Nome utente Cognito
   * @param {string} password - Password Cognito
   * @param {string} clientId - Client ID dell'app client Cognito
   */
  constructor(username, password, clientId) {
    if (!username || !password || !clientId) {
      throw new Error("Username, password e clientId sono obbligatori");
    }

    this.username = username;
    this.password = password;
    this.clientId = clientId;

    this.client = new CognitoIdentityProviderClient({});
  }

  /**
   * Esegue login con USER_PASSWORD_AUTH e restituisce un JWT
   * @returns {Promise<string>} IdToken (JWT)
   */
  async generateJwtToken() {
    const command = new InitiateAuthCommand({
      AuthFlow: "USER_PASSWORD_AUTH",
      ClientId: this.clientId,
      AuthParameters: {
        USERNAME: this.username,
        PASSWORD: this.password
      }
    });

    try {
      const response = await this.client.send(command);
      const token = response?.AuthenticationResult?.IdToken;

      if (!token) {
        throw new Error("Token JWT non restituito da Cognito");
      }

      return token;
    } catch (err) {
      console.error("❌ Errore durante l'autenticazione Cognito:", err.message || err);
      throw err;
    }
  }
}

module.exports = { Authenticator };