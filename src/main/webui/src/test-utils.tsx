import React, { ReactElement } from 'react'
import { render, RenderOptions } from '@testing-library/react'

// Custom render function that includes providers if needed
const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => {
  // Add providers here if needed (e.g., Router, Context providers)
  return render(ui, options)
}

export * from '@testing-library/react'
export { customRender as render }