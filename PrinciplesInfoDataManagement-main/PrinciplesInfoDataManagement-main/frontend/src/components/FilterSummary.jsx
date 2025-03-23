// src/components/FilterSummary.jsx
import React from "react";

function FilterSummary({ filters, handleDelete }) {
  if (!filters.length) {
    return <p>No filters applied yet.</p>;
  }

  return (
    <div className="filter-summary">
      <h3>Active Filters</h3>
      <ul>
        {filters.map((filter, index) => (
          <li key={index} className="filter-item">
            {filter.type}: {filter.value}
            <button
              className="delete-btn"
              onClick={() => handleDelete(index)}
            >
              Delete
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default FilterSummary;
