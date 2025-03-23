import React from "react";
import { Link } from "react-router-dom";

function Home() {
  return (
    <div className="home">
      <h1>Mortgage Backed Securities System</h1>
      <div className="feature-grid">
        <div className="feature-card">
          <h3>Mortgage Search</h3>
          <p>Search and filter through available mortgages</p>
          <Link to="/search" className="feature-link">Search Mortgages</Link>
        </div>
        <div className="feature-card">
          <h3>Loan Packaging</h3>
          <p>Package loans into securities</p>
          <Link to="/package" className="feature-link">Package Loans</Link>
        </div>
        <div className="feature-card">
          <h3>Dashboard</h3>
          <p>View system overview and statistics</p>
          <Link to="/dashboard" className="feature-link">View Dashboard</Link>
        </div>
      </div>
    </div>
  );
}

export default Home;
