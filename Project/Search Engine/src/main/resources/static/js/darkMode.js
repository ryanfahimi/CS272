document.addEventListener('DOMContentLoaded', () => {
  const button = document.getElementById('themeToggle');
  button.addEventListener('click', () => {
    const isDark = document.documentElement.classList.toggle('dark');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
  });
});
