import axios from 'axios'
import { useForm, SubmitHandler } from 'react-hook-form'
import useModal from '../../hooks/useAuthModal'
import { useState } from 'react'

type registerInfo = {
  email: string
  password: string
}

function parseEmailToUsername(email: string): string {
  const indexOfAt = email.indexOf('@')
  return email.substring(indexOfAt)
}

const RegisterModal = () => {
  const modalState = useModal()
  const [isValid, setIsValid] = useState(true)
  const { handleSubmit, register } = useForm<registerInfo>()
  const buttonSubmit: SubmitHandler<registerInfo> = async (data) => {
    try {
      const userObject = await axios.post('http://localhost:8080/register', data)
      modalState.closeRegisterModal()
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
      <label htmlFor="email" className="inline-block">
        Username
      </label>
      <input {...register('email')}></input>

      <label htmlFor="password" className="inline-block">
        Password
      </label>
      <input {...register('password')}></input>

      <button onClick={handleSubmit(buttonSubmit)}>Submit</button>
      {!isValid ? <p className="text-red-500">Account under this email already exists</p> : <p></p>}
    </div>
  )
}
export default RegisterModal
