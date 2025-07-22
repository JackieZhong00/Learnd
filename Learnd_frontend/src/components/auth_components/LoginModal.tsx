import axios from 'axios'
import { useForm, SubmitHandler } from 'react-hook-form'
import useModal from '../../hooks/useAuthModal'
import { useState } from 'react'

type loginInfo = {
  email: String
  password: String
}

function parseEmailToUsername(email: String): String {
  const indexOfAt = email.indexOf('@')
  return email.substring(indexOfAt)
}

const LoginModal = () => {
  const modalState = useModal()
  const [isValid, setIsValid] = useState(true)
  const { handleSubmit, register } = useForm<loginInfo>()
  //this syntax is used to denote that buttonSubmit follows the function type called SubmitHandler<loginInfo> 
  const buttonSubmit: SubmitHandler<loginInfo> = async (data) => {
    try {
      const response = await axios.post('http://localhost:8080/login', data)
      modalState.closeLoginModal()
      modalState.setIsValidCredentials()
      const username = parseEmailToUsername(data.email)
      window.location.href = `/home/${username}`
    } catch (error) {
      setIsValid(false)
      modalState.setNotValidCredentials()
    }
  }
  return (
    <div>
      <form onSubmit={handleSubmit(buttonSubmit)}>
        <label htmlFor="email" className="inline-block">
          Username
        </label>
        <input {...register('email')}></input>

        <label htmlFor="password" className="inline-block">
          Password
        </label>
        <input {...register('password')}></input>

        <button type="submit">Submit</button>
        {!isValid ? (
          <p className="text-red-500">Invalid Credentials</p>
        ) : (
          <p></p>
        )}
      </form>
    </div>
  )
}
export default LoginModal
