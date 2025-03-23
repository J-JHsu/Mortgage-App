// src/components/FilterManager.jsx
import React, { useState } from "react";

function FilterManager({ filters, setFilters }) {
  const [filterType, setFilterType] = useState("");
  const [filterValue, setFilterValue] = useState("");

  const handleAddFilter = () => {
    if (filterType && filterValue) {
      setFilters([...filters, { type: filterType, value: filterValue }]);
      setFilterType("");
      setFilterValue("");
    }
  };

  const handleDeleteFilter = (index) => {
    const newFilters = filters.filter((_, i) => i !== index);
    setFilters(newFilters);
  };

  return (
    <div className="filter-manager">
      <h3>Manage Filters</h3>
      <div className="filter-form">
        <label>
          Filter Type:
          <input
            type="text"
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
          />
        </label>
        <label>
          Filter Value:
          <input
            type="text"
            value={filterValue}
            onChange={(e) => setFilterValue(e.target.value)}
          />
        </label>
        <button onClick={handleAddFilter}>Add Filter</button>
      </div>

      <h4>Active Filters</h4>
      <ul>
        {filters.map((filter, index) => (
          <li key={index}>
            {filter.type}: {filter.value}
            <button onClick={() => handleDeleteFilter(index)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default FilterManager;
