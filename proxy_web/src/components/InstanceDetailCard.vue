<template>
  <q-card style="cursor: pointer">
    <q-card-section class="text-center bg-primary text-white">

      <div class="row">
        <div class="col">
          <div class="row">
            <div class="col">
              代理端口
            </div>
            <div class="col">
              本地端口
            </div>
          </div>
          <div class="row">
            <div class="col">
              {{rowData.row.proxyPort}}
            </div>
            <div class="col">
              {{rowData.row.localPort}}
            </div>
          </div>
        </div>

        <div class="col-auto">
          <q-btn color="white" round flat icon="more_vert">
            <q-menu cover auto-close>
              <q-list>
                <q-item clickable @click="deleteProxy">
                  <q-item-section>删除</q-item-section>
                </q-item>
                <q-item clickable @click="changeStatus">
                  <q-item-section>{{rowData.row.status==1?'禁用':'启用'}}</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-btn>
        </div>

      </div>

    </q-card-section>

    <q-separator />

    <q-card-section class="flex flex-center">
<!--      <q-btn-->
<!--        fab-->
<!--        color="primary"-->
<!--        size="5px"-->
<!--        icon="delete"-->
<!--        class="absolute"-->
<!--        style="top: 0; right: 12px; transform: translateY(-50%);"-->
<!--      />-->

      <p class="text-green text-weight-bolder text-center" v-if="rowData.row.status==1"><strong>已启用</strong></p>
      <p class="text-red text-weight-bolder text-center" v-else><strong>已禁用</strong></p>
    </q-card-section>
  </q-card>
</template>

<script>
  export default {
    props: {
      rowData: {
        type: Object
      }
    },
    name: "InstanceDetailCard",
    methods:{
      deleteProxy(e){
        this.$emit('deleteProxy',this.rowData.rowIndex)
      },
      changeStatus(e){
        this.$emit('changeStatus',this.rowData.rowIndex)
      }
    }
  }
</script>

<style scoped>

</style>
