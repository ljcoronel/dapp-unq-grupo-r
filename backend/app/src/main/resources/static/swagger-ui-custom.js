(function () {
    const PUBLIC_PATHS = new Set(['/login', '/register', '/users/{id}/', '/players', '/players/{id}']);

    function labelPublicOperations() {
        const operationEls = document.querySelectorAll('.opblock');
        operationEls.forEach((el) => {
            const summary = el.querySelector('.opblock-summary-path');
            if (!summary) return;
            const path = summary.getAttribute('data-path') || summary.textContent || '';
            if (!PUBLIC_PATHS.has(path)) return;

            const summaryControl = el.querySelector('.opblock-summary');
            if (!summaryControl || summaryControl.querySelector('.opblock-tag')) return;

            const badge = document.createElement('span');
            badge.className = 'opblock-tag';
            badge.textContent = 'Público';
            badge.setAttribute('aria-label', 'Operación pública');
            summaryControl.appendChild(badge);
        });
    }

    function requireRegisterConfirmation() {
        const registerOperation = document.querySelector('[data-operation-id="register"]');
        if (!registerOperation) return;

        const registerButton = registerOperation.querySelector('.try-it-out button, button.execute');
        if (!registerButton || registerButton.dataset.confirmHook === 'true') return;

        registerButton.dataset.confirmHook = 'true';
        registerButton.addEventListener('click', function (event) {
            const confirmed = window.confirm('Se va a crear una cuenta nueva. ¿Confirmás que deseas registrar este usuario?');
            if (!confirmed) {
                event.preventDefault();
                event.stopPropagation();
            }
        });
    }

    function setup() {
        const root = document.querySelector('#swagger-ui');
        if (!root) {
            setTimeout(setup, 250);
            return;
        }

        labelPublicOperations();
        requireRegisterConfirmation();

        const observer = new MutationObserver(() => {
            labelPublicOperations();
            requireRegisterConfirmation();
        });

        observer.observe(root, { childList: true, subtree: true });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', setup);
    } else {
        setup();
    }
})();
