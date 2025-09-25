import React, { useRef } from 'react'
import { ApiUserDto } from '../../../api'
import { ExtendedForm } from "../../../shared";
import { ExtendedFormConfig } from "../../../shared/components/ExtendedForm";

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

  // Determine the form mode
  const getFormMode = () => {
    if (isRenewalMode) return 'renewal' as const
    if (isEditMode) return 'edit' as const
    return 'create' as const
  }

  // Create the form configuration
  const formConfig: ExtendedFormConfig = {
    mode: getFormMode(),
    renewalTitle: isRenewalMode ? `RENEWING: ${editingUser?.username}` : undefined,
    editTitle: isEditMode ? `EDITING: ${editingUser?.username}` : undefined,
    createTitle: 'CREATE USER',
    hideFormOnSuccess: !!generatedKey,
    showSuccess: !!generatedKey,
    successContent: generatedKey ? (
      <div ref={systemUserGeneratedRef} className="p-4 bg-tui-dark border border-tui-accent rounded">
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
        key: 'username',
        type: 'text',
        label: 'Username',
        placeholder: isEditMode ? "Edit username" : "Enter username",
        required: true,
        getValue: (data: any) => data.username || '',
        setValue: (data: any, value: string) => { data.username = value },
        onChange: (value: string) => onUserChange({ ...newUser, username: value })
      },
      {
        key: 'password',
        type: 'password',
        label: 'New Password',
        placeholder: 'Enter new password',
        helpText: 'Leave empty to keep current password',
        editModeOnly: true,
        getValue: (data: any) => data.password || '',
        setValue: (data: any, value: string) => { data.password = value },
        onChange: (value: string) => onUserChange({ ...newUser, password: value })
      }
    ],
    onSubmit: () => onSubmit()
  }

  return (
    <ExtendedForm
      config={formConfig}
      initialData={newUser}
    />
  )
}

export default UserForm
