import React, { useMemo } from 'react'
import { SortingState, OnChangeFn } from '@tanstack/react-table'
import { DataTable, DataTableConfig, DataTableColumn, DataTableAction } from 'crt-dojo'
import { ApiUserDto } from '../../../api'

interface SystemUsersTableProps {
  users: ApiUserDto[]
  sorting: SortingState
  onSortingChange: OnChangeFn<SortingState>
  onEditUser: (user: ApiUserDto) => void
  onDeleteUser: (user: ApiUserDto) => void
}

const UserTable: React.FC<SystemUsersTableProps> = ({
  users,
  sorting,
  onSortingChange,
  onEditUser,
  onDeleteUser
}) => {
  const columns = useMemo((): DataTableColumn<ApiUserDto>[] => [
    {
      key: 'username',
      header: 'Username',
      accessor: 'username',
      cell: (value: string) => <span className="font-mono text-tui-light">{value}</span>
    },
    {
      key: 'role',
      header: 'Role',
      accessor: 'role',
      cell: (value: string) => <span className="text-tui-yellow">{value}</span>
    },
    {
      key: 'added',
      header: 'Added',
      accessor: 'added',
      cell: (value: string) => (
        <span className="text-tui-muted">
          {value ? new Date(value as string).toLocaleDateString() : 'Unknown'}
        </span>
      )
    }
  ], [])

  const actions = useMemo((): DataTableAction<ApiUserDto>[] => [
    {
      label: 'EDIT',
      variant: 'primary',
      onClick: onEditUser,
      hidden: (user: ApiUserDto) => true

    },
    {
      label: 'DELETE',
      variant: 'danger',
      onClick: onDeleteUser,
      //      hidden: (user: ApiUserDto) => user.role === 'app-admin'
      hidden: (user: ApiUserDto) => true
    }
  ], [onEditUser, onDeleteUser])

  const config: DataTableConfig<ApiUserDto> = {
    data: users,
    columns,
    sorting,
    onSortingChange,
    actions
  }

  return <DataTable config={config} />
}

export default UserTable
