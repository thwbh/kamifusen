import { render, screen } from '../../../../test-utils'
import userEvent from '@testing-library/user-event'
import UserForm from '../UserForm'

const mockUser = {
  username: 'testuser',
  expiresAt: '2024-12-31T23:59',
  password: ''
}

const defaultProps = {
  newUser: mockUser,
  onUserChange: vi.fn(),
  onSubmit: vi.fn(),
  isEditMode: false,
  isRenewalMode: false,
  editingUser: null,
  generatedKey: '',
  onGeneratedKeyDismiss: vi.fn(),
  onCopyToClipboard: vi.fn()
}

describe('UserForm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders form fields correctly', () => {
    render(<UserForm {...defaultProps} />)

    expect(screen.getByDisplayValue('testuser')).toBeInTheDocument()
    expect(screen.getByText('Username')).toBeInTheDocument()
  })

  it('renders password field in edit mode', () => {
    render(<UserForm {...defaultProps} isEditMode={true} />)

    expect(screen.getByLabelText(/new password/i)).toBeInTheDocument()
  })

  it('calls onUserChange when username is updated', async () => {
    const user = userEvent.setup()
    render(<UserForm {...defaultProps} />)

    const usernameInput = screen.getByDisplayValue('testuser')
    await user.clear(usernameInput)
    await user.type(usernameInput, 'newuser')

    expect(defaultProps.onUserChange).toHaveBeenCalledWith({
      ...mockUser,
      username: 'newuser'
    })
  })

  it('calls onUserChange when password is updated in edit mode', async () => {
    const user = userEvent.setup()
    render(<UserForm {...defaultProps} isEditMode={true} />)

    const passwordInput = screen.getByLabelText(/new password/i)
    await user.type(passwordInput, 'newpassword')

    expect(defaultProps.onUserChange).toHaveBeenCalledWith({
      ...mockUser,
      password: 'newpassword'
    })
  })

  it('calls onSubmit when form is submitted', async () => {
    const user = userEvent.setup()
    render(<UserForm {...defaultProps} />)

    const submitButton = screen.getByText('CREATE USER')
    await user.click(submitButton)

    expect(defaultProps.onSubmit).toHaveBeenCalledTimes(1)
  })

  it('shows UPDATE USER button in edit mode', () => {
    render(<UserForm {...defaultProps} isEditMode={true} />)

    expect(screen.getByText('UPDATE USER')).toBeInTheDocument()
    expect(screen.queryByText('CREATE USER')).not.toBeInTheDocument()
  })

  it('shows RENEW USER button in renewal mode', () => {
    render(<UserForm {...defaultProps} isRenewalMode={true} />)

    expect(screen.getByText('RENEW USER')).toBeInTheDocument()
    expect(screen.queryByText('CREATE USER')).not.toBeInTheDocument()
  })

  it('displays generated key when provided', () => {
    render(<UserForm {...defaultProps} generatedKey="test-api-key-123" />)

    expect(screen.getByText(/Generated API Key/i)).toBeInTheDocument()
    expect(screen.getByText('test-api-key-123')).toBeInTheDocument()
    expect(screen.getByText('COPY')).toBeInTheDocument()
    expect(screen.getByText('DISMISS')).toBeInTheDocument()
  })

  it('calls onCopyToClipboard when copy button is clicked', async () => {
    const user = userEvent.setup()
    render(<UserForm {...defaultProps} generatedKey="test-api-key-123" />)

    const copyButton = screen.getByText('COPY')
    await user.click(copyButton)

    expect(defaultProps.onCopyToClipboard).toHaveBeenCalledWith('test-api-key-123')
  })

  it('calls onGeneratedKeyDismiss when dismiss button is clicked', async () => {
    const user = userEvent.setup()
    render(<UserForm {...defaultProps} generatedKey="test-api-key-123" />)

    const dismissButton = screen.getByText('DISMISS')
    await user.click(dismissButton)

    expect(defaultProps.onGeneratedKeyDismiss).toHaveBeenCalledTimes(1)
  })

  it('prevents form submission when no username is provided', async () => {
    const user = userEvent.setup()
    const emptyUser = { username: '', expiresAt: '', password: '' }

    render(<UserForm {...defaultProps} newUser={emptyUser} />)

    const submitButton = screen.getByText('CREATE USER')
    await user.click(submitButton)

    expect(defaultProps.onSubmit).not.toHaveBeenCalled()
  })
})