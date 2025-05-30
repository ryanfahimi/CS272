document.addEventListener('DOMContentLoaded', () => {
  const searchContainer = document.getElementById('searchContainer');
  const input = document.getElementById('searchInput');
  const clearButton = document.getElementById('clearButton');
  const settingsButton = document.getElementById('settingsButton');
  const advancedSettings = document.getElementById('advancedSettings');
  const results = document.getElementById('results');
  const pagination = document.getElementById('pagination');

  settingsButton.addEventListener('click', () => {
    advancedSettings.classList.toggle('hidden');
	searchContainer.classList.toggle('rounded-b-none');
  });

  const updateClear = () => clearButton.classList.toggle('hidden', !input.value);
  input.addEventListener('input', updateClear);
  updateClear();

  function clearSearch() {
    input.value = '';
    updateClear();
    [results, pagination].forEach(el => el && (el.innerHTML = ''));
    input.focus();
  }
  clearButton.addEventListener('click', clearSearch);

  function loadSettings(settings) {
    const params = new URLSearchParams(window.location.search);
    settings.forEach(({ id, type }) => {
      const el = document.getElementById(id);
      if (!el || !params.has(id)) return;
      const val = params.get(id);
      if (type === 'checkbox') {
        el.checked = val === 'true';
      } else {
        el.value = val;
      }
    });
  }

  loadSettings([
    { id: 'exactSearch', type: 'checkbox' },
    { id: 'reverseSearch', type: 'checkbox' },
    { id: 'sourceType', type: 'select' }
  ]);
});
