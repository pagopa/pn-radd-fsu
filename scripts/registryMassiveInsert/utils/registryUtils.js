function convertToRegistryRequest(csvRow) {
  const request = {
    partnerId: csvRow.partnerId,
    locationId: csvRow.locationId,
    description: csvRow.description,
    phoneNumbers: csvRow.phoneNumbers ? csvRow.phoneNumbers.split('|') : [],
    externalCodes: csvRow.externalCodes ? csvRow.externalCodes.split('|') : [],
    address: {
      addressRow: csvRow.addressRow?.replace(/^"|"$/g, ''),
      cap: csvRow.cap,
      city: csvRow.city,
      province: csvRow.province
    }
  };

  // Add optional fields only if they have values
  if (!isFieldEmpty(csvRow.email)) {
    request.email = csvRow.email;
  }
  if (!isFieldEmpty(csvRow.openingTime)) {
    request.openingTime = csvRow.openingTime;
  }
  if (!isFieldEmpty(csvRow.startValidity)) {
    request.startValidity = csvRow.startValidity;
  }
  if (!isFieldEmpty(csvRow.endValidity)) {
    request.endValidity = csvRow.endValidity;
  }
  if (!isFieldEmpty(csvRow.website)) {
    request.website = csvRow.website;
  }
  if (!isFieldEmpty(csvRow.partnerType)) {
    request.partnerType = csvRow.partnerType;
  }
  if (!isFieldEmpty(csvRow.appointmentRequired)) {
    request.appointmentRequired = csvRow.appointmentRequired === 'true';
  }
  if (!isFieldEmpty(csvRow.country)) {
    request.address.country = csvRow.country;
  }

  return request;
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
