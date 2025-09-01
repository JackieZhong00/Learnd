import type { Config } from 'tailwindcss'

const config: Config = {
  theme: {
    extend: {
      backgroundImage: {
        'custom-radial':
          'radial-gradient(circle, #AFA4A2 0%, #988E8D 33%, #7B7473 68%, #716867 93%)',
      },
    },
  },
  plugins: [],
}

export default config
