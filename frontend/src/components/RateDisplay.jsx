// src/components/RateDisplay.jsx
import React from "react";

function RateDisplay({ rate, loanCount, totalAmount }) {
  if (loanCount === 0) {
    return <p>No loans found matching the filters.</p>;
  }

  return (
    <div className="rate-display">
      <h3>Loan Packaging Summary</h3>
      <p><strong>Number of Matching Loans:</strong> {loanCount}</p>
      <p><strong>Total Loan Amount:</strong> ${totalAmount.toLocaleString()}</p>
      <p><strong>Calculated Expected Rate:</strong> {rate.toFixed(2)}%</p>
    </div>
  );
}   

export default RateDisplay;
