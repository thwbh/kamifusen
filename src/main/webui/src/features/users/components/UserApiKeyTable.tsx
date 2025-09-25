import React, { useMemo } from 'react'
import { SortingState, OnChangeFn } from '@tanstack/react-table'
import { DataTable, DataTableConfig, DataTableColumn, DataTableAction } from 'crt-dojo'
import { ApiUserDto } from '../../../api'

interface ApiKeysTableProps {
  users: ApiUserDto[]
  sorting: SortingState
  onSortingChange: OnChangeFn<SortingState>
  onEditApiKey: (user: ApiUserDto) => void
  onRenewUser: (user: ApiUserDto) => void
  onRetireUser: (user: ApiUserDto) => void
  onDeleteUser: (user: ApiUserDto) => void
}

const UserApiKeyTable: React.FC<ApiKeysTableProps> = ({
  users,
  sorting,
  onSortingChange,
  onEditApiKey,
  onRenewUser,
  onRetireUser,
  onDeleteUser
}) => {
  const columns = useMemo((): DataTableColumn<ApiUserDto>[] => [
    {
      key: 'username',
      header: 'Name',
      accessor: 'username',
      cell: (value: string) => <span className="font-mono text-tui-light">{value}</span>
    },
    {
      key: 'added',
      header: 'Created',
      accessor: 'added',
      cell: (value: string) => (
        <span className="text-tui-muted">
          {value ? new Date(value as string).toLocaleDateString() : 'Unknown'}
        </span>
      )
    },
    {
      key: 'expiresAt',
      header: 'Expires',
      accessor: 'expiresAt',
      cell: (value: never) => {
        if (!value) {
          return <span className="text-tui-muted">Never</span>;
        }

        const expirationDate = new Date(value);
        const isExpired = expirationDate <= new Date();

        return (
          <span className={isExpired ? "text-tui-red" : "text-tui-muted"}>
            {expirationDate.toLocaleDateString()}
            {isExpired && <span className="ml-1 text-xs">(EXPIRED)</span>}
          </span>
        );
      }
    },
    {
      key: 'status',
      header: 'Status',
      accessor: (user: ApiUserDto) => user.expiresAt && new Date(user.expiresAt) <= new Date(),
      cell: (isExpired: boolean) => (
        <span className={isExpired ? "text-tui-red text-sm" : "text-tui-green text-sm"}>
          {isExpired ? 'EXPIRED' : 'ACTIVE'}
        </span>
      )
    }
  ], [])

  const actions = useMemo((): DataTableAction<ApiUserDto>[] => [
      {
        label: 'RENEW',
        variant: 'primary',
        onClick: onRenewUser,
        hidden: (user: ApiUserDto) => !(user.expiresAt && new Date(user.expiresAt) <= new Date())
      },
      {
        label: 'DELETE',
        variant: 'danger',
        onClick: onDeleteUser,
        hidden: (user: ApiUserDto) => !(user.expiresAt && new Date(user.expiresAt) <= new Date())
      },
      {
        label: 'EDIT',
        variant: 'primary',
        onClick: onEditApiKey,
        hidden: (user: ApiUserDto) => user.expiresAt && new Date(user.expiresAt) <= new Date()
      },
      {
        label: 'REVOKE',
        variant: 'danger',
        onClick: onRetireUser,
        hidden: (user: ApiUserDto) => user.expiresAt && new Date(user.expiresAt) <= new Date()
      }
  ], [onEditApiKey, onRenewUser, onRetireUser, onDeleteUser])

  const config: DataTableConfig<ApiUserDto> = {
    data: users,
    columns,
    sorting,
    onSortingChange,
    actions
  }

  return <DataTable config={config} />
}

export default UserApiKeyTable
