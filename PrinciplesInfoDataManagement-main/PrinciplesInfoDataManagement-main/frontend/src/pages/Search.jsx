/*

// src/pages/Search.jsx
import React, { useState } from "react";
import FilterManager from "../components/FilterManager";
import FilterSummary from "../components/FilterSummary";
import MortgageResultsTable from "../components/MortgageResultsTable";

function Search() {
  const [filters, setFilters] = useState([]);
  const [results, setResults] = useState([]);

  const handleDeleteFilter = (index) => {
    setFilters(filters.filter((_, i) => i !== index));
  };

  return (
    <div className="search-page">
      <h2>Search Mortgages</h2>
      <FilterManager filters={filters} setFilters={setFilters} />
      <FilterSummary filters={filters} handleDelete={handleDeleteFilter} />
      <MortgageResultsTable results={results} />
    </div>
  );
}

export default Search;
*/


//   *** TEST DATA ***
// src/pages/Search.jsx
import React, { useState } from "react";
import FilterManager from "../components/FilterManager";
import FilterSummary from "../components/FilterSummary";
import MortgageResultsTable from "../components/MortgageResultsTable";

function Search() {
  const [filters, setFilters] = useState([]);
  const [results, setResults] = useState([
    {
      applicationId: 12345,
      respondentId: "XYZ001",
      loanType: "Conventional",
      loanAmount: 250000,
      actionTaken: "Loan Originated",
      msamd: "Newark",
      county: "Essex",
      lienStatus: "First Lien",
      rateSpread: 3.5,
      purchaserType: "Fannie Mae",
    },
    {
      applicationId: 67890,
      respondentId: "ABC002",
      loanType: "FHA",
      loanAmount: 300000,
      actionTaken: "Loan Originated",
      msamd: "Jersey City",
      county: "Hudson",
      lienStatus: "Second Lien",
      rateSpread: 4.2,
      purchaserType: "Freddie Mac",
    },
  ]);

  const handleDeleteFilter = (index) => {
    setFilters(filters.filter((_, i) => i !== index));
  };

  return (
    <div className="search-page">
      <h2>Search Mortgages</h2>
      <FilterManager filters={filters} setFilters={setFilters} />
      <FilterSummary filters={filters} handleDelete={handleDeleteFilter} />
      <MortgageResultsTable results={results} />
    </div>
  );
}

export default Search;
