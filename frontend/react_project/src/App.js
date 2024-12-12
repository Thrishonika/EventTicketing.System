// Import necessary modules
import React, { useState } from "react";
import "./App.css";

function App() {
  // State for system configuration
  const [config, setConfig] = useState({
    totalTickets: 100, // Total tickets available in the system
    ticketReleaseRate: 5, // Rate at which vendors add tickets
    customerRetrievalRate: 3, // Rate at which customers purchase tickets
    maxTicketCapacity: 50, // Maximum ticket capacity in the system
  });

  // Temporary configuration for user edits
  const [tempConfig, setTempConfig] = useState({ ...config });

  // State for tickets and logs
  const [tickets, setTickets] = useState([]); // Stores the tickets in the system
  const [log, setLog] = useState([]); // Activity logs

  // State to manage system running status and intervals
  const [isRunning, setIsRunning] = useState(false);
  const [intervalIds, setIntervalIds] = useState([]); // Stores interval IDs for clearing

  // Function to add tickets to the pool
  const addTickets = (rate, vendorId) => {
    setTickets((prev) => {
      const newTickets = [...prev];
      if (newTickets.length + rate <= config.maxTicketCapacity) {
        for (let i = 0; i < rate; i++) {
          newTickets.push(1); // Add tickets to the array
        }
        setLog((prevLog) => [
          ...prevLog,
          `${vendorId} added ${rate} tickets. Total: ${newTickets.length}`,
        ]); // Log the activity
      }
      return newTickets;
    });
  };

  // Function to remove tickets from the pool
  const removeTickets = (rate, customerId) => {
    setTickets((prev) => {
      const newTickets = [...prev];
      if (newTickets.length >= rate) {
        newTickets.splice(0, rate); // Remove tickets from the beginning
        setLog((prevLog) => [
          ...prevLog,
          `${customerId} purchased ${rate} tickets. Remaining: ${newTickets.length}`,
        ]); // Log the activity
      }
      return newTickets;
    });
  };

  // Start the ticketing system
  const startSystem = () => {
    if (isRunning) return; // Prevent duplicate intervals

    // Create intervals for vendors and customers
    const vendor1 = setInterval(() => addTickets(config.ticketReleaseRate, "Vendor 1"), 1000);
    const vendor2 = setInterval(() => addTickets(config.ticketReleaseRate, "Vendor 2"), 1000);
    const customer1 = setInterval(() => removeTickets(config.customerRetrievalRate, "Customer 1"), 1500);
    const customer2 = setInterval(() => removeTickets(config.customerRetrievalRate, "Customer 2"), 1500);
    const customer3 = setInterval(() => removeTickets(config.customerRetrievalRate, "Customer 3"), 1500);

    // Store interval IDs for cleanup
    setIntervalIds([vendor1, vendor2, customer1, customer2, customer3]);
    setIsRunning(true); // Mark system as running
  };

  // Stop the ticketing system
  const stopSystem = () => {
    intervalIds.forEach((id) => clearInterval(id)); // Clear all intervals
    setIsRunning(false); // Mark system as stopped
    setIntervalIds([]); // Reset interval IDs
  };

  // Update temporary configuration
  const updateTempConfig = (key, value) => {
    setTempConfig((prevConfig) => ({ ...prevConfig, [key]: value }));
  };

  // Save temporary configuration to the main configuration
  const saveConfig = () => {
    setConfig(tempConfig); // Update configuration
    setLog((prevLog) => [
      ...prevLog,
      `Configuration saved: ${JSON.stringify(tempConfig)}`,
    ]); // Log the change
  };

  return (
    <div className="container">
      <h1>Ticketing System</h1>

      {/* Configuration Section */}
      <div className="configuration">
        <h2>Configuration</h2>
        {Object.keys(config).map((key) => (
          <div key={key}>
            <label>
              {key}:
              <input
                type="number"
                value={tempConfig[key]}
                onChange={(e) => updateTempConfig(key, parseInt(e.target.value, 10))}
              />
            </label>
          </div>
        ))}
        <button onClick={saveConfig}>Save Configuration</button>
      </div>

      {/* Controls Section */}
      <div className="controls">
        <button onClick={startSystem} disabled={isRunning}>
          Start System
        </button>
        <button onClick={stopSystem} disabled={!isRunning}>
          Stop System
        </button>
      </div>

      {/* Ticket Pool - Grid Representation */}
      <div className="ticket-pool">
        <h2>Ticket Pool</h2>
        <p>Total Tickets Sold: {tickets.length}</p>
        <p>Max Capacity: {config.maxTicketCapacity}</p>
        
        <div className="grid">
          {Array.from({ length: config.maxTicketCapacity }).map((_, index) => (
            <div
              key={index}
              className={`seat ${tickets.length > index ? "sold" : "available"}`}
            />
          ))}
        </div>
        <p>{config.maxTicketCapacity - tickets.length} tickets remaining</p>
      </div>

      {/* Logs Section */}
      <div className="logs">
        <h2>Activity Logs</h2>
        <div>
          {log.map((entry, index) => (
            <p key={index}>{entry}</p>
          ))}
        </div>
      </div>
    </div>
  );
}

export default App;

