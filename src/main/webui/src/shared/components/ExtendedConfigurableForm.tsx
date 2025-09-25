import React from 'react'
import { ConfigurableForm, FormConfig as BaseFormConfig } from 'crt-dojo'

// Extend the base FormMode type to include renewal
export type ExtendedFormMode = 'create' | 'edit' | 'view' | 'renewal'

// Extend the base FormConfig type
export interface ExtendedFormConfig extends Omit<BaseFormConfig, 'mode'> {
  mode: ExtendedFormMode
  renewalTitle?: string
  successContent?: React.ReactNode
  hideFormOnSuccess?: boolean
  showSuccess?: boolean
}

interface ExtendedConfigurableFormProps {
  config: ExtendedFormConfig
  initialData?: any
  className?: string
}

const ExtendedConfigurableForm: React.FC<ExtendedConfigurableFormProps> = ({
  config,
  initialData,
  className
}) => {
  // If we have success content and should hide the form, show only success content
  if (config.showSuccess && config.hideFormOnSuccess && config.successContent) {
    return (
      <div className={`p-4 border-b border-tui-border bg-tui-darker ${className}`}>
        {/* Custom Header for Renewal Mode */}
        {config.mode === 'renewal' && config.renewalTitle && (
          <div className="mb-4">
            <h3 className="text-tui-accent text-sm font-mono">
              {config.renewalTitle}
            </h3>
          </div>
        )}

        {/* Success Content */}
        {config.successContent}
      </div>
    )
  }

  // Convert extended config back to base config for ConfigurableForm
  const baseConfig: BaseFormConfig = {
    ...config,
    mode: config.mode === 'renewal' ? 'edit' : config.mode,
    headerContent: (
      <>
        {/* Custom Header for Renewal Mode */}
        {config.mode === 'renewal' && config.renewalTitle && (
          <div className="mb-4">
            <h3 className="text-tui-accent text-sm font-mono">
              {config.renewalTitle}
            </h3>
          </div>
        )}
        {config.headerContent}
      </>
    ),
    submitLabel: config.mode === 'renewal' ? 'RENEW USER' : config.submitLabel
  }

  return (
    <ConfigurableForm
      config={baseConfig}
      initialData={initialData}
      className={className}
    />
  )
}

export default ExtendedConfigurableForm