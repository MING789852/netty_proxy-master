<template>
  <q-page class="flex flex-center">
    <q-card class="my-card">
      <q-card-section>
        <q-input v-model="username" label="账号"  />
        <q-input v-model="password" type="password" label="密码"  />
      </q-card-section>

      <q-card-actions vertical align="center" class="q-pa-md">
        <q-btn outline color="light-blue" style="width: 100%" @click="login">登录</q-btn>
      </q-card-actions>
    </q-card>
  </q-page>
</template>

<script>
    import http from "../boot/http";
    export default {
      name: "Login",
      data () {
        return {
          username:'',
          password:''
        }
      },
      methods:{
        login(){
          let data={
            username:this.username,
            password:this.password
          }
          http.httpPost('config/login',data).then(res=>{
            console.log(res)
            sessionStorage.setItem('token',res.data)
            this.$router.push('/')
          })
        }
      }
    }
</script>

<style scoped>
  .my-card{
    width: 100%;
    max-width: 300px
  }
</style>
