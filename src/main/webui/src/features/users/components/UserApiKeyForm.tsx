import React, { useRef } from 'react'
import { ApiUserDto } from '../../../api'
import { ExtendedForm } from '../../../shared'
import {ExtendedFormConfig, FormConfig} from '../../../shared/components/ExtendedForm'

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

interface ApiKeyFormData {
  keyName: string
  keyExpiresAt: string
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

  // Determine the form mode
  const getFormMode = () => {
    if (isRenewalMode) return 'renewal' as const
    if (isEditingKey) return 'edit' as const
    return 'create' as const
  }

  // Create form data object
  const formData: ApiKeyFormData = {
    keyName,
    keyExpiresAt
  }

  // Create the form configuration
  const formConfig: ExtendedFormConfig = {
    mode: getFormMode(),
    renewalTitle: isRenewalMode ? `RENEWING: ${editingApiKey?.username}` : undefined,
    editTitle: isEditingKey ? `EDITING: ${editingApiKey?.username}` : undefined,
    createTitle: 'CREATE KEY',
    hideFormOnSuccess: !!generatedKey,
    showSuccess: !!generatedKey,
    successContent: generatedKey ? (
      <div ref={createKeyGeneratedRef} className="p-4 bg-tui-dark border border-tui-accent rounded">
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
    ) : undefined,
    fields: [
      {
        key: 'keyName',
        type: 'text',
        label: 'Key Name',
        placeholder: isEditingKey ? "Edit key name" : "Enter key name",
        required: true,
        getValue: (data: ApiKeyFormData) => data.keyName || '',
        setValue: (data: ApiKeyFormData, value: string) => { data.keyName = value },
        onChange: (value: string) => onKeyNameChange(value)
      },
      {
        key: 'keyExpiresAt',
        type: 'datetime-local',
        label: 'Expiration Date',
        helpText: 'Leave empty for no expiration',
        getValue: (data: ApiKeyFormData) => data.keyExpiresAt ? data.keyExpiresAt.split('T')[0] : '',
        setValue: (data: ApiKeyFormData, value: string) => { data.keyExpiresAt = value },
        onChange: (value: string) => onKeyExpiresAtChange(value)
      }
    ],
    onSubmit: () => onSubmit()
  }

  return (
    <ExtendedForm
      config={formConfig}
      initialData={formData}
    />
  )
}

export default UserApiKeyForm
