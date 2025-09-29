import { useState, useEffect } from 'react';
import { SystemHealthDto } from '../../../api';
import { healthApi } from '../../../config/apiClient';

export const useSystemHealth = (refreshInterval = 5000) => {
  const [healthData, setHealthData] = useState<SystemHealthDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchHealthData = async () => {
    try {
      const response = await healthApi.getSystemHealth();
      setHealthData(response.data);
      setError(null);
    } catch (err) {
      console.error('Failed to fetch system health:', err);
      setError('Failed to fetch system health data');
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
    refetch: fetchHealthData
  };
};
