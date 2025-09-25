import logo from '../assets/learnd_logo.png'
import { useParams, useNavigate} from 'react-router-dom'
import { useState } from 'react'
import axios from 'axios'
import { useQuery } from '@tanstack/react-query'
import CalendarHeatmap from 'react-calendar-heatmap'
import {Tooltip} from 'react-tooltip'
type Day = { date: string; isConsistent: boolean }
const Profile = () => {
  const [menuOpen, setMenuOpen] = useState<boolean>(false)
  const param = useParams()
  const navigate = useNavigate()
  const currentYear = new Date().getFullYear()


  const {data, isLoading, isError, error} = useQuery({
    queryKey:["getConsistencyData"],
    queryFn: async () : Promise<Day[]> => {
      const response = await axios.get(`http://localhost:8080/api/consistency/year`, {withCredentials: true})
      return response.data
    }
  })

  const values =
    data?.map((d) => ({
      date: d.date, // 'YYYY-MM-DD'
      count: d.isConsistent ? 1 : 0,
    })) ?? []

  console.log(values)
  if (isLoading) return <div>Loading...</div>
  if (isError) return <div>Error: {(error as Error).message}</div>

  return (
    <div className="w-screen h-screen bg-[radial-gradient(circle,_#BCA8A8_0%,_#837675_83%,_#847674_100%)]">
      <div className="block flex flex-row h-[10vh]">
        <div className="flex w-full h-[100px]">
          <div className="w-[80px] h-[60px] overflow-hidden mt-[30px] ml-[30px]">
            <img
              src={logo}
              alt="learnd_logo"
              className="w-full h-full object-cover rounded-[55px]"
            />
          </div>
        </div>
        <div className="flex justify-center items-center mr-[3vw] h-[10vh]">
          {!menuOpen && (
            <button
              onClick={() => setMenuOpen(!menuOpen)}
              className="w-[2vw] h-[3.5vh]"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="size-6"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
                />
              </svg>
            </button>
          )}
          {menuOpen && (
            <div className="absolute right-[3vw] top-[3vh] mt-2 w-48 bg-white rounded-md shadow-lg flex flex-col">
              <div className="flex ml-auto">
                <button
                  onClick={() => {
                    setMenuOpen(!menuOpen)
                  }}
                  className="bg-[#D5627F] mb-[10px]"
                >
                  x
                </button>
              </div>
              <div className="flex flex-row gap-[10px]">
                <button
                  className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => {
                    navigate(`/${param.username}`)
                  }}
                >
                  Profile
                </button>
                <button
                  className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => {
                    try {
                      axios.post(
                        'http://localhost:8080/api/user/logout',
                        null,
                        { withCredentials: true }
                      )
                    } catch (error) {
                      console.log('error with logging out: ' + error)
                    }
                  }}
                >
                  Log out
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
      <div className="">
        <p>{param.username}'s dashboard</p>
        <p>Most Accurate</p>
        <p>Least Accurate</p>
        <p>Number of Days Consistent: </p>
      </div>
      <div className="" id="heatmap">
        <CalendarHeatmap
          startDate={new Date(currentYear, 0, 1)}
          endDate={new Date(currentYear, 11, 31)}
          values={values}
          showWeekdayLabels={false}
          gutterSize={2}
          classForValue={(value) => {
            if (value && value.count === 0) return 'color-inconsistent'
            return 'color-consistent'
          }}
          tooltipDataAttrs={(value: any) => {
            if (!value || value.count === 0) {
              return { 'data-tip': `${value?.date} — Not consistent` }
            } else {
              return { 'data-tip': `${value.date} — Consistent` }
            }
          }}
        />
        <Tooltip id="heatmap-tooltip" />
      </div>
    </div>
  )
}
export default Profile