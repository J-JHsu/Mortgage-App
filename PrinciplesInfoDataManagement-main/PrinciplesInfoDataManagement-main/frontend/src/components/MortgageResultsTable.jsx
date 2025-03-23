// src/components/MortgageResultsTable.jsx
import React from "react";

function MortgageResultsTable({ results }) {
  if (!results.length) {
    return <p className="status-message">No matching mortgages found.</p>;
  }

  return (
    <table className="mortgage-results-table">
      <thead>
        <tr>
          <th>Application ID</th>
          <th>Respondent ID</th>
          <th>Loan Type</th>
          <th>Loan Amount</th>
          <th>Action Taken</th>
          <th>MSAMD</th>
          <th>County</th>
          <th>Lien Status</th>
          <th>Rate Spread</th>
          <th>Purchaser Type</th>
        </tr>
      </thead>
      <tbody>
        {results.map((mortgage, index) => (
          <tr key={index}>
            <td>{mortgage.applicationId}</td>
            <td>{mortgage.respondentId}</td>
            <td>{mortgage.loanType}</td>
            <td>${mortgage.loanAmount.toLocaleString()}</td>
            <td>{mortgage.actionTaken}</td>
            <td>{mortgage.msamd}</td>
            <td>{mortgage.county}</td>
            <td>{mortgage.lienStatus}</td>
            <td>{mortgage.rateSpread}</td>
            <td>{mortgage.purchaserType}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default MortgageResultsTable;
