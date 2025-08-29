function convertToRegistryRequest(csvRow) {
  return {
    partnerId: csvRow.partnerId,
    locationId: csvRow.locationId,
    description: csvRow.description,
    phoneNumbers: csvRow.phoneNumbers ? csvRow.phoneNumbers.split('|') : [],
    email: csvRow.email || null,
    openingTime: csvRow.openingTime,
    startValidity: csvRow.startValidity,
    endValidity: csvRow.endValidity,
    externalCodes: csvRow.externalCodes ? csvRow.externalCodes.split('|') : [],
    appointmentRequired: csvRow.appointmentRequired === 'true',
    website: csvRow.website || null,
    partnerType: csvRow.partnerType,
    address: {
      addressRow: csvRow.addressRow?.replace(/^"|"$/g, ''),
      cap: csvRow.cap,
      city: csvRow.city,
      province: csvRow.province,
      country: csvRow.country
    }
  };
}

function mapFieldsToUpdate(csvRow) {
  const mappings = {
    description: v => v,
    phoneNumbers: v => v.split('|'),
    email: v => v,
    openingTime: v => v,
    endValidity: v => v,
    externalCodes: v => v.split('|'),
    appointmentRequired: v => isBoolean(v) ? v === 'true' : v,
    website: v => v
  };

  return Object.entries(mappings).reduce((acc, [key, transform]) => {
    const value = csvRow[key];
    if (!isFieldEmpty(value)) {
      acc[key] = transform(value);
    }
    return acc;
  }, {});
}

function isFieldEmpty(value) {
  return value === undefined || value === null || value.toString().trim() === '';
}

function isBoolean(value) {
  return value === 'true' || value === 'false';
}

function findLocationId(apiRegistries, csvRegistry) {
  return apiRegistries.find(r => r.locationId === csvRegistry.locationId)?.locationId;
}

module.exports = { convertToRegistryRequest, mapFieldsToUpdate, findLocationId, isFieldEmpty };
