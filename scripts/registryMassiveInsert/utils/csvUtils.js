const fs = require('fs');
const csv = require('csv-parser');
const { createObjectCsvWriter } = require('csv-writer');

async function readCsv(filePath) {
  return new Promise((resolve, reject) => {
    const results = [];
    fs.createReadStream(filePath)
      .pipe(csv())
      .on('data', data => results.push(data))
      .on('end', () => resolve(results))
      .on('error', reject);
  });
}

async function writeReport(report, partnerId) {
  const csvWriter = createObjectCsvWriter({
    path: `report-${partnerId}-${Date.now()}.csv`,
    header: [
      ...(report.length > 0
        ? Object.keys(report[0])
            .filter(k => !['status', 'error', 'result'].includes(k))
            .map(k => ({ id: k, title: k }))
        : []),
      { id: 'status', title: 'Stato' },
      { id: 'error', title: 'Errore' },
      { id: 'result', title: 'Risultato' }
    ]
  });

  await csvWriter.writeRecords(report);
}
module.exports = { readCsv, writeReport };
