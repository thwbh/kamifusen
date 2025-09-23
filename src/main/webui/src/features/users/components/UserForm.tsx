import React, { useRef } from 'react'
import { ApiUserDto } from '../../../api'

interface UserFormProps {
  newUser: {
    username: string
    expiresAt: string
    password: string
  }
  onUserChange: (user: { username: string; expiresAt: string; password: string }) => void
  onSubmit: () => void
  isEditMode: boolean
  isRenewalMode: boolean
  editingUser: ApiUserDto | null
  generatedKey: string
  onGeneratedKeyDismiss: () => void
  onCopyToClipboard: (text: string) => void
}

const UserForm: React.FC<UserFormProps> = ({
  newUser,
  onUserChange,
  onSubmit,
  isEditMode,
  isRenewalMode,
  editingUser,
  generatedKey,
  onGeneratedKeyDismiss,
  onCopyToClipboard
}) => {
  const systemUserGeneratedRef = useRef<HTMLDivElement>(null)

  return (
    <div className="p-4 border-b border-tui-border bg-tui-darker">
      {(isEditMode || isRenewalMode) && (
        <div className="mb-4">
          <h3 className="text-tui-accent text-sm font-mono">
            {isEditMode ? `EDITING: ${editingUser?.username}`
             : isRenewalMode ? `RENEWING: ${editingUser?.username}`
             : ''}
          </h3>
        </div>
      )}
      <div className="space-y-4">
        <div>
          <label className="block text-tui-muted text-sm mb-2">Username</label>
          <input
            type="text"
            value={newUser.username}
            onChange={(e) => onUserChange({ ...newUser, username: e.target.value })}
            className="w-full bg-tui-dark border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
            placeholder={isEditMode ? "Edit username" : "Enter username"}
          />
        </div>
        {isEditMode && (
          <div>
            <label className="block text-tui-muted text-sm mb-2">New Password</label>
            <input
              type="password"
              value={newUser.password}
              onChange={(e) => onUserChange({ ...newUser, password: e.target.value })}
              className="w-full bg-tui-dark border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
              placeholder="Enter new password"
            />
            <p className="text-tui-muted text-xs mt-1">Leave empty to keep current password</p>
          </div>
        )}
        <button
          onClick={onSubmit}
          disabled={!newUser.username}
          className="tui-button disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isEditMode ? 'UPDATE USER'
           : isRenewalMode ? 'RENEW USER'
           : 'CREATE USER'}
        </button>

        {generatedKey && !isEditMode && (
          <div ref={systemUserGeneratedRef} className="p-4 bg-tui-dark border border-tui-accent rounded mt-4">
            <p className="text-tui-accent text-sm mb-2">
              {isRenewalMode ? 'Renewed API Key (copy now, won\'t be shown again):'
               : 'Generated API Key (copy now, won\'t be shown again):'}
            </p>
            <div className="flex items-center space-x-2">
              <code className="flex-1 p-2 bg-tui-darker text-tui-light font-mono text-sm">
                {generatedKey}
              </code>
              <button
                onClick={() => onCopyToClipboard(generatedKey)}
                className="px-3 py-2 bg-tui-accent text-tui-dark text-sm font-mono hover:bg-tui-accent-hover"
              >
                COPY
              </button>
            </div>
            <button
              onClick={onGeneratedKeyDismiss}
              className="mt-2 text-tui-muted hover:text-tui-accent text-sm"
            >
              DISMISS
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

export default UserForm