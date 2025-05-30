document.addEventListener('DOMContentLoaded', () => {
  const adminButton = document.getElementById('adminButton');
  const adminModal = document.getElementById('adminModal');
  const closeAdminModalButton = document.getElementById('closeAdminModalButton');
  const adminForm = document.getElementById('adminForm');
  const adminMessage = document.getElementById('adminMessage');

  function toggleModal(show) {
    if (!adminModal || !adminButton) return;
    adminModal.classList.toggle('hidden', !show);
    adminModal.setAttribute('aria-hidden', String(!show));
    adminButton.setAttribute('aria-expanded', String(show));
    
    if (show && adminForm) {
      adminForm.reset();
      hideMessage();
    }
  }

  function showMessage(text, type) {
    if (!adminMessage) return;
    
	adminMessage.className = 'mb-4 mt-3 p-3 rounded-lg text-sm text-center font-medium border';

	switch (type) {
		case 'success':
		  adminMessage.classList.add('bg-green-50', 'text-green-800', 'border-green-500', 'dark:bg-green-200', 'dark:text-green-900', 'dark:border-green-500');
		  break;
		case 'error':
		  adminMessage.classList.add('bg-red-50', 'text-red-700', 'border-red-300', 'dark:bg-red-200', 'dark:text-red-900', 'dark:border-red-500');
		  break;
	}
    
    adminMessage.textContent = text;
    adminMessage.classList.remove('hidden');
  }

  function hideMessage() {
    if (adminMessage) {
      adminMessage.classList.add('hidden');
    }
  }

  adminButton.addEventListener('click', () => toggleModal(true));
  closeAdminModalButton.addEventListener('click', () => toggleModal(false));
  adminModal.addEventListener('click', e => {
    if (e.target === adminModal) toggleModal(false);
  });

  if (adminForm) {
    adminForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      
      const token = document.getElementById('adminPassword').value;
      const submitButton = document.getElementById('shutdownButton');
      
      submitButton.disabled = true;
      submitButton.textContent = 'Shutting down...';
      hideMessage();
      
      try {
        const response = await fetch(`/shutdown?token=${encodeURIComponent(token)}`, {
          method: 'POST'
        });
        
        if (response.ok) {
          showMessage('Shutdown initiated successfully!', 'success');
          setTimeout(() => toggleModal(false), 2000);
        } else {
          showMessage('Invalid password. Please try again.', 'error');
          document.getElementById('adminPassword')?.focus();
        }
      } catch (error) {
        showMessage('Network error. Please check your connection.', 'error');
      } finally {
        submitButton.disabled = false;
        submitButton.textContent = 'Shutdown Server';
      }
    });
  }
});