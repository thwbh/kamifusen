import { useState, useEffect, useCallback, useMemo } from 'react'
import { AppAdminResourceApi, ApiUserDto } from '../../../api'
import { useAsyncOperation } from '../../../shared'

interface UseUsersState {
  users: ApiUserDto[]
  systemUsers: ApiUserDto[]
  apiUsers: ApiUserDto[]
  loading: boolean
  error: string | null
}

interface UseUsersActions {
  refreshUsers: () => Promise<void>
  generateApiKey: (username: string, expiresAt: string) => Promise<string>
  renewApiKey: (userId: string, expiresAt?: string) => Promise<string>
  retireApiKey: (userId: string) => Promise<void>
  deleteUser: (userId: string) => Promise<void>
  updateUser: (userId: string, username: string, expiresAt?: string) => Promise<void>
  updateUserPassword: (userId: string, username: string, password?: string) => Promise<void>
  clearError: () => void
}

export const useUsers = (): UseUsersState & UseUsersActions => {
  const [users, setUsers] = useState<ApiUserDto[]>([]);
  const { loading, error, execute, clearError } = useAsyncOperation();

  const adminApi = useMemo(() => new AppAdminResourceApi(), []);

  const systemUsers = useMemo(() => users.filter(user => user.role === 'app-admin'), [users]);
  const apiUsers = useMemo(() => users.filter(user => user.role === 'api-user'), [users]);

  const refreshUsers = useCallback(async () => {
    const result = await execute(async () => {
      const response = await adminApi.listUsers()
      if (response.status === 200) {
        return response.data;
      } else {
        throw new Error(`Failed to fetch users: ${response.status}`);
      }
    });

    if (result) {
      setUsers(result);
    }
  }, [adminApi, execute])

  const generateApiKey = useCallback(async (username: string, expiresAt: string): Promise<string> => {
    const result = await execute(async () => {
      const response = await adminApi.generateApiKey(username, 'api-user', expiresAt)

      if (response.status === 200) {
        const generatedKey = response.data;

        // Manually refresh user list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }

        return generatedKey;
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to generate API key: ${response.status}`)
      }
    });

    if (!result) {
      throw new Error('Failed to generate API key')
    }

    return result;
  }, [adminApi, execute])

  const renewApiKey = useCallback(async (userId: string, expiresAt?: string): Promise<string> => {
    const result = await execute(async () => {
      const response = await adminApi.renewApiKey(userId, expiresAt || '')

      if (response.status === 200) {
        const renewedKey = response.data;

        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }

        return renewedKey
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to renew API key: ${response.status}`)
      }
    });

    if (!result) {
      throw new Error('Failed to renew API key')
    }

    return result;
  }, [adminApi, execute])

  const retireApiKey = useCallback(async (userId: string): Promise<void> => {
    await execute(async () => {
      const response = await adminApi.retireApiKey(userId)

      if (response.status === 200) {
        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to retire API key: ${response.status}`)
      }
    });
  }, [adminApi, execute])

  const deleteUser = useCallback(async (userId: string): Promise<void> => {
    await execute(async () => {
      const response = await adminApi.deleteUser(userId)

      if (response.status === 200) {
        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to delete user: ${response.status}`)
      }
    });
  }, [adminApi, execute])

  const updateUser = useCallback(async (userId: string, username: string, expiresAt?: string): Promise<void> => {
    await execute(async () => {
      const response = await adminApi.updateUser(userId, username, expiresAt || '')

      if (response.status === 200) {
        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to update user: ${response.status}`)
      }
    });
  }, [adminApi, execute])

  const updateUserPassword = useCallback(async (userId: string, username: string, password?: string): Promise<void> => {
    await execute(async () => {
      const response = await adminApi.updateUserPassword(userId, username, password || '')

      if (response.status === 200) {
        // Manually refresh users list
        const usersResponse = await adminApi.listUsers()
        if (usersResponse.status === 200) {
          setUsers(usersResponse.data)
        }
      } else {
        const errorText = response.data;
        throw new Error(errorText || `Failed to update user password: ${response.status}`)
      }
    });
  }, [adminApi, execute])

  useEffect(() => {
    refreshUsers()
  }, [refreshUsers])

  return {
    users,
    systemUsers,
    apiUsers,
    loading,
    error,
    refreshUsers,
    generateApiKey,
    renewApiKey,
    retireApiKey,
    deleteUser,
    updateUser,
    updateUserPassword,
    clearError
  }
}