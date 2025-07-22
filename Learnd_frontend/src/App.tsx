import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import Home from './components/RegisterLoginPage.tsx'
import ErrorPage from './components/ErrorPage.tsx'
import DeckHome from './components/DeckHome.tsx'
import Deck from './components/Deck.tsx'
import Profile from './components/Profile.tsx'
import Settings from './components/Settings.tsx'

const router = createBrowserRouter([
  {
    path: '/',
    element: <Home />,
    errorElement: <ErrorPage />,
  },
  {
    path: '/:username',
    element: <Profile />,
    errorElement: <ErrorPage />,
  },
  {
    path: '/:username/deck_home',
    element: <DeckHome />,
    errorElement: <ErrorPage />,
  },
  {
    path: '/:username/:deck_name/:deck_id',
    element: <Deck/>,
    errorElement: <ErrorPage/>
  },
  {
    path: '/:username/settings',
    element: <Settings/>,
    errorElement: <ErrorPage/>
  }
  // {
  //   path: '/:username/:topicName',
  //   element: <TopicHome />,
  //   errorElement: <ErrorPage />,
  // }
])

function App() {
  
  return (
    <div className="">
      <RouterProvider router={router}></RouterProvider>
    </div>
  )
}

export default App
