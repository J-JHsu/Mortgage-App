// src/pages/LoanPackager.jsx
import React, { useState } from "react";
import RateDisplay from "../components/RateDisplay";

function LoanPackager() {
  // Simulated Loan Packaging State
  const [packagingStatus, setPackagingStatus] = useState("");
  const [rate, setRate] = useState(2.33);        // Placeholder for calculated rate
  const [loanCount, setLoanCount] = useState(10); // Example loan count
  const [totalAmount, setTotalAmount] = useState(500000); // Example total amount

  // Simulate Loan Packaging Process
  const handleConfirmPackaging = () => {
    // Simulate API Response
    const success = true; // This can later depend on backend response.

    if (success) {
      setPackagingStatus("Loans successfully packaged!");
    } else {
      setPackagingStatus("Error: Loan packaging failed.");
    }
  };

  return (
    <div className="loan-packager">
      <h2>Loan Packaging</h2>
      <p>Review the loan details before proceeding.</p>

      {/* Loan Details Section */}
      <RateDisplay rate={rate} loanCount={loanCount} totalAmount={totalAmount} />

      {/* Confirmation Button */}
      <button className="confirm-btn" onClick={handleConfirmPackaging}>
        Confirm Packaging
      </button>

      {/* Status Message */}
      {packagingStatus && (
        <p className="status-message">{packagingStatus}</p>
      )}
    </div>
  );
}

export default LoanPackager;
