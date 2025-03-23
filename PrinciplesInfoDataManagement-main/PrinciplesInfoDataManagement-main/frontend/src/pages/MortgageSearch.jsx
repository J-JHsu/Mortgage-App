import React, { useState } from 'react';
import FilterManager from './FilterManager';
import FilterSummary from './FilterSummary';
import MortgageResultsTable from './MortgageResultsTable';

function MortgageSearch() {
  const [searchCriteria, setSearchCriteria] = useState({});
  const [searchResults, setSearchResults] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSearch = async (filters) => {
    try {
      setIsLoading(true);
      setError(null);
      
      const response = await fetch('http://localhost:8080/api/mortgages/search', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(filters),
      });

      if (!response.ok) {
        throw new Error('Search failed');
      }

      const data = await response.json();
      setSearchResults(data);
      setSearchCriteria(filters);
    } catch (err) {
      setError(err.message);
      setSearchResults([]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="mortgage-search-container">
      <h2>Search Mortgages</h2>
      <FilterManager onSearch={handleSearch} />
      <FilterSummary filters={searchCriteria} />
      {isLoading && <div className="loading">Loading...</div>}
      {error && <div className="error">{error}</div>}
      <MortgageResultsTable results={searchResults} />
    </div>
  );
}

export default MortgageSearch;
