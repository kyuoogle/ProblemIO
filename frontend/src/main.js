import './assets/main.css'
import './assets/utilities.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import { definePreset } from '@primevue/themes'
import App from './App.vue'
import router from './router'

// PrimeVue 컴포넌트
import Button from 'primevue/button'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Textarea from 'primevue/textarea'
import Dialog from 'primevue/dialog'
import Toast from 'primevue/toast'
import ToastService from 'primevue/toastservice'
import ProgressBar from 'primevue/progressbar'
import Avatar from 'primevue/avatar'
import Badge from 'primevue/badge'
import Divider from 'primevue/divider'
import FileUpload from 'primevue/fileupload'
import Menubar from 'primevue/menubar'
import Chips from 'primevue/chips'
import Paginator from 'primevue/paginator'
import ConfirmDialog from 'primevue/confirmdialog'
import ConfirmationService from 'primevue/confirmationservice'

// PrimeIcons
import 'primeicons/primeicons.css'

const app = createApp(App)
const pinia = createPinia()

// PrimeVue 설정
app.use(PrimeVue, {
  theme: {
    preset: definePreset(Aura, {
      semantic: {
        colorScheme: {
          light: {
            primary: {
              50: '#ecfeff',
              100: '#cffafe',
              200: '#a5f3fc',
              300: '#67e8f9',
              400: '#22d3ee',
              500: '#06b6d4',
              600: '#0891b2',
              700: '#0e7490',
              800: '#155e75',
              900: '#164e63',
              950: '#083344',
              color: '{primary.400}',
              inverseColor: '#0f172a',
              hoverColor: '{primary.500}',
              activeColor: '{primary.600}'
            },
            highlight: {
              background: '{primary.50}',
              focusBackground: '{primary.100}',
              color: '{primary.700}',
              focusColor: '{primary.800}'
            }
          },
          dark: {
            primary: {
              50: '#e0fbff',
              100: '#b3f2ff',
              200: '#80e7ff',
              300: '#4ddbff',
              400: '#1accff',
              500: '#00a6cc',
              600: '#0085a3',
              700: '#00637a',
              800: '#004252',
              900: '#002129',
              950: '#001014',
              color: '{primary.500}',
              inverseColor: '#ffffff',
              hoverColor: '{primary.600}',
              activeColor: '{primary.700}'
            },
            highlight: {
              background: 'rgba(250, 250, 250, .16)',
              focusBackground: 'rgba(250, 250, 250, .24)',
              color: 'rgba(255,255,255,.87)',
              focusColor: 'rgba(255,255,255,.87)'
            }
          }
        }
      }
    }),
    options: {
      darkModeSelector: '.dark-mode',
      cssLayer: false,
    },
  },
})

app.use(ToastService)
app.use(ConfirmationService)
app.use(pinia)
app.use(router)

// PrimeVue 컴포넌트 등록
app.component('Button', Button)
app.component('Card', Card)
app.component('InputText', InputText)
app.component('Password', Password)
app.component('Textarea', Textarea)
app.component('Dialog', Dialog)
app.component('Toast', Toast)
app.component('ProgressBar', ProgressBar)
app.component('Avatar', Avatar)
app.component('Badge', Badge)
app.component('Divider', Divider)
app.component('FileUpload', FileUpload)
app.component('Menubar', Menubar)
app.component('Chips', Chips)
app.component('Paginator', Paginator)
app.component('ConfirmDialog', ConfirmDialog)

app.mount('#app')
