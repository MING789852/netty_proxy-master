import axios from 'axios'
// eslint-disable-next-line no-unused-vars
import router from "../router/index"

import { Notify,Loading } from 'quasar'

axios.interceptors.request.use(config=>{
  let token = sessionStorage.getItem('token')
  if (token==null){
    token=''
  }
  config.headers.token=token
  console.log(config)
  return config
})


axios.interceptors.response.use(response=>{
  let data=response.data
  if (data.code==909){
    Notify.create({
      message: 'Token失效'
    })
    router.push({ path:'/login' })
  }
  return response
})

const httpGet = (url) => {
  // 默认选项
  Loading.show()
  // eslint-disable-next-line no-undef
  url = window.config.baseUrl + url
  /*debugger */
  return new Promise((resolve, reject) => {
    // eslint-disable-next-line no-undef
    axios.get(url).then(res => {
      if (res.data != null) {
        let data=res.data
        console.log(data)
        if (data.code==200){
          resolve(data)
        }else {
          Notify.create(data.msg)
        }
      }
    }).catch(res => {
      Notify.create('请求失败')
      reject(res)
    }).finally(res=>{
      Loading.hide()
    })
  })
}

const httpPost = (url, data) => {
  // 默认选项
  Loading.show()
  // eslint-disable-next-line no-undef
  url = window.config.baseUrl + url

  return new Promise((resolve, reject) => {
    axios.post(url, data).then(res => {
      if (res.data != null) {
        let data=res.data
        console.log(data)
        if (data.code==200){
          resolve(data)
        }else {
          Notify.create(data.msg)
        }
      }
    }).catch(res => {
      Notify.create('请求失败')
      reject(res)
    }).finally(res=>{
      Loading.hide()
    })
  })
}


export default {
  httpGet:httpGet,
  httpPost:httpPost
}
