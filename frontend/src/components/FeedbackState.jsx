export function LoadingState({ message = 'Cargando...' }) {
  return (
    <div
      role="status"
      className="flex flex-col items-center justify-center rounded-lg border border-slate-200 bg-white p-12 text-center shadow-sm"
    >
      <div
        className="h-8 w-8 animate-spin rounded-full border-4 border-slate-300 border-t-slate-800"
        aria-hidden="true"
      />
      <p className="mt-4 text-sm font-medium text-slate-600">{message}</p>
    </div>
  )
}

export function ErrorState({ message, onRetry, retryLabel = 'Reintentar' }) {
  return (
    <div
      role="alert"
      className="rounded-lg border border-red-200 bg-red-50 p-6 text-center shadow-sm"
    >
      <p className="text-sm font-semibold text-red-800">
        {message || 'Ocurrió un error al cargar la información.'}
      </p>
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="mt-4 inline-flex items-center rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
        >
          {retryLabel}
        </button>
      )}
    </div>
  )
}

export function EmptyState({ message = 'No hay datos disponibles.', children }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-8 text-center shadow-sm">
      <p className="text-sm text-slate-600">{message}</p>
      {children && <div className="mt-4">{children}</div>}
    </div>
  )
}

export default function FeedbackState({
  isLoading,
  error,
  isEmpty,
  onRetry,
  loadingMessage = 'Cargando...',
  emptyMessage = 'No hay datos disponibles.',
  retryLabel = 'Reintentar',
  children,
}) {
  if (isLoading) {
    return <LoadingState message={loadingMessage} />
  }

  if (error) {
    return <ErrorState message={error} onRetry={onRetry} retryLabel={retryLabel} />
  }

  if (isEmpty) {
    return <EmptyState message={emptyMessage} />
  }

  return children
}
