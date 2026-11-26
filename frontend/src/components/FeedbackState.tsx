interface FeedbackStateProps {
  title?: string
  message: string
  variant?: 'loading' | 'error' | 'empty' | 'info'
  action?: {
    label: string
    onClick: () => void
  }
}

const variantStyles = {
  loading: 'border-slate-200 bg-white text-slate-600',
  error: 'border-red-200 bg-red-50 text-red-800',
  empty: 'border-slate-200 bg-white text-slate-600',
  info: 'border-sky-200 bg-sky-50 text-sky-800',
}

const FeedbackState = ({
  title,
  message,
  variant = 'info',
  action,
}: FeedbackStateProps) => (
  <div
    className={`rounded-xl border p-6 text-center shadow-sm ${variantStyles[variant]}`}
    role={variant === 'error' ? 'alert' : 'status'}
  >
    {variant === 'loading' && (
      <div className="mx-auto mb-4 h-7 w-7 animate-spin rounded-full border-4 border-slate-300 border-t-sky-600" />
    )}
    {title && <h2 className="font-semibold">{title}</h2>}
    <p className={title ? 'mt-1 text-sm' : 'text-sm'}>{message}</p>
    {action && (
      <button
        type="button"
        onClick={action.onClick}
        className="mt-4 rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-800"
      >
        {action.label}
      </button>
    )}
  </div>
)

export default FeedbackState
