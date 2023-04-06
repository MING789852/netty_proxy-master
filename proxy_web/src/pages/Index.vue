<template>
  <q-page class="flex flex-center">
    <div class="q-pa-md" style="width: 100%;height: 90vh">
      <q-table
        grid
        :data="data"
        :columns="columns"
        row-key="name"
        hide-header
        :pagination.sync="pagination"
        :rows-per-page-options="rowsPerPageOptions"
      >
        <template v-slot:top-left>
          <div class="q-gutter-md">
            <q-btn @click="back" v-if="type==2" color="primary" icon="arrow_back" label="返回" />
            <q-btn @click="addProxy" v-if="type==2"  color="primary" icon="add" label="新建" />
            <q-btn @click="refresh" color="primary" icon="refresh" label="刷新" />
          </div>
        </template>

        <template v-slot:item="props">
          <div class="q-pa-md col-xs-12 col-sm-6 col-md-3">
            <instance-card :rowData="props" v-if="type==1" @clickCard="clickCard"></instance-card>
            <instance-detail-card :rowData="props" v-else  @deleteProxy="deleteProxy" @changeStatus="changeStatus"></instance-detail-card>
          </div>
        </template>
      </q-table>
    </div>

    <q-dialog v-model="showAddProxy">
      <q-card>
        <q-card-section>
          <div class="text-h6">新增映射</div>
        </q-card-section>

        <q-card-section class="q-pa-md">
          <q-input v-model="proxyPort" label="代理端口(外网端口)" type="number"  />
          <q-input v-model="localPort" label="本地端口(内网端口)" type="number"  />
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="确定" color="primary"   @click="submitProxy" />
          <q-btn flat label="取消" color="primary"  @click="cancelProxy" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script>
  const columns = [
    {name: 'instance', label: 'instance', field: 'instance'},
    {name: 'count', label: 'count', field: 'count'}
  ]

  const detailColumns = [
    {name: 'localPort', label: 'localPort', field: 'localPort'},
    {name: 'proxyPort', label: 'proxyPort', field: 'proxyPort'},
    {name: 'status', label: 'status', field: 'status'},
  ]

  import http from "../boot/http";

  export default {
    data () {
      return {
        pagination: {
          page: 1,
          rowsPerPage: this.getItemsPerPage()
        },
        instance:null,
        //类型1、汇总  2、详情
        type:1,
        // data
        data:[],
        localPort:null,
        proxyPort:null,
        showAddProxy:false
      }
    },
    components:{
      InstanceCard: () => import('../components/InstanceCard'),
      InstanceDetailCard: () => import('../components/InstanceDetailCard')
    },
    mounted () {
      this.getData()
    },
    computed: {
      columns () {
        if (this.type==1){
          return columns
        }else {
          return detailColumns
        }
      },
      rowsPerPageOptions () {
        if (this.$q.screen.gt.xs) {
          return this.$q.screen.gt.sm ? [ 4, 8, 12 ] : [ 4, 8 ]
        }

        return [ 4 ]
      }
    },
    watch: {
      '$q.screen.name' () {
        this.pagination.rowsPerPage = this.getItemsPerPage()
      },
    },
    methods: {
      getData(fn){
        if (this.type==1){
          http.httpGet('config/getAllInstance').then(res=>{
             this.data=res.data
             if (fn!=undefined){
               fn()
             }
          })
        }else {
          http.httpGet('config/getInstanceDetail?instance='+this.instance).then(res=>{
            this.data=res.data
            if (fn!=undefined){
              fn()
            }
          })
        }
      },
      back(e){
        this.type = 1
        this.getData()
      },
      addProxy(e){
        this.localPort=null
        this.proxyPort=null
        this.showAddProxy=true
      },
      submitProxy(){
        if (this.instance==null){
          this.$q.notify('实例不能为空')
        }
        if (this.proxyPort==null){
          this.$q.notify('代理端口不能为空')
        }
        if (this.localPort==null){
          this.$q.notify('本地端口不能为空')
        }
        let url='config/addProxy?instance='+this.instance+'&proxyPort='+this.proxyPort+'&localPort='+this.localPort;
        http.httpGet(url).then(res=>{
          this.getData(()=>{
            this.$q.notify('操作成功')
          })
        })
      },
      cancelProxy(){
        this.showAddProxy=false
      },
      refresh(e){
        this.getData()
      },
      clickCard (val) {
        let instance=val.row.instance
        console.log(instance)
        this.instance=instance
        this.type = 2
        this.getData()
      },
      deleteProxy(index){
        let data=this.data[index]
        let instance=data.instance
        let proxyPort=data.proxyPort
        http.httpGet('config/removeProxy?instance='+instance+'&proxyPort='+proxyPort).then(res=>{
          this.getData(()=>{
            this.$q.notify('操作成功')
          })
        })
      },
      changeStatus(index){
        let data=this.data[index]
        let instance=data.instance
        let proxyPort=data.proxyPort
        let status=data.status
        if (status==1){
          http.httpGet('config/forbid?instance='+instance+'&proxyPort='+proxyPort).then(res=>{
            this.getData(()=>{
              this.$q.notify('操作成功')
            })
          })
        }else {
          http.httpGet('config/allow?instance='+instance+'&proxyPort='+proxyPort).then(res=>{
            this.getData(()=>{
              this.$q.notify('操作成功')
            })
          })
        }
      },
      getItemsPerPage () {
        const { screen } = this.$q
        //小屏幕
        if (screen.lt.sm) {
          return 4
        }
        if (screen.lt.md) {
          return 8
        }
        return 12
      }
    }
  }
</script>

<style scoped>

</style>
