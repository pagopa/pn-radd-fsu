const axios = require('axios');
const { convertToRegistryRequest, mapFieldsToUpdate } = require('../utils/registryUtils');

class RegistryService {
  constructor(apiBaseUrl) {
    this.apiBaseUrl = apiBaseUrl;
  }

  // Private helper to add the partnerId header
  #prepareHeaders(partnerId, headers = {}) {
    return {
      ...headers,
      'x-pagopa-pn-cx-id': partnerId,
    };
  }

  // Private helper to extract error message
  #getErrorMessage(error) {
    if (error?.response?.data) return JSON.stringify(error.response.data);
    else return error.message;
  }

  async getRegistriesByPartnerId(partnerId, headers = {}) {
    try {
      const finalHeaders = this.#prepareHeaders(partnerId, headers);
      const res = await axios.get(`${this.apiBaseUrl}/radd-bo/api/v2/registry`, { headers: finalHeaders });
      return res.data.items || [];
    } catch (err) {
      throw new Error(`Errore lettura sedi da API: ${this.#getErrorMessage(err)}`);
    }
  }

  async deleteRegistry(partnerId, locationId, headers = {}) {
    try {
      const finalHeaders = this.#prepareHeaders(partnerId, headers);
      await axios.delete(`${this.apiBaseUrl}/radd-bo/api/v2/registry/${locationId}`, { headers: finalHeaders });
      console.log(`✅ Eliminata sede ${locationId}`);
    } catch (err) {
      console.error(`❌ Errore eliminazione sede ${locationId}: ${this.#getErrorMessage(err)}`);
    }
  }

  async createRegistry(partnerId, csvRegistry, headers = {}) {
    try {
      const finalHeaders = this.#prepareHeaders(partnerId, headers);
      const registry = convertToRegistryRequest(csvRegistry, partnerId);
      const res = await axios.post(`${this.apiBaseUrl}/radd-bo/api/v2/registry`, registry, { headers: finalHeaders });
      console.log(`➕ Aggiunta sede ${res.data.locationId}`);
      return { ...csvRegistry, status: 'OK', result: JSON.stringify(res.data) };
    } catch (err) {
      const reason = this.#getErrorMessage(err);
      console.error(`⚠️ Inserimento KO: ${reason}`);
      return { ...csvRegistry, status: 'KO', error: reason };
    }
  }

  async updateRegistry(partnerId, locationId, csvRegistry, headers = {}) {
    try {
      const finalHeaders = this.#prepareHeaders(partnerId, headers);
      const updateRequest = mapFieldsToUpdate(csvRegistry);
      const res = await axios.patch(`${this.apiBaseUrl}/radd-bo/api/v2/registry/${locationId}`, updateRequest, { headers: finalHeaders });
      console.log(`📝 Aggiornata sede ${res.data.locationId}`);
      return { ...csvRegistry, status: 'OK', result: JSON.stringify(res.data) };
    } catch (err) {
      const reason = this.#getErrorMessage(err);
      console.error(`⚠️ Aggiornamento KO: ${locationId} - ${reason}`);
      return { ...csvRegistry, status: 'KO', error: reason };
    }
  }
}

module.exports = RegistryService;