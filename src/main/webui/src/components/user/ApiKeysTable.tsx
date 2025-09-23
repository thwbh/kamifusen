import React, { useMemo, useCallback } from 'react'
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  flexRender,
  createColumnHelper,
  SortingState
} from '@tanstack/react-table'
import { ApiUserDto } from '../../api/gen'

interface ApiKeysTableProps {
  users: ApiUserDto[]
  sorting: SortingState
  onSortingChange: (sorting: SortingState) => void
  onEditApiKey: (user: ApiUserDto) => void
  onRenewUser: (user: ApiUserDto) => void
  onRetireUser: (user: ApiUserDto) => void
  onDeleteUser: (user: ApiUserDto) => void
}

const ApiKeysTable: React.FC<ApiKeysTableProps> = ({
  users,
  sorting,
  onSortingChange,
  onEditApiKey,
  onRenewUser,
  onRetireUser,
  onDeleteUser
}) => {
  const columnHelper = createColumnHelper<ApiUserDto>()

  const handleEditApiKey = useCallback((user: ApiUserDto) => {
    onEditApiKey(user)
  }, [onEditApiKey])

  const handleRenewUser = useCallback((user: ApiUserDto) => {
    onRenewUser(user)
  }, [onRenewUser])

  const handleRetireUser = useCallback((user: ApiUserDto) => {
    onRetireUser(user)
  }, [onRetireUser])

  const handleDeleteUser = useCallback((user: ApiUserDto) => {
    onDeleteUser(user)
  }, [onDeleteUser])

  const columns = useMemo(() => [
    columnHelper.accessor('username', {
      header: 'Name',
      cell: info => <span className="font-mono text-tui-light">{info.getValue()}</span>
    }),
    columnHelper.accessor('added', {
      header: 'Created',
      cell: info => (
        <span className="text-tui-muted">
          {info.getValue() ? new Date(info.getValue()).toLocaleDateString() : 'Unknown'}
        </span>
      )
    }),
    columnHelper.accessor('expiresAt', {
      header: 'Expires',
      cell: info => {
        const expiresAt = info.getValue();
        if (!expiresAt) {
          return <span className="text-tui-muted">Never</span>;
        }

        const expirationDate = new Date(expiresAt);
        const isExpired = expirationDate <= new Date();

        return (
          <span className={isExpired ? "text-tui-red" : "text-tui-muted"}>
            {expirationDate.toLocaleDateString()}
            {isExpired && <span className="ml-1 text-xs">(EXPIRED)</span>}
          </span>
        );
      }
    }),
    columnHelper.display({
      id: 'status',
      header: 'Status',
      cell: info => {
        const user = info.row.original;
        const isExpired = user.expiresAt && new Date(user.expiresAt) <= new Date();

        return (
          <span className={isExpired ? "text-tui-red text-sm" : "text-tui-green text-sm"}>
            {isExpired ? 'EXPIRED' : 'ACTIVE'}
          </span>
        );
      }
    }),
    columnHelper.display({
      id: 'actions',
      header: 'Actions',
      cell: info => {
        const user = info.row.original;
        const isExpired = user.expiresAt && new Date(user.expiresAt) <= new Date();

        return (
          <div>
            {isExpired ? (
              <>
                <button
                  onClick={() => handleRenewUser(user)}
                  className="text-tui-green hover:text-tui-accent text-sm mr-2"
                >
                  RENEW
                </button>
                <button
                  onClick={() => handleDeleteUser(user)}
                  className="text-tui-red hover:text-tui-accent text-sm"
                >
                  DELETE
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={() => handleEditApiKey(user)}
                  className="text-tui-accent hover:text-tui-accent-hover text-sm mr-2"
                >
                  EDIT
                </button>
                <button
                  onClick={() => handleRetireUser(user)}
                  className="text-tui-red hover:text-tui-accent text-sm"
                >
                  REVOKE
                </button>
              </>
            )}
          </div>
        );
      }
    })
  ], [columnHelper, handleEditApiKey, handleRenewUser, handleRetireUser, handleDeleteUser])

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

export default ApiKeysTable