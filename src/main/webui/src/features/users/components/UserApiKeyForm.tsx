import React, { useRef } from 'react'
import { ApiUserDto } from '../../../api'

interface ApiKeyFormProps {
  keyName: string
  keyExpiresAt: string
  onKeyNameChange: (name: string) => void
  onKeyExpiresAtChange: (expiresAt: string) => void
  onSubmit: () => void
  isEditingKey: boolean
  isRenewalMode: boolean
  editingApiKey: ApiUserDto | null
  generatedKey: string
  onGeneratedKeyDismiss: () => void
  onCopyToClipboard: (text: string) => void
}

const UserApiKeyForm: React.FC<ApiKeyFormProps> = ({
  keyName,
  keyExpiresAt,
  onKeyNameChange,
  onKeyExpiresAtChange,
  onSubmit,
  isEditingKey,
  isRenewalMode,
  editingApiKey,
  generatedKey,
  onGeneratedKeyDismiss,
  onCopyToClipboard
}) => {
  const createKeyGeneratedRef = useRef<HTMLDivElement>(null)

  return (
    <div className="p-4 border-b border-tui-border bg-tui-darker">
      {(isEditingKey || isRenewalMode) && (
        <div className="mb-4">
          <h3 className="text-tui-accent text-sm font-mono">
            {isEditingKey ? `EDITING: ${editingApiKey?.username}`
             : isRenewalMode ? `RENEWING: ${editingApiKey?.username}`
             : ''}
          </h3>
        </div>
      )}
      <div className="space-y-4">
        <div>
          <label className="block text-tui-muted text-sm mb-2">
            {isEditingKey ? 'Key Name' : 'Key Name'}
          </label>
          <input
            type="text"
            value={keyName}
            onChange={(e) => onKeyNameChange(e.target.value)}
            className="w-full bg-tui-dark border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
            placeholder={isEditingKey ? "Edit key name" : "Enter key name"}
          />
        </div>
        <div>
          <label className="block text-tui-muted text-sm mb-2">Expiration Date</label>
          <input
            type="date"
            value={keyExpiresAt ? keyExpiresAt.split('T')[0] : ''}
            onChange={(e) => onKeyExpiresAtChange(e.target.value ? `${e.target.value}T23:59:59` : '')}
            className="w-full bg-tui-dark border border-tui-border p-2 text-tui-light font-mono focus:border-tui-accent focus:outline-none"
          />
          <p className="text-tui-muted text-xs mt-1">Leave empty for no expiration</p>
        </div>
        <button
          onClick={onSubmit}
          disabled={!keyName}
          className="tui-button disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isEditingKey ? 'UPDATE KEY'
           : isRenewalMode ? 'RENEW KEY'
           : 'GENERATE KEY'}
        </button>

        {generatedKey && (
          <div ref={createKeyGeneratedRef} className="p-4 bg-tui-dark border border-tui-accent rounded mt-4">
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

export default UserApiKeyForm