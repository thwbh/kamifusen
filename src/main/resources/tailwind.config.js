/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [ "META-INF/resources/*.html", "../kotlin/io/tohuwabohu/kamifusen/**/htmx*.kt" ],
  theme: {
    extend: {},
  },
  plugins: [ require('@tailwindcss/forms') ],
}

