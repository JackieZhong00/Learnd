
import { useState, useEffect } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z, ZodType } from 'zod'
import logo from '../assets/learnd_logo.png'
import useIsLoggedOut from '../hooks/useIsLoggedOut'


export type UserObjectType = {
    email : String
    username: String //we need to define a function to take substring of email up to the @ symbol to get this 
    password: String
}

export type UserRegisterForm = {
  email_register : String
  password_register : String 
  confirmPassword_register : String
}

export type UserLoginForm = {
  email_login : String
  password_login : String 
}

const RegisterLoginPage = () => {
  const navigate = useNavigate()
  const [isRegisterMode, setRegisterMode] = useState(false)
  const loggedOutHook = useIsLoggedOut();
  const [loginError, setLoginError] = useState(false);
  const registerSchema: ZodType<UserRegisterForm> = z
    .object({
      email_register: z.string().max(150),
      password_register: z.string().max(150),
      confirmPassword_register: z.string().max(150),
    })
    .refine((data) => data.password_register === data.confirmPassword_register, {
      message: 'Passwords do not match',
      path: ['confirmPassword'], // set the error on confirmPassword field
    })

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<UserRegisterForm>({
    resolver: zodResolver(registerSchema),
  })
  console.log("Form values:"  , watch())

  const {
    register: registerLogin,
    handleSubmit: handleSubmitLogin,
    formState: { errors: errorsLogin },
  } = useForm<UserLoginForm>()

  const parseEmailForUsername = (email: String): String => {
    let username: string = ''
    for (let i = 0; i < email.length; ++i) {
      if (email[i] == '@') {
        return username
      }
      username += email[i]
    }
    //we will never run into email with no @ bc it's checked prior to submission in registration form
    return ''
  }

  useEffect(() => {
    if(loggedOutHook.isLoggedOut) {return}
    const verifyJWT = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/user/verifyJwt', {
          withCredentials: true,
        })
        if (response.status === 200) {
          navigate(`/${parseEmailForUsername(response.data.username)}/deck_home`)
          console.log(response.data)
        }
      } catch (error) {
        console.warn('Error verifying JWT:', error)
      }
    }
    verifyJWT()
  },[loggedOutHook.isLoggedOut])


  

  const onSubmitLogin = async (data: UserLoginForm) => {
    console.log('Login data:', data)
    const email = data.email_login
    const response = await axios.post('http://localhost:8080/api/user/login', 
      {
      email: data.email_login,
      password: data.password_login
      },
      {withCredentials: true})
    if (response.status === 200) {
      console.log('Login successful:', response.data)
      const username = parseEmailForUsername(email)
      navigate(`/${username}/deck_home`)
      setLoginError(false)
    }
    else {
      setLoginError(true)
      console.log('Login failed: email was null or response was not 200')
    }
  }

  //this should not require user to reenter login and should immediately refresh, log in user, direct user to deckhome
  //if response is unsuccessful, display error message by setting error state
  const onSubmitRegister = async (data: UserRegisterForm) => {
    const response = await axios.post('http://localhost:8080/api/user/register', {
      email: data.email_register,
      password: data.password_register
    },{withCredentials: true})
    console.log(data)
    if (response.status === 200) {
      const username = parseEmailForUsername(response.data.email)
      navigate(`/${username}/deck_home`)
    }
  }

  const handleGoogleOAuth = () => {
    window.location.href = `http://localhost:8080/oauth2/authorization/google`
  }  
  return (
    <div className="w-screen h-screen flex bg-[radial-gradient(circle,_#BCA8A8_0%,_#837675_83%,_#847674_100%)]">
      <section className="flex flex-col bg-blue-200 w-[50vw]">
        <div className="mt-[20vh] mb-[10vh] flex justify-center items-center">
          <img
            src={logo}
            className="w-[16vw] h-[24vh] object-cover rounded-[75px]"
          />
        </div>
        <div className="flex flex-col items-center ">
          <a href="/about" className="no-underline text-inherit mb-[48px]">
            About
          </a>
          <a href="/guide" className="no-underline text-inherit mb-[48px]">
            Guide
          </a>
          <a
            href="/subscription"
            className="no-underline text-inherit mb-[48px]"
          >
            Subscription
          </a>
        </div>
      </section>
      <section className="flex flex-col w-[50vw]">
        {isRegisterMode ? (
          <div className="">
            <form onSubmit={handleSubmit(onSubmitRegister)} className="">
              <p className="mt-[20vh] mb-[10vh] flex items-center text-[45px] font-bold">
                Register
              </p>
              <div className="flex flex-col gap-[20px]">
                <label htmlFor="email_register">Email</label>
                <input
                  className="rounded-[4px] w-1/2 border border-none"
                  type="text"
                  id="email_register"
                  {...register('email_register')}
                />
                <label htmlFor="password">Password</label>
                <input
                  className="rounded-[4px] w-1/2 border border-none"
                  type="password"
                  id="confirmPassword"
                  {...register('password_register')}
                />
                <label htmlFor="confirmPassword">Confirm Password</label>
                <input
                  className="rounded-[4px] w-1/2 border border-none"
                  type="password"
                  id="password"
                  {...register('confirmPassword_register')}
                />
              </div>
              <button type="submit" className="mt-[5vh] mb-[2vh]">
                Create Account
              </button>
            </form>
            <p className="my-[2vh]">or</p>
            <button
            className='my-[2vh]'
              type="button"
              onClick={() => {
                setRegisterMode(false)
              }}
            >
              Sign in with existing account
            </button>
          </div>
        ) : (
          <div className="">
            <form onSubmit={handleSubmitLogin(onSubmitLogin)} className="">
              <p className="mt-[20vh] mb-[10vh] flex items-center text-[45px] font-bold">
                Login
              </p>
              <div className="flex flex-col gap-[20px]">
                <div className="flex flex-col gap-[20px]">
                  <label htmlFor="email">Email</label>
                  <input
                    className="rounded-[4px] w-1/2 border border-none"
                    type="text"
                    id="email"
                    {...registerLogin('email_login')}
                  />
                </div>
                <div className="flex flex-col gap-[20px] mb-[35px]">
                  <label htmlFor="password">Password</label>
                  <input
                    className="rounded-[4px] w-1/2 border border-none"
                    type="password"
                    id="password"
                    {...registerLogin('password_login')}
                  />
                </div>
              </div>
              <button type="submit" className="my-[2vh]">
                Log In
              </button>
              <p className="my-[2vh]">or</p>
              {loginError && (
                <p className="text-red-500 mt-[1vh]">
                  Login failed. Please check your credentials.
                </p>
              )}
            </form>
            <button
              type="button"
              className=""
              onClick={() => {
                setRegisterMode(true)
              }}
            >
              Create an Account
            </button>
          </div>
        )}
        <button onClick={handleGoogleOAuth} className='cursor-pointer w-[10vw]'>Google OAuth</button>
      </section>
    </div>
  )
}

export default RegisterLoginPage
