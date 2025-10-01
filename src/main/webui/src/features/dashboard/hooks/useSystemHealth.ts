import { useState, useEffect } from 'react';
import { SystemHealthDto } from '../../../api';
import { healthApi } from '../../../config/apiClient';

export const useSystemHealth = (refreshInterval = 5000) => {
  const [healthData, setHealthData] = useState<SystemHealthDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [responseTime, setResponseTime] = useState<number | null>(null);
  const [errorCount, setErrorCount] = useState(0);
  const [totalRequests, setTotalRequests] = useState(0);

  const fetchHealthData = async () => {
    try {
      const startTime = performance.now();
      const response = await healthApi.getSystemHealth();
      const endTime = performance.now();
      const duration = Math.round(endTime - startTime);

      setHealthData(response.data);
      setResponseTime(duration);
      setError(null);
      setTotalRequests(prev => prev + 1);
    } catch (err) {
      console.error('Failed to fetch system health:', err);
      setError('Failed to fetch system health data');
      setResponseTime(null);
      setErrorCount(prev => prev + 1);
      setTotalRequests(prev => prev + 1);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // Initial fetch
    fetchHealthData();

    // Set up interval for refreshing data
    const interval = setInterval(fetchHealthData, refreshInterval);

    return () => clearInterval(interval);
  }, [refreshInterval]);

  return {
    healthData,
    loading,
    error,
    responseTime,
    errorCount,
    totalRequests,
    refetch: fetchHealthData
  };
};
