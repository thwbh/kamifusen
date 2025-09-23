import React, { useMemo, useCallback } from 'react'
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  flexRender,
  createColumnHelper,
  SortingState,
  OnChangeFn
} from '@tanstack/react-table'
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
  const columnHelper = createColumnHelper<ApiUserDto>()

  const handleEditUser = useCallback((user: ApiUserDto) => {
    onEditUser(user)
  }, [onEditUser])

  const handleDeleteUser = useCallback((user: ApiUserDto) => {
    onDeleteUser(user)
  }, [onDeleteUser])

  const columns = useMemo(() => [
    columnHelper.accessor('username', {
      header: 'Username',
      cell: info => <span className="font-mono text-tui-light">{info.getValue()}</span>
    }),
    columnHelper.accessor('role', {
      header: 'Role',
      cell: info => <span className="text-tui-yellow">{info.getValue()}</span>
    }),
    columnHelper.accessor('added', {
      header: 'Added',
      cell: info => (
        <span className="text-tui-muted">
          {info.getValue() ? new Date(info.getValue() as string).toLocaleDateString() : 'Unknown'}
        </span>
      )
    }),
    columnHelper.display({
      id: 'actions',
      header: 'Actions',
      cell: info => {
        const user = info.row.original;
        const isAdmin = user.role === 'app-admin';

        return (
          <div>
            <button
              onClick={() => handleEditUser(user)}
              className="text-tui-accent hover:text-tui-accent-hover text-sm mr-2"
            >
              EDIT
            </button>
            {!isAdmin && (
              <button
                onClick={() => handleDeleteUser(user)}
                className="text-tui-red hover:text-tui-accent text-sm"
              >
                DELETE
              </button>
            )}
          </div>
        );
      }
    })
  ], [columnHelper, handleEditUser, handleDeleteUser])

  const table = useReactTable({
    data: users,
    columns,
    state: {
      sorting,
    },
    onSortingChange,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  })

  return (
    <div className="p-0">
      <table className="tui-table">
        <thead>
          {table.getHeaderGroups().map(headerGroup => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map(header => (
                <th
                  key={header.id}
                  className={header.column.getCanSort() ? 'cursor-pointer select-none' : ''}
                  onClick={header.column.getToggleSortingHandler()}
                >
                  <div className="flex items-center space-x-1">
                    <span>
                      {flexRender(header.column.columnDef.header, header.getContext())}
                    </span>
                    {header.column.getCanSort() && (
                      <span className="text-tui-muted">
                        {{
                          asc: '↑',
                          desc: '↓',
                        }[header.column.getIsSorted() as string] ?? '↕'}
                      </span>
                    )}
                  </div>
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody>
          {table.getRowModel().rows.map(row => (
            <tr key={row.id}>
              {row.getVisibleCells().map(cell => (
                <td key={cell.id}>
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default UserTable